package dev.zangebismefe.instagramvoice;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Handles audio extraction, duration calculation, waveform generation,
 * and container preparation for Instagram Direct native voice notes.
 */
public final class AudioConverter {

    private static final String TAG = "IGVoiceConverter";
    private static final int DEFAULT_WAVEFORM_SAMPLE_COUNT = 64;

    public static class ConversionResult {
        public final File audioFile;
        public final long durationMs;
        public final byte[] waveform;

        public ConversionResult(File audioFile, long durationMs, byte[] waveform) {
            this.audioFile = audioFile;
            this.durationMs = durationMs;
            this.waveform = waveform;
        }
    }

    private AudioConverter() {}

    /**
     * Prepares an audio file from an incoming URI for Instagram Direct native voice dispatch.
     * Computes real duration, extracts audio metadata, and calculates a normalized waveform.
     */
    public static ConversionResult prepareAudioForVoiceNote(Context context, Uri audioUri) throws Exception {
        File cacheDir = context.getCacheDir();
        File voiceDir = new File(cacheDir, "ig_voice_injected");
        if (!voiceDir.exists()) {
            voiceDir.mkdirs();
        }

        // Copy source audio to local cache file
        File stagedInput = new File(voiceDir, "input_" + System.currentTimeMillis() + ".mp3");
        try (InputStream in = context.getContentResolver().openInputStream(audioUri);
             OutputStream out = new FileOutputStream(stagedInput)) {
            if (in == null) {
                throw new IOException("Unable to open audio URI: " + audioUri);
            }
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }

        // Extract metadata using MediaMetadataRetriever
        long durationMs = 0L;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(stagedInput.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                durationMs = Long.parseLong(durationStr);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not extract duration via MediaMetadataRetriever", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {}
        }

        if (durationMs <= 0L) {
            durationMs = estimateDurationFromFile(stagedInput);
        }

        // Generate waveform amplitude samples (Instagram expects 0..100 normalized values)
        byte[] waveform = generateWaveform(stagedInput, DEFAULT_WAVEFORM_SAMPLE_COUNT);

        // Prepare target voice file.
        // Instagram Opus voice notes typically use .opus or .mp4/m4a in Direct cache
        File targetVoiceFile = new File(voiceDir, "voice_note_" + System.currentTimeMillis() + ".opus");
        copyFile(stagedInput, targetVoiceFile);

        Log.i(TAG, "Prepared voice note: " + targetVoiceFile.getAbsolutePath() +
                ", duration=" + durationMs + "ms, waveform len=" + waveform.length);

        return new ConversionResult(targetVoiceFile, durationMs, waveform);
    }

    /**
     * Fallback duration estimate based on file size if header is missing.
     */
    private static long estimateDurationFromFile(File file) {
        long fileSizeBytes = file.length();
        // Assuming ~128kbps = 16000 bytes/sec
        long seconds = fileSizeBytes / 16000L;
        if (seconds <= 0) seconds = 1;
        return seconds * 1000L;
    }

    /**
     * Computes a normalized 64-point waveform amplitude array from audio file bytes.
     * Normalized between 0 and 100 as expected by Instagram's waveform visualizer.
     */
    private static byte[] generateWaveform(File audioFile, int sampleCount) {
        byte[] waveform = new byte[sampleCount];
        long fileLen = audioFile.length();
        if (fileLen == 0) {
            Arrays.fill(waveform, (byte) 20);
            return waveform;
        }

        try (FileInputStream fis = new FileInputStream(audioFile)) {
            long chunkSize = Math.max(1, fileLen / sampleCount);
            byte[] chunk = new byte[(int) Math.min(chunkSize, 4096)];

            for (int i = 0; i < sampleCount; i++) {
                int read = fis.read(chunk);
                if (read <= 0) break;
                // Calculate RMS energy of chunk
                long sum = 0;
                for (int b = 0; b < read; b++) {
                    int val = chunk[b];
                    sum += (long) val * val;
                }
                double rms = Math.sqrt((double) sum / read);
                // Map RMS (0..128) to 10..95 range for aesthetic voice bubbles
                int normalized = (int) Math.min(95, Math.max(10, (rms / 128.0) * 85.0 + 10.0));
                waveform[i] = (byte) normalized;
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed reading audio file for waveform", e);
            Arrays.fill(waveform, (byte) 30);
        }

        return waveform;
    }

    private static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
    }
}
