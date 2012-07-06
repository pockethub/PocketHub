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
package com.github.mobile.core.repo;

import org.eclipse.egit.github.core.Repository;

/**
 * Utilities for working with {@link Repository} objects
 */
public class RepositoryUtils {

    /**
     * Does the repository have details denoting it was loaded from an API call?
     * <p>
     * This uses a simple heuristic of either being a fork or having a non-zero
     * amount of forks meaning it came back from an API call providing those
     * details and more.
     *
     * @param repository
     * @return true if complete, false otherwise
     *
     */
    public static boolean isComplete(Repository repository) {
        return repository.isFork() || repository.getForks() != 0;
    }
}
