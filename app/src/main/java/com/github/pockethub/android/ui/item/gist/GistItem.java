package com.github.pockethub.android.ui.item.gist;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.User;

import butterknife.BindView;

public class GistItem extends BaseDataItem<Gist, GistItem.ViewHolder> {

    public GistItem(AvatarLoader avatarLoader, Gist dataItem) {
        super(avatarLoader, dataItem, dataItem.id().hashCode());
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        Gist gist = getData();
        viewHolder.id.setText(gist.id());

        String description = gist.description();
        if (!TextUtils.isEmpty(description)) {
            viewHolder.title.setText(description);
        } else {
            viewHolder.title.setText(R.string.no_description_given);
        }

        User user = gist.owner();
        getAvatarLoader().bind(viewHolder.avatar, user);

        StyledText authorText = new StyledText();
        if (user != null) {
            authorText.bold(user.login());
        } else {
            Resources res = viewHolder.getRoot().getResources();
            authorText.bold(res.getString(R.string.anonymous));
        }
        authorText.append(' ');
        authorText.append(gist.createdAt());
        viewHolder.author.setText(authorText);

        viewHolder.comments.setText(String.valueOf(gist.comments()));
        viewHolder.files.setText(String.valueOf(gist.files().size()));
    }

    @Override
    public int getLayout() {
        return R.layout.gist_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_gist_id) TextView id;
        @BindView(R.id.tv_gist_title) TextView title;
        @BindView(R.id.tv_gist_author) TextView author;
        @BindView(R.id.tv_gist_comments) TextView comments;
        @BindView(R.id.tv_gist_files) TextView files;
        @BindView(R.id.iv_avatar) ImageView avatar;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
