package com.codelabs.securitymodule

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
import android.provider.Settings
import android.util.Base64
import com.scottyab.rootbeer.RootBeer
import java.security.MessageDigest

class SecurityUtils(private val context: Context) {

    fun isRooted(): Boolean {
        return RootBeer(context).isRooted
    }

    fun isRootedWithBusyBoxCheck(): Boolean {
        return RootBeer(context).isRootedWithBusyBoxCheck
    }

    fun isAdbEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    fun isDebuggable(): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun isEmulator(): Boolean {
        val buildProps = listOf(
            Build.FINGERPRINT to listOf("generic", "unknown"),
            Build.MODEL to listOf("google_sdk", "Emulator", "Android SDK built for x86"),
            Build.MANUFACTURER to listOf("Genymotion", "unknown"),
            Build.BRAND to listOf("generic", "generic_x86"),
            Build.DEVICE to listOf("generic"),
            Build.PRODUCT to listOf("sdk", "google_sdk")
        )
        return buildProps.any { (prop, keywords) ->
            keywords.any { prop.contains(it, ignoreCase = true) }
        } || NativeHelper().nativeIsEmulator()
    }

    fun isInstalledOnExternalStorage(): Boolean {
        val flags = context.applicationInfo.flags
        return (flags and android.content.pm.ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0
    }

    fun isDeveloperOptionsEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) == 1
        } catch (e: Exception) {
            false
        }
    }

    fun isSignatureValid(knownSignature: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val signatures = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo?.apkContentsSigners

                val currentSignature = signatures?.firstOrNull()?.toByteArray()?.let { bytes ->
                    val md = MessageDigest.getInstance("SHA-256")
                    val digest = md.digest(bytes)
                    Base64.encodeToString(digest, Base64.NO_WRAP)
                }
                currentSignature == knownSignature
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
    }

    private fun isFridaServerRunning(): Boolean {
        return try {
            NativeHelper().checkFridaServer()
        } catch (e: Exception) {
            false
        }
    }

    private fun isObjectionInjected(): Boolean {
        return try {
            NativeHelper().checkObjectionInjected()
        } catch (e: Exception) {
            false
        }
    }

    private fun isMagiskMounted(): Boolean {
        return try {
            NativeHelper().checkMagiskMounted()
        } catch (e: Exception) {
            false
        }
    }

    fun  isRunningOnSuspiciousEnvironment():Boolean {
        return isFridaServerRunning() || isObjectionInjected() || isMagiskMounted()
    }

//    fun isApktoolArtifactPresent(assetManager: AssetManager): Boolean {
//        return try {
//            NativeHelper().isApktoolArtifactPresent(assetManager)
//        } catch (e: Exception) {
//            false
//        }
//    }

    fun isSecurityValidated(assetManager: AssetManager, knownSignature: String): Boolean {
        return !(isRooted()
                || isAdbEnabled()
                || isDebuggable()
                || isEmulator()
                || isInstalledOnExternalStorage()
                || isDeveloperOptionsEnabled()
                || isSignatureValid(knownSignature)
                || isRunningOnSuspiciousEnvironment()
//                || isApktoolArtifactPresent(assetManager)
                )
    }
}