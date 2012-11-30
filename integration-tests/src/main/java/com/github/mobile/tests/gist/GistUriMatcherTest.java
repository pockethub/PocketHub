/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package com.github.mobile.tests.gist;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.github.mobile.core.gist.GistUriMatcher;

import org.eclipse.egit.github.core.Gist;

/**
 * Unit tests of {@link GistUriMatcher}
 */
public class GistUriMatcherTest extends AndroidTestCase {

    /**
     * Verify empty uri
     */
    public void testEmptyUri() {
        assertNull(GistUriMatcher.getGist(Uri.parse("")));
    }

    /**
     * Verify invalid Gist ids in URIs
     */
    public void testNonGistId() {
        assertNull(GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/TEST")));
        assertNull(GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abc%20")));
        assertNull(GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abcdefg")));
    }

    /**
     * Verify public Gist id
     */
    public void testPublicGist() {
        Gist gist = GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/1234"));
        assertNotNull(gist);
        assertEquals("1234", gist.getId());
    }

    /**
     * Verify public Gist id
     */
    public void testPrivateGist() {
        Gist gist = GistUriMatcher.getGist(Uri
                .parse("https://gist.github.com/abcd1234abcd1234abcd"));
        assertNotNull(gist);
        assertEquals("abcd1234abcd1234abcd", gist.getId());
    }
}
