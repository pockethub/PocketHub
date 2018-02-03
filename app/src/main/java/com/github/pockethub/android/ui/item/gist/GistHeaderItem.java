package com.github.pockethub.android.ui.item.gist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.Gist;

import java.util.Date;

import butterknife.BindView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class GistHeaderItem extends BaseDataItem<Gist, GistHeaderItem.ViewHolder> {

    private Context context;

    public GistHeaderItem(Context context, Gist dataItem) {
        super(null, dataItem, dataItem.id().hashCode());
        this.context = context;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        Date createdAt = getData().createdAt();
        if (createdAt != null) {
            StyledText text = new StyledText();
            text.append(context.getString(R.string.prefix_created));
            text.append(createdAt);
            holder.created.setText(text);
            holder.created.setVisibility(VISIBLE);
        } else {
            holder.created.setVisibility(GONE);
        }

        Date updatedAt = getData().updatedAt();
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            StyledText text = new StyledText();
            text.append(context.getString(R.string.prefix_updated));
            text.append(updatedAt);
            holder.updated.setText(text);
            holder.updated.setVisibility(VISIBLE);
        } else {
            holder.updated.setVisibility(GONE);
        }

        String desc = getData().description();
        if (!TextUtils.isEmpty(desc)) {
            holder.description.setText(desc);
        } else {
            holder.description.setText(R.string.no_description_given);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.gist_header;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_gist_creation) TextView created;
        @BindView(R.id.tv_gist_description) TextView description;
        @BindView(R.id.tv_gist_updated) TextView updated;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
