package com.github.pockethub.android.ui.item.news

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import androidx.core.text.bold
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.repo.RepositoryViewActivity
import com.github.pockethub.android.ui.user.UserViewActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ConvertUtils
import com.github.pockethub.android.util.TimeUtils
import com.github.pockethub.android.util.android.text.clickable
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.GitHubEventType.CommitCommentEvent
import com.meisolsson.githubsdk.model.GitHubEventType.CreateEvent
import com.meisolsson.githubsdk.model.GitHubEventType.DeleteEvent
import com.meisolsson.githubsdk.model.GitHubEventType.DownloadEvent
import com.meisolsson.githubsdk.model.GitHubEventType.FollowEvent
import com.meisolsson.githubsdk.model.GitHubEventType.ForkEvent
import com.meisolsson.githubsdk.model.GitHubEventType.GistEvent
import com.meisolsson.githubsdk.model.GitHubEventType.GollumEvent
import com.meisolsson.githubsdk.model.GitHubEventType.IssueCommentEvent
import com.meisolsson.githubsdk.model.GitHubEventType.IssuesEvent
import com.meisolsson.githubsdk.model.GitHubEventType.MemberEvent
import com.meisolsson.githubsdk.model.GitHubEventType.PublicEvent
import com.meisolsson.githubsdk.model.GitHubEventType.PullRequestEvent
import com.meisolsson.githubsdk.model.GitHubEventType.PullRequestReviewCommentEvent
import com.meisolsson.githubsdk.model.GitHubEventType.PushEvent
import com.meisolsson.githubsdk.model.GitHubEventType.ReleaseEvent
import com.meisolsson.githubsdk.model.GitHubEventType.TeamAddEvent
import com.meisolsson.githubsdk.model.GitHubEventType.WatchEvent
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*

open class NewsItem(
        private val avatarLoader: AvatarLoader,
        val gitHubEvent: GitHubEvent
) : Item(gitHubEvent.id()!!.hashCode().toLong()) {

    override fun getLayout(): Int = R.layout.news_item

    override fun bind(holder: ViewHolder, position: Int) {
        avatarLoader.bind(holder.iv_avatar, gitHubEvent.actor())
        holder.iv_avatar.setOnClickListener {
            val context = holder.root.context
            val user = gitHubEvent.actor()!!
            context.startActivity(UserViewActivity.createIntent(user))
        }
        holder.tv_event_date.text = TimeUtils.getRelativeTime(gitHubEvent.createdAt())
    }

    protected fun boldActor(context: Context, text: SpannableStringBuilder, event: GitHubEvent?) =
            boldUser(context, text, event?.actor())

    protected fun boldUser(context: Context, text: SpannableStringBuilder, user: User?) {
        text.bold {
            clickable(onClick = {
                context.startActivity(UserViewActivity.createIntent(user!!))
            }) {
                append(user!!.login())
            }
        }
    }

    protected fun boldRepo(context: Context, text: SpannableStringBuilder, event: GitHubEvent?) {
        text.bold {
            val eventRepo = event?.repo()!!
            clickable(onClick = {
                val repository = ConvertUtils.eventRepoToRepo(eventRepo)
                context.startActivity(RepositoryViewActivity.createIntent(repository))
            }) {
                append(eventRepo.repoWithUserName())
            }
        }
    }

    protected fun boldRepoName(
            context: Context,
            text: SpannableStringBuilder,
            event: GitHubEvent?
    ) {
        val repo = event?.repo()
        val name = repo?.repoWithUserName()
        if (!TextUtils.isEmpty(name)) {
            val slash: Int = name!!.indexOf('/')
            if (slash != -1 && slash + 1 < name.length) {
                text.bold {
                    clickable(onClick = {
                        val repository = ConvertUtils.eventRepoToRepo(repo)
                        context.startActivity(RepositoryViewActivity.createIntent(repository))
                    }) {
                        append(name.substring(slash + 1))
                    }
                }
            }
        }
    }

    protected fun appendText(text: SpannableStringBuilder, toAppend: String?) {
        var textToAppend: String? = toAppend ?: return
        textToAppend = textToAppend!!.trim { it <= ' ' }
        if (textToAppend.isEmpty()) {
            return
        }

        text.append(textToAppend)
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
                    DeleteEvent -> DeleteEventItem(avatars, item)
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
                    PullRequestReviewCommentEvent ->
                        PullRequestReviewCommentEventItem(avatars, item)
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
