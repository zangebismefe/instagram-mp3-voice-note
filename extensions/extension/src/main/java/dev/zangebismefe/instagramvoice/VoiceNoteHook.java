package dev.zangebismefe.instagramvoice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

/**
 * Bytecode hook integration bridge.
 * Methods in this class are invoked directly from patched Instagram DEX methods.
 */
public final class VoiceNoteHook {

    private static final String TAG = "IGVoiceHook";

    private VoiceNoteHook() {}

    /**
     * Hooked into VoiceMessagingComposerController or DirectThreadFragment view binding.
     * Attaches a long-click listener to the microphone button so that long-pressing gives
     * the option to select an MP3 voice note without breaking normal tap-to-hold voice recording.
     */
    public static void attachComposerHook(final Object composerInstance, final View micButton) {
        if (micButton == null) {
            Log.w(TAG, "attachComposerHook called with null micButton");
            return;
        }

        try {
            micButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Context context = v.getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Instagram Voice Note");

                    if (VoiceNoteState.hasPendingInjection()) {
                        File staged = VoiceNoteState.getInjectedAudioFile();
                        long dur = VoiceNoteState.getInjectedDurationMs();
                        builder.setMessage("Ready to send staged audio:\n" + staged.getName() +
                                " (" + (dur / 1000) + "s)\n\nWhat would you like to do?");
                        builder.setPositiveButton("Send Now", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                triggerNativeSend(composerInstance, context);
                            }
                        });
                        builder.setNeutralButton("Choose Different Audio", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchAudioPicker(context);
                            }
                        });
                        builder.setNegativeButton("Cancel Staged Audio", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                VoiceNoteState.clearPendingInjection();
                                Toast.makeText(context, "Voice note injection canceled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        builder.setMessage("Select an MP3 audio file to send as a native voice message in this direct thread.");
                        builder.setPositiveButton("Select MP3...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launchAudioPicker(context);
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                    }

                    builder.show();
                    return true; // Consumed long click
                }
            });
            Log.i(TAG, "Successfully attached long-press listener to Direct mic button");
        } catch (Throwable t) {
            Log.e(TAG, "Error attaching mic button hook", t);
        }
    }

    /**
     * Launches the audio picker activity.
     */
    public static void launchAudioPicker(Context context) {
        try {
            Intent intent = new Intent(context, AudioPickerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to launch AudioPickerActivity", t);
            Toast.makeText(context, "Failed to open audio picker: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Intercepts recorded voice audio file path before sending.
     * Replaces the recorded temporary file with the staged MP3 voice note.
     */
    public static String interceptAudioFilePath(String originalFilePath) {
        if (VoiceNoteState.hasPendingInjection()) {
            File injectedFile = VoiceNoteState.getInjectedAudioFile();
            Log.i(TAG, "Intercepted voice note path: replacing [" + originalFilePath +
                    "] with [" + injectedFile.getAbsolutePath() + "]");
            return injectedFile.getAbsolutePath();
        }
        return originalFilePath;
    }

    /**
     * Intercepts audio duration to reflect the staged MP3's actual duration.
     */
    public static long interceptDurationMs(long originalDurationMs) {
        if (VoiceNoteState.hasPendingInjection()) {
            long injectedDur = VoiceNoteState.getInjectedDurationMs();
            Log.i(TAG, "Intercepted voice note duration: replacing [" + originalDurationMs +
                    "ms] with [" + injectedDur + "ms]");
            return injectedDur;
        }
        return originalDurationMs;
    }

    /**
     * Intercepts waveform amplitude samples to display the correct waveform in the chat bubble.
     */
    public static byte[] interceptWaveform(byte[] originalWaveform) {
        if (VoiceNoteState.hasPendingInjection()) {
            byte[] injectedWaveform = VoiceNoteState.getInjectedWaveform();
            Log.i(TAG, "Intercepted voice note waveform (" + injectedWaveform.length + " samples)");
            return injectedWaveform;
        }
        return originalWaveform;
    }

    /**
     * Called when a voice message dispatch completes.
     */
    public static void onSendVoiceMessageFinished() {
        if (VoiceNoteState.hasPendingInjection()) {
            Log.i(TAG, "Native voice message dispatch completed; resetting pending state");
            VoiceNoteState.clearPendingInjection();
        }
    }

    /**
     * Attempts to trigger the composer's native voice message dispatch method programmatically.
     */
    private static void triggerNativeSend(Object composerInstance, Context context) {
        try {
            // Reflected lookup for sendVoiceMessage or dispatch method on controller
            java.lang.reflect.Method[] methods = composerInstance.getClass().getMethods();
            for (java.lang.reflect.Method m : methods) {
                if (m.getName().toLowerCase().contains("sendvoicemessage") ||
                    m.getName().toLowerCase().contains("sendvoice")) {
                    m.setAccessible(true);
                    if (m.getParameterTypes().length == 0) {
                        m.invoke(composerInstance);
                        Log.i(TAG, "Invoked native send method: " + m.getName());
                        return;
                    }
                }
            }
            Toast.makeText(context, "Audio ready. Tap mic button to send.", Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            Log.w(TAG, "Could not reflectively trigger send; user will tap mic to send", t);
            Toast.makeText(context, "Audio ready. Tap mic button to send.", Toast.LENGTH_LONG).show();
        }
    }
}
