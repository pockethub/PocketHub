package com.github.mobile.android.util;

import static android.content.Context.WINDOW_SERVICE;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.github.mobile.android.R.drawable;

import java.io.File;
import java.io.IOException;

import roboguice.util.RoboAsyncTask;

/**
 * Getter for an image
 */
public class HttpImageGetter implements ImageGetter {

    private class LoadingImageGetter implements ImageGetter {

        public Drawable getDrawable(String source) {
            Drawable image = context.getResources().getDrawable(drawable.image_loading_icon);
            image.setBounds(0, 0, 48, 48);
            return image;
        }
    }

    private LoadingImageGetter loading = new LoadingImageGetter();

    private final Context context;

    private final File dir;

    private final int width;

    /**
     * Create image getter for context
     *
     * @param context
     */
    public HttpImageGetter(Context context) {
        this.context = context;
        dir = context.getCacheDir();
        Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        width = display.getWidth();
    }

    /**
     * Bind text view to HTML string
     *
     * @param view
     * @param html
     * @return this image getter
     */
    public HttpImageGetter bind(final TextView view, final String html) {
        view.setText(Html.encode(html, loading));
        new RoboAsyncTask<CharSequence>(context) {

            public CharSequence call() throws Exception {
                return Html.encode(html, HttpImageGetter.this);
            }

            protected void onSuccess(CharSequence html) throws Exception {
                view.setText(html);
            }
        }.execute();
        return this;
    }

    public Drawable getDrawable(String source) {
        File output = null;
        try {
            output = File.createTempFile("image", ".jpg", dir);
            synchronized (this) {
                HttpRequest request = HttpRequest.get(source);
                if (!request.ok())
                    return null;
                request.receive(output).disconnect();
            }
            Bitmap bitmap = Image.getBitmap(output, width, Integer.MAX_VALUE);
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        } catch (IOException e) {
            return null;
        } catch (HttpRequestException e) {
            return null;
        } finally {
            if (output != null)
                output.delete();
        }
    }

}
