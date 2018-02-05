package com.github.pockethub.android.ui.item;

import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.comment.DeleteCommentListener;
import com.github.pockethub.android.ui.comment.EditCommentListener;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.GitHubComment;

import butterknife.BindView;

public class GitHubCommentItem extends BaseDataItem<GitHubComment, GitHubCommentItem.ViewHolder> {

    private final HttpImageGetter imageGetter;
    private final EditCommentListener editCommentListener;
    private final DeleteCommentListener deleteCommentListener;
    private final String username;
    private final boolean canWrite;

    public GitHubCommentItem(AvatarLoader avatarLoader, HttpImageGetter imageGetter,
                             EditCommentListener editCommentListener,
                             DeleteCommentListener deleteCommentListener, String username,
                             boolean canWrite, GitHubComment dataItem) {
        super(avatarLoader, dataItem, dataItem.id());
        this.imageGetter = imageGetter;
        this.editCommentListener = editCommentListener;
        this.deleteCommentListener = deleteCommentListener;
        this.username = username;
        this.canWrite = canWrite;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        imageGetter.bind(holder.body, getData().body(), getData().id());
        getAvatarLoader().bind(holder.avatar, getData().user());

        holder.author.setText(getData().user().login());
        holder.date.setText(TimeUtils.getRelativeTime(getData().updatedAt()));

        final boolean canEdit = (canWrite || getData().user().login().equals(username))
                && editCommentListener != null;

        final boolean canDelete = (canWrite || getData().user().login().equals(username))
                && deleteCommentListener != null;

        if (canDelete) {
            holder.deleteIcon.setVisibility(View.VISIBLE);
            holder.deleteIcon.setOnClickListener(v ->
                    deleteCommentListener.onDeleteComment(getData()));
        } else {
            holder.deleteIcon.setVisibility(View.INVISIBLE);
        }

        if (canEdit) {
            holder.editIcon.setVisibility(View.VISIBLE);
            holder.editIcon.setOnClickListener(v -> editCommentListener.onEditComment(getData()));
        } else {
            holder.editIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.comment_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_comment_body) TextView body;
        @BindView(R.id.tv_comment_author) TextView author;
        @BindView(R.id.tv_comment_date) TextView date;
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.iv_edit) ImageView editIcon;
        @BindView(R.id.iv_delete) ImageView deleteIcon;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            body.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
