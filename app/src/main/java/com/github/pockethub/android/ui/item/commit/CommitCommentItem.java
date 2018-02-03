package com.github.pockethub.android.ui.item.commit;

import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.git.GitComment;

import butterknife.BindView;

public class CommitCommentItem extends BaseDataItem<GitComment, CommitCommentItem.ViewHolder> {

    private final HttpImageGetter imageGetter;
    private final boolean isLineComment;

    public CommitCommentItem(AvatarLoader avatars, HttpImageGetter imageGetter,
                             GitComment comment) {
        this(avatars, imageGetter, comment, false);
    }

    public CommitCommentItem(AvatarLoader avatars, HttpImageGetter imageGetter,
                             GitComment comment, boolean isLineComment) {
        super(avatars, comment, comment.id());
        this.imageGetter = imageGetter;
        this.isLineComment = isLineComment;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        GitComment comment = getData();
        getAvatarLoader().bind(holder.avatar, comment.user());
        holder.author.setText(comment.user().login());
        holder.date.setText(TimeUtils.getRelativeTime(comment.updatedAt()));
        imageGetter.bind(holder.body, comment.bodyHtml(), comment.id());
    }

    @Override
    public int getLayout() {
        return isLineComment ? R.layout.diff_comment_item : R.layout.commit_comment_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_comment_body) TextView body;
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.tv_comment_author) TextView author;
        @BindView(R.id.tv_comment_date) TextView date;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            body.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
