package com.github.pockethub.android.ui.item.repository;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.xwray.groupie.Item;

import butterknife.BindView;

public class RepositoryHeaderItem extends Item<RepositoryHeaderItem.ViewHolder> {

    private String id;

    public RepositoryHeaderItem(String id) {
        super(id.hashCode());
        this.id = id;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.header.setText(id);
    }

    @Override
    public int getLayout() {
        return R.layout.repo_header_item;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public boolean isLongClickable() {
        return false;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_header) TextView header;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
