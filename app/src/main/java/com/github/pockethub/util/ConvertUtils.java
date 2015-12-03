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
