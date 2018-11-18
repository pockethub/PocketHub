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
package com.github.pockethub.android.ui.gist

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.github.pockethub.android.Intents.EXTRA_GIST_FILE
import com.github.pockethub.android.Intents.EXTRA_GIST_ID
import com.github.pockethub.android.R
import com.github.pockethub.android.core.gist.GistStore
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.util.PreferenceUtils
import com.github.pockethub.android.util.PreferenceUtils.WRAP
import com.github.pockethub.android.util.SourceEditor
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.model.Gist
import com.meisolsson.githubsdk.model.GistFile
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_commit_file_view.*
import java.io.IOException
import javax.inject.Inject

/**
 * Fragment to display the content of a file in a Gist
 */
class GistFileFragment : BaseFragment(), OnSharedPreferenceChangeListener {

    @Inject
    lateinit var store: GistStore

    private var gistId: String? = null

    private var file: GistFile? = null

    private var gist: Gist? = null

    private lateinit var editor: SourceEditor

    private var codePrefs: SharedPreferences? = null

    private var wrapItem: MenuItem? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        gistId = getStringExtra(EXTRA_GIST_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        file = arguments!!.get(EXTRA_GIST_FILE) as GistFile
        gist = store.getGist(gistId)
        if (gist == null) {
            gist = Gist.builder().id(gistId).build()
        }

        codePrefs = PreferenceUtils.getCodePreferences(activity)
        codePrefs!!.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        codePrefs!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_code_view, menu)

        wrapItem = menu.findItem(R.id.m_wrap)
        updateWrapItem()
    }

    private fun updateWrapItem() {
        if (wrapItem != null) {
            if (codePrefs!!.getBoolean(WRAP, false)) {
                wrapItem!!.setTitle(R.string.disable_wrapping)
            } else {
                wrapItem!!.setTitle(R.string.enable_wrapping)
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.m_wrap -> {
                if (editor.wrap) {
                    item.setTitle(R.string.enable_wrapping)
                    editor.wrap = false
                } else {
                    item.setTitle(R.string.disable_wrapping)
                    editor.wrap = true
                }
                PreferenceUtils.save(codePrefs!!.edit().putBoolean(WRAP,
                    editor.wrap))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSource() {
        store.refreshGist(gistId)
            .map { gist ->
                val files = gist.files() ?: throw IOException()

                val loadedFile = files[file!!.filename()] ?: throw IOException()

                loadedFile
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ loadedFile ->
                file = loadedFile
                arguments!!.putParcelable(EXTRA_GIST_FILE, file)
                if (file!!.content() != null) {
                    showSource()
                }
            }, { e -> ToastUtils.show(activity, R.string.error_gist_file_load) })
    }

    private fun showSource() {
        editor.setSource(file!!.filename(), file!!.content(), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_gist_file_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editor = SourceEditor(wv_code)
        editor.wrap = PreferenceUtils.getCodePreferences(activity).getBoolean(WRAP, false)

        if (file!!.content() != null) {
            showSource()
        } else {
            loadSource()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (WRAP == key) {
            updateWrapItem()
            editor.wrap = sharedPreferences.getBoolean(WRAP, false)
        }
    }
}
