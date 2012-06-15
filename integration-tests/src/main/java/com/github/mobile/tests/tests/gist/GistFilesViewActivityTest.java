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
package com.github.mobile.tests.tests.gist;

import android.support.v4.view.ViewPager;

import com.github.mobile.core.gist.GistStore;
import com.github.mobile.tests.ActivityTest;
import com.github.mobile.ui.gist.GistFilesViewActivity;
import com.google.inject.Inject;
import com.viewpagerindicator.R.id;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import roboguice.RoboGuice;

/**
 * Tests of {@link GistFilesViewActivity}
 */
public class GistFilesViewActivityTest extends
        ActivityTest<GistFilesViewActivity> {

    @Inject
    private GistStore store;

    private Gist gist;

    /**
     * Create test
     */
    public GistFilesViewActivityTest() {
        super(GistFilesViewActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        RoboGuice.injectMembers(getInstrumentation().getTargetContext()
                .getApplicationContext(), this);

        gist = new Gist();
        gist.setId("abcd");
        Map<String, GistFile> files = new LinkedHashMap<String, GistFile>();
        files.put("a", new GistFile().setFilename("a").setContent("aa"));
        files.put("b", new GistFile().setFilename("b").setContent("bb"));
        gist.setFiles(files);
        store.addGist(gist);
        setActivityIntent(GistFilesViewActivity.createIntent(gist, 0));
    }

    /**
     * Verify changing pages between gist files
     *
     * @throws Throwable
     */
    public void testChangingPages() throws Throwable {
        final ViewPager pager = (ViewPager) getActivity().findViewById(
                id.vp_pages);
        assertEquals(0, pager.getCurrentItem());
        ui(new Runnable() {

            public void run() {
                pager.setCurrentItem(1, true);
            }
        });
        assertEquals(1, pager.getCurrentItem());
        ui(new Runnable() {

            public void run() {
                pager.setCurrentItem(0, true);
            }
        });
        assertEquals(0, pager.getCurrentItem());
    }
}
