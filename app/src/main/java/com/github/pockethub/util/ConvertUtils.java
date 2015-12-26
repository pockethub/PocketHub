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

package com.github.pockethub.util;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;

public class ConvertUtils {

	/**
	 * Converts an event repo to a new, clean repo.
	 *
	 * @param repo The original repo.
	 * @return A new repo, with a name and a new owner's login.
	 */
	public static Repo eventRepoToRepo(Repo repo) {
		Repo newRepo = new Repo();
		String[] ref = repo.name.split("/");
		newRepo.owner = new User();
		newRepo.owner.login = ref[0];
		newRepo.name = ref[1];
		return newRepo;
	}
}
