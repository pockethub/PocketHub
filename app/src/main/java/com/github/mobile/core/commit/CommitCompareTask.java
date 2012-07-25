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

import android.accounts.Account;
import android.content.Context;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryCommitCompare;
import org.eclipse.egit.github.core.service.CommitService;

/**
 * Task to compare two commits
 */
public class CommitCompareTask extends
        AuthenticatedUserTask<RepositoryCommitCompare> {

    @Inject
    private CommitService service;

    private final IRepositoryIdProvider repository;

    private final String base;

    private final String head;

    /**
     * @param context
     * @param repository
     * @param base
     * @param head
     */
    public CommitCompareTask(Context context, IRepositoryIdProvider repository,
            String base, String head) {
        super(context);

        this.repository = repository;
        this.base = base;
        this.head = head;
    }

    @Override
    protected RepositoryCommitCompare run(Account account) throws Exception {
        return service.compare(repository, base, head);
    }
}
