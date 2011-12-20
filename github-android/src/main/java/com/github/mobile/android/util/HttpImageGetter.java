package com.github.mobile.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.File;
import java.io.IOException;

/**
 * Getter for an image
 */
public class HttpImageGetter implements ImageGetter {

    private final File dir;

    /**
     * Create image getter for context
     *
     * @param context
     */
    public HttpImageGetter(Context context) {
        dir = context.getCacheDir();
    }

    public Drawable getDrawable(String source) {
        File output = null;
        try {
            output = File.createTempFile("image", ".jpg", dir);
            synchronized (this) {
                HttpRequest.get(source).receive(output).disconnect();
            }
            Bitmap bitmap = Image.getBitmap(output);
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
