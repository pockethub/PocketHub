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
package com.github.pockethub.ui.gist;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.github.pockethub.R;
import com.github.pockethub.ui.BaseActivity;
import com.github.pockethub.ui.TextWatcherAdapter;
import com.github.pockethub.util.ShareUtils;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class CreateGistActivity extends BaseActivity {

    private EditText descriptionText;

    private EditText nameText;

    private EditText contentText;

    private CheckBox publicCheckBox;

    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gist_create);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        descriptionText = finder.find(R.id.et_gist_description);
        nameText = finder.find(R.id.et_gist_name);
        contentText = finder.find(R.id.et_gist_content);
        publicCheckBox = finder.find(R.id.cb_public);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_github_gist_white_32dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        String text = ShareUtils.getBody(getIntent());
        if (!TextUtils.isEmpty(text))
            contentText.setText(text);

        String subject = ShareUtils.getSubject(getIntent());
        if (!TextUtils.isEmpty(subject))
            descriptionText.setText(subject);

        contentText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCreateMenu(s);
            }
        });
        updateCreateMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_create_gist, menu);
        menuItem = menu.findItem(R.id.create_gist);
        updateCreateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.create_gist:
                createGist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateCreateMenu() {
        if (contentText != null)
            updateCreateMenu(contentText.getText());
    }

    private void updateCreateMenu(CharSequence text) {
        if (menuItem != null)
            menuItem.setEnabled(!TextUtils.isEmpty(text));
    }

    private void createGist() {
        final boolean isPublic = publicCheckBox.isChecked();

        String enteredDescription = descriptionText.getText().toString().trim();
        final String description = enteredDescription.length() > 0 ? enteredDescription
            : getString(R.string.gist_description_hint);

        String enteredName = nameText.getText().toString().trim();
        final String name = enteredName.length() > 0 ? enteredName
            : getString(R.string.gist_file_name_hint);

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
