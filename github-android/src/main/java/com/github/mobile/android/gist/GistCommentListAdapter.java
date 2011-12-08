package com.github.mobile.android.gist;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.util.Avatar;

import org.eclipse.egit.github.core.Comment;

/**
 * List adapter to render Gist comments
 */
public class GistCommentListAdapter extends ArrayAdapter<Comment> {

    private final Activity activity;

    /**
     * Create adapter for files
     *
     * @param activity
     * @param comments
     */
    public GistCommentListAdapter(Activity activity, Comment[] comments) {
        super(activity, layout.gist_view_comment_item, comments);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment comment = getItem(position);
        View commentRoot = activity.getLayoutInflater().inflate(layout.gist_view_comment_item, null);
        final TextView bodyView = (TextView) commentRoot.findViewById(id.tv_gist_comment_body);
        bodyView.setText(comment.getBody());
        final TextView authorView = (TextView) commentRoot.findViewById(id.tv_gist_comment_author);
        authorView.setText(comment.getUser().getLogin());
        final TextView dateView = (TextView) commentRoot.findViewById(id.tv_gist_comment_date);
        dateView.setText(DateUtils.getRelativeTimeSpanString(comment.getUpdatedAt().getTime()));
        final ImageView avatarView = (ImageView) commentRoot.findViewById(id.iv_gravatar);
        Avatar.bind(activity, avatarView, comment.getUser().getAvatarUrl());
        return commentRoot;
    }

}
