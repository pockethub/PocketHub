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
package com.github.pockethub.android.ui.gist;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.TextWatcherAdapter;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GistFile;
import com.meisolsson.githubsdk.model.request.gist.CreateGist;
import com.meisolsson.githubsdk.service.gists.GistService;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class CreateGistActivity extends BaseActivity {

    private static final String TAG = "CreateGistActivity";

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

        descriptionText = (EditText) findViewById(R.id.et_gist_description);
        nameText = (EditText) findViewById(R.id.et_gist_name);
        contentText = (EditText) findViewById(R.id.et_gist_content);
        publicCheckBox = (CheckBox) findViewById(R.id.cb_public);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        // Fully expand the AppBar if something in it gets focus
        View.OnFocusChangeListener expandAppBarOnFocusChangeListener = (v, hasFocus) -> {
            if (hasFocus) {
                appBarLayout.setExpanded(true);
            }
        };
        nameText.setOnFocusChangeListener(expandAppBarOnFocusChangeListener);
        descriptionText.setOnFocusChangeListener(expandAppBarOnFocusChangeListener);
        publicCheckBox.setOnFocusChangeListener(expandAppBarOnFocusChangeListener);

        // Fully expand the AppBar if something in it changes its value
        TextWatcher expandAppBarTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                appBarLayout.setExpanded(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                appBarLayout.setExpanded(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                appBarLayout.setExpanded(true);
            }
        };
        nameText.addTextChangedListener(expandAppBarTextWatcher);
        descriptionText.addTextChangedListener(expandAppBarTextWatcher);
        publicCheckBox.addTextChangedListener(expandAppBarTextWatcher);
        publicCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                appBarLayout.setExpanded(true));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        String text = ShareUtils.getBody(getIntent());
        if (!TextUtils.isEmpty(text)) {
            contentText.setText(text);
        }

        String subject = ShareUtils.getSubject(getIntent());
        if (!TextUtils.isEmpty(subject)) {
            descriptionText.setText(subject);
        }

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
        switch (item.getItemId()) {
            case R.id.create_gist:
                createGist();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateCreateMenu() {
        if (contentText != null) {
            updateCreateMenu(contentText.getText());
        }
    }

    private void updateCreateMenu(CharSequence text) {
        if (menuItem != null) {
            menuItem.setEnabled(!TextUtils.isEmpty(text));
        }
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
        Map<String, GistFile> map = new HashMap<>();
        map.put(name, GistFile.builder().filename(name).content(content).build());

        CreateGist createGist = CreateGist.builder()
                .files(map)
                .description(description)
                .isPublic(isPublic)
                .build();

        ServiceGenerator.createService(this, GistService.class)
                .createGist(createGist)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .compose(RxProgress.bindToLifecycle(this, R.string.creating_gist))
                .subscribe(response -> {
                    startActivity(GistsViewActivity.createIntent(response.body()));
                    setResult(RESULT_OK);
                    finish();
                }, e -> {
                    Log.d(TAG, "Exception creating Gist", e);
                    ToastUtils.show(this, e.getMessage());
                });
    }
}
