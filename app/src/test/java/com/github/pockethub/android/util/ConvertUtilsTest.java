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

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConvertUtilsTest {

	private static final String REPO_NAME_FIRST_PART = "first";
	private static final String REPO_NAME_SECOND_PART = "second";
	private static final String REPO_NAME = REPO_NAME_FIRST_PART + "/" + REPO_NAME_SECOND_PART;

	private GitHubEvent event;

	@Before
	public void setup() {

		event = GitHubEvent.builder()
                .repo(new GitHubEvent.RepoIdentifier() {
                    @Nullable
                    @Override
                    public Long id() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public String url() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public String repoWithUserName() {
                        return REPO_NAME;
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel parcel, int i) {

                    }
                })
                .build();
	}

	@Test
	public void testOriginalRepoIsNotChanged() throws Exception {
		ConvertUtils.eventRepoToRepo(event.repo());
		assertThat(event.repo().repoWithUserName(), equalTo(REPO_NAME));
	}

	@Test
	public void testNewRepoIsCreated() throws Exception {
		Repository newRepo = ConvertUtils.eventRepoToRepo(event.repo());
		assertThat(newRepo.owner().login(), equalTo(REPO_NAME_FIRST_PART));
		assertThat(newRepo.name(), equalTo(REPO_NAME_SECOND_PART));
	}

}
