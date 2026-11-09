package dev.zangebismefe.instagramvoice

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Target metadata only. This does NOT claim a working patch exists.
 * Version 452.1.0.53.64 was supplied as an APKM for research.
 */
internal val instagramTarget = Compatibility(
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
