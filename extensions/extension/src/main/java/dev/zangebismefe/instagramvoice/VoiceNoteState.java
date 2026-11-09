package dev.zangebismefe.instagramvoice;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared state manager for injected native Instagram Direct voice messages.
 * Holds imported audio metadata and coordinates replacement of the recorded voice note.
 */
public final class VoiceNoteState {

    private static volatile File sInjectedAudioFile = null;
    private static volatile long sInjectedDurationMs = 0L;
    private static volatile byte[] sInjectedWaveform = null;
    private static final AtomicBoolean sIsInjectionPending = new AtomicBoolean(false);

    private VoiceNoteState() {}

    /**
     * Stash a transcoded voice note ready to be dispatched as a native voice message.
     */
    public static void setInjectedAudio(File audioFile, long durationMs, byte[] waveform) {
        sInjectedAudioFile = audioFile;
        sInjectedDurationMs = durationMs;
        sInjectedWaveform = waveform;
        sIsInjectionPending.set(true);
    }

    public static boolean hasPendingInjection() {
        return sIsInjectionPending.get() && sInjectedAudioFile != null && sInjectedAudioFile.exists();
    }

    public static File getInjectedAudioFile() {
        return sInjectedAudioFile;
    }

    public static long getInjectedDurationMs() {
        return sInjectedDurationMs;
    }

    public static byte[] getInjectedWaveform() {
        return sInjectedWaveform;
    }

    /**
     * Clears pending state after message dispatch to ensure standard mic recording works as usual.
     */
    public static void clearPendingInjection() {
        sIsInjectionPending.set(false);
        sInjectedAudioFile = null;
        sInjectedDurationMs = 0L;
        sInjectedWaveform = null;
    }
}
