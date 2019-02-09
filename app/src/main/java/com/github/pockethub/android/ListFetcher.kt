package com.github.pockethub.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.xwray.groupie.Item
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers

class ListFetcher<E>(
    private val swipeRefreshLayout: SwipeRefreshLayout?,
    private val lifecycle: Lifecycle,
    private val itemListHandler: ItemListHandler,
    private val showError: (Throwable) -> Unit,
    private val loadData: (force: Boolean) -> Single<List<E>>,
    private val createItem: (item: E) -> Item<*>
): LifecycleObserver {

    /**
     * Disposable for data load request.
     */
    private var dataLoadDisposable: Disposable = Disposables.disposed()

    private var isLoading = false

    var onDataLoaded: (MutableList<Item<*>>) -> MutableList<Item<*>> = { items -> items }

    init {
        lifecycle.addObserver(this)
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::forceRefresh)
            swipeRefreshLayout.setColorSchemeResources(
                R.color.pager_title_background_top_start,
                R.color.pager_title_background_end,
                R.color.text_link,
                R.color.pager_title_background_end)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart()  {
        refresh()
    }

    private fun refresh(force: Boolean) {
        if (isLoading) {
            return
        }

        if (!dataLoadDisposable.isDisposed) {
            dataLoadDisposable.dispose()
        }

        isLoading = true

        dataLoadDisposable = loadData(force)
            .flatMap { items ->
                Observable.fromIterable<E>(items)
                    .map<Item<*>> { this.createItem(it) }
                    .toList()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .`as`(AutoDisposeUtils.bindToLifecycle<MutableList<Item<*>>>(lifecycle))
            .subscribe(
                { this.onLoadFinished(it) },
                { this.onDataLoadError(it) }
            )
    }

    fun refresh() {
        refresh(false)
    }

    fun forceRefresh() {
        swipeRefreshLayout!!.isRefreshing = true
        refresh(true)
    }
    /**
     * Called when the data has loaded.
     *
     * @param newItems The items added to the list.
     */
    private fun onLoadFinished(newItems: MutableList<Item<*>>) {
        isLoading = false
        swipeRefreshLayout!!.isRefreshing = false

        val items = onDataLoaded(newItems)
        itemListHandler.update(items)
    }

    private fun onDataLoadError(throwable: Throwable) {
        isLoading = false
        swipeRefreshLayout!!.isRefreshing = false

        showError(throwable)
    }
}