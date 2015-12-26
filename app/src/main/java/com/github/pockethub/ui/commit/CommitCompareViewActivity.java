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
package com.github.pockethub.ui.commit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.repo.RepositoryViewActivity;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.InfoUtils;
import com.google.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.Intents.EXTRA_BASE;
import static com.github.pockethub.Intents.EXTRA_HEAD;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;


/**
 * Activity to display a comparison between two commits
 */
public class CommitCompareViewActivity extends DialogFragmentActivity {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @param base
     * @param head
     * @return intent
     */
    public static Intent createIntent(final Repo repository,
        final String base, final String head) {
        Builder builder = new Builder("commits.compare.VIEW");
        builder.add(EXTRA_BASE, base);
        builder.add(EXTRA_HEAD, head);
        builder.repo(repository);
        return builder.toIntent();
    }

    private Repo repository;

    @Inject
    private AvatarLoader avatars;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = getIntent().getParcelableExtra(EXTRA_REPOSITORY);

        setContentView(R.layout.commit_compare);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle(InfoUtils.createRepoId(repository));
        avatars.bind(actionBar, repository.owner);

        fragment = getSupportFragmentManager()
            .findFragmentById(android.R.id.list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        if (fragment != null)
            fragment.onCreateOptionsMenu(optionsMenu, getMenuInflater());

        return super.onCreateOptionsMenu(optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = RepositoryViewActivity.createIntent(repository);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                if (fragment != null)
                    return fragment.onOptionsItemSelected(item);
                else
                    return super.onOptionsItemSelected(item);
        }
    }
}
