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
package com.github.pockethub.android.tests.gist;

import android.content.Context;
import androidx.collection.ArrayMap;
import androidx.test.rule.ActivityTestRule;
import androidx.viewpager.widget.ViewPager;
import com.github.pockethub.android.PocketHub;
import com.github.pockethub.android.R.id;
import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.ui.gist.GistFilesViewActivity;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GistFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

/**
 * Tests of {@link GistFilesViewActivity}
 */
public class GistFilesViewActivityTest {

    protected GistStore store;

    private Gist gist;

    @Rule
    public ActivityTestRule<GistFilesViewActivity> activityTestRule =
            new ActivityTestRule<>(GistFilesViewActivity.class, false, false);

    @Before
    public void setUp() {
        Context context = getInstrumentation().getTargetContext();
        PocketHub pocketHub = (PocketHub) context.getApplicationContext();
        store = pocketHub.applicationComponent().gistStore();

        Map<String, GistFile> files = new ArrayMap<>();

        GistFile a = GistFile.builder()
                .content("aa")
                .filename("a")
                .build();
        GistFile b = GistFile.builder()
                .content("bb")
                .filename("b")
                .build();

        files.put("a", a);
        files.put("b", b);

        gist = Gist.builder()
                .id("abcd")
                .files(files)
                .build();

        store.addGist(gist);
        activityTestRule.launchActivity(GistFilesViewActivity.createIntent(gist, 0));
    }

    /**
     * Verify changing pages between gist files
     *
     * @throws Throwable
     */
    @Test
    public void testChangingPages() throws Throwable {
        final ViewPager pager = activityTestRule.getActivity().findViewById(id.vp_pages);

        assertEquals(0, pager.getCurrentItem());
        activityTestRule.runOnUiThread(() -> pager.setCurrentItem(1, true));
        assertEquals(1, pager.getCurrentItem());
        activityTestRule.runOnUiThread(() -> pager.setCurrentItem(0, true));
        assertEquals(0, pager.getCurrentItem());
    }
}
