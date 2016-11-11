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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.MainActivity;
import com.github.pockethub.android.ui.TextWatcherAdapter;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GistFile;
import com.meisolsson.githubsdk.model.request.gist.CreateGist;
import com.meisolsson.githubsdk.service.gists.GistService;

import java.util.HashMap;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class CreateGistActivity extends BaseActivity {

    private static final String TAG = "CreateGistActivity";

    private EditText descriptionText;

    private EditText nameText;

    private EditText contentText;

    private CheckBox publicCheckBox;

    private MenuItem createItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gist_create);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        descriptionText = finder.find(R.id.et_gist_description);
        nameText = finder.find(R.id.et_gist_name);
        contentText = finder.find(R.id.et_gist_content);
        publicCheckBox = finder.find(R.id.cb_public);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.new_gist);
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
        getMenuInflater().inflate(R.menu.activity_gist_create, options);
        createItem = options.findItem(R.id.m_apply);
        updateCreateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_apply:
                createGist();
                return true;
            case android.R.id.home:
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                .compose(this.<Gist>bindToLifecycle())
                .subscribe(new ProgressObserverAdapter<Gist>(this, R.string.creating_gist) {

                    @Override
                    public void onNext(Gist gist) {
                        super.onNext(gist);
                        startActivity(GistsViewActivity.createIntent(gist));
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.d(TAG, "Exception creating Gist", e);
                        ToastUtils.show(CreateGistActivity.this, e.getMessage());
                    }
                }.start());
    }
}
