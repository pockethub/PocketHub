/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.github.mobile.android.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
		final Options options = new Options();
		options.inDither = false;
		options.inPurgeable = true;
		options.inInputShareable = true;

		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(imagePath, "r");
			return BitmapFactory.decodeFileDescriptor(file.getFD(), null,
					options);
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
	 * Get a bitmap from the image file
	 *
	 * @param image
	 * @return bitmap or null if read fails
	 */
	public static Bitmap getBitmap(final File image) {
		return getBitmap(image.getAbsolutePath());
	}

	/**
	 * Load a {@link Bitmap} from the given path and set it on the given
	 * {@link ImageView}
	 *
	 * @param imagePath
	 * @param view
	 */
	public static void setImage(final String imagePath, final ImageView view) {
		setImage(new File(imagePath), view);
	}

	/**
	 * Load a {@link Bitmap} from the given {@link File} and set it on the given
	 * {@link ImageView}
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
	public static Bitmap roundCorners(final Bitmap bitmap, final int radiusX,
			final int radiusY) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);

		Bitmap clipped = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(clipped);
		canvas.drawRoundRect(new RectF(0, 0, width, height), radiusX, radiusY,
				paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		Bitmap rounded = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		canvas = new Canvas(rounded);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.drawBitmap(clipped, 0, 0, paint);

		return rounded;
	}
}
