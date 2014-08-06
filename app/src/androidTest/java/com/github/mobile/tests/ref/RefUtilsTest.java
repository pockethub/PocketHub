/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.tests.ref;

import android.test.AndroidTestCase;

import com.github.mobile.core.ref.RefUtils;

import org.eclipse.egit.github.core.Reference;

/**
 * Tests of {@link RefUtils}
 */
public class RefUtilsTest extends AndroidTestCase {

    /**
     * Verify {@link RefUtils#isBranch(org.eclipse.egit.github.core.Reference)}
     */
    public void testIsBranch() {
        assertFalse(RefUtils.isBranch(null));
        assertFalse(RefUtils.isBranch(new Reference()));
        assertFalse(RefUtils.isBranch(new Reference().setRef("")));
        assertFalse(RefUtils.isBranch(new Reference().setRef("test")));
        assertFalse(RefUtils.isBranch(new Reference().setRef("refs/tags/v1")));
        assertFalse(RefUtils.isBranch(new Reference().setRef("refs/b1")));
        assertTrue(RefUtils.isBranch(new Reference().setRef("refs/heads/b2")));
    }

    /**
     * Verify {@link RefUtils#isTag(org.eclipse.egit.github.core.Reference)}
     */
    public void testIsTag() {
        assertFalse(RefUtils.isTag((Reference) null));
        assertFalse(RefUtils.isTag(new Reference()));
        assertFalse(RefUtils.isTag(new Reference().setRef("")));
        assertFalse(RefUtils.isTag(new Reference().setRef("test")));
        assertFalse(RefUtils.isTag(new Reference().setRef("refs/b1")));
        assertFalse(RefUtils.isTag(new Reference().setRef("refs/heads/b2")));
        assertTrue(RefUtils.isTag(new Reference().setRef("refs/tags/v1")));
    }

    /**
     * Verify {@link RefUtils#isValid(org.eclipse.egit.github.core.Reference)}
     */
    public void testIsValid() {
        assertFalse(RefUtils.isValid(null));
        assertFalse(RefUtils.isValid(new Reference()));
        assertFalse(RefUtils.isValid(new Reference().setRef("")));
        assertFalse(RefUtils.isValid(new Reference()
                .setRef("refs/pull/6/merge")));
        assertFalse(RefUtils
                .isValid(new Reference().setRef("refs/pull/6/head")));
        assertTrue(RefUtils.isValid(new Reference().setRef("refs/pull")));
        assertTrue(RefUtils.isValid(new Reference().setRef("refs/heads/b1")));
        assertTrue(RefUtils.isValid(new Reference().setRef("refs/tags/v1")));
    }

    /**
     * Verify {@link RefUtils#getName(Reference)}
     */
    public void testGetName() {
        assertNull(RefUtils.getName((Reference) null));
        assertNull(RefUtils.getName(new Reference()));
        assertEquals("", RefUtils.getName(new Reference().setRef("")));
        assertEquals("unchanged",
                RefUtils.getName(new Reference().setRef("unchanged")));
        assertEquals("branch",
                RefUtils.getName(new Reference().setRef("refs/heads/branch")));
        assertEquals("tag",
                RefUtils.getName(new Reference().setRef("refs/tags/tag")));
        assertEquals("notes",
                RefUtils.getName(new Reference().setRef("refs/notes")));

    }

    /**
     * Verify {@link RefUtils#getPath(Reference)}
     */
    public void testGetPath() {
        assertNull(RefUtils.getPath(null));
        assertNull(RefUtils.getPath(new Reference()));
        assertEquals("", RefUtils.getPath(new Reference().setRef("")));
        assertEquals("unchanged",
                RefUtils.getPath(new Reference().setRef("unchanged")));
        assertEquals("heads/branch",
                RefUtils.getPath(new Reference().setRef("refs/heads/branch")));
        assertEquals("tags/tag",
                RefUtils.getPath(new Reference().setRef("refs/tags/tag")));
        assertEquals("notes",
                RefUtils.getPath(new Reference().setRef("refs/notes")));

    }
}
