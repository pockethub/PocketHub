/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.ui.commit

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY
import com.github.pockethub.android.ItemListHandler
import com.github.pockethub.android.PagedListFetcher
import com.github.pockethub.android.PagedScrollListener
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.COMMIT_VIEW
import com.github.pockethub.android.RequestCodes.REF_UPDATE
import com.github.pockethub.android.core.commit.CommitStore
import com.github.pockethub.android.core.ref.RefUtils
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.DialogResultListener
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.ui.item.commit.CommitItem
import com.github.pockethub.android.ui.ref.RefDialog
import com.github.pockethub.android.ui.ref.RefDialogFragment
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Commit
import com.meisolsson.githubsdk.model.Page
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.git.GitReference
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService
import com.meisolsson.githubsdk.service.repositories.RepositoryService
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_commit_list.view.*
import kotlinx.android.synthetic.main.ref_footer.view.*
import retrofit2.Response
import javax.inject.Inject

/**
 * Fragment to display a list of repo commits
 */
class CommitListFragment : BaseFragment(), DialogResultListener {

    @Inject
    protected lateinit var service: RepositoryCommitService

    @Inject
    protected lateinit var repoService: RepositoryService

    /**
     * Avatar loader
     */
    @Inject
    protected lateinit var avatars: AvatarLoader

    @Inject
    protected lateinit var store: CommitStore

    protected lateinit var pagedListFetcher: PagedListFetcher<Commit>

    protected lateinit var itemListHandler: ItemListHandler

    protected lateinit var pagedScrollListener: PagedScrollListener

    private var repo: Repository? = null

    private var dialog: RefDialog? = null

    private var ref: String? = null

    protected val loadingMessage: Int
        get() = R.string.loading_commits

    protected val errorMessage: Int
        get() = R.string.error_commits_load

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        val activity = context as Activity?
        repo = activity!!.intent.getParcelableExtra(EXTRA_REPOSITORY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_commit_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemListHandler = ItemListHandler(
            view.list,
            view.empty,
            lifecycle,
            activity,
            OnItemClickListener(this::onItemClick)
        )

        pagedListFetcher = PagedListFetcher(
            view.swipe_item,
            lifecycle,
            itemListHandler,
            { t -> ToastUtils.show(activity, errorMessage)},
            this::loadData,
            this::createItem
        )
        pagedListFetcher.onPageLoaded = this::onPageLoaded

        pagedScrollListener = PagedScrollListener(
            itemListHandler.mainSection,
            pagedListFetcher,
            view.list,
            loadingMessage
        )
        itemListHandler.setEmptyText(R.string.no_commits)

        view.rl_branch.setOnClickListener { switchRefs()}
        view.rl_branch.visibility = View.VISIBLE
    }

    private fun loadData(page: Int): Single<Response<Page<Commit>>> {
        val refSingle: Single<String>
        if (TextUtils.isEmpty(ref)) {
            val defaultBranch = repo!!.defaultBranch()
            refSingle = if (TextUtils.isEmpty(defaultBranch)) {
                repoService.getRepository(repo!!.owner()!!.login(), repo!!.name())
                    .map { it.body().defaultBranch() }
                    .map {
                        return@map if (TextUtils.isEmpty(it)) {
                            "master"
                        } else {
                            it
                        }
                    }
            } else {
                Single.just(defaultBranch!!)
            }
        } else {
            refSingle = Single.just(ref!!)
        }

        return refSingle
            .map { ref ->
                this@CommitListFragment.ref = ref
                ref
            }
            .flatMap { branch ->
                service.getCommits(repo!!.owner()!!.login(), repo!!.name(), branch, page.toLong())
            }
    }

    protected fun createItem(dataItem: Commit): Item<*> {
        return CommitItem(avatars, dataItem)
    }

    protected fun onPageLoaded(items: MutableList<Item<*>>): MutableList<Item<*>> {
        if (ref != null) {
            updateRefLabel()
        }

        return items
    }

    fun onItemClick(item: Item<*>, view: View) {
        if (item is CommitItem) {
            val position = itemListHandler.getItemPosition(item)
            startActivityForResult(
                CommitViewActivity.createIntent(repo!!, position, itemListHandler.items),
                COMMIT_VIEW
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == COMMIT_VIEW) {
            itemListHandler.mainSection.notifyChanged()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        if (RESULT_OK != resultCode) {
            return
        }

        when (requestCode) {
            REF_UPDATE -> setRef(RefDialogFragment.getSelected(arguments))
        }
    }

    private fun updateRefLabel() {
        view!!.tv_branch.text = RefUtils.getName(ref)
        if (RefUtils.isTag(ref)) {
            view!!.tv_branch_icon.setText(R.string.icon_tag)
        } else {
            view!!.tv_branch_icon.setText(R.string.icon_fork)
        }
    }

    private fun setRef(ref: GitReference) {
        this.ref = ref.ref()
        updateRefLabel()
        pagedListFetcher.refresh()
    }

    fun switchRefs() {
        if (ref == null) {
            return
        }

        if (dialog == null) {
            dialog = RefDialog(activity as BaseActivity?,
                REF_UPDATE, repo)
        }
        val reference = GitReference.builder()
            .ref(ref)
            .build()
        dialog!!.show(reference)
    }
}
