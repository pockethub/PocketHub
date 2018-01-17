package com.github.pockethub.android.ui.item;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.User;

public class ContributorItem extends UserItem {

    public ContributorItem(AvatarLoader avatarLoader, User dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        super.bind(holder, position);
        TextView contributions = holder.getRoot().findViewById(R.id.tv_contributions);

        Resources res = holder.getRoot().getResources();
        String text = res.getString(R.string.contributions, getData().contributions());

        contributions.setText(text);
    }

    @Override
    public int getLayout() {
        return R.layout.contributor_item;
    }
}
