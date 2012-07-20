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
package com.github.mobile.core.commit;

import android.content.Context;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;

/**
 * Task to load a commit by SHA-1 id
 */
public class RefreshCommitTask extends AuthenticatedUserTask<RepositoryCommit> {

    @Inject
    private CommitService service;

    private final IRepositoryIdProvider repository;

    private final String id;

    /**
     * @param context
     * @param repository
     * @param id
     */
    public RefreshCommitTask(Context context, IRepositoryIdProvider repository,
            String id) {
        super(context);

        this.repository = repository;
        this.id = id;
    }

    @Override
    protected RepositoryCommit run() throws Exception {
        return service.getCommit(repository, id);
    }
}
