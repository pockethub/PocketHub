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
package com.github.pockethub.ui.commit;

import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.github.pockethub.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Styler for the file diffs introduced in a commit
 */
public class DiffStyler {

    private final Map<String, List<CharSequence>> diffs = new HashMap<>();

    private final int markerColor;

    private final int defaultColor;

    /**
     * Create diff styler
     *
     * @param resources
     */
    public DiffStyler(final Resources resources) {
        markerColor = resources.getColor(R.color.diff_marker_text);
        defaultColor = resources.getColor(R.color.text);
    }

    private int nextLine(final String patch, final int start, final int length) {
        final int end = patch.indexOf('\n', start);
        if (end != -1)
            return end;
        else
            return length;
    }

    /**
     * Style view for line
     *
     * @param line
     * @param view
     */
    public void updateColors(final CharSequence line, final TextView view) {
        if (TextUtils.isEmpty(line)) {
            view.setBackgroundResource(R.drawable.list_item_background);
            view.setTextColor(defaultColor);
            return;
        }

        switch (line.charAt(0)) {
        case '@':
            view.setBackgroundResource(R.drawable.diff_marker_background);
            view.setTextColor(markerColor);
            return;
        case '+':
            view.setBackgroundResource(R.drawable.diff_add_background);
            view.setTextColor(defaultColor);
            return;
        case '-':
            view.setBackgroundResource(R.drawable.diff_remove_background);
            view.setTextColor(defaultColor);
            return;
        default:
            view.setBackgroundResource(R.drawable.list_item_background);
            view.setTextColor(defaultColor);
        }
    }

    /**
     * Set files to styler
     *
     * @param files
     * @return this styler
     */
    public DiffStyler setFiles(final Collection<CommitFile> files) {
        diffs.clear();
        if (files == null || files.isEmpty())
            return this;

        for (CommitFile file : files) {
            String patch = file.patch;
            if (TextUtils.isEmpty(patch))
                continue;

            int start = 0;
            int length = patch.length();
            int end = nextLine(patch, start, length);
            List<CharSequence> lines = new ArrayList<>();
            while (start < length) {
                lines.add(patch.substring(start, end));
                start = end + 1;
                end = nextLine(patch, start, length);
            }
            diffs.put(file.filename, lines);
        }
        return this;
    }

    /**
     * Get lines for file path
     *
     * @param file
     * @return styled text
     */
    public List<CharSequence> get(final String file) {
        if (TextUtils.isEmpty(file))
            return Collections.emptyList();
        List<CharSequence> lines = diffs.get(file);
        return lines != null ? lines : Collections.<CharSequence>emptyList();
    }
}
