package com.github.pockethub.android.ui.item.dialog;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.git.GitReference;

import butterknife.BindView;

public class RefDialogItem extends BaseDataItem<GitReference, RefDialogItem.ViewHolder> {

    private int selected;

    public RefDialogItem(GitReference dataItem, int selected) {
        super(null, dataItem, dataItem.ref().hashCode());
        this.selected = selected;
    }

    @Override
    public int getLayout() {
        return R.layout.ref_item;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        if (RefUtils.isTag(getData())) {
            holder.refIcon.setText(R.string.icon_tag);
        } else {
            holder.refIcon.setText(R.string.icon_fork);
        }
        holder.ref.setText(RefUtils.getName(getData()));
        holder.selected.setChecked(selected == position);
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_ref_icon) TextView refIcon;
        @BindView(R.id.tv_ref) TextView ref;
        @BindView(R.id.rb_selected) RadioButton selected;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
