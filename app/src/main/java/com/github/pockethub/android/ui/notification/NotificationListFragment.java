package com.github.pockethub.android.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueUriMatcher;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.ItemListFragment;
import com.github.pockethub.android.ui.issue.IssuesViewActivity;
import com.github.pockethub.android.ui.item.notification.NotificationHeaderItem;
import com.github.pockethub.android.ui.item.notification.NotificationItem;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.NotificationThread;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.request.NotificationReadRequest;
import com.meisolsson.githubsdk.service.activity.NotificationService;
import com.xwray.groupie.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NotificationListFragment extends ItemListFragment<NotificationThread>
        implements NotificationReadListener {

    public static final String EXTRA_FILTER = "filter";

    /**
     * Filters for the request to GitHub.
     */
    private Map<String, Object> filters = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args.containsKey(EXTRA_FILTER)) {
            filters.put(args.getString(EXTRA_FILTER), true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_notifications_load;
    }

    @Override
    protected Single<List<NotificationThread>> loadData(boolean forceRefresh) {
        return getPageAndNext(1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toList();
    }

    @Override
    protected void onDataLoaded(List<Item> newItems) {
        items.clear();
        updateHeaders(newItems);
        super.onDataLoaded(newItems);
    }

    private void updateHeaders(final List<Item> notifications) {
        if (notifications.isEmpty()) {
            return;
        }

        Collections.sort(notifications, (i1, i2) -> {
            Repository r1 = ((NotificationItem) i1).getData().repository();
            Repository r2 = ((NotificationItem) i2).getData().repository();
            return r1.fullName().compareToIgnoreCase(r2.fullName());
        });

        Repository repoFound = null;
        for (int i = 0; i < notifications.size(); i++) {
            NotificationItem item = (NotificationItem) notifications.get(i);
            NotificationThread thread = item.getData();
            String fullName = thread.repository().fullName();

            if (repoFound == null || !fullName.equals(repoFound.fullName())) {
                notifications.add(i, new NotificationHeaderItem(thread.repository(), this));
            }

            repoFound = thread.repository();
        }
    }


    @Override
    protected Item createItem(NotificationThread item) {
        return new NotificationItem(item, this);
    }

    private Observable<Page<NotificationThread>> getPageAndNext(int i) {
        return ServiceGenerator.createService(getActivity(), NotificationService.class)
                .getNotifications(filters, i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(response -> {
                    Page<NotificationThread> page = response.body();
                    if (page.next() == null) {
                        return Observable.just(page);
                    }

                    return Observable.just(page).concatWith(getPageAndNext(page.next()));
                });
    }

    @Override
    public void readNotification(@NonNull NotificationThread thread) {
        ServiceGenerator.createService(getActivity(), NotificationService.class)
                .markNotificationRead(thread.id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> refresh(), Throwable::printStackTrace);
    }

    @Override
    public void readNotifications(@Nullable Repository repository) {
        ServiceGenerator.createService(getActivity(), NotificationService.class)
                .markAllRepositoryNotificationsRead(repository.owner().login(),
                        repository.name(), NotificationReadRequest.builder().build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> refresh(), Throwable::printStackTrace);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof NotificationItem) {
            NotificationThread thread = ((NotificationItem) item).getData();
            String url = thread.subject().url();

            Issue issue = IssueUriMatcher.getApiIssue(url);
            if (issue != null) {
                Intent intent = IssuesViewActivity.createIntent(issue, thread.repository());
                startActivity(intent);
            } else {
                ToastUtils.show(getActivity(), R.string.releases_not_yet_in_app);
            }
        }
    }
}
