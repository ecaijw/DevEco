package com.example.caijingwei.devecocontact;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Helper {
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static Bitmap sRedBitmap = null;
    public static Bitmap getRedBitmap() {
        if (sRedBitmap == null) {
            sRedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sRedBitmap);
            canvas.drawColor(Color.RED);
        }
        return sRedBitmap;
    }

    private static final String DEFAULT_TAG = "luke";
    public static void log(String tag, String text) {
        Log.d(tag, text);
    }

    public static void log(String text) {
        log(DEFAULT_TAG, text);
    }
}
