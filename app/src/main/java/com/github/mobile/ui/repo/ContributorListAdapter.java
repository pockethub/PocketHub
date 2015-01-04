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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.R;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.commit.CommitItemView;
import com.github.mobile.util.AvatarLoader;

import java.util.List;

import org.eclipse.egit.github.core.Contributor;

/**
 * List adapter for a list of contributors
 */
public class ContributorListAdapter extends ItemListAdapter<Contributor, ContributorItemView> {

    private final Context context;

    private final AvatarLoader avatars;

    /**
     * Create contributor list adapter
     *
     * @param context
     * @param elements
     * @param avatars
     */
    public ContributorListAdapter(final Context context,
            final List<Contributor> elements, final AvatarLoader avatars) {
        super(R.layout.contributor_item, LayoutInflater.from(context), elements);

        this.context = context.getApplicationContext();
        this.avatars = avatars;
        setItems(elements);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }

    @Override
    protected void update(final int position, final ContributorItemView view,
        final Contributor contributor) {
        avatars.bind(view.avatarView, contributor);
        view.loginView.setText(contributor.getLogin());
        view.contributionView.setText(context.getString(R.string.contributions, contributor.getContributions()));
    }

    @Override
    protected ContributorItemView createView(final View view) {
        return new ContributorItemView(view);
    }
}
