package com.github.mobile.android.util;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_MARK_MARK;
import android.text.Editable;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.TypefaceSpan;

import org.xml.sax.XMLReader;

/**
 * HTML Utilities
 */
public class Html {

    private static final String TAG_DEL = "del";

    private static final String TAG_UL = "ul";

    private static final String TAG_OL = "ol";

    private static final String TAG_LI = "li";

    private static final String TAG_CODE = "code";

    private static final TagHandler TAG_HANDLER = new TagHandler() {

        private int indentLevel;

        public void handleTag(final boolean opening, final String tag, final Editable output, final XMLReader xmlReader) {
            if (TAG_DEL.equalsIgnoreCase(tag)) {
                if (opening) {
                    int length = output.length();
                    output.setSpan(new StrikethroughSpan(), length, length, SPAN_MARK_MARK);
                } else {
                    int length = output.length();
                    Object span = getLast(output, StrikethroughSpan.class);
                    int start = output.getSpanStart(span);
                    output.removeSpan(span);
                    if (start != length)
                        output.setSpan(span, start, length, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return;
            }

            if (TAG_UL.equalsIgnoreCase(tag) || TAG_OL.equalsIgnoreCase(tag)) {
                if (opening)
                    indentLevel++;
                else
                    indentLevel--;
                return;
            }

            if (TAG_LI.equalsIgnoreCase(tag)) {
                if (opening) {
                    output.append('\n');
                    for (int i = 0; i < indentLevel * 2; i++)
                        output.append(' ');
                    output.append('\u2022').append(' ').append(' ');
                } else
                    output.append('\n');
                return;
            }

            if (TAG_CODE.equalsIgnoreCase(tag)) {
                if (opening) {
                    int length = output.length();
                    TypefaceSpan span = new TypefaceSpan("monospace");
                    output.setSpan(span, length, length, SPAN_MARK_MARK);
                } else {
                    int length = output.length();
                    Object span = getLast(output, TypefaceSpan.class);
                    int start = output.getSpanStart(span);
                    output.removeSpan(span);
                    if (start != length)
                        output.setSpan(span, start, length, SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return;
            }
        }
    };

    /**
     * Get last span of given kind
     *
     * @param text
     * @param kind
     * @return span
     */
    private static Object getLast(final Spanned text, final Class<?> kind) {
        Object[] spans = text.getSpans(0, text.length(), kind);
        return spans.length > 0 ? spans[spans.length - 1] : null;
    }

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
    public static CharSequence encode(final String html, final ImageGetter imageGetter) {
        if (html == null)
            return "";
        if (html.length() == 0)
            return html;

        return android.text.Html.fromHtml(html, imageGetter, TAG_HANDLER);
    }
}
