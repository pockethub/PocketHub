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
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mobile.R;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        configureTabPager();
        slidingTabsLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                adapter.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.m_apply:
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
                return getString(R.string.write);
            case 1:
                return getString(R.string.preview);
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
        getMenuInflater().inflate(R.menu.comment, options);
        applyItem = options.findItem(R.id.m_apply);
        return true;
    }
}
