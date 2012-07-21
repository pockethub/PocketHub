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
package com.github.mobile.ui.commit;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_HEAD;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.repo.RepositoryViewActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;

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
    public static Intent createIntent(final Repository repository,
            final String base, final String head) {
        Builder builder = new Builder("commits.compare.VIEW");
        builder.add(EXTRA_BASE, base);
        builder.add(EXTRA_HEAD, head);
        builder.repo(repository);
        return builder.toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.commit_compare);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle(repository.generateId());
        avatars.bind(actionBar, repository.getOwner());
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
