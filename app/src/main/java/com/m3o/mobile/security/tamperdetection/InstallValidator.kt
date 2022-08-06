package com.m3o.mobile.security.tamperdetection

import android.content.Context
import android.os.Build

internal enum class Installer(val id: String) {
    GOOGLE_PLAY_STORE(id = "com.android.vending")
}

internal fun Context.verifyInstaller(installer: Installer = Installer.GOOGLE_PLAY_STORE): Boolean? {
    kotlin.runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return packageManager.getInstallSourceInfo(packageName).installingPackageName?.startsWith(
                installer.id
            )
        @Suppress("DEPRECATION")
        return packageManager.getInstallerPackageName(packageName)?.startsWith(installer.id)
    }
    return null
}
