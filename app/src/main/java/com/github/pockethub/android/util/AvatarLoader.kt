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
package com.github.pockethub.android.util

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.pockethub.android.R
import com.meisolsson.githubsdk.model.User
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Avatar utilities
 *
 * @constructor Create avatar helper
 */
@Singleton
class AvatarLoader @Inject constructor(context: Context) {

    companion object {
        private const val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB

        private const val TAG = "AvatarLoader"

        private const val CORNER_RADIUS_IN_DIP = 3f

        /**
         * The max size of avatar images, used to rescale images to save memory.
         */
        private var avatarSize = 0
    }

    private val requestManager: RequestManager = Glide.with(context)

    private val cornerRadius: Int

    private val transformation: RoundedCorners

    init {

        val density = context.resources.displayMetrics.density
        cornerRadius = (CORNER_RADIUS_IN_DIP * density).toInt()
        transformation = RoundedCorners(cornerRadius)

        if (avatarSize == 0) {
            avatarSize = getMaxAvatarSize(context)
        }
    }

    /**
     * Bind view to image at URL
     *
     * @param view The ImageView that is to display the user's avatar.
     * @param user A User object that points to the desired user.
     */
    fun bind(view: ImageView, user: User?) {
        bind(view, getAvatarUrl(user))
    }

    private fun bind(view: ImageView, url: String?) {
        var localUrl = url
        if (localUrl == null) {
            requestManager.load(R.drawable.spinner_inner).override(avatarSize, avatarSize).into(view)
            return
        }

        if (localUrl.contains("?") && !localUrl.contains("gravatar")) {
            localUrl = localUrl.substring(0, localUrl.indexOf("?"))
        }

        requestManager.load(localUrl)
                .placeholder(R.drawable.gravatar_icon)
                .override(avatarSize, avatarSize)
                .transform(transformation)
                .into(view)
    }

    private fun getAvatarUrl(user: User?): String? {
        if (user == null) {
            return null
        }

        val avatarUrl = user.avatarUrl()
        val email = user.email()
        return if (avatarUrl != null && avatarUrl.isNotBlank()) {
            HttpUrl.parse(avatarUrl)
                ?.newBuilder()
                ?.addQueryParameter("size", avatarSize.toString())
                ?.build()
                ?.toString()
        } else if (email != null && email.isNotBlank()){
            getAvatarUrl(GravatarUtils.getHash(user.email()))
        } else {
            // This redirects to avatar URL
            "http://github.com/${user.login()}.png?size=$avatarSize"
        }
    }

    private fun getAvatarUrl(id: String?): String? {
        return if (!id.isNullOrEmpty()) "http://gravatar.com/avatar/$id?d=404" else null
    }

    private fun getMaxAvatarSize(context: Context): Int {
        val attrs = intArrayOf(android.R.attr.layout_height)
        val array = context.theme.obtainStyledAttributes(R.style.AvatarXLarge, attrs)
        // Passing default value of 100px, but it shouldn't resolve to default anyway.
        val size = array.getLayoutDimension(0, 100)
        array.recycle()
        return size
    }
}
