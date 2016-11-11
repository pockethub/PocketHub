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

package com.github.pockethub.android.util;

import com.meisolsson.githubsdk.model.Repository;

public class ConvertUtils {

	/**
	 * Converts an event repo to a new, clean repo.
	 *
	 * @param repo The original repo.
	 * @return A new repo, with a name and a new owner's login.
	 */
	public static Repository eventRepoToRepo(Repository repo) {
		String[] ref = repo.name().split("/");
		return InfoUtils.createRepoFromData(ref[0], ref[1]);
	}
}
