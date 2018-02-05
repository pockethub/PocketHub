package com.github.pockethub.android.ui.item.commit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Commit;

import java.util.Date;

import butterknife.BindView;

public class CommitHeaderItem extends BaseDataItem<Commit, CommitHeaderItem.ViewHolder> {

    private final Context context;

    public CommitHeaderItem(AvatarLoader avatarLoader, Context context, Commit dataItem) {
        super(avatarLoader, dataItem, dataItem.sha().hashCode());
        this.context = context;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        Commit commit = getData();
        holder.commitMessage.setText(commit.commit().message());

        String commitAuthor = CommitUtils.getAuthor(commit);
        String commitCommitter = CommitUtils.getCommitter(commit);

        if (commitAuthor != null) {
            CommitUtils.bindAuthor(commit, getAvatarLoader(), holder.authorAvatar);
            holder.authorName.setText(commitAuthor);
            StyledText styledAuthor = new StyledText();
            styledAuthor.append(context.getString(R.string.authored));

            Date commitAuthorDate = CommitUtils.getAuthorDate(commit);
            if (commitAuthorDate != null) {
                styledAuthor.append(' ').append(commitAuthorDate);
            }

            holder.authorDate.setText(styledAuthor);
            holder.authorArea.setVisibility(View.VISIBLE);
        } else {
            holder.authorArea.setVisibility(View.GONE);
        }

        if (isDifferentCommitter(commitAuthor, commitCommitter)) {
            CommitUtils.bindCommitter(commit, getAvatarLoader(), holder.committerAvatar);
            holder.committerName.setText(commitCommitter);
            StyledText styledCommitter = new StyledText();
            styledCommitter.append(context.getString(R.string.committed));

            Date commitCommitterDate = CommitUtils.getCommitterDate(commit);
            if (commitCommitterDate != null) {
                styledCommitter.append(' ').append(commitCommitterDate);
            }

            holder.committerDate.setText(styledCommitter);
            holder.committerArea.setVisibility(View.VISIBLE);
        } else {
            holder.committerArea.setVisibility(View.GONE);
        }
    }

    private boolean isDifferentCommitter(final String author,
                                         final String committer) {
        return committer != null && !committer.equals(author);
    }

    @Override
    public int getLayout() {
        return R.layout.commit_header;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.ll_author) LinearLayout authorArea;
        @BindView(R.id.iv_author) ImageView authorAvatar;
        @BindView(R.id.tv_author) TextView authorName;
        @BindView(R.id.tv_author_date) TextView authorDate;

        @BindView(R.id.ll_committer) LinearLayout committerArea;
        @BindView(R.id.iv_committer) ImageView committerAvatar;
        @BindView(R.id.tv_committer) TextView committerName;
        @BindView(R.id.tv_commit_date) TextView committerDate;
        @BindView(R.id.tv_commit_message) TextView commitMessage;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            commitMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
