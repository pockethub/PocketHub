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
package com.github.pockethub.android.ui.comment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

import com.github.pockethub.android.R
import com.github.pockethub.android.ui.MarkdownLoader
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.util.HttpImageGetter
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Repository

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comment_preview.*

/**
 * Fragment to display rendered comment fragment
 */
class RenderedCommentFragment : BaseFragment() {

    @Inject
    lateinit var imageGetter: HttpImageGetter

    /**
     * Set text to render
     *
     * @param raw
     * @param repo
     */
    fun setText(raw: String, repo: Repository?) {
        loadMarkdown(raw, repo)
        hideSoftKeyboard()
        showLoading(true)
    }

    private fun hideSoftKeyboard() {
        val context = context
        if (context != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(tv_comment_body.windowToken, 0)
        }
    }

    private fun showLoading(loading: Boolean) {
        if (loading) {
            pb_loading.visibility = View.VISIBLE
            tv_comment_body.visibility = View.GONE
        } else {
            pb_loading.visibility = View.GONE
            tv_comment_body.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_comment_preview, null)
    }

    private fun loadMarkdown(raw: String, repo: Repository?) {
        MarkdownLoader.load(activity, raw, repo, imageGetter, true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ rendered ->
                tv_comment_body!!.text = rendered
                showLoading(false)
            }, { e -> ToastUtils.show(activity, R.string.error_rendering_markdown) })
    }

    companion object {

        private val ARG_TEXT = "text"

        private val ARG_REPO = "repo"
    }
}
