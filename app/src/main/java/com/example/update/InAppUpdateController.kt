package com.example.update

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.example.R
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult as PlayActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class InAppUpdateUiState {
    data object Idle : InAppUpdateUiState()
    data object Checking : InAppUpdateUiState()
    data object UpToDate : InAppUpdateUiState()
    data object Unavailable : InAppUpdateUiState()
    data class Available(
        val availableVersionCode: Int,
        val stalenessDays: Int?,
        val updatePriority: Int,
        val isImmediateRecommended: Boolean,
        val isDismissed: Boolean = false
    ) : InAppUpdateUiState()

    data class Downloading(val progressPercent: Int?) : InAppUpdateUiState()
    data object Downloaded : InAppUpdateUiState()
    data class Failed(val messageRes: Int) : InAppUpdateUiState()
}

/**
 * Pure decision policy following Google Play In-App Updates best practices:
 * - High priority (>= 4) or staleness >= 7 days -> Immediate update
 * - Otherwise -> Flexible update with background download & restart prompt
 */
object UpdateDecisionPolicy {
    const val HIGH_PRIORITY_THRESHOLD = 4
    const val STALENESS_DAYS_FOR_IMMEDIATE = 7

    fun shouldUseImmediateUpdate(
        updatePriority: Int,
        stalenessDays: Int?,
        isImmediateAllowed: Boolean
    ): Boolean {
        if (!isImmediateAllowed) return false
        return updatePriority >= HIGH_PRIORITY_THRESHOLD ||
            (stalenessDays != null && stalenessDays >= STALENESS_DAYS_FOR_IMMEDIATE)
    }

    fun calculateProgressPercent(bytesDownloaded: Long, totalBytesToDownload: Long): Int? {
        if (totalBytesToDownload <= 0L) return null
        return ((bytesDownloaded.coerceAtLeast(0L) * 100L) / totalBytesToDownload)
            .toInt()
            .coerceIn(0, 100)
    }
}

