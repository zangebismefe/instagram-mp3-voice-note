package dev.zangebismefe.instagramvoice

import app.morphe.patcher.patch.bytecodePatch

/**
 * Morphe Bytecode Patch for Instagram Direct MP3 native voice notes.
 *
 * Target: Instagram Android 452.1.0.53.64 (version code 385811006, ARM64 APKM).
 * Bundles: extensions/extension.mpe (AudioPickerActivity, AudioConverter, VoiceNoteHook, VoiceNoteState).
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
        // Morphe merges extensions/extension.mpe into the target Instagram APKM DEX.
        // Runtime hooks in VoiceNoteHook attach to VoiceMessagingComposerController and OpusVoiceRecorder.
    }
}
