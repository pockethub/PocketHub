package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.util.android.text.monospace
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.PushPayload
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.news_item.*
import java.text.NumberFormat

class PushEventItem(
        avatarLoader: AvatarLoader,
        gitHubEvent: GitHubEvent
) : NewsItem(avatarLoader, gitHubEvent) {

    override fun bind(holder: ViewHolder, position: Int) {
        super.bind(holder, position)
        holder.tv_event_icon.text = OcticonTextView.ICON_PUSH

        val payload = gitHubEvent.payload() as PushPayload?

        holder.tv_event.text = buildSpannedString {
            boldActor(this, gitHubEvent)
            append(" pushed to ")
            var ref = payload?.ref()
            if (ref!!.startsWith("refs/heads/")) {
                ref = ref.substring(11)
            }
            bold {
                append(ref)
            }
            append(" at ")
            boldRepo(this, gitHubEvent)
        }

        val details = buildSpannedString {
            val commits = payload?.commits()
            val size = commits!!.size
            if (size > 0) {
                if (size != 1) {
                    val numberFormat = NumberFormat.getIntegerInstance()
                    append(numberFormat.format(size.toLong())).append(" new commits")
                } else {
                    append("1 new commit")
                }

                val max = 3
                var appended = 0
                for (commit in commits) {

                    val sha = commit.sha()

                    append('\n')
                    monospace {
                        if (sha!!.length > 7) {
                            append(sha.substring(0, 7))
                        } else {
                            append(sha)
                        }
                    }

                    val message = commit.message()
                    if (!TextUtils.isEmpty(message)) {
                        append(' ')
                        val newline = message!!.indexOf('\n')
                        if (newline > 0) {
                            append(message.subSequence(0, newline))
                        } else {
                            append(message)
                        }
                    }

                    appended++
                    if (appended == max) {
                        break
                    }
                }
            }
        }

        if (TextUtils.isEmpty(details)) {
            holder.tv_event_details.visibility = View.GONE
        } else {
            holder.tv_event_details.text = details
        }
    }
}
