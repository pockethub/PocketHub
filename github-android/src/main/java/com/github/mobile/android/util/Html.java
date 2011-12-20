package com.github.mobile.android.util;

import android.text.Html.ImageGetter;

/**
 * Html Utilities
 */
public class Html {

    /**
     * Encode HTML
     *
     * @param html
     * @return html
     */
    public static CharSequence encode(String html) {
        return encode(html, null);
    }

    /**
     * Encode HTML
     *
     * @param html
     * @param imageGetter
     * @return html
     */
    public static CharSequence encode(String html, ImageGetter imageGetter) {
        if (html == null)
            return "";
        if (html.length() == 0)
            return html;
        // These add extra padding that should be styled explicitly
        if (html.startsWith("<p>") && html.endsWith("</p>"))
            html = html.substring(3, html.length() - 4);
        return android.text.Html.fromHtml(html, imageGetter, null);
    }
}
