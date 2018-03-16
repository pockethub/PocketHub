package com.github.pockethub.android.ui.item.repository

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.StyledText
import com.github.pockethub.android.ui.item.BaseDataItem
import com.github.pockethub.android.ui.item.BaseViewHolder
import com.github.pockethub.android.ui.view.OcticonTextView.*
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User

class RepositoryItem(dataItem: Repository, private val user: User?) : BaseDataItem<Repository, RepositoryItem.ViewHolder>(null, dataItem, dataItem.id()!!) {

    private var descriptionColor = -1

    override fun getId() = data.id()!!

    override fun getLayout() = R.layout.user_repo_item

    override fun createViewHolder(itemView: View) = ViewHolder(itemView)

    override fun bind(holder: ViewHolder, position: Int) {
        val repo = data

        if (descriptionColor == -1) {
            descriptionColor = holder.root.resources.getColor(R.color.text_description)
        }

        val name = StyledText()
        if (user == null) {
            name.append(repo.owner()!!.login()).append('/')
        } else {
            if (user.login() != repo.owner()!!.login()) {
                name.foreground(repo.owner()!!.login(), descriptionColor)
                        .foreground('/', descriptionColor)
            }
        }

        name.bold(repo.name())
        holder.name.text = name

        if (TextUtils.isEmpty(repo.mirrorUrl())) {
            when {
                repo.isPrivate!! -> holder.icon.text = ICON_PRIVATE
                repo.isFork!! -> holder.icon.text = ICON_FORK
                else -> holder.icon.text = ICON_PUBLIC
            }
        } else {
            if (repo.isPrivate!!) {
                holder.icon.text = ICON_MIRROR_PRIVATE
            } else {
                holder.icon.text = ICON_MIRROR_PUBLIC
            }
        }

        if (!TextUtils.isEmpty(repo.description())) {
            holder.description.text = repo.description()
            holder.description.visibility = View.VISIBLE
        } else {
            holder.description.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(repo.language())) {
            holder.language.text = repo.language()
            holder.language.visibility = View.VISIBLE
        } else {
            holder.language.visibility = View.GONE
        }

        holder.watchers.text = repo.watchersCount().toString()
        holder.forks.text = repo.forksCount().toString()
    }

    inner class ViewHolder(rootView: View) : BaseViewHolder(rootView) {

        @BindView(R.id.tv_repo_icon)
        lateinit var icon: TextView

        @BindView(R.id.tv_repo_description)
        lateinit var description: TextView

        @BindView(R.id.tv_language)
        lateinit var language: TextView

        @BindView(R.id.tv_watchers)
        lateinit var watchers: TextView

        @BindView(R.id.tv_forks)
        lateinit var forks: TextView

        @BindView(R.id.tv_repo_name)
        lateinit var name: TextView
    }
}
