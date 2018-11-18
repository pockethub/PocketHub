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
package com.github.pockethub.android.tests.commit;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.rule.ActivityTestRule;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.commit.CreateCommentActivity;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

/**
 * Tests of {@link CreateCommentActivity}
 */
public class CreateCommentActivityTest {

    @Rule
    public ActivityTestRule<CreateCommentActivity> activityTestRule =
            new ActivityTestRule<>(CreateCommentActivity.class, false, false);

    @Before
    public void setUp() {
        User user = User.builder()
                .login("owner")
                .build();

        Repository repo = Repository.builder()
                .name("name")
                .owner(user)
                .build();

        activityTestRule.launchActivity(CreateCommentActivity.createIntent(repo, "abcdef"));
    }

    /**
     * Verify empty comment can't be created
     */
    @Test
    public void testEmptyCommentIsProhibited() {
        ViewInteraction createMenu = onView(withId(R.id.m_apply));
        ViewInteraction comment = onView(withId(R.id.et_comment));

        createMenu.check(ViewAssertions.matches(not(isEnabled())));

        closeSoftKeyboard();
        comment.perform(ViewActions.typeText("a"));

        createMenu.check(ViewAssertions.matches(isEnabled()));
        comment.perform(ViewActions.replaceText(""));

        createMenu.check(ViewAssertions.matches(not(isEnabled())));
    }
}
