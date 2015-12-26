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
package com.github.pockethub.tests.gist;

import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.github.pockethub.core.gist.GistStore;

/**
 * Unit tests of {@link GistStore}
 */
public class GistStoreTest extends AndroidTestCase {

    /**
     * Verify issue is updated when re-added
     */
    public void testReuseIssue() {
        GistStore store = new GistStore(mContext);
        assertNull(store.getGist("abcd"));

        Gist gist = new Gist();
        gist.id = "abcd";
        gist.description = "description";
        assertSame(gist, store.addGist(gist));
        assertSame(gist, store.getGist("abcd"));

        Gist gist2 = new Gist();
        gist2.id = "abcd";
        gist.description = "description2";
        assertSame(gist, store.addGist(gist2));
        assertEquals(gist2.description, gist.description);
        assertSame(gist, store.getGist("abcd"));
    }
}
