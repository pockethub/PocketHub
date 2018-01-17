package com.github.pockethub.android.ui.item.notification;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.ui.notification.NotificationListFragment;
import com.meisolsson.githubsdk.model.Repository;

import butterknife.BindView;

public class NotificationHeaderItem
        extends BaseDataItem<Repository, NotificationHeaderItem.ViewHolder> {

    private NotificationListFragment notificationReadListener;

    public NotificationHeaderItem(Repository dataItem,
                                  NotificationListFragment notificationReadListener) {
        super(null, dataItem, dataItem.id());
        this.notificationReadListener = notificationReadListener;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        Repository repository = getData();
        holder.name.setText(repository.fullName());

        holder.readIcon.setText("\uf03a");
        if (!holder.readIcon.hasOnClickListeners()) {
            holder.readIcon.setOnClickListener(v ->
                    notificationReadListener.readNotifications(repository));
        }
    }

    @Override
    public int getLayout() {
        return R.layout.notification_item_header;
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

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_name) TextView name;
        @BindView(R.id.tv_notifications_read_icon) TextView readIcon;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
