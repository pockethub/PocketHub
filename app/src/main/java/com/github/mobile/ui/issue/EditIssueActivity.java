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
package com.github.mobile.ui.issue;

import static com.github.mobile.Intents.EXTRA_ISSUE;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import org.eclipse.egit.github.core.Issue;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to edit the title and description for an issue
 */
public class EditIssueActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent to edit an issue
     *
     * @param issue
     * @return intent
     */
    public static Intent createIntent(final Issue issue) {
        return new Builder("repo.issues.edit.VIEW").issue(issue).toIntent();
    }

    @InjectView(id.et_issue_title)
    private EditText titleText;

    @InjectView(id.et_issue_body)
    private EditText bodyText;

    @InjectExtra(EXTRA_ISSUE)
    private Issue issue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.issue_edit);
        setTitle(getString(string.issue_title) + issue.getNumber());

        titleText.setText(issue.getTitle());
        bodyText.setText(issue.getBody());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_edit, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.issue_edit:
            Intent intent = new Intent();
            Issue edited = new Issue();
            edited.setTitle(titleText.getText().toString());
            edited.setBody(bodyText.getText().toString());
            intent.putExtra(EXTRA_ISSUE, edited);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
