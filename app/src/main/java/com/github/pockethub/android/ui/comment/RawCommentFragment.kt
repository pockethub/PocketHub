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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.pockethub.android.R
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.TextWatcherAdapter
import com.github.pockethub.android.ui.base.BaseFragment
import com.github.pockethub.android.util.ImageBinPoster
import com.github.pockethub.android.util.PermissionsUtils
import com.github.pockethub.android.util.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comment_create.*

/**
 * Fragment to display raw comment text
 */
class RawCommentFragment : BaseFragment() {

    /**
     * Text to populate comment window.
     */
    private var initComment: String? = null

    /**
     * Get comment text
     *
     * @return text
     */
    /**
     * Set comment text
     *
     * @return text
     */
    var text: String?
        get() {
            return et_comment.text.toString()
        }
        set(comment) = if (et_comment != null) {
            et_comment!!.setText(comment)
            et_comment!!.selectAll()
        } else {
            initComment = comment
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_add_image!!.setOnClickListener { v ->
            val fragment = this@RawCommentFragment
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                PermissionsUtils.askForPermission(
                    fragment,
                    READ_PERMISSION_REQUEST,
                    permission,
                    R.string.read_permission_title,
                    R.string.read_permission_content
                )
            } else {
                startImagePicker()
            }
        }

        et_comment.addTextChangedListener(object : TextWatcherAdapter() {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                activity?.invalidateOptionsMenu()
            }
        })
        et_comment.setOnTouchListener { v, event ->
            et_comment.requestFocusFromTouch()
            false
        }

        text = initComment
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PERMISSION_REQUEST) {

            var result = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    result = false
                }
            }

            if (result) {
                startImagePicker()
            }
        }
    }

    private fun startImagePicker() {
        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            ImageBinPoster.post(activity!!, data!!.data!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxProgress.bindToLifecycle(activity, R.string.loading))
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe({ response ->
                    if (response.isSuccessful) {
                        insertImage(ImageBinPoster.getUrl(response.body()!!.string()))
                    } else {
                        showImageError()
                    }
                }, { throwable -> showImageError() })
        }
    }

    private fun showImageError() {
        ToastUtils.show(activity, R.string.error_image_upload)
    }

    private fun insertImage(url: String?) {
        activity!!.runOnUiThread { et_comment.append("![]($url)") }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_comment_create, container, false)
    }

    companion object {

        private val REQUEST_CODE_SELECT_PHOTO = 0
        private val READ_PERMISSION_REQUEST = 1
    }
}
