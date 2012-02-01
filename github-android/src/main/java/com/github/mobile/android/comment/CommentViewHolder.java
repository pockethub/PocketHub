package com.github.mobile.android.comment;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.Time;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Comment;

/**
 * Comment view holder
 */
public class CommentViewHolder implements ViewHolder<Comment> {

    private final Context context;

    private final HttpImageGetter imageGetter;

    private final TextView authorView, dateView, bodyView;
    private final ImageView avatarView;

    /**
     * Create a comment holder
     *
     * @param view
     * @param context
     * @param imageGetter
     */
    public CommentViewHolder(View view, Context context, HttpImageGetter imageGetter) {
        this.context = context;
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
        Avatar.bind(context, avatarView, comment.getUser());
    }
}
