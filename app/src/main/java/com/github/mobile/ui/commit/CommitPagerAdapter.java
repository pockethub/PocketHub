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

import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.eclipse.egit.github.core.Repository;

/**
 * Pager over commits
 */
public class CommitPagerAdapter extends FragmentStatePagerAdapter {

    private final Repository repository;

    private final CharSequence[] ids;

    /**
     * @param fm
     * @param repository
     * @param ids
     */
    public CommitPagerAdapter(FragmentManager fm, Repository repository,
            CharSequence[] ids) {
        super(fm);

        this.repository = repository;
        this.ids = ids;
    }

    @Override
    public Fragment getItem(final int item) {
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_BASE, ids[item].toString());
        arguments.putSerializable(EXTRA_REPOSITORY, repository);
        CommitDiffListFragment fragment = new CommitDiffListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public int getCount() {
        return ids.length;
    }
}
