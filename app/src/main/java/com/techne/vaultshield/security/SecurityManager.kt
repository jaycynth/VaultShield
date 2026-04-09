package com.techne.vaultshield.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     *  Integrity check for the device environment.
     */
    fun checkEnvironmentIntegrity(): IntegrityResult {
        val rootBeer = RootBeer(context)
        val isRooted = rootBeer.isRooted
        val isEmulator = isEmulator()
        val isDebuggerConnected = android.os.Debug.isDebuggerConnected()
        val isDevOptionsEnabled = isDeveloperOptionsEnabled()
        val isADBEnabled = isADBEnabled()

        // Check if the app is side-loaded (optional, but good for security narrative)
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        val isSideLoaded = installer == null || installer == "com.android.packageinstaller"

        return IntegrityResult(
            isRooted = isRooted,
            isEmulator = isEmulator,
            isDebuggerConnected = isDebuggerConnected,
            isDevOptionsEnabled = isDevOptionsEnabled,
            isADBEnabled = isADBEnabled,
            isSideLoaded = isSideLoaded,
            isPass = !isRooted && !isEmulator && !isDebuggerConnected
        )
    }

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }

    private fun isDeveloperOptionsEnabled(): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
    }

    private fun isADBEnabled(): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
    }

    data class IntegrityResult(
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val isDebuggerConnected: Boolean,
        val isDevOptionsEnabled: Boolean,
        val isADBEnabled: Boolean,
        val isSideLoaded: Boolean,
        val isPass: Boolean
    )
}
