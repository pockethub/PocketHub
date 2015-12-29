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
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.ui.FragmentProvider;
import com.github.pockethub.ui.PagerActivity;
import com.github.pockethub.ui.ViewPager;
import com.github.pockethub.ui.repo.RepositoryViewActivity;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.InfoUtils;
import com.google.inject.Inject;

import java.util.Collection;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.Intents.EXTRA_BASES;
import static com.github.pockethub.Intents.EXTRA_POSITION;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;

/**
 * Activity to display a commit
 */
public class CommitViewActivity extends PagerActivity {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @param id
     * @return intent
     */
    public static Intent createIntent(final Repo repository,
        final String id) {
        return createIntent(repository, 0, id);
    }

    /**
     * Create intent for this activity
     *
     * @param repository
     * @param position
     * @param commits
     * @return intent
     */
    public static Intent createIntent(final Repo repository,
        final int position, final Collection<Commit> commits) {
        String[] ids = new String[commits.size()];
        int index = 0;
        for (Commit commit : commits)
            ids[index++] = commit.sha;
        return createIntent(repository, position, ids);
    }

    /**
     * Create intent for this activity
     *
     * @param repository
     * @param position
     * @param ids
     * @return intent
     */
    public static Intent createIntent(final Repo repository,
        final int position, final String... ids) {
        Builder builder = new Builder("commits.VIEW");
        builder.add(EXTRA_POSITION, position);
        builder.add(EXTRA_BASES, ids);
        builder.repo(repository);
        return builder.toIntent();
    }

    private ViewPager pager;

    private Repo repository;

    private CharSequence[] ids;

    private int initialPosition;

    @Inject
    private AvatarLoader avatars;

    private CommitPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pager);

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        pager = finder.find(R.id.vp_pages);

        repository = getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        ids = getCharSequenceArrayExtra(EXTRA_BASES);
        initialPosition = getIntExtra(EXTRA_POSITION);

        adapter = new CommitPagerAdapter(this, repository, ids);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        pager.scheduleSetItem(initialPosition, this);
        onPageSelected(initialPosition);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setSubtitle(InfoUtils.createRepoId(repository));
        avatars.bind(actionBar, repository.owner);
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

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        final String id = CommitUtils.abbreviate(ids[position].toString());
        getSupportActionBar().setTitle(getString(R.string.commit_prefix) + id);
    }

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }
}
