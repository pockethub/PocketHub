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
package com.github.mobile.ui.comment;

import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.github.mobile.ui.FragmentPagerAdapter;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

/**
 * Pager of a raw and rendered comment text
 */
public class CommentPreviewPagerAdapter extends FragmentPagerAdapter {

    private final IRepositoryIdProvider repo;

    private RawCommentFragment textFragment;

    private RenderedCommentFragment htmlFragment;

    /**
     * @param activity
     * @param repo
     */
    public CommentPreviewPagerAdapter(SherlockFragmentActivity activity,
            IRepositoryIdProvider repo) {
        super(activity);

        this.repo = repo;
    }

    @Override
    public Fragment getItem(final int position) {
        switch (position) {
        case 0:
            textFragment = new RawCommentFragment();
            return textFragment;
        case 1:
            htmlFragment = new RenderedCommentFragment();
            return htmlFragment;
        default:
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    /**
     * Get comment text
     *
     * @return text
     */
    public String getCommentText() {
        return textFragment != null ? textFragment.getText() : null;
    }

    /**
     * Set current item
     *
     * @param position
     * @return this adapter
     */
    public CommentPreviewPagerAdapter setCurrentItem(int position) {
        if (position == 1 && htmlFragment != null)
            htmlFragment.setText(getCommentText(), repo);
        return this;
    }
}
