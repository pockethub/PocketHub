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
import com.google.android.material.appbar.AppBarLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.pockethub.android.R;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GistFile;
import com.meisolsson.githubsdk.model.request.gist.CreateGist;
import com.meisolsson.githubsdk.service.gists.GistService;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class CreateGistActivity extends BaseActivity {

    private static final String TAG = "CreateGistActivity";

    @BindView(R.id.et_gist_description)
    protected EditText descriptionText;

    @BindView(R.id.et_gist_name)
    protected EditText nameText;

    @BindView(R.id.et_gist_content)
    protected EditText contentText;

    @BindView(R.id.cb_public)
    protected CheckBox publicCheckBox;

    @BindView(R.id.appbar)
    protected AppBarLayout appBarLayout;

    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gist_create);

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

    // Fully expand the AppBar if something in it gets focus
    @OnFocusChange({R.id.et_gist_description, R.id.et_gist_name, R.id.cb_public})
    protected void expandAppBarOnFocusChangeListener(View v, boolean hasFocus) {
        if (hasFocus) {
            appBarLayout.setExpanded(true);
        }
    }

    @OnCheckedChanged(R.id.cb_public)
    @OnTextChanged(value = {R.id.et_gist_description, R.id.et_gist_name, R.id.cb_public})
    protected void expandAppBarOnChange() {
        appBarLayout.setExpanded(true);
    }

    @OnTextChanged(R.id.et_gist_content)
    protected void onContentTextChange() {
        updateCreateMenu();
    }

    private void updateCreateMenu() {
        updateCreateMenu(contentText.getText());
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
                .compose(RxProgress.bindToLifecycle(this, R.string.creating_gist))
                .as(AutoDisposeUtils.bindToLifecycle(this))
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
