package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.update.InAppUpdateUiState

@Composable
fun UpdateNotificationBanner(
    updateState: InAppUpdateUiState,
    onStartUpdate: () -> Unit,
    onDismissAvailable: () -> Unit,
    onCompleteUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShow = when (updateState) {
        is InAppUpdateUiState.Available -> !updateState.isDismissed
        is InAppUpdateUiState.Downloading -> true
        is InAppUpdateUiState.Downloaded -> true
        else -> false
    }

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        when (updateState) {
            is InAppUpdateUiState.Available -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("update_available_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = stringResource(R.string.update_available_title),
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.update_available_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.update_available_message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onDismissAvailable,
                                modifier = Modifier.testTag("update_later_button")
                            ) {
                                Text(stringResource(R.string.update_action_later))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onStartUpdate,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("update_now_button")
                            ) {
                                Text(stringResource(R.string.update_action_update))
                            }
                        }
                    }
                }
            }

            is InAppUpdateUiState.Downloading -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("update_downloading_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = stringResource(R.string.update_downloading_title),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (updateState.progressPercent != null) {
                                    "${stringResource(R.string.update_downloading_title)} (${updateState.progressPercent}%)"
                                } else {
                                    stringResource(R.string.update_downloading_title)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (updateState.progressPercent != null) {
                            LinearProgressIndicator(
                                progress = { updateState.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )
                        }
                    }
                }
            }

            is InAppUpdateUiState.Downloaded -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("update_downloaded_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.update_downloaded_message),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(R.string.update_downloaded_message),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onCompleteUpdate,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("update_restart_button")
                        ) {
                            Text(stringResource(R.string.update_action_restart))
                        }
                    }
                }
            }

            else -> Unit
        }
    }
}
