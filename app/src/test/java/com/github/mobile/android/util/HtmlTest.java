package com.github.mobile.android.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.mobile.util.HtmlFormatter;

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
     * Email fragment div is removed and newlines are replaced with br tags
     */
    @Test
    public void emailFragment() {
        String html = "before <div class=\"email-fragment\">in\nside</div> after";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("before in<br>side after", formatted.toString());
    }

    /**
     * Email fragment div is removed and newlines are replaced with br tags
     */
    @Test
    public void emailFragments() {
        String html = "before <div class=\"email-fragment\">in\nside</div> after <div class=\"email-fragment\">out\nside</div>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("before in<br>side after out<br>side", formatted.toString());
    }

    /**
     * Email fragment div is removed and newlines are replaced with br tags
     */
    @Test
    public void trailingEmailFragment() {
        String html = "before <div class=\"email-fragment\">in\nside</div>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("before in<br>side", formatted.toString());
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

    /**
     * Pre untouched
     */
    @Test
    public void preWithNoWhitespace() {
        String html = "a<pre>b</pre> c";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("a<pre>b</pre> c", formatted.toString());
    }

    /**
     * Pre space escaped
     */
    @Test
    public void preWithSpaces() {
        String html = "a<pre> b</pre> c";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("a<pre>&nbsp;b</pre> c", formatted.toString());
    }

    /**
     * Pre tab escaped
     */
    @Test
    public void preWithTabs() {
        String html = "a<pre>\tb</pre> c";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("a<pre>&nbsp;&nbsp;&nbsp;&nbsp;b</pre> c", formatted.toString());
    }

    /**
     * Pre newline escaped
     */
    @Test
    public void preWithNewline() {
        String html = "a<pre>\nb</pre> c";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("a<pre><br>b</pre> c", formatted.toString());
    }

    /**
     * Pre space, tab, and newline escaped
     */
    @Test
    public void preWithAllWhitepsace() {
        String html = "a<pre>\nb\tc </pre>d";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("a<pre><br>b&nbsp;&nbsp;&nbsp;&nbsp;c&nbsp;</pre>d", formatted.toString());
    }

    /**
     * Multiple pre elements escaped
     */
    @Test
    public void multiplePresEscaped() {
        String html = "a<pre> c </pre>d<pre>\te\t</pre>";
        CharSequence formatted = HtmlFormatter.format(html);
        assertNotNull(formatted);
        assertEquals("a<pre>&nbsp;c&nbsp;</pre>d<pre>&nbsp;&nbsp;&nbsp;&nbsp;e&nbsp;&nbsp;&nbsp;&nbsp;</pre>",
                formatted.toString());
    }
}
