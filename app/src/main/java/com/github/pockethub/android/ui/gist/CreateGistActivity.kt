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

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import com.github.pockethub.android.R
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.TextWatcherAdapter
import com.github.pockethub.android.util.ShareUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.GistFile
import com.meisolsson.githubsdk.model.request.gist.CreateGist
import com.meisolsson.githubsdk.service.gists.GistService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gist_create.*
import java.util.HashMap

/**
 * Activity to share a text selection as a public or private Gist
 */
class CreateGistActivity : BaseActivity() {

    private var menuItem: MenuItem? = null

    private val expandAppBarOnFocusChangeListener =
        OnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (hasFocus) {
                appbar.setExpanded(true)
            }
        }

    private val expandAppBarOnChange = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            appbar.setExpanded(true)
        }
    }

    private val onContentTextChange = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateCreateMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gist_create)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val text = ShareUtils.getBody(intent)
        if (!TextUtils.isEmpty(text)) {
            et_gist_content.setText(text)
        }

        val subject = ShareUtils.getSubject(intent)
        if (!TextUtils.isEmpty(subject)) {
            et_gist_description.setText(subject)
        }

        et_gist_description.onFocusChangeListener = expandAppBarOnFocusChangeListener
        et_gist_name.onFocusChangeListener = expandAppBarOnFocusChangeListener
        cb_public.onFocusChangeListener = expandAppBarOnFocusChangeListener

        cb_public.setOnCheckedChangeListener { buttonView, isChecked -> appbar.setExpanded(true) }

        et_gist_description.addTextChangedListener(expandAppBarOnChange)
        et_gist_name.addTextChangedListener(expandAppBarOnChange)
        cb_public.addTextChangedListener(expandAppBarOnChange)

        et_gist_content.addTextChangedListener(onContentTextChange)

        updateCreateMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        et_gist_description.removeTextChangedListener(expandAppBarOnChange)
        et_gist_name.removeTextChangedListener(expandAppBarOnChange)
        cb_public.removeTextChangedListener(expandAppBarOnChange)

        et_gist_content.removeTextChangedListener(onContentTextChange)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_create_gist, menu)
        menuItem = menu.findItem(R.id.create_gist)
        updateCreateMenu()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.create_gist -> {
                createGist()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateCreateMenu(text: CharSequence = et_gist_content.text) {
        if (menuItem != null) {
            menuItem!!.isEnabled = !TextUtils.isEmpty(text)
        }
    }

    private fun createGist() {
        val isPublic = cb_public.isChecked

        val enteredDescription = et_gist_description.text.toString().trim { it <= ' ' }
        val description = if (enteredDescription.isNotEmpty())
            enteredDescription
        else
            getString(R.string.gist_description_hint)

        val enteredName = et_gist_name.text.toString().trim { it <= ' ' }
        val name = if (enteredName.isNotEmpty())
            enteredName
        else
            getString(R.string.gist_file_name_hint)

        val content = et_gist_content.text.toString()
        val map = HashMap<String, GistFile>()
        map[name] = GistFile.builder().filename(name).content(content).build()

        val createGist = CreateGist.builder()
            .files(map)
            .description(description)
            .isPublic(isPublic)
            .build()

        ServiceGenerator.createService(this, GistService::class.java)
            .createGist(createGist)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(RxProgress.bindToLifecycle(this, R.string.creating_gist))
            .`as`(AutoDisposeUtils.bindToLifecycle(this))
            .subscribe({ response ->
                startActivity(GistsViewActivity.createIntent(response.body()))
                setResult(RESULT_OK)
                finish()
            }, { e ->
                Log.d(TAG, "Exception creating Gist", e)
                ToastUtils.show(this, e.message)
            })
    }

    companion object {

        private val TAG = "CreateGistActivity"
    }
}
