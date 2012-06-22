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
package com.github.mobile.tests.tests.issue;

import static android.view.KeyEvent.KEYCODE_DEL;
import android.view.View;
import android.widget.EditText;

import com.github.mobile.tests.ActivityTest;
import com.github.mobile.ui.issue.EditIssueActivity;
import com.viewpagerindicator.R.id;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Tests of {@link EditIssueActivity}
 */
public class EditIssueActivityTest extends ActivityTest<EditIssueActivity> {

    /**
     * Create test
     */
    public EditIssueActivityTest() {
        super(EditIssueActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Repository repo = new Repository();
        repo.setName("repo");
        repo.setOwner(new User().setLogin("owner"));
        setActivityIntent(EditIssueActivity.createIntent(repo));
    }

    /**
     * Verify save menu is properly enabled/disable depending on the issue have
     * a non-empty title
     *
     * @throws Throwable
     */
    public void testSaveMenuEnabled() throws Throwable {
        View saveMenu = view(id.m_apply);
        assertFalse(saveMenu.isEnabled());
        EditText title = editText(id.et_issue_title);
        focus(title);
        send("a");
        assertTrue(saveMenu.isEnabled());
        sendKeys(KEYCODE_DEL);
        assertFalse(saveMenu.isEnabled());
    }
}
