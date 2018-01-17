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
package com.github.pockethub.android.ui;

import com.github.pockethub.android.ui.item.LoadingItem;
import com.xwray.groupie.Section;

/**
 * Helper for showing more items are being loaded at the bottom of a list via a
 * custom footer view
 */
public class ResourceLoadingIndicator {

    private Section section;

    private boolean showing;

    private final LoadingItem loadingItem;

    /**
     * Create indicator using given inflater.
     *
     * @param loadingResId
     *            string resource id to show when loading
     */
    public ResourceLoadingIndicator(final int loadingResId) {
        loadingItem = new LoadingItem(loadingResId);
    }

    /**
     * Set the adapter that this indicator should be added as a footer to.
     *
     * @param section
     * @return this indicator
     */
    public ResourceLoadingIndicator setSection(final Section section) {
        this.section = section;
        return this;
    }

    /**
     * Set visibility of entire indicator view.
     *
     * @param visible
     * @return this indicator
     */
    public ResourceLoadingIndicator setVisible(final boolean visible) {
        if (showing != visible && section != null) {
            if (visible) {
                section.setFooter(loadingItem);
            } else {
                section.removeFooter();
            }
        }
        showing = visible;
        return this;
    }
}
