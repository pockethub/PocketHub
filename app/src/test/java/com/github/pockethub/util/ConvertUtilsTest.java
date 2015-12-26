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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConvertUtilsTest {

	private static final String REPO_NAME_FIRST_PART = "first";
	private static final String REPO_NAME_SECOND_PART = "second";
	private static final String REPO_NAME = REPO_NAME_FIRST_PART + "/" + REPO_NAME_SECOND_PART;
	private static final String REPO_OWNER_LOGIN = "repo_owner_login";

	private Repo repo;

	@Before
	public void setup() {
		repo = new Repo();
		repo.name = REPO_NAME;
		repo.owner = new User();
		repo.owner.login = REPO_OWNER_LOGIN;
	}

	@Test
	public void testOriginalRepoIsNotChanged() throws Exception {
		ConvertUtils.eventRepoToRepo(repo);
		assertThat(repo.owner.login, equalTo(REPO_OWNER_LOGIN));
		assertThat(repo.name, equalTo(REPO_NAME));
	}

	@Test
	public void testNewRepoIsCreated() throws Exception {
		Repo newRepo = ConvertUtils.eventRepoToRepo(repo);
		assertThat(newRepo.owner.login, equalTo(REPO_NAME_FIRST_PART));
		assertThat(newRepo.name, equalTo(REPO_NAME_SECOND_PART));
	}

}
