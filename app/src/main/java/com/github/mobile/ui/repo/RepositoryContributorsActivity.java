/*
 * Copyright 2013 GitHub Inc.
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
package com.github.mobile.ui.repo;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents;
import com.github.mobile.R.string;
import com.github.mobile.R.layout;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Activity to view repository contributors
 */
public class RepositoryContributorsActivity extends DialogFragmentActivity {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return new Intents.Builder("repo.contributors.VIEW").repo(repository).toIntent();
    }

    private Repository repository;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.repo_contributors);

        repository = getSerializableExtra(EXTRA_REPOSITORY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repository.getName());
        actionBar.setSubtitle(string.contributors);
        actionBar.setDisplayHomeAsUpEnabled(true);

        User owner = repository.getOwner();
        avatars.bind(getSupportActionBar(), owner);
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
                return super.onOptionsItemSelected(item);
        }
    }
}
