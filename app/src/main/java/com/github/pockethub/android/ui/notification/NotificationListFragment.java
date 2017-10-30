package com.github.pockethub.android.ui.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueUriMatcher;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.issue.IssuesViewActivity;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.NotificationThread;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.request.NotificationReadRequest;
import com.meisolsson.githubsdk.service.activity.NotificationService;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationListFragment extends DialogFragment
        implements NotificationReadListener, AdapterView.OnItemClickListener {

    public static final String EXTRA_FILTER = "filter";

    /**
     * Filters for the request to GitHub.
     */
    private Map<String, Object> filters = new HashMap<>();

    private NotificationListAdapter adapter;

    private ListView list;
    private TextView emptyText;
    private ProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_item);
        refreshLayout.setOnRefreshListener(this::refreshNotifications);

        emptyText = (TextView) view.findViewById(android.R.id.empty);
        emptyText.setText(R.string.no_notifications);

        progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
        list = (ListView) view.findViewById(android.R.id.list);
        list.setVisibility(View.VISIBLE);
        list.setOnItemClickListener(this);

        adapter = new NotificationListAdapter(getActivity().getLayoutInflater(), this);

        list.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        refreshNotifications();
    }

    private void refreshNotifications() {
        list.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        getPageAndNext(1)
                .flatMap(page -> Observable.fromIterable(page.items()))
                .toList()
                .subscribe(threads -> {
                    adapter.setItems(threads);
                    progressBar.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(threads.size() == 0 ? View.VISIBLE : View.GONE);
                    refreshLayout.setRefreshing(false);
                }, e -> ToastUtils.show(getActivity(), R.string.error_notifications_load));
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
                .compose(this.bindToLifecycle())
                .subscribe(response -> refreshNotifications(), Throwable::printStackTrace);
    }

    @Override
    public void readNotifications(@Nullable Repository repository) {
        ServiceGenerator.createService(getActivity(), NotificationService.class)
                .markAllRepositoryNotificationsRead(repository.owner().login(),
                        repository.name(), NotificationReadRequest.builder().build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> refreshNotifications(), Throwable::printStackTrace);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = adapter.getItem(position);
        if (item instanceof NotificationThread) {
            NotificationThread thread = (NotificationThread) item;
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
