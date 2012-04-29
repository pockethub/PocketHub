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
package com.github.mobile.issue;

import java.util.Comparator;

import org.eclipse.egit.github.core.Issue;

/**
 * Comparators that sort issue's by created at date
 */
public class CreatedAtComparator implements Comparator<Issue> {

    /**
     * Descending order
     */
    public static final int DESC = -1;

    /**
     * Ascending order
     */
    public static final int ASC = 1;

    private final int direction;

    /**
     * Create comparator in descending order
     */
    public CreatedAtComparator() {
        this(DESC);
    }

    /**
     * Create comparator
     *
     * @param direction
     */
    public CreatedAtComparator(final int direction) {
        this.direction = direction;
    }

    public int compare(Issue issue1, Issue issue2) {
        return issue1.getCreatedAt().compareTo(issue2.getCreatedAt()) * direction;
    }
}
