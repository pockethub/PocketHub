package com.github.pockethub.android.ui.item.commit;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.meisolsson.githubsdk.model.GitHubFile;

import java.text.NumberFormat;

import butterknife.BindView;

public class CommitFileHeaderItem
        extends BaseDataItem<GitHubFile, CommitFileHeaderItem.ViewHolder> {

    private final int addTextColor;
    private final int removeTextColor;

    public CommitFileHeaderItem(Context context, GitHubFile file) {
        super(null, file, file.sha().hashCode());

        Resources resources = context.getResources();
        addTextColor = resources.getColor(R.color.diff_add_text);
        removeTextColor = resources.getColor(R.color.diff_remove_text);
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        GitHubFile file = getData();
        String path = file.filename();

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            holder.filename.setText(path.substring(lastSlash + 1));
            holder.folder.setText(path.substring(0, lastSlash + 1));
            holder.folder.setVisibility(View.VISIBLE);
        } else {
            holder.filename.setText(path);
            holder.folder.setVisibility(View.GONE);
        }

        NumberFormat numberFormat = NumberFormat.getIntegerInstance();

        StyledText stats = new StyledText();
        stats.foreground('+', addTextColor);
        stats.foreground(numberFormat.format(file.additions()), addTextColor);
        stats.append(' ').append(' ').append(' ');
        stats.foreground('-', removeTextColor);
        stats.foreground(numberFormat.format(file.deletions()), removeTextColor);
        holder.stats.setText(stats);
    }

    @Override
    public int getLayout() {
        return R.layout.commit_diff_file_header;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_name) TextView filename;
        @BindView(R.id.tv_folder) TextView folder;
        @BindView(R.id.tv_stats) TextView stats;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
