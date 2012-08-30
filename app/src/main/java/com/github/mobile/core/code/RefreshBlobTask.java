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

import static org.eclipse.egit.github.core.Blob.ENCODING_BASE64;
import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;
import android.accounts.Account;
import android.content.Context;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.util.EncodingUtils;

/**
 * Task to refresh a blob
 */
public class RefreshBlobTask extends AuthenticatedUserTask<String> {

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

    protected String run(Account account) throws Exception {
        Blob blob = service.getBlob(repository, blobSha);
        String content = blob.getContent();
        if (ENCODING_BASE64.equals(blob.getEncoding()))
            content = new String(EncodingUtils.fromBase64(content),
                    CHARSET_UTF8);
        return content;
    }
}
