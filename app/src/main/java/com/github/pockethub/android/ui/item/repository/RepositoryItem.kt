package com.github.pockethub.android.ui.item.repository

import android.support.v4.content.ContextCompat
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.view.OcticonTextView.*
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.repo_details.view.*
import kotlinx.android.synthetic.main.user_repo_item.view.*

class RepositoryItem(val repo: Repository, private val user: User?) : Item<RepositoryItem.ItemViewHolder>(repo.id()!!) {

    override fun bind(holder: ItemViewHolder, position: Int) {
        val name = StyledText()
        if (user == null) {
            name.append(repo.owner()!!.login()).append('/')
        } else {
            if (user.login() != repo.owner()!!.login()) {
                name.foreground(repo.owner()!!.login(), holder.descriptionColor)
                        .foreground('/', holder.descriptionColor)
            }
        }

        name.bold(repo.name())
        holder.root.tv_repo_name.text = name

        if (repo.mirrorUrl().isNullOrEmpty()) {
            when {
                repo.isPrivate!! -> holder.root.tv_repo_icon.text = ICON_PRIVATE
                repo.isFork!! -> holder.root.tv_repo_icon.text = ICON_FORK
                else -> holder.root.tv_repo_icon.text = ICON_PUBLIC
            }
        } else {
            if (repo.isPrivate!!) {
                holder.root.tv_repo_icon.text = ICON_MIRROR_PRIVATE
            } else {
                holder.root.tv_repo_icon.text = ICON_MIRROR_PUBLIC
            }
        }

        if (!repo.description().isNullOrEmpty()) {
            holder.root.tv_repo_description.text = repo.description()
            holder.root.tv_repo_description.visibility = View.VISIBLE
        } else {
            holder.root.tv_repo_description.visibility = View.GONE
        }

        if (!repo.language().isNullOrEmpty()) {
            holder.root.tv_language.text = repo.language()
            holder.root.tv_language.visibility = View.VISIBLE
        } else {
            holder.root.tv_language.visibility = View.GONE
        }

        holder.root.tv_watchers.text = repo.watchersCount().toString()
        holder.root.tv_forks.text = repo.forksCount().toString()
    }

    override fun getLayout() = R.layout.user_repo_item

    override fun getId() = repo.id()!!

    override fun createViewHolder(itemView: View) = ItemViewHolder(itemView)

    inner class ItemViewHolder(rootView: View) : ViewHolder(rootView) {

        val descriptionColor = ContextCompat.getColor(root.context, R.color.text_description)
    }
}
