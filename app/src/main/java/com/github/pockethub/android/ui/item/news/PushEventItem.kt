package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
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

        val main = StyledText()
        boldActor(main, gitHubEvent)

        main.append(" pushed to ")
        var ref = payload?.ref()
        if (ref!!.startsWith("refs/heads/")) {
            ref = ref.substring(11)
        }
        main.bold(ref)
        main.append(" at ")

        boldRepo(main, gitHubEvent)
        holder.tv_event.text = main

        val details = StyledText()
        val commits = payload?.commits()
        val size = commits!!.size
        if (size > 0) {
            if (size != 1) {
                val numberFormat = NumberFormat.getIntegerInstance()
                details.append(numberFormat.format(size.toLong())).append(" new commits")
            } else {
                details.append("1 new commit")
            }

            val max = 3
            var appended = 0
            for (commit in commits) {

                val sha = commit.sha()

                details.append('\n')
                if (sha!!.length > 7) {
                    details.monospace(sha.substring(0, 7))
                } else {
                    details.monospace(sha)
                }

                val message = commit.message()
                if (!TextUtils.isEmpty(message)) {
                    details.append(' ')
                    val newline = message!!.indexOf('\n')
                    if (newline > 0) {
                        details.append(message.subSequence(0, newline))
                    } else {
                        details.append(message)
                    }
                }

                appended++
                if (appended == max) {
                    break
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
