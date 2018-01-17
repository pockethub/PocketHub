package com.github.pockethub.android.ui.item.notification;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.ui.notification.NotificationListFragment;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.NotificationThread;

import butterknife.BindView;

public class NotificationItem
        extends BaseDataItem<NotificationThread, NotificationItem.ViewHolder> {

    private NotificationListFragment notificationReadListener;

    public NotificationItem(NotificationThread dataItem,
                            NotificationListFragment notificationReadListener) {
        super(null, dataItem, dataItem.id().hashCode());
        this.notificationReadListener = notificationReadListener;
    }

    @Override
    public void bind(@NonNull ViewHolder holder, int position) {
        NotificationThread thread = getData();

        String type = thread.subject().type();
        switch (type) {
            case "Issue":
                holder.icon.setText(OcticonTextView.ICON_ISSUE_OPEN);
                break;
            case "Release":
                holder.icon.setText(OcticonTextView.ICON_TAG);
                break;
            default:
                holder.icon.setText(OcticonTextView.ICON_PULL_REQUEST);
                break;
        }

        holder.title.setText(thread.subject().title());
        holder.date.setText(TimeUtils.getRelativeTime(thread.updatedAt()));

        holder.readIcon.setText(OcticonTextView.ICON_READ);
        holder.readIcon.setVisibility(thread.unread() ? View.VISIBLE : View.GONE);

        if (!holder.readIcon.hasOnClickListeners()) {
            holder.readIcon.setOnClickListener(v ->
                    notificationReadListener.readNotification(getData()));
        }
    }

    @Override
    public int getLayout() {
        return R.layout.notification_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_notification_icon) TextView icon;
        @BindView(R.id.tv_notification_title) TextView title;
        @BindView(R.id.tv_notification_date) TextView date;
        @BindView(R.id.tv_notification_read_icon) TextView readIcon;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
