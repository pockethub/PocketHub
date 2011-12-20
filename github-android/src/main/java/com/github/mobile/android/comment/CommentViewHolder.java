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
import com.madgag.android.listviews.ViewHolderFactory;

import org.eclipse.egit.github.core.Comment;

/**
 * Comment view holder
 */
public class CommentViewHolder implements ViewHolder<Comment> {

    /**
     * Factory for creating view holder
     *
     * @param context
     * @param imageGetter
     * @return view holder
     */
    public static final ViewHolderFactory<Comment> createFactory(final Context context,
            final HttpImageGetter imageGetter) {
        return new ViewHolderFactory<Comment>() {

            public ViewHolder<Comment> createViewHolderFor(View view) {
                return new CommentViewHolder(context, imageGetter, view);
            }
        };
    }

    private final Context context;

    private final HttpImageGetter imageGetter;

    private final View view;

    /**
     * Create a comment holder
     *
     * @param context
     * @param imageGetter
     * @param view
     */
    public CommentViewHolder(Context context, HttpImageGetter imageGetter, View view) {
        this.context = context;
        this.imageGetter = imageGetter;
        this.view = view;
    }

    public void updateViewFor(Comment comment) {
        final TextView bodyView = (TextView) view.findViewById(id.tv_gist_comment_body);
        bodyView.setMovementMethod(LinkMovementMethod.getInstance());
        imageGetter.bind(bodyView, comment.getBodyHtml());
        final TextView authorView = (TextView) view.findViewById(id.tv_gist_comment_author);
        authorView.setText(comment.getUser().getLogin());
        final TextView dateView = (TextView) view.findViewById(id.tv_gist_comment_date);
        dateView.setText(Time.relativeTimeFor(comment.getUpdatedAt()));
        final ImageView avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        Avatar.bind(context, avatarView, comment.getUser());
    }
}
