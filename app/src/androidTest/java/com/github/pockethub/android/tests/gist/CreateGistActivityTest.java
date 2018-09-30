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

import android.content.Intent;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.rule.ActivityTestRule;
import com.github.pockethub.android.R;
import com.github.pockethub.android.R.id;
import com.github.pockethub.android.ui.gist.CreateGistActivity;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.EXTRA_TEXT;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.not;

/**
 * Tests of {@link CreateGistActivity}
 */
public class CreateGistActivityTest {

    @Rule
    public ActivityTestRule<CreateGistActivity> activityTestRule =
            new ActivityTestRule<>(CreateGistActivity.class, false, false);

    /**
     * Create Gist with initial text
     */
    @Test
    public void testCreateWithInitialText() {
        activityTestRule.launchActivity(new Intent().putExtra(EXTRA_TEXT, "gist content"));

        onView(withId(id.create_gist))
                .check(ViewAssertions.matches(isEnabled()));

        onView(withId(id.et_gist_content))
                .check(ViewAssertions.matches(withText("gist content")));
    }

    /**
     * Create Gist with no initial text
     *
     * @throws Throwable
     */
    @Test
    public void testCreateWithNoInitialText() throws Throwable {
        activityTestRule.launchActivity(new Intent());
        ViewInteraction createMenu = onView(withId(R.id.create_gist));
        ViewInteraction content = onView(withId(R.id.et_gist_content));

        createMenu.check(ViewAssertions.matches(not(isEnabled())));

        content.perform(ViewActions.typeText("gist content"));

        createMenu.check(ViewAssertions.matches(isEnabled()));
    }
}
