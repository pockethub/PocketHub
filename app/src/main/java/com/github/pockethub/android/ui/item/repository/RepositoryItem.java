package com.github.pockethub.android.ui.item.repository;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;

import butterknife.BindView;

import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_FORK;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_MIRROR_PRIVATE;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_MIRROR_PUBLIC;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_PRIVATE;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_PUBLIC;

public class RepositoryItem extends BaseDataItem<Repository, RepositoryItem.ViewHolder> {

    private final User user;
    private int descriptionColor = -1;

    public RepositoryItem(Repository dataItem, User user) {
        super(null, dataItem, dataItem.id());
        this.user = user;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        Repository repo = getData();

        if (descriptionColor == -1) {
            descriptionColor = holder.getRoot().getResources().getColor(R.color.text_description);
        }

        StyledText name = new StyledText();
        if (user == null) {
            name.append(repo.owner().login()).append('/');
        } else {
            if (!user.login().equals(repo.owner().login())) {
                name.foreground(repo.owner().login(), descriptionColor)
                        .foreground('/', descriptionColor);
            }
        }

        name.bold(repo.name());
        holder.name.setText(name);

        if (TextUtils.isEmpty(repo.mirrorUrl())) {
            if (repo.isPrivate()) {
                holder.icon.setText(ICON_PRIVATE);
            } else if (repo.isFork()) {
                holder.icon.setText(ICON_FORK);
            } else {
                holder.icon.setText(ICON_PUBLIC);
            }
        } else {
            if (repo.isPrivate()) {
                holder.icon.setText(ICON_MIRROR_PRIVATE);
            } else {
                holder.icon.setText(ICON_MIRROR_PUBLIC);
            }
        }

        if (!TextUtils.isEmpty(repo.description())) {
            holder.description.setText(repo.description());
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(repo.language())) {
            holder.language.setText(repo.language());
            holder.language.setVisibility(View.VISIBLE);
        } else {
            holder.language.setVisibility(View.GONE);
        }

        holder.watchers.setText(String.valueOf(repo.watchersCount()));
        holder.forks.setText(String.valueOf(repo.forksCount()));
    }

    @Override
    public int getLayout() {
        return R.layout.user_repo_item;
    }

    @Override
    public long getId() {
        return getData().id();
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_repo_icon) TextView icon;
        @BindView(R.id.tv_repo_description) TextView description;
        @BindView(R.id.tv_language) TextView language;
        @BindView(R.id.tv_watchers) TextView watchers;
        @BindView(R.id.tv_forks) TextView forks;
        @BindView(R.id.tv_repo_name) TextView name;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
