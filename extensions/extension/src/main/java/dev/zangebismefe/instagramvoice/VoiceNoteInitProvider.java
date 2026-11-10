package dev.zangebismefe.instagramvoice;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Auto-initializes the Instagram Voice Note hook using Android's ContentProvider startup mechanism.
 * Runs automatically when Instagram launches, before any Activity is created.
 */
public class VoiceNoteInitProvider extends ContentProvider {

    private static final String TAG = "IGVoiceInit";

    @Override
    public boolean onCreate() {
        try {
            Context context = getContext();
            if (context != null) {
                Context appCtx = context.getApplicationContext();
                if (appCtx instanceof Application) {
                    Application app = (Application) appCtx;
                    app.registerActivityLifecycleCallbacks(new DirectChatLifecycleListener());
                    Log.i(TAG, "Successfully registered DirectChatLifecycleListener via ContentProvider");
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error initializing VoiceNoteInitProvider", t);
        }
        return true;
    }

    private static class DirectChatLifecycleListener implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            try {
                DirectOverlayController.attachToActivity(activity);
            } catch (Throwable t) {
                Log.e(TAG, "Error in onActivityResumed", t);
            }
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
        @Override
        public void onActivityStarted(Activity activity) {}
        @Override
        public void onActivityPaused(Activity activity) {}
        @Override
        public void onActivityStopped(Activity activity) {}
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
        @Override
        public void onActivityDestroyed(Activity activity) {}
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
