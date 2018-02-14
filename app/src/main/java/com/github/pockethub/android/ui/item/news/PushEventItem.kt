package com.github.pockethub.android.ui.item.news

import android.text.TextUtils
import android.view.View
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.GitHubEvent
import com.meisolsson.githubsdk.model.payload.PushPayload
import java.text.NumberFormat

class PushEventItem(avatarLoader: AvatarLoader, dataItem: GitHubEvent) : NewsItem(avatarLoader, dataItem) {

    override fun bind(viewHolder: NewsItem.ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.icon.text = OcticonTextView.ICON_PUSH

        val payload = data.payload() as PushPayload?

        val main = StyledText()
        boldActor(main, data)

        main.append(" pushed to ")
        var ref = payload?.ref()
        if (ref!!.startsWith("refs/heads/")) {
            ref = ref.substring(11)
        }
        main.bold(ref)
        main.append(" at ")

        boldRepo(main, data)
        viewHolder.event.text = main

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
            viewHolder.details.visibility = View.GONE
        } else {
            viewHolder.details.text = details
        }
    }
}
