package com.github.mobile.android.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests of HTML conversions done when rendering markdown
 */
public class HtmlTest {

    /**
     * Single email toggle span is removed
     */
    @Test
    public void toggleRemoved() {
        String html = "before <span class=\"email-hidden-toggle\"><a href=\"#\">…</a></span>after";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("before after", formatted.toString());
    }

    /**
     * Multiple email toggle spans are removed
     */
    @Test
    public void togglesRemoved() {
        String html = "before <span class=\"email-hidden-toggle\"><a href=\"#\">…</a></span>after<span class=\"email-hidden-toggle\"><a href=\"#\">…</a></span>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("before after", formatted.toString());
    }

    /**
     * Email div is transformed into block quote
     */
    @Test
    public void emailQuoted() {
        String html = "before <div class=\"email-quoted-reply\">quoted</div> after";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("before <blockquote>quoted</blockquote> after", formatted.toString());
    }

    /**
     * Leading break is removed
     */
    @Test
    public void leadingBreak() {
        String html = "<br>content";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }

    /**
     * Trailing break is removed
     */
    @Test
    public void trailingBreak() {
        String html = "content<br>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }

    /**
     * Leading & trailing breaks are removed
     */
    @Test
    public void wrappedBreaks() {
        String html = "<br>content<br>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }

    /**
     * Leading & trailing breaks are removed
     */
    @Test
    public void wrappedParagraphs() {
        String html = "<p>content</p>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }

    /**
     * Leading whitespace is removed
     */
    @Test
    public void leadingWhitespace() {
        String html = " content";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }

    /**
     * Trailing whitespace is removed
     */
    @Test
    public void trailingWhitespace() {
        String html = "content ";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }

    /**
     * Leading & trailing whitespace is removed
     */
    @Test
    public void wrappedWhitetspace() {
        String html = " content ";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("content", formatted.toString());
    }
}
