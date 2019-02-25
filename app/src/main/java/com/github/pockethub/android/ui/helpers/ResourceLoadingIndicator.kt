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
package com.github.pockethub.android.ui.helpers

import com.github.pockethub.android.ui.item.LoadingItem
import com.xwray.groupie.Section

/**
 * Helper for showing more items are being loaded at the bottom of a list via a
 * custom footer view
 *
 * @param loadingResId string resource id to show when loading
 * @param section the adapter that this indicator should be added as a footer to.
 */
class ResourceLoadingIndicator(loadingResId: Int, private val section: Section) {

    private val loadingItem = LoadingItem(loadingResId)

    /**
     * Visibility of entire indicator view.
     */
    var visible: Boolean = false
        set(value) {
            if (visible != value) {
                if (value) {
                    section.setFooter(loadingItem)
                } else {
                    section.removeFooter()
                }
                field = value
            }
        }
}