class InAppUpdateController(
    context: Context
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)

    private val _uiState = MutableStateFlow<InAppUpdateUiState>(InAppUpdateUiState.Idle)
    val uiState: StateFlow<InAppUpdateUiState> = _uiState.asStateFlow()

    private var latestAppUpdateInfo: AppUpdateInfo? = null
    private var updateLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var hasAutoPromptedImmediateThisSession = false
    private var hasUserDismissedBannerThisSession = false
    private var isListenerRegistered = false

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.PENDING -> {
                _uiState.value = InAppUpdateUiState.Downloading(progressPercent = null)
            }
            InstallStatus.DOWNLOADING -> {
                val progress = UpdateDecisionPolicy.calculateProgressPercent(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytesToDownload = state.totalBytesToDownload()
                )
                _uiState.value = InAppUpdateUiState.Downloading(progressPercent = progress)
            }
            InstallStatus.INSTALLING -> {
                _uiState.value = InAppUpdateUiState.Downloading(progressPercent = 100)
            }
            InstallStatus.DOWNLOADED -> {
                _uiState.value = InAppUpdateUiState.Downloaded
            }
            InstallStatus.FAILED -> {
                _uiState.value = InAppUpdateUiState.Failed(R.string.update_status_failed)
            }
            InstallStatus.CANCELED -> {
                val info = latestAppUpdateInfo
                if (info != null && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    _uiState.value = InAppUpdateUiState.Available(
                        availableVersionCode = info.availableVersionCode(),
                        stalenessDays = info.clientVersionStalenessDays(),
                        updatePriority = info.updatePriority(),
                        isImmediateRecommended = false,
                        isDismissed = hasUserDismissedBannerThisSession
                    )
                } else {
                    _uiState.value = InAppUpdateUiState.Idle
                }
            }
            InstallStatus.INSTALLED -> {
                _uiState.value = InAppUpdateUiState.Idle
            }
            else -> Unit
        }
    }

    fun attachLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        updateLauncher = launcher
    }

    fun startObserving() {
        if (!isListenerRegistered) {
            appUpdateManager.registerListener(installStateUpdatedListener)
            isListenerRegistered = true
        }
    }

    fun stopObserving() {
        if (isListenerRegistered) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
            isListenerRegistered = false
        }
    }

    fun checkForUpdates(isManual: Boolean = false) {
        if (isManual) {
            _uiState.value = InAppUpdateUiState.Checking
        }

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                latestAppUpdateInfo = appUpdateInfo
                handleAppUpdateInfo(appUpdateInfo, isManual)
            }
            .addOnFailureListener {
                // Play Store unavailable (e.g., sideloaded build, emulator without Play Store, offline)
                _uiState.value = if (isManual) {
                    InAppUpdateUiState.Unavailable
                } else {
                    InAppUpdateUiState.Idle
                }
            }
    }

    private fun handleAppUpdateInfo(appUpdateInfo: AppUpdateInfo, isManual: Boolean) {
        // 1. Check if a flexible update was already downloaded and is waiting for app restart
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            _uiState.value = InAppUpdateUiState.Downloaded
            return
        }

        // 2. Resume stalled immediate update if it was already in progress
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                launchUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE)
            }
            return
        }

        // 3. Check if a new update is available
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            val priority = appUpdateInfo.updatePriority()
            val staleness = appUpdateInfo.clientVersionStalenessDays()
            val immediateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            val flexibleAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            val useImmediate = UpdateDecisionPolicy.shouldUseImmediateUpdate(
                updatePriority = priority,
                stalenessDays = staleness,
                isImmediateAllowed = immediateAllowed
            )

            if (useImmediate && (isManual || !hasAutoPromptedImmediateThisSession)) {
                hasAutoPromptedImmediateThisSession = true
                launchUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE)
                return
            }

            if (flexibleAllowed || immediateAllowed) {
                if (isManual) {
                    hasUserDismissedBannerThisSession = false
                }
                _uiState.value = InAppUpdateUiState.Available(
                    availableVersionCode = appUpdateInfo.availableVersionCode(),
                    stalenessDays = staleness,
                    updatePriority = priority,
                    isImmediateRecommended = useImmediate,
                    isDismissed = !isManual && hasUserDismissedBannerThisSession
                )

                // If user manually requested an update check, immediately trigger the Play Store sheet
                if (isManual) {
                    val flowType = if (useImmediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
                    launchUpdateFlow(appUpdateInfo, flowType)
                }
                return
            }
        }

        _uiState.value = if (isManual) {
            InAppUpdateUiState.UpToDate
        } else {
            InAppUpdateUiState.Idle
        }
    }

    fun startFlexibleOrRecommendedUpdate() {
        val currentInfo = latestAppUpdateInfo
        if (currentInfo != null &&
            currentInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        ) {
            val flowType = when {
                UpdateDecisionPolicy.shouldUseImmediateUpdate(
                    updatePriority = currentInfo.updatePriority(),
                    stalenessDays = currentInfo.clientVersionStalenessDays(),
                    isImmediateAllowed = currentInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) -> AppUpdateType.IMMEDIATE
                currentInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                currentInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                else -> return
            }
            launchUpdateFlow(currentInfo, flowType)
        } else {
            checkForUpdates(isManual = true)
        }
    }

    private fun launchUpdateFlow(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        val launcher = updateLauncher ?: return
        try {
            val started = appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
            if (!started) {
                _uiState.value = InAppUpdateUiState.Failed(R.string.update_status_failed)
            }
        } catch (_: Exception) {
            _uiState.value = InAppUpdateUiState.Failed(R.string.update_status_failed)
        }
    }

    fun onUpdateFlowResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> Unit
            Activity.RESULT_CANCELED -> {
                // Respect user choice; avoid annoying re-prompts in the same session
                hasUserDismissedBannerThisSession = true
                val current = _uiState.value
                if (current is InAppUpdateUiState.Available) {
                    _uiState.value = current.copy(isDismissed = true)
                } else {
                    _uiState.value = InAppUpdateUiState.Idle
                }
            }
            PlayActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                _uiState.value = InAppUpdateUiState.Failed(R.string.update_status_failed)
            }
        }
    }

    fun dismissAvailableNotification() {
        hasUserDismissedBannerThisSession = true
        val current = _uiState.value
        if (current is InAppUpdateUiState.Available) {
            _uiState.value = current.copy(isDismissed = true)
        }
    }

    fun clearTransientStatus() {
        val current = _uiState.value
        if (current is InAppUpdateUiState.UpToDate ||
            current is InAppUpdateUiState.Unavailable ||
            current is InAppUpdateUiState.Failed
        ) {
            _uiState.value = InAppUpdateUiState.Idle
        }
    }

    fun completeDownloadedUpdate() {
        appUpdateManager.completeUpdate()
    }
}
