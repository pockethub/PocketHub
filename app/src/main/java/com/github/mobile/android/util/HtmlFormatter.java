package com.github.mobile.android.util;

/**
 * Formatter for HTML strings so that they are ready to be displayed in text views
 */
public class HtmlFormatter {

    private static final String TOGGLE_START = "<span class=\"email-hidden-toggle\">";

    private static final String TOGGLE_END = "</span>";

    private static final String REPLY_START = "<div class=\"email-quoted-reply\">";

    private static final String REPLY_END = "</div>";

    private static final String EMAIL_START = "<div class=\"email-fragment\">";

    private static final String EMAIL_END = "</div>";

    private static final String HIDDEN_REPLY_START = "<div class=\"email-hidden-reply style=\" display:none>";

    private static final String HIDDEN_REPLY_END = "</div>";

    private static final String BREAK = "<br>";

    private static final String PARAGRAPH_START = "<p>";

    private static final String PARAGRAPH_END = "</p>";

    private static final String BLOCKQUOTE_START = "<blockquote>";

    private static final String BLOCKQUOTE_END = "</blockquote>";

    private static final String SPACE = "&nbsp;";

    private static final String PRE_START = "<pre>";

    private static final String PRE_END = "</pre>";

    private static StringBuilder strip(final StringBuilder input, final String prefix, final String suffix) {
        int start = input.indexOf(prefix);
        while (start != -1) {
            int end = input.indexOf(suffix, start + prefix.length());
            if (end == -1)
                end = input.length();
            input.delete(start, end + suffix.length());
            start = input.indexOf(prefix, start);
        }
        return input;
    }

    private static StringBuilder replace(final StringBuilder input, final String from, final String to) {
        int start = input.indexOf(from);
        int length = from.length();
        while (start != -1) {
            input.delete(start, start + length);
            input.insert(start, to);
            start = input.indexOf(from, start);
        }
        return input;
    }

    private static StringBuilder replace(final StringBuilder input, final String fromStart, final String fromEnd,
            final String toStart, final String toEnd) {
        int start = input.indexOf(fromStart);
        while (start != -1) {

            input.delete(start, start + fromStart.length());
            input.insert(start, toStart);

            int end = input.indexOf(fromEnd, start + toStart.length());
            if (end != -1) {
                input.delete(end, end + fromEnd.length());
                input.insert(end, toEnd);
            }

            start = input.indexOf(fromStart);
        }
        return input;
    }

    private static StringBuilder formatPres(final StringBuilder input) {
        int start = input.indexOf(PRE_START);
        int spaceAdvance = SPACE.length() - 1;
        int breakAdvance = BREAK.length() - 1;
        while (start != -1) {
            int end = input.indexOf(PRE_END, start + PRE_START.length());
            if (end == -1)
                break;
            for (int i = start; i < end; i++) {
                switch (input.charAt(i)) {
                case ' ':
                    input.deleteCharAt(i);
                    input.insert(i, SPACE);
                    start += spaceAdvance;
                    end += spaceAdvance;
                    break;
                case '\t':
                    input.deleteCharAt(i);
                    input.insert(i, SPACE);
                    start += spaceAdvance;
                    end += spaceAdvance;
                    for (int j = 0; j < 3; j++) {
                        input.insert(i, SPACE);
                        start += spaceAdvance + 1;
                        end += spaceAdvance + 1;
                    }
                    break;
                case '\n':
                    input.deleteCharAt(i);
                    input.insert(i, BREAK);
                    start += breakAdvance;
                    end += breakAdvance;
                    break;
                }
            }
            start = input.indexOf(PRE_START, end + PRE_END.length());
        }
        return input;
    }

    /**
     * Format given HTML string so it is ready to be presented in a text view
     *
     * @param html
     * @return formatted HTML
     */
    public static final CharSequence format(final String html) {
        if (html == null)
            return "";
        if (html.length() == 0)
            return "";

        StringBuilder formatted = new StringBuilder(html);

        // Remove e-mail toggle link
        strip(formatted, TOGGLE_START, TOGGLE_END);

        // Replace div with e-mail content with block quote
        replace(formatted, REPLY_START, REPLY_END, BLOCKQUOTE_START, BLOCKQUOTE_END);

        // Remove hidden div
        strip(formatted, HIDDEN_REPLY_START, HIDDEN_REPLY_END);

        // Replace paragraphs with breaks
        replace(formatted, PARAGRAPH_START, BREAK);
        replace(formatted, PARAGRAPH_END, BREAK);

        formatPres(formatted);

        // Remove e-mail div around actual body
        if (formatted.indexOf(EMAIL_START) == 0) {
            int emailEnd = formatted.indexOf(EMAIL_END, EMAIL_START.length());
            if (emailEnd != -1) {
                formatted.delete(emailEnd, emailEnd + EMAIL_END.length());
                formatted.delete(0, EMAIL_START.length());
                int fullEmail = emailEnd - EMAIL_START.length();
                for (int i = 0; i < fullEmail; i++)
                    if (formatted.charAt(i) == '\n') {
                        formatted.deleteCharAt(i);
                        formatted.insert(i, BREAK);
                        i += BREAK.length() - 1;
                        fullEmail += BREAK.length() - 1;
                    }
            }
        }

        // Trim trailing breaks and whitespace
        int length = formatted.length();
        int breakLength = BREAK.length();
        while (length > 0) {
            if (formatted.indexOf(BREAK) == 0)
                formatted.delete(0, breakLength);
            else if (length >= breakLength && formatted.lastIndexOf(BREAK) == length - breakLength)
                formatted.delete(length - breakLength, length);
            else if (Character.isWhitespace(formatted.charAt(0)))
                formatted.deleteCharAt(0);
            else if (Character.isWhitespace(formatted.charAt(length - 1)))
                formatted.deleteCharAt(length - 1);
            else
                break;
            length = formatted.length();
        }

        return formatted;
    }
}
