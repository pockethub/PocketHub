package com.github.pockethub.android.ui.repo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.github.pockethub.android.Intents
import com.github.pockethub.android.R
import com.github.pockethub.android.markwon.FontResolver
import com.github.pockethub.android.markwon.MarkwonUtils
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService
import com.uber.autodispose.SingleSubscribeProxy
import io.noties.markwon.Markwon
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.recycler.SimpleEntry
import io.noties.markwon.recycler.table.TableEntry
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HtmlBlock
import retrofit2.Response

class RepositoryReadmeFragment : BaseFragment() {
    private lateinit var recyclerView: RecyclerView

    private var data: String? = null
    private var repo: Repository? = null
    private var markwon: Markwon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = requireActivity().intent.getParcelableExtra(Intents.EXTRA_REPOSITORY)
        val defaultBranch = if (repo!!.defaultBranch() == null) "master" else repo!!.defaultBranch()!!
        val baseString = String.format("https://github.com/%s/%s/%s/%s/", repo!!.owner()!!.login(), repo!!.name(), "%s", defaultBranch)
        SVG.registerExternalFileResolver(FontResolver(requireActivity().assets))
        markwon = MarkwonUtils.createMarkwon(requireContext(), baseString)

        loadReadMe()
    }

    private fun loadReadMe() {
        ServiceGenerator.createService(activity, RepositoryContentService::class.java)
                .getReadmeRaw(repo!!.owner()!!.login(), repo!!.name(), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`<SingleSubscribeProxy<Response<String?>>>(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe { response: Response<String?> ->
                    if (response.isSuccessful) {
                        setMarkdown(response.body())
                    } else {
                        ToastUtils.show(requireActivity(), R.string.error_rendering_markdown)
                    }
                }
    }

    private fun setMarkdown(body: String?) {
        data = body
        if (!this::recyclerView.isInitialized) {
            return
        }
        val adapter = MarkwonAdapter.builderTextViewIsRoot(R.layout.markwon_adapter)
                .include(HtmlBlock::class.java, SimpleEntry.createTextViewIsRoot(R.layout.markwon_adapter_test))
                .include(FencedCodeBlock::class.java, SimpleEntry.create(R.layout.adapter_fenced_code_block, R.id.text))
                .include(TableBlock::class.java, TableEntry.create { builder: TableEntry.Builder ->
                    builder
                            .tableLayout(R.layout.markwon_table_block, R.id.table_layout)
                            .textLayoutIsRoot(R.layout.markwon_table_cell)
                })
                .build()
        recyclerView.adapter = adapter
        adapter.setMarkdown(markwon!!, body!!)
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recyclerView = RecyclerView(inflater.context)
        recyclerView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        return recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view as RecyclerView
        if (data != null) {
            setMarkdown(data)
        }
    }
}