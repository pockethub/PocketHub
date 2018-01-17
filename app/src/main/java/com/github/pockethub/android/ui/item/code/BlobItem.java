package com.github.pockethub.android.ui.item.code;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.code.FullTree;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.ServiceUtils;

import butterknife.BindView;

public class BlobItem extends BaseDataItem<FullTree.Entry, BlobItem.ViewHolder> {

    private static final int INDENTED_PADDING = 16;

    private final int indentedPaddingLeft;
    private boolean indented;

    private Context context;

    public BlobItem(Context context, FullTree.Entry dataItem, boolean indented) {
        super(null, dataItem, dataItem.entry.sha().hashCode());
        this.context = context;
        indentedPaddingLeft = ServiceUtils.getIntPixels(context.getResources(), INDENTED_PADDING);
        this.indented = indented;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        FullTree.Entry file = getData();
        viewHolder.file.setText(file.name);
        viewHolder.size.setText(Formatter.formatFileSize(context, file.entry.size()));

        viewHolder.updatePadding(indented, indentedPaddingLeft);
    }

    @Override
    public int getLayout() {
        return R.layout.blob_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        private final int paddingLeft;
        private final int paddingRight;
        private final int paddingTop;
        private final int paddingBottom;

        @BindView(R.id.tv_file) TextView file;
        @BindView(R.id.tv_size) TextView size;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            paddingLeft = rootView.getPaddingLeft();
            paddingRight = rootView.getPaddingRight();
            paddingTop = rootView.getPaddingTop();
            paddingBottom = rootView.getPaddingBottom();
        }

        private void updatePadding(boolean indented, int indentedPaddingLeft) {
            if (indented) {
                getRoot().setPadding(indentedPaddingLeft, paddingTop,
                        paddingRight, paddingBottom);
            } else {
                getRoot().setPadding(paddingLeft, paddingTop, paddingRight,
                        paddingBottom);
            }
        }
    }
}
