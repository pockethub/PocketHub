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

import static com.github.mobile.Intents.EXTRA_COMMENT;
import static com.github.mobile.util.TypefaceUtils.ICON_EDIT;
import static com.github.mobile.util.TypefaceUtils.ICON_WATCH;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.string;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;
import com.viewpagerindicator.R.menu;

import org.eclipse.egit.github.core.Comment;

/**
 * Base activity for creating comments
 */
public abstract class CreateCommentActivity extends
        TabPagerActivity<CommentPreviewPagerAdapter> {

    private MenuItem applyItem;

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureTabPager();
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();

        if (applyItem != null)
            applyItem.setEnabled(adapter != null
                    && !TextUtils.isEmpty(adapter.getCommentText()));

    }

    @Override
    protected void setCurrentItem(int position) {
        super.setCurrentItem(position);

        adapter.setCurrentItem(position);
    }

    /**
     * Create comment
     *
     * @param comment
     */
    protected abstract void createComment(String comment);

    /**
     * Finish this activity passing back the created comment
     *
     * @param comment
     */
    protected void finish(Comment comment) {
        Intent data = new Intent();
        data.putExtra(EXTRA_COMMENT, comment);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_apply:
            createComment(adapter.getCommentText());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getTitle(final int position) {
        switch (position) {
        case 0:
            return getString(string.write);
        case 1:
            return getString(string.preview);
        default:
            return super.getTitle(position);
        }
    }

    @Override
    protected String getIcon(final int position) {
        switch (position) {
        case 0:
            return ICON_EDIT;
        case 1:
            return ICON_WATCH;
        default:
            return super.getIcon(position);
        }
    }

    @Override
    protected CommentPreviewPagerAdapter createAdapter() {
        return new CommentPreviewPagerAdapter(this, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.comment, options);
        applyItem = options.findItem(id.m_apply);
        return true;
    }
}
