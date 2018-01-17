package com.github.pockethub.android.ui.item.dialog;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.Milestone;

import butterknife.BindView;

public class MilestoneDialogItem extends BaseDataItem<Milestone, MilestoneDialogItem.ViewHolder> {

    private int selected;

    public MilestoneDialogItem(Milestone dataItem, int selected) {
        super(null, dataItem, dataItem.id());
        this.selected = selected;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.title.setText(getData().title());

        String description = getData().description();
        if (!TextUtils.isEmpty(description)) {
            viewHolder.description.setText(description);
            viewHolder.description.setVisibility(View.VISIBLE);
        } else {
            viewHolder.description.setVisibility(View.GONE);
        }

        viewHolder.selected.setChecked(selected == position);
    }

    @Override
    public int getLayout() {
        return R.layout.milestone_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.rb_selected) RadioButton selected;
        @BindView(R.id.tv_milestone_title) TextView title;
        @BindView(R.id.tv_milestone_description) TextView description;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
