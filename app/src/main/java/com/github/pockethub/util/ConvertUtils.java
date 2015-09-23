package com.github.pockethub.util;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;

public class ConvertUtils {
    public static Repo eventRepoToRepo(Repo repo) {
        String[] ref = repo.name.split("/");
        repo.owner = new User();
        repo.owner.login = ref[0];
        repo.name = ref[1];
        return repo;
    }
}
