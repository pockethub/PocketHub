package com.github.mobile.android.comment;

import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.HtmlViewer;
import com.github.mobile.android.util.Time;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Comment;

/**
 * Comment view holder
 */
public class CommentViewHolder implements ViewHolder<Comment> {

    private final AvatarHelper avatarHelper;

    private final TextView authorView;

    private final TextView dateView;

    private final HtmlViewer bodyViewer;

    private final ImageView avatarView;

    /**
     * Create a comment holder
     *
     * @param view
     * @param avatarHelper
     */
    public CommentViewHolder(View view, AvatarHelper avatarHelper) {
        this.avatarHelper = avatarHelper;
        bodyViewer = new HtmlViewer((WebView) view.findViewById(id.wv_gist_comment_body));
        authorView = (TextView) view.findViewById(id.tv_gist_comment_author);
        dateView = (TextView) view.findViewById(id.tv_gist_comment_date);
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
    }

    public void updateViewFor(final Comment comment) {
        bodyViewer.setHtml(comment.getBodyHtml());
        authorView.setText(comment.getUser().getLogin());
        dateView.setText(Time.relativeTimeFor(comment.getUpdatedAt()));
        avatarView.setBackgroundDrawable(null);
        avatarHelper.bind(avatarView, comment.getUser());
    }
}
