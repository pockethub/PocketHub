package com.github.pockethub.android.ui.item.commit;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.commit.DiffStyler;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;

import butterknife.BindView;

public class CommitFileLineItem extends BaseDataItem<CharSequence, CommitFileLineItem.ViewHolder> {

    private final DiffStyler diffStyler;

    public CommitFileLineItem(DiffStyler diffStyler, CharSequence line) {
        super(null, line, line.hashCode());
        this.diffStyler = diffStyler;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.diff.setText(getData());
        diffStyler.updateColors(getData(), viewHolder.diff);
    }

    @Override
    public int getLayout() {
        return R.layout.commit_diff_line;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_diff) TextView diff;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
