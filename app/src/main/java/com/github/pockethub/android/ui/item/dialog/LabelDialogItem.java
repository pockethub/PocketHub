package com.github.pockethub.android.ui.item.dialog;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.issue.LabelDrawableSpan;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.Label;

import butterknife.BindView;

public class LabelDialogItem extends BaseDataItem<Label, LabelDialogItem.ViewHolder> {

    private boolean selected;

    public LabelDialogItem(Label dataItem, boolean selected) {
        super(null, dataItem, dataItem.name().hashCode());
        this.selected = selected;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        LabelDrawableSpan.setText(viewHolder.name, getData());
        viewHolder.selected.setChecked(selected);
    }

    @Override
    public int getLayout() {
        return R.layout.label_item;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }


    class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_label_name) TextView name;
        @BindView(R.id.cb_selected) CheckBox selected;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
