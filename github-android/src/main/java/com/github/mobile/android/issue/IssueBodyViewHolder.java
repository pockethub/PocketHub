package com.github.mobile.android.issue;

import android.content.Context;
import android.view.View;

import com.github.mobile.android.MarkdownViewHolder;
import com.github.mobile.android.R.id;
import com.github.mobile.android.util.HttpImageGetter;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;

import org.eclipse.egit.github.core.Issue;

/**
 * View holder for an issue's body
 */
public class IssueBodyViewHolder extends MarkdownViewHolder<Issue> {

    /**
     * Create factory
     *
     * @param context
     * @param imageGetter
     * @return view holder factory
     */
    public static ViewHolderFactory<Issue> createFactory(final Context context, final HttpImageGetter imageGetter) {
        return new ViewHolderFactory<Issue>() {

            public ViewHolder<Issue> createViewHolderFor(View view) {
                return new IssueBodyViewHolder(context, imageGetter, view);
            }
        };
    }

    /**
     * @param context
     * @param imageGetter
     * @param view
     */
    public IssueBodyViewHolder(Context context, HttpImageGetter imageGetter, View view) {
        super(context, imageGetter, view);
    }

    public void updateViewFor(Issue issue) {
        bindHtml(id.tv_issue_body, issue.getBodyHtml());
    }
}
