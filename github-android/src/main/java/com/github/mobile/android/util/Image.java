package com.github.mobile.android.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Image utilities
 */
public class Image {

    private static final String TAG = "AUIU";

    /**
     * Get a bitmap from the image path
     *
     * @param imagePath
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final String imagePath) {
        return getBitmap(imagePath, 1);
    }

    /**
     * Get a bitmap from the image path
     *
     * @param imagePath
     * @param sampleSize
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final String imagePath, int sampleSize) {
        final Options options = new Options();
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inSampleSize = sampleSize;

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(imagePath, "r");
            return BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (file != null)
                try {
                    file.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage(), e);
                }
        }
    }

    /**
     * Get size of image
     *
     * @param imagePath
     * @return size
     */
    public static Point getSize(final String imagePath) {
        final Options options = new Options();
        options.inJustDecodeBounds = true;

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(imagePath, "r");
            BitmapFactory.decodeFileDescriptor(file.getFD(), null, options);
            return new Point(options.outWidth, options.outHeight);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (file != null)
                try {
                    file.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage(), e);
                }
        }
    }

    /**
     * Get bitmap with maximum height or width
     *
     * @param imagePath
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final String imagePath, int width, int height) {
        Point size = getSize(imagePath);
        int currWidth = size.x;
        int currHeight = size.y;

        int scale = 1;
        while (currWidth >= width || currHeight >= height) {
            currWidth /= 2;
            currHeight /= 2;
            scale *= 2;
        }

        return getBitmap(imagePath, scale);
    }

    /**
     * Get bitmap with maximum height or width
     *
     * @param image
     * @param width
     * @param height
     * @return image
     */
    public static Bitmap getBitmap(final File image, int width, int height) {
        return getBitmap(image.getAbsolutePath(), width, height);
    }

    /**
     * Get a bitmap from the image file
     *
     * @param image
     * @return bitmap or null if read fails
     */
    public static Bitmap getBitmap(final File image) {
        return getBitmap(image.getAbsolutePath());
    }

    /**
     * Load a {@link Bitmap} from the given path and set it on the given {@link ImageView}
     *
     * @param imagePath
     * @param view
     */
    public static void setImage(final String imagePath, final ImageView view) {
        setImage(new File(imagePath), view);
    }

    /**
     * Load a {@link Bitmap} from the given {@link File} and set it on the given {@link ImageView}
     *
     * @param image
     * @param view
     */
    public static void setImage(final File image, final ImageView view) {
        Bitmap bitmap = getBitmap(image);
        if (bitmap != null)
            view.setImageBitmap(bitmap);
    }

    /**
     * Round the corners of a {@link Bitmap} using a radius of 10
     *
     * @param bitmap
     * @return rounded corner bitmap
     */
    public static Bitmap roundCorners(final Bitmap bitmap) {
        return roundCorners(bitmap, 10);
    }

    /**
     * Round the corners of a {@link Bitmap}
     *
     * @param bitmap
     * @param radius
     * @return rounded corner bitmap
     */
    public static Bitmap roundCorners(final Bitmap bitmap, final int radius) {
        return roundCorners(bitmap, radius, radius);
    }

    /**
     * Round the corners of a {@link Bitmap}
     *
     * @param bitmap
     * @param radiusX
     * @param radiusY
     * @return rounded corner bitmap
     */
    public static Bitmap roundCorners(final Bitmap bitmap, final int radiusX, final int radiusY) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        Bitmap clipped = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(clipped);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radiusX, radiusY, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        Bitmap rounded = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        canvas = new Canvas(rounded);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(clipped, 0, 0, paint);

        return rounded;
    }
}
