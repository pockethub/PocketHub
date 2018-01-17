package com.github.pockethub.android.ui.item.commit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.Commit;

import butterknife.BindView;

public class CommitParentItem extends BaseDataItem<Commit, CommitParentItem.ViewHolder> {

    private final Context context;

    public CommitParentItem(Context context, Commit commit) {
        super(null, commit, commit.sha().hashCode());
        this.context = context;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        StyledText parentText = new StyledText()
                .append(context.getString(R.string.parent_prefix))
                .monospace(CommitUtils.abbreviate(getData().sha()))
                .underlineAll();
        viewHolder.commitId.setText(parentText);
    }

    @Override
    public int getLayout() {
        return R.layout.commit_parent_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_commit_id) TextView commitId;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
