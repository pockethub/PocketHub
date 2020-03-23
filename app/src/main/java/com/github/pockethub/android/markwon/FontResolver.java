package com.github.pockethub.android.markwon;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.caverock.androidsvg.SimpleAssetResolver;

public class FontResolver extends SimpleAssetResolver {

    public FontResolver(AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    public Typeface resolveFont(String fontFamily, int fontWeight, String fontStyle) {
        Typeface typeface = super.resolveFont(fontFamily, fontWeight, fontStyle);

        int style = Typeface.NORMAL;
        switch (fontStyle) {
            case "normal":
                style = Typeface.NORMAL;
                break;
            case "italic":
            case "oblique":
                style = Typeface.ITALIC;
                break;
        }

        if (fontWeight >= 600) {
            style += Typeface.BOLD;
        }

        if (typeface == null) {
            try {
                return Typeface.create(fontFamily, style);
            } catch (Exception e) {
                return null;
            }
        }

        return typeface;
    }
}
