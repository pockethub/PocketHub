package com.github.mobile.comment;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.TimeUtils;
import com.github.mobile.util.TypefaceUtils;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Comment;

/**
 * Comment view holder
 */
public class CommentViewHolder implements ViewHolder<Comment> {

    private final AvatarUtils avatarHelper;

    private final TextView authorView;

    private final TextView dateView;

    private final TextView bodyView;

    private final ImageView avatarView;

    private HttpImageGetter imageGetter;

    /**
     * Create a comment holder
     *
     * @param view
     * @param avatarHelper
     */
    public CommentViewHolder(View view, AvatarUtils avatarHelper) {
        this.avatarHelper = avatarHelper;
        bodyView = (TextView) view.findViewById(id.tv_comment_body);
        bodyView.setMovementMethod(LinkMovementMethod.getInstance());
        authorView = (TextView) view.findViewById(id.tv_comment_author);
        dateView = (TextView) view.findViewById(id.tv_comment_date);
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        imageGetter = new HttpImageGetter(view.getContext());
        ((TextView) view.findViewById(id.tv_comment_icon)).setTypeface(TypefaceUtils.getOctocons(view.getContext()));
    }

    @Override
    public void updateViewFor(final Comment comment) {
        imageGetter.bind(bodyView, comment.getBodyHtml(), comment.getId());
        authorView.setText(comment.getUser().getLogin());
        dateView.setText(TimeUtils.getRelativeTime(comment.getUpdatedAt()));
        avatarHelper.bind(avatarView, comment.getUser());
    }
}
