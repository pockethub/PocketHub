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
package com.github.mobile.core.ref;

import android.text.TextUtils;

import org.eclipse.egit.github.core.Reference;

/**
 * Utilities for working with {@link Reference}s
 */
public class RefUtils {

    private static final String PREFIX_REFS = "refs/";

    private static final String PREFIX_TAG = "refs/tags/";

    private static final String PREFIX_HEADS = "refs/heads/";

    /**
     * Is reference a branch?
     *
     * @param ref
     * @return true if branch, false otherwise
     */
    public static boolean isBranch(final Reference ref) {
        if (ref != null) {
            String name = ref.getRef();
            return !TextUtils.isEmpty(name) && name.startsWith(PREFIX_HEADS);
        } else
            return false;
    }

    /**
     * Is reference a tag?
     *
     * @param ref
     * @return true if tag, false otherwise
     */
    public static boolean isTag(final Reference ref) {
        if (ref != null) {
            String name = ref.getRef();
            return !TextUtils.isEmpty(name) && name.startsWith(PREFIX_TAG);
        } else
            return false;
    }

    /**
     * Get path of ref with leading 'refs/' segment removed if present
     *
     * @param ref
     * @return full path
     */
    public static String getPath(final Reference ref) {
        if (ref == null)
            return null;
        String name = ref.getRef();
        if (!TextUtils.isEmpty(name) && name.startsWith(PREFIX_REFS))
            return name.substring(PREFIX_REFS.length());
        else
            return name;
    }

    /**
     * Get short name for ref
     *
     * @param ref
     * @return short name
     */
    public static String getName(final Reference ref) {
        if (ref == null)
            return null;
        String name = ref.getRef();
        if (TextUtils.isEmpty(name))
            return name;
        if (name.startsWith(PREFIX_HEADS))
            return name.substring(PREFIX_HEADS.length());
        else if (name.startsWith(PREFIX_TAG))
            return name.substring(PREFIX_TAG.length());
        else if (name.startsWith(PREFIX_REFS))
            return name.substring(PREFIX_REFS.length());
        else
            return name;
    }

    /**
     * Should the given reference be included as valid?
     * <p>
     * This filters out pull request refs
     *
     * @param ref
     * @return true if valid, false otherwise
     */
    public static boolean isValid(final Reference ref) {
        if (ref == null)
            return false;

        String name = ref.getRef();
        return !TextUtils.isEmpty(name) && !name.startsWith("refs/pull");
    }
}
