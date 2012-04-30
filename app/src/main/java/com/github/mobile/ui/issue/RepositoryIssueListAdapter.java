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
package com.github.mobile.ui.issue;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.TimeUtils;
import com.github.mobile.util.TypefaceUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Issue;

/**
 * Adapter for a list of {@link Issue} objects
 */
public class RepositoryIssueListAdapter extends ItemListAdapter<Issue, RepositoryIssueItemView> {

    private final AvatarUtils avatars;

    private final TextView numberView;

    private int numberWidth;

    /**
     * @param inflater
     * @param elements
     * @param avatars
     */
    public RepositoryIssueListAdapter(LayoutInflater inflater, Issue[] elements, AvatarUtils avatars) {
        super(layout.repo_issue_list_item, inflater, elements);

        this.avatars = avatars;
        this.numberView = (TextView) inflater.inflate(layout.repo_issue_list_item, null).findViewById(
                id.tv_issue_number);

        if (elements != null)
            computeNumberWidth(elements);
    }

    /**
     * @param inflater
     * @param avatars
     */
    public RepositoryIssueListAdapter(LayoutInflater inflater, AvatarUtils avatars) {
        this(inflater, null, avatars);
    }

    private void computeNumberWidth(final Object[] items) {
        int[] numbers = new int[items.length];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = ((Issue) items[i]).getNumber();
        int digits = TypefaceUtils.getMaxDigits(numbers);
        numberWidth = TypefaceUtils.getWidth(numberView, digits) + numberView.getPaddingLeft()
                + numberView.getPaddingRight();
    }

    @Override
    public ItemListAdapter<Issue, RepositoryIssueItemView> setItems(final Object[] items) {
        computeNumberWidth(items);

        return super.setItems(items);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected void update(RepositoryIssueItemView view, Issue issue) {
        view.number.setText(Integer.toString(issue.getNumber()));
        if (issue.getClosedAt() != null)
            view.number.setPaintFlags(view.numberPaintFlags | STRIKE_THRU_TEXT_FLAG);
        else
            view.number.setPaintFlags(view.numberPaintFlags);
        view.number.getLayoutParams().width = numberWidth;

        avatars.bind(view.avatar, issue.getUser());

        view.pullRequestIcon.setVisibility(issue.getPullRequest().getHtmlUrl() == null ? GONE : VISIBLE);

        view.title.setText(issue.getTitle());
        view.user.setText(issue.getUser().getLogin());
        view.creation.setText(TimeUtils.getRelativeTime(issue.getCreatedAt()));
        view.comments.setText(Integer.toString(issue.getComments()));
    }

    @Override
    protected RepositoryIssueItemView createView(View view) {
        return new RepositoryIssueItemView(view);
    }
}
