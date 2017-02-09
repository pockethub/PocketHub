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

import android.view.View;
import android.widget.EditText;

import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R.id;
import com.github.pockethub.android.tests.ActivityTest;
import com.github.pockethub.android.ui.gist.CreateCommentActivity;

import static android.view.KeyEvent.KEYCODE_DEL;

/**
 * Tests of {@link CreateCommentActivity}
 */
public class CreateCommentActivityTest extends
    ActivityTest<CreateCommentActivity> {

    /**
     * Create navigation_drawer_header_background
     */
    public CreateCommentActivityTest() {
        super(CreateCommentActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        User user = User.builder()
                .login("abc")
                .build();

        Gist gist = Gist.builder()
                .user(user)
                .id("123")
                .build();

        setActivityIntent(CreateCommentActivity.createIntent(gist));
    }

    /**
     * Verify empty comment can't be created
     *
     * @throws Throwable
     */
    public void testEmptyCommentIsProhibited() throws Throwable {
        View createMenu = view(id.m_apply);
        assertFalse(createMenu.isEnabled());
        final EditText comment = editText(id.et_comment);
        focus(comment);
        send("a");
        assertTrue(createMenu.isEnabled());
        sendKeys(KEYCODE_DEL);
        assertFalse(createMenu.isEnabled());
    }
}
