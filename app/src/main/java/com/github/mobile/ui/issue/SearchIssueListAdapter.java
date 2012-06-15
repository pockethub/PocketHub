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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TypefaceUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Adapter for a list of searched for issues
 */
public class SearchIssueListAdapter extends
        ItemListAdapter<SearchIssue, RepositoryIssueItemView> {

    private final AvatarLoader avatars;

    private final TextView numberView;

    private int numberWidth;

    /**
     * @param inflater
     * @param elements
     * @param avatars
     */
    public SearchIssueListAdapter(LayoutInflater inflater,
            SearchIssue[] elements, AvatarLoader avatars) {
        super(layout.repo_issue_item, inflater, elements);

        this.avatars = avatars;
        this.numberView = (TextView) inflater.inflate(layout.repo_issue_item,
                null).findViewById(id.tv_issue_number);

        if (elements != null)
            computeNumberWidth(elements);
    }

    /**
     * @param inflater
     * @param avatars
     */
    public SearchIssueListAdapter(LayoutInflater inflater, AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    private void computeNumberWidth(final Object[] items) {
        int[] numbers = new int[items.length];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = ((SearchIssue) items[i]).getNumber();
        int digits = Math.max(TypefaceUtils.getMaxDigits(numbers), 4);
        numberWidth = TypefaceUtils.getWidth(numberView, digits)
                + numberView.getPaddingLeft() + numberView.getPaddingRight();
    }

    @Override
    public ItemListAdapter<SearchIssue, RepositoryIssueItemView> setItems(
            final Object[] items) {
        computeNumberWidth(items);

        return super.setItems(items);
    }

    @Override
    protected void update(final int position,
            final RepositoryIssueItemView view, final SearchIssue issue) {
        view.number.setText(Integer.toString(issue.getNumber()));
        if (IssueService.STATE_CLOSED.equals(issue.getState()))
            view.number.setPaintFlags(view.numberPaintFlags
                    | STRIKE_THRU_TEXT_FLAG);
        else
            view.number.setPaintFlags(view.numberPaintFlags);
        view.number.getLayoutParams().width = numberWidth;

        String gravatarId = issue.getGravatarId();
        if (!TextUtils.isEmpty(gravatarId)) {
            User user = new User();
            user.setGravatarId(gravatarId);
            avatars.bind(view.avatar, user);
        } else
            avatars.bind(view.avatar, null);

        view.pullRequestIcon.setVisibility(GONE);

        view.title.setText(issue.getTitle());

        StyledText reporterText = new StyledText();
        reporterText.bold(issue.getUser());
        reporterText.append(' ');
        reporterText.append(issue.getCreatedAt());
        view.reporter.setText(reporterText);

        view.comments.setText(Integer.toString(issue.getComments()));

        for (View label : view.labels)
            label.setVisibility(GONE);
    }

    @Override
    protected RepositoryIssueItemView createView(View view) {
        return new RepositoryIssueItemView(view);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getNumber();
    }
}
