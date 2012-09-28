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
package com.github.mobile.core.code;

import android.accounts.Account;
import android.content.Context;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.DataService;

/**
 * Task to refresh a blob
 */
public class RefreshBlobTask extends AuthenticatedUserTask<Blob> {

    private final Repository repository;

    private final String blobSha;

    @Inject
    private DataService service;

    /**
     * @param repository
     * @param blobSha
     * @param context
     */
    public RefreshBlobTask(Repository repository, String blobSha,
            Context context) {
        super(context);

        this.repository = repository;
        this.blobSha = blobSha;
    }

    @Override
    protected Blob run(Account account) throws Exception {
        return service.getBlob(repository, blobSha);
    }
}
