package com.github.pockethub.android.ui.item.commit;

import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Commit;

import butterknife.BindView;

public class CommitItem extends BaseDataItem<Commit, CommitItem.ViewHolder> {

    public CommitItem(AvatarLoader avatars, Commit dataItem) {
        super(avatars, dataItem, dataItem.sha().hashCode());
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        holder.id.setText(CommitUtils.abbreviate(getData().sha()));

        StyledText authorText = new StyledText();
        authorText.bold(CommitUtils.getAuthor(getData()));
        authorText.append(' ');
        authorText.append(CommitUtils.getAuthorDate(getData()));
        holder.author.setText(authorText);

        CommitUtils.bindAuthor(getData(), getAvatarLoader(), holder.avatar);
        holder.message.setText(getData().commit().message());
        holder.comments.setText(CommitUtils.getCommentCount(getData()));
    }

    @Override
    public int getLayout() {
        return R.layout.commit_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_commit_id) TextView id;
        @BindView(R.id.tv_commit_author) TextView author;
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.tv_commit_message) TextView message;
        @BindView(R.id.tv_commit_comments) TextView comments;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
