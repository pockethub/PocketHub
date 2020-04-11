package com.github.pockethub.android.ui.helpers

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.OnItemLongClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import java.util.ArrayList

class ItemListHandler(
    private val recyclerView: RecyclerView,
    private val emptyView: TextView,
    private val lifecycle: Lifecycle,
    context: Context?,
    clickListener: OnItemClickListener = OnItemClickListener { _, _ -> },
    longClickListener: OnItemLongClickListener = OnItemLongClickListener { _, _ -> false }
): LifecycleObserver {

    /**
     * The adapter used by the [RecyclerView] to display [com.xwray.groupie.Group]:s
     * from Groupie.
     */
    private val adapter = GroupAdapter<ViewHolder>()

    /**
     * The [Section] containing headers, footers and the items.
     */
    val mainSection = Section()

    /**
     * List items.
     */
    var items: MutableList<Item<*>> = ArrayList()
        private set

    init {
        adapter.add(mainSection)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(clickListener)
        adapter.setOnItemLongClickListener(longClickListener)
    }

    fun getItemPosition(item: Item<*>): Int {
        return adapter.getAdapterPosition(item)
    }

    private fun show(view: View) {
        view.visibility = View.VISIBLE
    }

    private fun hide(view: View) {
        view.visibility = View.GONE
    }

    private fun fadeIn(view: View?, animate: Boolean) {
        if (view != null) {
            if (animate) {
                val animation = AnimationUtils.loadAnimation(view.context, android.R.anim.fade_in)
                view.startAnimation(animation)
            } else {
                view.clearAnimation()
            }
        }
    }

    /**
     * Set empty text on list fragment.
     *
     * @param message
     * @return this fragment
     */
    fun setEmptyText(message: String) {
        emptyView.text = message
    }

    /**
     * Set empty text on list fragment.
     *
     * @param resId
     * @return this fragment
     */
    fun setEmptyText(resId: Int) {
        emptyView.setText(resId)
    }

    /**
     * Update
     *
     * @param animate
     * @return this fragment
     */
    private fun updateEmptyView(animate: Boolean = false) {
        if (items.isNotEmpty()) {
            hide(emptyView)
        } else {
            fadeIn(emptyView, animate)
            show(emptyView)
        }
    }

    fun addItems(newItems: List<Item<*>>) {
        items.addAll(newItems)
        mainSection.update(items)

        updateEmptyView(lifecycle.currentState == Lifecycle.State.RESUMED)
    }

    fun update(newItems: List<Item<*>>) {
        items.clear()
        items.addAll(newItems)
        mainSection.update(items)

        updateEmptyView(lifecycle.currentState == Lifecycle.State.RESUMED)
    }

    fun isEmpty(): Boolean = items.isEmpty()
}
