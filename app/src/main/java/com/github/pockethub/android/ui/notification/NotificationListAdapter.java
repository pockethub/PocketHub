package com.github.pockethub.android.ui.notification;

import android.view.LayoutInflater;

import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.NotificationThread;
import com.meisolsson.githubsdk.model.Repository;

import java.util.Collections;
import java.util.List;

class NotificationListAdapter extends MultiTypeAdapter {

    private static final int ITEM_NOTIFICATION = 0;
    private static final int ITEM_HEADER = 1;
    private final NotificationReadListener notificationReadListener;

    public NotificationListAdapter(LayoutInflater inflater,
                                   NotificationReadListener notificationReadListener,
                                   List<NotificationThread> items) {
        this(inflater, notificationReadListener);
        setItems(items);
    }

    public NotificationListAdapter(LayoutInflater inflater,
                                   NotificationReadListener notificationReadListener) {
        super(inflater);
        this.notificationReadListener = notificationReadListener;
    }

    public void setItems(List<NotificationThread> items) {
        clear();
        Collections.sort(items, (o1, o2) ->
                o1.repository().fullName().compareToIgnoreCase(o2.repository().fullName()));

        Repository repoFound = null;
        for (NotificationThread thread : items) {
            String fullName = thread.repository().fullName();

            if (repoFound == null || !fullName.equals(repoFound.fullName())) {
                addItem(ITEM_HEADER, thread.repository());
            }

            addItem(ITEM_NOTIFICATION, thread);
            repoFound = thread.repository();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected int getChildLayoutId(int viewType) {
        switch (viewType) {
            case ITEM_NOTIFICATION:
                return R.layout.notification_item;
            case ITEM_HEADER:
                return R.layout.notification_item_header;
            default:
                throw new IllegalStateException("Incorrect view type");
        }
    }

    @Override
    protected int[] getChildViewIds(int viewType) {
        switch (viewType) {
            case ITEM_NOTIFICATION:
                return new int[] {R.id.tv_notification_icon, R.id.tv_notification_title,
                                  R.id.tv_notification_date, R.id.tv_notification_read_icon};
            case ITEM_HEADER:
                return new int[] {R.id.tv_name, R.id.tv_notifications_read_icon};
            default:
                throw new IllegalStateException("Incorrect view type");
        }
    }

    @Override
    protected void update(int position, Object object, int viewType) {

        if (viewType == ITEM_HEADER) {
            // The header should not be clickable
            updater.view.setEnabled(false);
            updater.view.setOnClickListener(null);

            Repository repository = (Repository) object;
            setText(0, repository.fullName());

            setText(1, "\uf03a");
            if (!textView(1).hasOnClickListeners()) {
                textView(1).setOnClickListener(v ->
                        notificationReadListener.readNotifications(repository));
            }
        } else if (viewType == ITEM_NOTIFICATION) {
            NotificationThread notificationThread = (NotificationThread) object;

            String type = notificationThread.subject().type();
            switch (type) {
                case "Issue":
                    setText(0, OcticonTextView.ICON_ISSUE_OPEN);
                    break;
                case "Release":
                    setText(0, OcticonTextView.ICON_TAG);
                    break;
                default:
                    setText(0, OcticonTextView.ICON_PULL_REQUEST);
                    break;
            }

            setText(1, notificationThread.subject().title());
            setText(2, TimeUtils.getRelativeTime(notificationThread.updatedAt()));

            setText(3, OcticonTextView.ICON_READ);
            setGone(3, !notificationThread.unread());

            if (!textView(3).hasOnClickListeners()) {
                textView(3).setOnClickListener(v ->
                        notificationReadListener.readNotification(notificationThread));
            }
        }
    }
}
