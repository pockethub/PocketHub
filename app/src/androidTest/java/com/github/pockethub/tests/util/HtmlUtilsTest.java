/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.tests.util;

import android.test.AndroidTestCase;

import com.github.pockethub.util.HtmlUtils;

/**
 * Unit tests of HTML conversions done when rendering markdown
 */
public class HtmlUtilsTest extends AndroidTestCase {

    private String format(String html) {
        CharSequence formatted = HtmlUtils.format(html);
        assertNotNull(formatted);
        return formatted.toString().replace("<githubroot>", "")
                .replace("</githubroot>", "");
    }

    /**
     * Single email toggle span is removed
     */
    public void testToggleRemoved() {
        String html = "before <span class=\"email-hidden-toggle\"><a href=\"#\">…</a></span>after";
        assertEquals("before after", format(html));
    }

    /**
     * Multiple email toggle spans are removed
     */
    public void testTogglesRemoved() {
        String html = "before <span class=\"email-hidden-toggle\"><a href=\"#\">…</a></span>after<span class=\"email-hidden-toggle\"><a href=\"#\">…</a></span>";
        assertEquals("before after", format(html));
    }

    /**
     * Email div is transformed into block quote
     */
    public void testEmailQuoted() {
        String html = "before <div class=\"email-quoted-reply\">quoted</div> after";
        assertEquals("before <blockquote>quoted</blockquote> after",
                format(html));
    }

    /**
     * Email fragment div is removed and newlines are replaced with br tags
     */
    public void testEmailFragment() {
        String html = "before <div class=\"email-fragment\">in\nside</div> after";
        assertEquals("before in<br>side after", format(html));
    }

    /**
     * Email fragment div is removed and newlines are replaced with br tags
     */
    public void testEmailFragments() {
        String html = "before <div class=\"email-fragment\">in\nside</div> after <div class=\"email-fragment\">out\nside</div>";
        assertEquals("before in<br>side after out<br>side", format(html));
    }

    /**
     * Email fragment div is removed and newlines are replaced with br tags
     */
    public void testTrailingEmailFragment() {
        String html = "before <div class=\"email-fragment\">in\nside</div>";
        assertEquals("before in<br>side", format(html));
    }

    /**
     * Leading break is removed
     */
    public void testLeadingBreak() {
        String html = "<br>content";
        assertEquals("content", format(html));
    }

    /**
     * Trailing break is removed
     */
    public void testTrailingBreak() {
        String html = "content<br>";
        assertEquals("content", format(html));
    }

    /**
     * Leading & trailing breaks are removed
     */
    public void testWrappedBreaks() {
        String html = "<br>content<br>";
        assertEquals("content", format(html));
    }

    /**
     * Leading & trailing breaks are removed
     */
    public void testWrappedParagraphs() {
        String html = "<p>content</p>";
        assertEquals("content", format(html));
    }

    /**
     * Paragraph replaced with break
     */
    public void testParagraphReplacedWithBreak() {
        String html = "line1<p>line2</p>";
        assertEquals("line1<br>line2", format(html));
    }

    /**
     * em tags replaced with i tags
     */
    public void testEmReplacedWithI() {
        String html = "a<em>b</em>c";
        assertEquals(html, format(html));
    }

    /**
     * strong tags replaced with b tags
     */
    public void testStrongReplacedWithB() {
        String html = "<strong>a</strong>";
        assertEquals(html, format(html));
    }

    /**
     * Leading whitespace is removed
     */
    public void testLeadingWhitespace() {
        String html = " content";
        assertEquals("content", format(html));
    }

    /**
     * Trailing whitespace is removed
     */
    public void testTrailingWhitespace() {
        String html = "content ";
        assertEquals("content", format(html));
    }

    /**
     * Leading & trailing whitespace is removed
     */
    public void testWrappedWhitetspace() {
        String html = " content ";
        assertEquals("content", format(html));
    }

    /**
     * Pre untouched
     */
    public void testPreWithNoWhitespace() {
        String html = "a<pre>b</pre> c";
        assertEquals("a<pre>b</pre> c", format(html));
    }

    /**
     * Pre space escaped
     */
    public void testPreWithSpaces() {
        String html = "a<pre> b</pre> c";
        assertEquals("a<pre>&nbsp;b</pre> c", format(html));
    }

    /**
     * Pre tab escaped
     */
    public void testPreWithTabs() {
        String html = "a<pre>\tb</pre> c";
        assertEquals("a<pre>&nbsp;&nbsp;&nbsp;&nbsp;b</pre> c", format(html));
    }

    /**
     * Pre newline escaped
     */
    public void testPreWithNewline() {
        String html = "a<pre>\nb</pre> c";
        assertEquals("a<pre><br>b</pre> c", format(html));
    }

    /**
     * Pre space, tab, and newline escaped
     */
    public void testPreWithAllWhitepsace() {
        String html = "a<pre>\nb\tc </pre>d";
        assertEquals("a<pre><br>b&nbsp;&nbsp;&nbsp;&nbsp;c&nbsp;</pre>d",
                format(html));
    }

    /**
     * Multiple pre elements escaped
     */
    public void testMultiplePresEscaped() {
        String html = "a<pre> c </pre>d<pre>\te\t</pre>";
        assertEquals(
                "a<pre>&nbsp;c&nbsp;</pre>d<pre>&nbsp;&nbsp;&nbsp;&nbsp;e&nbsp;&nbsp;&nbsp;&nbsp;</pre>",
                format(html));
    }

    /**
     * Single code element inside a pre element
     */
    public void testFormatPreCodeOnly() {
        String html = "<pre><code>a\nb\nc\n</code></pre>";
        assertEquals("<pre><code>a<br>b<br>c</code></pre>", format(html));
    }
}
