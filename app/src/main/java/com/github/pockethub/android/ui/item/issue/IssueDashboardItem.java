package com.github.pockethub.android.ui.item.issue;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.model.Issue;

public class IssueDashboardItem extends IssueItem {

    public IssueDashboardItem(AvatarLoader avatarLoader, Issue dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        super.bind(holder, position);
        TextView textView = holder.getRoot().findViewById(R.id.tv_issue_repo_name);
        textView.setText(InfoUtils.createRepoId(getData().repository()));
    }

    @Override
    public int getLayout() {
        return R.layout.dashboard_issue_item;
    }
}
