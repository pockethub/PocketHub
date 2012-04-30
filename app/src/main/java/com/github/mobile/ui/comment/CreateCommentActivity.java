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
package com.github.mobile.ui.comment;

import static com.github.mobile.Intents.EXTRA_COMMENT_BODY;
import static com.github.mobile.Intents.EXTRA_SUBTITLE;
import static com.github.mobile.Intents.EXTRA_TITLE;
import static com.github.mobile.Intents.EXTRA_USER;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.util.AvatarUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;

/**
 * Activity to create a comment on a {@link Gist} or {@link Issue}
 */
public class CreateCommentActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent to create a comment
     *
     * @return intent
     */
    public static Intent createIntent() {
        return createIntent(null, null, null);
    }

    /**
     * Create intent to create a comment
     *
     * @param title
     * @param subtitle
     * @return intent
     */
    public static Intent createIntent(String title, String subtitle) {
        return createIntent(title, subtitle, null);
    }

    /**
     * Create intent to create a comment
     *
     * @param title
     * @param subtitle
     * @param user
     * @return intent
     */
    public static Intent createIntent(String title, String subtitle, User user) {
        Builder builder = new Builder("comment.create.VIEW");
        if (title != null)
            builder.add(EXTRA_TITLE, title);
        if (subtitle != null)
            builder.add(EXTRA_SUBTITLE, subtitle);
        if (user != null)
            builder.add(EXTRA_USER, user);
        return builder.toIntent();
    }

    @Inject
    private AvatarUtils avatarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.comment_create);

        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        if (title != null)
            actionBar.setTitle(title);
        else
            actionBar.setTitle(string.create_comment_title);
        actionBar.setSubtitle(intent.getStringExtra(EXTRA_SUBTITLE));
        User user = (User) intent.getSerializableExtra(EXTRA_USER);
        if (user != null)
            avatarHelper.bind(actionBar, user);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.comment, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.apply:
            String comment = ((EditText) findViewById(id.et_comment)).getText().toString();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_COMMENT_BODY, comment);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
