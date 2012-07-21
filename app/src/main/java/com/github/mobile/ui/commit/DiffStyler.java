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
package com.github.mobile.ui.commit;

import android.content.res.Resources;
import android.text.TextUtils;

import com.actionbarsherlock.R.color;
import com.github.mobile.ui.StyledText;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.CommitFile;

/**
 * Styler for the file diffs introduced in a commit
 */
public class DiffStyler {

    private final Map<String, CharSequence> diffs = new HashMap<String, CharSequence>();

    private final int markerColor;

    private final int addColor;

    private final int removeColor;

    /**
     * Create diff styler
     *
     * @param resources
     */
    public DiffStyler(final Resources resources) {
        markerColor = resources.getColor(color.diff_marker);
        addColor = resources.getColor(color.diff_add);
        removeColor = resources.getColor(color.diff_remove);
    }

    /**
     * Set files to style
     *
     * @param files
     * @return this styler
     */
    public DiffStyler setFiles(final Collection<CommitFile> files) {
        diffs.clear();
        if (files == null || files.isEmpty())
            return this;

        for (CommitFile file : files) {
            String patch = file.getPatch();
            if (TextUtils.isEmpty(patch))
                continue;

            int start = 0;
            int end = patch.indexOf('\n');
            StyledText styled = new StyledText();
            while (end != -1) {
                String line = patch.substring(start, end + 1);
                switch (patch.charAt(start)) {
                case '@':
                    styled.foreground(line, markerColor);
                    break;
                case '+':
                    styled.foreground(line, addColor);
                    break;
                case '-':
                    styled.foreground(line, removeColor);
                    break;
                default:
                    styled.append(line);
                    break;
                }
                diffs.put(file.getFilename(), styled);
                start = end + 1;
                end = patch.indexOf('\n', start);
            }
        }
        return this;
    }

    /**
     * Get styled text for file path
     *
     * @param file
     * @return styled text
     */
    public CharSequence get(final String file) {
        return diffs.get(file);
    }
}
