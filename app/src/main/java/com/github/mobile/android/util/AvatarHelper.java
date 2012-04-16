package com.github.mobile.android.util;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.util.Image.roundCornersAndOverlayOnWhite;
import static org.apache.commons.io.IOUtils.closeQuietly;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.github.mobile.android.R;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.madgag.android.lazydrawables.ImageProcessor;
import com.madgag.android.lazydrawables.ImageResourceDownloader;
import com.madgag.android.lazydrawables.ImageResourceStore;
import com.madgag.android.lazydrawables.ImageSession;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.User;

/**
 * Avatar utilities
 */
public class AvatarHelper {

    private static final float CORNER_RADIUS_IN_DIP = 3;

    private static final String TAG = "GHAU";
    private final static Pattern gravatarIdWithinUrl = Pattern.compile("/avatar/(\\p{XDigit}{32})");

    private final Resources resources;

    @Inject
    @Named("gravatarStore")
    private ImageResourceStore<String, Bitmap> store;

    private final GravatarDownloader downloader = new GravatarDownloader();

    private final LoadingCache<Integer, ImageSession<String, Bitmap>> avatarLoaders =
            CacheBuilder.newBuilder().build(
                    new CacheLoader<Integer, ImageSession<String, Bitmap>>() {
                        public ImageSession<String, Bitmap> load(Integer imageSize) {
                            return new ImageSession<String, Bitmap>(new ScaledAndRoundedAvatarGenerator(imageSize),
                                    downloader, store, resources.getDrawable(R.drawable.gravatar_icon));
                        }
                    });

    private final float cornerRadius;

    @Inject
    public AvatarHelper(Resources resources) {
        this.resources = resources;
        cornerRadius = CORNER_RADIUS_IN_DIP * resources.getDisplayMetrics().density;
    }

    /**
     * Sets the image on the ImageView to the user's avatar.
     *
     * If the avatar is not immediately available, a holding 'octocat' avatar will be displayed,
     * the image will update itself once the avatar has finished downloading.
     *
     * @param view
     * @param user
     */
    public void bind(final ImageView view, final User user) {
        String gravatarId = gravatarIdFor(user);

        if (gravatarId != null) {
            view.setImageDrawable(avatarLoaders.getUnchecked(view.getLayoutParams().width).get(gravatarId));
            view.setVisibility(VISIBLE);
        }
    }

    private class ScaledAndRoundedAvatarGenerator implements ImageProcessor<Bitmap> {

        private final int sizeInPixels;

        public ScaledAndRoundedAvatarGenerator(int sizeInPixels) {
            this.sizeInPixels = sizeInPixels;
        }

        public Drawable convert(Bitmap bitmap) {
            Bitmap scaledBitmap = createScaledBitmap(bitmap, sizeInPixels, sizeInPixels, true);
            return new BitmapDrawable(resources, roundCornersAndOverlayOnWhite(scaledBitmap, cornerRadius));
        }

    }

    private static class GravatarDownloader implements ImageResourceDownloader<String, Bitmap> {

        public Bitmap get(String gravatarId) {
            String avatarUrl = "https://secure.gravatar.com/avatar/" + gravatarId +
                    "?s=128&d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png";
            HttpRequest request = HttpRequest.get(avatarUrl);
            if (!request.ok())
                return null;

            InputStream is = null;
            try {
                is = request.stream();
                return BitmapFactory.decodeStream(new FlushedInputStream(is));
            } catch (HttpRequestException hre) {
                Log.e(TAG, "Error downloading " + gravatarId, hre);
                throw hre;
            } finally {
                closeQuietly(is);
            }
        }

    }

    /**
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF. This is needed for
     * older versions of BitmapFactory.decodeStream() which can not handle partial skips - this was fixed in the
     * Android platform around 2010, but the precise version of Android the fix was applied to is not apparent.
     * <p/>
     * Taken from the android-imagedownloader project, licenced under Apache License, Version 2.0
     * http://code.google.com/p/android-imagedownloader/source/browse/trunk/src/com/example/android/imagedownloader/ImageDownloader.java?spec=svn4&r=4#210
     * <p/>
     * See also http://android-developers.blogspot.co.uk/2010/07/multithreading-for-performance.html
     */
    private static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    /**
     * Allows us to key off the gravatar id rather than the entire avatar url, which is quite long
     * <p/>
     * https://github.com/eclipse/egit-github/pull/4
     *
     * @param user
     * @return gravatar id for user
     */
    private static String gravatarIdFor(User user) {
        String id = user.getGravatarId();
        if (!TextUtils.isEmpty(id))
            return id;

        String avatarUrl = user.getAvatarUrl();

        if (avatarUrl == null)
            return null;

        Matcher matcher = gravatarIdWithinUrl.matcher(avatarUrl);
        return matcher.find() ? matcher.group(1) : null;
    }

}
