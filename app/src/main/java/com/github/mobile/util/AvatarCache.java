package com.github.mobile.util;

import android.graphics.drawable.BitmapDrawable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Singleton instance of the in-memory cache of loaded avatars
 */
public class AvatarCache {

    private static final int CACHE_SIZE = 75;

    private Map<Object, BitmapDrawable> loaded;

    private static AvatarCache instance;


    private AvatarCache() {
        loaded = new LinkedHashMap<Object, BitmapDrawable>(
            CACHE_SIZE, 1.0F) {

            private static final long serialVersionUID = -4191624209581976720L;

            @Override
            protected boolean removeEldestEntry(
                Map.Entry<Object, BitmapDrawable> eldest) {
                return size() >= CACHE_SIZE;
            }
        };
    }

    public static AvatarCache getInstance() {
        if (instance == null) {
            instance = new AvatarCache();
        }

        return instance;
    }

    public void putImage(final String key, final BitmapDrawable image) {
        loaded.put(key, image);
    }

    public BitmapDrawable getImage(final String key) {
        return loaded.get(key);
    }

    public void clearCache() {
        loaded.clear();
    }
}
