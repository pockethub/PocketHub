package com.github.pockethub.android.ui.item.gist;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.GistFile;

import butterknife.BindView;

public class GistFileItem extends BaseDataItem<GistFile, GistFileItem.ViewHolder> {

    public GistFileItem(GistFile dataItem) {
        super(null, dataItem, dataItem.filename().hashCode());
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.filename.setText(getData().filename());
    }

    @Override
    public int getLayout() {
        return R.layout.gist_file_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_file) TextView filename;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
