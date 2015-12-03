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
