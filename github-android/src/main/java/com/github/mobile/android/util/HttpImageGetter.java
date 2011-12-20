package com.github.mobile.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.view.Display;
import android.view.WindowManager;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.File;
import java.io.IOException;

/**
 * Getter for an image
 */
public class HttpImageGetter implements ImageGetter {

    private final File dir;

    private final int width;

    /**
     * Create image getter for context
     *
     * @param context
     */
    public HttpImageGetter(Context context) {
        dir = context.getCacheDir();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        width = point.x;
    }

    public Drawable getDrawable(String source) {
        File output = null;
        try {
            output = File.createTempFile("image", ".jpg", dir);
            synchronized (this) {
                HttpRequest.get(source).receive(output).disconnect();
            }
            Bitmap bitmap = Image.getBitmap(output, width, Integer.MAX_VALUE);
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        } catch (IOException e) {
            return null;
        } finally {
            if (output != null)
                output.delete();
        }
    }

}
