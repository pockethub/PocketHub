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
package com.github.mobile.tests.gist;

import android.test.AndroidTestCase;

import com.github.mobile.core.gist.GistStore;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Unit tests of {@link GistStore}
 */
public class GistStoreTest extends AndroidTestCase {

    /**
     * Verify issue is updated when re-added
     */
    public void testReuseIssue() {
        GistStore store = new GistStore(new GistService());
        assertNull(store.getGist("abcd"));

        Gist gist = new Gist();
        gist.setId("abcd").setDescription("description");
        assertSame(gist, store.addGist(gist));
        assertSame(gist, store.getGist("abcd"));

        Gist gist2 = new Gist();
        gist2.setId("abcd").setDescription("description2");
        assertSame(gist, store.addGist(gist2));
        assertEquals(gist2.getDescription(), gist.getDescription());
        assertSame(gist, store.getGist("abcd"));
    }
}
