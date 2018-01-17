package com.github.pockethub.android.ui.item;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.User;

import butterknife.BindView;

public class UserItem extends BaseDataItem<User, UserItem.ViewHolder> {

    public UserItem(AvatarLoader avatarLoader, User dataItem) {
        super(avatarLoader, dataItem, dataItem.id());
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        getAvatarLoader().bind(holder.avatar, getData());
        holder.login.setText(getData().login());
    }

    @Override
    public int getLayout() {
        return R.layout.user_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.tv_login) TextView login;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
