/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.gist;

import static android.graphics.Typeface.ITALIC;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.TimeUtils;
import com.madgag.android.listviews.ViewHolder;

import java.util.Date;

import org.eclipse.egit.github.core.Gist;

/**
 * Holder for a Gist header view
 */
public class GistHeaderViewHolder implements ViewHolder<Gist> {

    private static final SpannableStringBuilder NO_DESCRIPTION;

    static {
        NO_DESCRIPTION = new SpannableStringBuilder("No description");
        NO_DESCRIPTION.setSpan(new StyleSpan(ITALIC), 0, NO_DESCRIPTION.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private final TextView created;

    private final TextView updated;

    private final TextView description;

    /**
     * Create view holder
     *
     * @param view
     */
    public GistHeaderViewHolder(final View view) {
        created = (TextView) view.findViewById(id.tv_gist_creation);
        updated = (TextView) view.findViewById(id.tv_gist_updated);
        description = (TextView) view.findViewById(id.tv_gist_description);
    }

    public void updateViewFor(Gist gist) {
        Date createdAt = gist.getCreatedAt();
        if (createdAt != null) {
            created.setText("Created " + TimeUtils.getRelativeTime(createdAt));
            created.setVisibility(VISIBLE);
        } else
            created.setVisibility(GONE);

        Date updatedAt = gist.getUpdatedAt();
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            updated.setText("Updated " + TimeUtils.getRelativeTime(updatedAt));
            updated.setVisibility(VISIBLE);
        } else
            updated.setVisibility(GONE);

        String desc = gist.getDescription();
        if (!TextUtils.isEmpty(desc))
            description.setText(desc);
        else
            description.setText(NO_DESCRIPTION);
    }
}
