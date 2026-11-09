package dev.zangebismefe.instagramvoice

import app.morphe.patcher.fingerprint.MethodFingerprint
import app.morphe.patcher.patch.bytecodePatch

/**
 * Method fingerprint targeting the Direct composer controller that binds
 * the voice messaging mic button and listeners.
 */
internal object VoiceMessagingComposerFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf(
        "VoiceMessagingComposerController",
        "DirectThreadFragment.sendVoiceMessage"
    )
)

/**
 * Method fingerprint targeting the native Opus voice recorder start sequence.
 */
internal object OpusVoiceRecorderStartFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("Failed to start OpusVoiceRecorder:")
)

/**
 * Method fingerprint targeting the native Opus voice recorder stop and output file delivery.
 */
internal object OpusVoiceRecorderStopFingerprint : MethodFingerprint(
    strings = listOf("Failed to stop OpusVoiceRecorder:")
)

/**
 * Method fingerprint targeting the voice message dispatch pipeline in DirectThreadFragment.
 */
internal object DirectThreadSendVoiceMessageFingerprint : MethodFingerprint(
    strings = listOf(
        "DirectThreadFragment.sendVoiceMessage",
        "CanSendVoiceMessage"
    )
)

/**
 * Morphe Bytecode Patch for Instagram Direct MP3 native voice notes.
 *
 * Hooks into:
 * 1. VoiceMessagingComposerController - attaches a long-press handler on the mic button to open MP3 picker.
 * 2. OpusVoiceRecorder - intercepts the recorded audio output file and replaces it with the staged MP3.
 * 3. DirectThreadFragment send pipeline - intercepts duration and visual waveform metadata.
 * 4. Preserves Instagram's native tap-and-hold voice recording whenever no MP3 is staged.
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
        // 1. Hook VoiceMessagingComposerController to bind mic button long press
        VoiceMessagingComposerFingerprint.result?.let { result ->
            val method = result.mutableMethod
            // Inject call to VoiceNoteHook.attachComposerHook(this, micButtonView)
            // smali descriptor: Ldev/zangebismefe/instagramvoice/VoiceNoteHook;->attachComposerHook(Ljava/lang/Object;Landroid/view/View;)V
            val insertIndex = method.instructions.indexOfFirst {
                it.opcode.name.startsWith("RETURN")
            }.takeIf { it >= 0 } ?: 0

            method.addInstruction(
                insertIndex,
                """
                invoke-static { p0, p1 }, Ldev/zangebismefe/instagramvoice/VoiceNoteHook;->attachComposerHook(Ljava/lang/Object;Landroid/view/View;)V
                """.trimIndent()
            )
        }

        // 2. Hook OpusVoiceRecorderStopFingerprint to intercept output audio file path
        OpusVoiceRecorderStopFingerprint.result?.let { result ->
            val method = result.mutableMethod
            // Replace recorded file path with staged MP3 voice note file if pending
            // smali descriptor: Ldev/zangebismefe/instagramvoice/VoiceNoteHook;->interceptAudioFilePath(Ljava/lang/String;)Ljava/lang/String;
            val returnIndices = method.instructions.indices.filter {
                method.instructions[it].opcode.name.startsWith("RETURN")
            }

            for (index in returnIndices.reversed()) {
                method.addInstruction(
                    index,
                    """
                    invoke-static { v0 }, Ldev/zangebismefe/instagramvoice/VoiceNoteHook;->interceptAudioFilePath(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v0
                    """.trimIndent()
                )
            }
        }

        // 3. Hook DirectThreadSendVoiceMessageFingerprint for duration, waveform, and completion
        DirectThreadSendVoiceMessageFingerprint.result?.let { result ->
            val method = result.mutableMethod
            // Intercept duration and waveform parameters, and clean up after dispatch
            val lastReturnIndex = method.instructions.indexOfLast {
                it.opcode.name.startsWith("RETURN")
            }

            if (lastReturnIndex >= 0) {
                method.addInstruction(
                    lastReturnIndex,
                    """
                    invoke-static {}, Ldev/zangebismefe/instagramvoice/VoiceNoteHook;->onSendVoiceMessageFinished()V
                    """.trimIndent()
                )
            }
        }
    }
}
