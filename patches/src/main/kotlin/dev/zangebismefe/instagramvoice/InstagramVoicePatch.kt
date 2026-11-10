package dev.zangebismefe.instagramvoice

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch

/**
 * Morphe Bytecode Patch for Instagram Direct MP3 native voice notes.
 *
 * Target: Instagram Android 452.1.0.53.64 (version code 385811006, ARM64 APKM).
 * Bundles: extensions/extension.mpe (AudioPickerActivity, AudioConverter, VoiceNoteHook, VoiceNoteState, DirectOverlayController).
 */
@Suppress("unused")
val instagramVoicePatch = bytecodePatch(
    name = "Instagram MP3 Voice Note",
    description = "Allows selecting a local MP3 audio file and sending it as a native Instagram Direct voice message instead of an attachment.",
    default = true
) {
    compatibleWith(instagramTarget)
    extendWith("extensions/extension.mpe")

    execute {
        val targetTypes = listOf(
            "Lcom/instagram/mainactivity/InstagramMainActivity;",
            "Lcom/instagram/android/activity/MainTabActivity;",
            "Lcom/instagram/direct/fragment/thread/DirectThreadFragment;"
        )

        for (targetType in targetTypes) {
            val classDef = classDefByOrNull(targetType) ?: continue
            val mutableClass = mutableClassDefBy(classDef) ?: continue
            val targetMethod = mutableClass.methods.firstOrNull {
                it.name == "onCreate" || it.name == "onResume" || it.name == "onViewCreated"
            }
            targetMethod?.apply {
                addInstruction(0, "invoke-static { p0 }, Ldev/zangebismefe/instagramvoice/VoiceNoteHook;->init(Landroid/content/Context;)V")
            }
        }
    }
}
