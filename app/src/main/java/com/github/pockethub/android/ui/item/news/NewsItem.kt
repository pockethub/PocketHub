package com.github.pockethub.android.ui.item.news

import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.TimeUtils
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.GitHubEventType.*
import com.meisolsson.githubsdk.model.User
import kotlinx.android.synthetic.main.news_item.view.*

open class NewsItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) :
        BaseDataItem<GitHubEvent, NewsItem.ViewHolder>(avatarLoader, dataItem,
                dataItem.id()!!.hashCode().toLong()) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        avatarLoader.bind(viewHolder.avatar, data.actor())
        viewHolder.date.text = TimeUtils.getRelativeTime(data.createdAt())
    }

    protected fun boldActor(text: StyledText, event: GitHubEvent?): StyledText =
        boldUser(text, event?.actor())

    protected fun boldUser(text: StyledText, user: User?): StyledText {
        text.bold(user?.login())
        return text
    }

    protected fun boldRepo(text: StyledText, event: GitHubEvent?): StyledText {
        val repo = event?.repo()
        text.bold(repo?.repoWithUserName())
        return text
    }

    protected fun boldRepoName(text: StyledText,
                               event: GitHubEvent?): StyledText {
        val repo = event?.repo()
        val name = repo?.repoWithUserName()
        if (!TextUtils.isEmpty(name)) {
            val slash: Int = name!!.indexOf('/')
            if (slash != -1 && slash + 1 < name.length) {
                text.bold(name.substring(slash + 1))
            }
        }
        return text
    }


    protected fun appendText(text: StyledText, toAppend: String?) {
        var textToAppend: String? = toAppend ?: return
        textToAppend = textToAppend!!.trim { it <= ' ' }
        if (textToAppend.isEmpty()) {
            return
        }

        text.append(textToAppend)
    }

    override fun getLayout(): Int = R.layout.news_item

    override fun createViewHolder(itemView: View): ViewHolder = ViewHolder(itemView)

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        var event: TextView

        var avatar: ImageView
        var details: TextView
        var icon: TextView
        var date: TextView

        init {
            avatar = rootView.iv_avatar
            event = rootView.tv_event
            details = rootView.tv_event_details
            icon = rootView.tv_event_icon
            date = rootView.tv_event_date
        }
    }

    companion object {

        /**
         * Create a instance of the [NewsItem] corresponding to the event type.
         *
         * @param avatars Avatar image loader
         * @param item Event item
         * @return Subclass of [NewsItem] corresponding to the event type
         */
        @JvmStatic
        fun createNewsItem(avatars: AvatarLoader, item: GitHubEvent): NewsItem? =
            when (item.type()) {
                CommitCommentEvent -> CommitCommentEventItem(avatars, item)
                CreateEvent -> CreateEventItem(avatars, item)
                DeleteEvent ->DeleteEventItem(avatars, item)
                DownloadEvent, ReleaseEvent -> ReleaseEventItem(avatars, item)
                FollowEvent -> FollowEventItem(avatars, item)
                ForkEvent -> ForkEventItem(avatars, item)
                GistEvent -> GistEventItem(avatars, item)
                GollumEvent -> GollumEventItem(avatars, item)
                IssueCommentEvent -> IssueCommentEventItem(avatars, item)
                IssuesEvent -> IssuesEventItem(avatars, item)
                MemberEvent -> MemberEventItem(avatars, item)
                PublicEvent -> PublicEventItem(avatars, item)
                PullRequestEvent -> PullRequestEventItem(avatars, item)
                PullRequestReviewCommentEvent -> PullRequestReviewCommentEventItem(avatars, item)
                PushEvent -> PushEventItem(avatars, item)
                TeamAddEvent -> TeamAddEventItem(avatars, item)
                WatchEvent -> WatchEventItem(avatars, item)
                else -> {
                    Log.d("NewsItem", "Event type not allowed: " + item.type()!!)
                    null
                }
            }
    }
}
