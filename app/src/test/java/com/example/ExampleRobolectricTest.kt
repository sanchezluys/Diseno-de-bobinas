package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Diseño de Bobinas", appName)
  }

  @Test
  fun `verify package and build config version`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
      pInfo.longVersionCode.toInt()
    } else {
      @Suppress("DEPRECATION")
      pInfo.versionCode
    }
    assertEquals(BuildConfig.VERSION_NAME, pInfo.versionName)
    assertEquals(BuildConfig.VERSION_CODE, vCode)
  }
}
