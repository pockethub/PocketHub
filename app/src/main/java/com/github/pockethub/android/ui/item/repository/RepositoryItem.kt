package com.github.pockethub.android.ui.item.repository

import android.text.TextUtils
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_FORK
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_MIRROR_PRIVATE
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_MIRROR_PUBLIC
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_PRIVATE
import com.github.pockethub.android.ui.view.OcticonTextView.ICON_PUBLIC
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.repo_details.*
import kotlinx.android.synthetic.main.user_repo_item.*

class RepositoryItem(val repo: Repository, private val user: User?) : Item(repo.id()!!) {

    private var descriptionColor = -1

    override fun getId() = repo.id()!!

    override fun getLayout() = R.layout.user_repo_item

    override fun bind(holder: ViewHolder, position: Int) {
        if (descriptionColor == -1) {
            descriptionColor = holder.root.resources.getColor(R.color.text_description)
        }

        holder.tv_repo_name.text = buildSpannedString {
            if (user == null) {
                append(repo.owner()!!.login()).append('/')
            } else {
                if (user.login() != repo.owner()!!.login()) {
                    color(descriptionColor) {
                        append("${repo.owner()!!.login()}/")
                    }
                }
            }

            bold {
                append(repo.name())
            }
        }

        if (TextUtils.isEmpty(repo.mirrorUrl())) {
            when {
                repo.isPrivate!! -> holder.tv_repo_icon.text = ICON_PRIVATE
                repo.isFork!! -> holder.tv_repo_icon.text = ICON_FORK
                else -> holder.tv_repo_icon.text = ICON_PUBLIC
            }
        } else {
            if (repo.isPrivate!!) {
                holder.tv_repo_icon.text = ICON_MIRROR_PRIVATE
            } else {
                holder.tv_repo_icon.text = ICON_MIRROR_PUBLIC
            }
        }

        if (!TextUtils.isEmpty(repo.description())) {
            holder.tv_repo_description.text = repo.description()
            holder.tv_repo_description.visibility = View.VISIBLE
        } else {
            holder.tv_repo_description.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(repo.language())) {
            holder.tv_language.text = repo.language()
            holder.tv_language.visibility = View.VISIBLE
        } else {
            holder.tv_language.visibility = View.GONE
        }

        holder.tv_watchers.text = repo.watchersCount().toString()
        holder.tv_forks.text = repo.forksCount().toString()
    }
}
