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

import static java.lang.String.CASE_INSENSITIVE_ORDER;

import java.util.Comparator;

import org.eclipse.egit.github.core.CommitFile;

/**
 * Comparator for commit files
 */
public class CommitFileComparator implements Comparator<CommitFile> {

    @Override
    public int compare(final CommitFile lhs, final CommitFile rhs) {
        String lPath = lhs.getFilename();
        final int lSlash = lPath.lastIndexOf('/');
        if (lSlash != -1)
            lPath = lPath.substring(lSlash + 1);

        String rPath = rhs.getFilename();
        final int rSlash = rPath.lastIndexOf('/');
        if (rSlash != -1)
            rPath = rPath.substring(rSlash + 1);

        return CASE_INSENSITIVE_ORDER.compare(lPath, rPath);
    }
}
