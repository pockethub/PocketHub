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
package com.github.pockethub.core.code;

import android.accounts.Account;
import android.content.Context;

import com.alorma.github.sdk.bean.dto.response.GitBlob;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.git.GetGitBlobClient;
import com.github.pockethub.accounts.AuthenticatedUserTask;
import com.github.pockethub.util.InfoUtils;

/**
 * Task to refresh a blob
 */
public class RefreshBlobTask extends AuthenticatedUserTask<GitBlob> {

    private final Repo repository;

    private final String blobSha;

    /**
     * @param repository
     * @param blobSha
     * @param context
     */
    public RefreshBlobTask(Repo repository, String blobSha,
            Context context) {
        super(context);

        this.repository = repository;
        this.blobSha = blobSha;
    }

    @Override
    protected GitBlob run(Account account) throws Exception {
        return new GetGitBlobClient(InfoUtils.createCommitInfo(repository, blobSha)).observable().toBlocking().first();
    }
}
