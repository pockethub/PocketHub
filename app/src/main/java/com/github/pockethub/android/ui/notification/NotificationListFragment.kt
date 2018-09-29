package com.github.pockethub.android.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.ListFetcher
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueUriMatcher
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.issue.IssuesViewActivity
import com.github.pockethub.android.ui.item.notification.NotificationHeaderItem
import com.github.pockethub.android.ui.item.notification.NotificationItem
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.NotificationThread
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.request.NotificationReadRequest
import com.meisolsson.githubsdk.service.activity.NotificationService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import java.util.HashMap
import javax.inject.Inject

// NotificationThread
class NotificationListFragment : BaseFragment(), NotificationReadListener {

    @Inject
    protected lateinit var notificationService: NotificationService

    private lateinit var listFetcher: ListFetcher<NotificationThread>

    private lateinit var itemListHandler: ItemListHandler

    /**
     * Filters for the request to GitHub.
     */
    private val filters = HashMap<String, Any>()

    protected val errorMessage: Int
        get() = R.string.error_notifications_load

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments!!.containsKey(EXTRA_FILTER)) {
            filters[arguments!!.getString(EXTRA_FILTER)] = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemListHandler = ItemListHandler(
            view.list,
            view.empty,
            lifecycle,
            activity,
            OnItemClickListener(this::onItemClick))

        listFetcher = ListFetcher(
            view.swipe_item,
            lifecycle,
            itemListHandler,
            { t -> ToastUtils.show(activity, errorMessage)},
            this::loadData,
            this::createItem
        )

        listFetcher.onDataLoaded = this::onDataLoaded
    }

    private fun loadData(forceRefresh: Boolean): Single<List<NotificationThread>> {
        return getPageAndNext(1)
            .flatMap { page -> Observable.fromIterable(page.items()) }
            .toList()
    }

    private fun onDataLoaded(newItems: MutableList<Item<*>>): MutableList<Item<*>> {
        updateHeaders(newItems)
        return newItems
    }

    private fun updateHeaders(notifications: MutableList<Item<*>>) {
        if (notifications.isEmpty()) {
            return
        }

        notifications.sortWith(Comparator { i1, i2 ->
            val r1 = (i1 as NotificationItem).notificationThread.repository()
            val r2 = (i2 as NotificationItem).notificationThread.repository()
            r1!!.fullName()!!.compareTo(r2!!.fullName()!!, ignoreCase = true)
        })

        var repoFound: Repository? = null
        for (i in notifications.indices) {
            val item = notifications[i] as NotificationItem
            val thread = item.notificationThread
            val fullName = thread.repository()!!.fullName()

            if (repoFound == null || fullName != repoFound.fullName()) {
                notifications.add(i, NotificationHeaderItem(thread.repository()!!, this))
            }

            repoFound = thread.repository()
        }
    }

    private fun createItem(item: NotificationThread): Item<*> {
        return NotificationItem(item, this)
    }

    private fun getPageAndNext(i: Int): Observable<Page<NotificationThread>> {
        return notificationService
            .getNotifications(filters, i.toLong())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapObservable { response ->
                val page = response.body()
                if (page.next() == null) {
                    return@flatMapObservable notificationService
                        .getNotifications(filters, i.toLong())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapObservable { Observable.just(page) }
                }

                return@flatMapObservable Observable.just(page)
                    .concatWith(getPageAndNext(page.next()!!))
            }
    }

    override fun readNotification(thread: NotificationThread) {
        ServiceGenerator.createService(activity, NotificationService::class.java)
            .markNotificationRead(thread.id())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ listFetcher.forceRefresh() }, { e -> e.printStackTrace()})
    }

    override fun readNotifications(repository: Repository?) {
        ServiceGenerator.createService(activity, NotificationService::class.java)
            .markAllRepositoryNotificationsRead(repository!!.owner()!!.login(),
                repository.name(), NotificationReadRequest.builder().build())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ listFetcher.forceRefresh() }, { e -> e.printStackTrace()})
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is NotificationItem) {
            val thread = item.notificationThread
            val url = thread.subject()!!.url()

            val issue = IssueUriMatcher.getApiIssue(url)
            if (issue != null) {
                val intent = IssuesViewActivity.createIntent(issue, thread.repository())
                startActivity(intent)
            } else {
                ToastUtils.show(activity, R.string.releases_not_yet_in_app)
            }
        }
    }

    companion object {
        const val EXTRA_FILTER = "filter"
    }
}
