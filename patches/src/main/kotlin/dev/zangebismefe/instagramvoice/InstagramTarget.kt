package dev.zangebismefe.instagramvoice

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Target compatibility metadata supporting official Instagram and Piko clones.
 */
internal val instagramOfficialTarget = Compatibility(
    name = "Instagram",
    packageName = "com.instagram.android",
    apkFileType = ApkFileType.APKM,
    appIconColor = 0xE1306C,
    targets = listOf(
        AppTarget(
            version = "452.1.0.53.64",
            isExperimental = true
        )
    )
)

internal val instagramPikoCloneTarget = Compatibility(
    name = "Instagram (Piko Clone)",
    packageName = "com.instagram.android.piko",
    apkFileType = ApkFileType.APK,
    appIconColor = 0xE1306C,
    targets = listOf(
        AppTarget(
            version = "452.1.0.53.64",
            isExperimental = true
        )
    )
)

internal val instagramPikoAltTarget = Compatibility(
    name = "Instagram (Piko Clone Alt)",
    packageName = "com.instagram.piko",
    apkFileType = ApkFileType.APK,
    appIconColor = 0xE1306C,
    targets = listOf(
        AppTarget(
            version = "452.1.0.53.64",
            isExperimental = true
        )
    )
)
