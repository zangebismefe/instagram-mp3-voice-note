package dev.zangebismefe.instagramvoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Injects a clean, non-intrusive MP3 Voice Note action button into Instagram Direct chats.
 * Bypasses touch-interception issues on Instagram's native microphone button.
 */
public final class DirectOverlayController {

    private static final String TAG = "IGVoiceOverlay";
    private static final int OVERLAY_BUTTON_ID = 0x7f099021;
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private DirectOverlayController() {}

    public static void attachToActivity(final Activity activity) {
        if (activity == null || activity.isFinishing()) return;

        final View decorView = activity.getWindow().getDecorView();
        if (decorView == null) return;

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    // Check if current screen is Direct or has a composer
                    scanAndAttach(activity, decorView);
                } catch (Throwable t) {
                    Log.e(TAG, "Error in onGlobalLayout scan", t);
                }
            }
        });

        // Run an immediate scan
        sMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanAndAttach(activity, decorView);
            }
        }, 500);
    }

    private static void scanAndAttach(final Activity activity, final View decorView) {
        if (!(decorView instanceof ViewGroup)) return;
        ViewGroup root = (ViewGroup) decorView;

        // Check if overlay is already attached
        if (root.findViewById(OVERLAY_BUTTON_ID) != null) {
            return;
        }

        // Search for message input or voice recording view in current hierarchy
        boolean isDirectThread = searchForDirectViews(root);

        // If in direct thread, inject the MP3 Voice Note trigger button
        if (isDirectThread) {
            injectVoiceActionButton(activity, root);
        }
    }

    /**
     * Scans for Direct-specific strings or composer views in the view hierarchy.
     */
    private static boolean searchForDirectViews(View view) {
        if (view == null) return false;

        CharSequence desc = view.getContentDescription();
        if (desc != null) {
            String d = desc.toString().toLowerCase();
            if (d.contains("voice message") || d.contains("voice note") ||
                d.contains("send message") || d.contains("message...") ||
                d.contains("audio message")) {
                return true;
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                if (searchForDirectViews(vg.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Injects a circular button above the chat composer bar.
     */
    private static void injectVoiceActionButton(final Activity activity, final ViewGroup root) {
        try {
            int dp44 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, activity.getResources().getDisplayMetrics());
            int dp16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, activity.getResources().getDisplayMetrics());
            int dp70 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, activity.getResources().getDisplayMetrics());

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp44, dp44);
            params.gravity = Gravity.BOTTOM | Gravity.END;
            params.rightMargin = dp16;
            params.bottomMargin = dp70; // Positioned right above the microphone button

            final LinearLayout button = new LinearLayout(activity);
            button.setId(OVERLAY_BUTTON_ID);
            button.setLayoutParams(params);
            button.setGravity(Gravity.CENTER);
            button.setClickable(true);
            button.setFocusable(true);

            // Instagram gradient style
            GradientDrawable background = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.parseColor("#E1306C"), Color.parseColor("#833AB4")}
            );
            background.setShape(GradientDrawable.OVAL);
            button.setBackground(background);
            button.setElevation(8.0f);

            TextView icon = new TextView(activity);
            icon.setText("🎵");
            icon.setTextSize(18);
            icon.setGravity(Gravity.CENTER);
            button.addView(icon);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVoiceNoteDialog(activity);
                }
            });

            root.addView(button);
            Log.i(TAG, "Successfully injected MP3 Voice Note overlay button into Direct chat");
        } catch (Throwable t) {
            Log.e(TAG, "Failed injecting voice action button", t);
        }
    }

    private static void showVoiceNoteDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Instagram MP3 Voice Note");

        if (VoiceNoteState.hasPendingInjection()) {
            File staged = VoiceNoteState.getInjectedAudioFile();
            long dur = VoiceNoteState.getInjectedDurationMs();
            builder.setMessage("Ready to send:\n" + staged.getName() + " (" + (dur / 1000) + "s)\n\n" +
                    "Tap 'Send Now' or tap the native microphone button to dispatch as a voice message.");
            builder.setPositiveButton("Send Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(activity, "Audio staged. Tap mic button in chat to dispatch.", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNeutralButton("Choose Another MP3", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    launchPicker(activity);
                }
            });
            builder.setNegativeButton("Cancel Staged Audio", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VoiceNoteState.clearPendingInjection();
                    Toast.makeText(activity, "Cleared staged voice note", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            builder.setMessage("Choose a local MP3 or audio file from your device storage to send as a native voice message in this chat.");
            builder.setPositiveButton("Select MP3...", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    launchPicker(activity);
                }
            });
            builder.setNegativeButton("Cancel", null);
        }

        builder.show();
    }

    private static void launchPicker(Activity activity) {
        try {
            Intent intent = new Intent(activity, AudioPickerActivity.class);
            activity.startActivity(intent);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to launch picker activity", t);
            Toast.makeText(activity, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
