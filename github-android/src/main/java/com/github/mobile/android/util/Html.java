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
        html = html.replace("<p>", "");
        html = html.replace("</p>", "<br><br>");
        while (html.length() > 0)
            if (html.startsWith("<br>"))
                html = html.substring(4);
            else if (html.endsWith("<br>"))
                html = html.substring(0, html.length() - 4);
            else
                break;
        return android.text.Html.fromHtml(html, imageGetter, null);
    }
}
