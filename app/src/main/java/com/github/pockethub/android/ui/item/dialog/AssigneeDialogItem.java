package com.github.pockethub.android.ui.item.dialog;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.User;

import butterknife.BindView;

public class AssigneeDialogItem extends BaseDataItem<User, AssigneeDialogItem.ViewHolder> {

    private int selected;

    public AssigneeDialogItem(AvatarLoader avatarLoader, User dataItem, int selected) {
        super(avatarLoader, dataItem, dataItem.id());
        this.selected = selected;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.login.setText(getData().login());
        viewHolder.selected.setChecked(selected == position);
        getAvatarLoader().bind(viewHolder.avatar, getData());
    }

    @Override
    public int getLayout() {
        return R.layout.collaborator_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_login) TextView login;
        @BindView(R.id.iv_avatar) ImageView avatar;
        @BindView(R.id.rb_selected) RadioButton selected;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
