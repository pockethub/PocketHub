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
package com.github.mobile.ui.gist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.R.drawable;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.ui.BaseActivity;
import com.github.mobile.ui.TextWatcherAdapter;
import com.github.mobile.util.ShareUtils;

import org.eclipse.egit.github.core.Gist;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class CreateGistActivity extends BaseActivity {

    private EditText descriptionText;

    private EditText nameText;

    private EditText contentText;

    private CheckBox publicCheckBox;

    private MenuItem createItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.gist_create);

        descriptionText = finder.find(id.et_gist_description);
        nameText = finder.find(id.et_gist_name);
        contentText = finder.find(id.et_gist_content);
        publicCheckBox = finder.find(id.cb_public);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.new_gist);
        actionBar.setIcon(drawable.action_gist);

        String text = ShareUtils.getBody(getIntent());
        if (!TextUtils.isEmpty(text))
            contentText.setText(text);

        String subject = ShareUtils.getSubject(getIntent());
        if (!TextUtils.isEmpty(subject))
            descriptionText.setText(subject);

        contentText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                updateCreateMenu(s);
            }
        });
        updateCreateMenu();
    }

    private void updateCreateMenu() {
        if (contentText != null)
            updateCreateMenu(contentText.getText());
    }

    private void updateCreateMenu(CharSequence text) {
        if (createItem != null)
            createItem.setEnabled(!TextUtils.isEmpty(text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.gist_create, options);
        createItem = options.findItem(id.m_apply);
        updateCreateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_apply:
            createGist();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void createGist() {
        final boolean isPublic = publicCheckBox.isChecked();

        String enteredDescription = descriptionText.getText().toString().trim();
        final String description = enteredDescription.length() > 0 ? enteredDescription
                : getString(string.gist_description_hint);

        String enteredName = nameText.getText().toString().trim();
        final String name = enteredName.length() > 0 ? enteredName
                : getString(string.gist_file_name_hint);

        final String content = contentText.getText().toString();

        new CreateGistTask(this, description, isPublic, name, content) {

            @Override
            protected void onSuccess(Gist gist) throws Exception {
                super.onSuccess(gist);

                startActivity(GistsViewActivity.createIntent(gist));
                setResult(RESULT_OK);
                finish();
            }
        }.create();
    }
}
