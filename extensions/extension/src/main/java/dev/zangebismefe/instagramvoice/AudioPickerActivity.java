package dev.zangebismefe.instagramvoice;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Lightweight translucent Activity that launches the Android document/audio picker,
 * processes the selected MP3/audio file, and loads it into VoiceNoteState.
 */
public class AudioPickerActivity extends Activity {

    private static final String TAG = "IGAudioPicker";
    private static final int REQUEST_PICK_AUDIO = 9021;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
                "audio/mpeg", "audio/mp3", "audio/ogg", "audio/opus", "audio/m4a", "audio/x-m4a", "audio/aac"
        });

        try {
            startActivityForResult(Intent.createChooser(intent, "Select Voice Note Audio (MP3)"), REQUEST_PICK_AUDIO);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch audio picker", e);
            Toast.makeText(this, "No file manager found to select audio", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_AUDIO && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            if (audioUri != null) {
                try {
                    AudioConverter.ConversionResult result =
                            AudioConverter.prepareAudioForVoiceNote(getApplicationContext(), audioUri);
                    VoiceNoteState.setInjectedAudio(result.audioFile, result.durationMs, result.waveform);

                    String msg = String.format("Voice Note loaded: %.1f sec. Tap Send to dispatch.",
                            result.durationMs / 1000.0f);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Successfully staged voice note from URI: " + audioUri);
                } catch (Exception e) {
                    Log.e(TAG, "Failed processing picked audio file", e);
                    Toast.makeText(this, "Failed to load audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    VoiceNoteState.clearPendingInjection();
                }
            }
        } else {
            Log.d(TAG, "Audio picking canceled or failed, resultCode=" + resultCode);
        }

        finish();
    }
}
