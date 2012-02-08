package com.github.mobile.android.comment;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.Time;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Comment;

/**
 * Comment view holder
 */
public class CommentViewHolder implements ViewHolder<Comment> {

    private final AvatarHelper avatarHelper;

    private final HttpImageGetter imageGetter;

    private final TextView authorView, dateView, bodyView;
    private final ImageView avatarView;

    /**
     * Create a comment holder
     *
     * @param view
     * @param avatarHelper
     * @param imageGetter
     */
    public CommentViewHolder(View view, AvatarHelper avatarHelper, HttpImageGetter imageGetter) {
        this.avatarHelper = avatarHelper;
        this.imageGetter = imageGetter;
        bodyView = (TextView) view.findViewById(id.tv_gist_comment_body);
        bodyView.setMovementMethod(LinkMovementMethod.getInstance());
        authorView = (TextView) view.findViewById(id.tv_gist_comment_author);
        dateView = (TextView) view.findViewById(id.tv_gist_comment_date);
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
    }

    public void updateViewFor(Comment comment) {
        imageGetter.bind(bodyView, comment.getBodyHtml());
        authorView.setText(comment.getUser().getLogin());
        dateView.setText(Time.relativeTimeFor(comment.getUpdatedAt()));
        avatarView.setBackgroundDrawable(null);
        avatarHelper.bind(avatarView, comment.getUser());
    }
}
