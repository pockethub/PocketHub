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
package com.github.pockethub.ui;

import android.accounts.Account;
import android.content.Context;
import android.text.Html.ImageGetter;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.content.GetMarkdownClient;
import com.github.pockethub.accounts.AuthenticatedUserLoader;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.RequestUtils;

/**
 * Markdown loader
 */
public class MarkdownLoader extends AuthenticatedUserLoader<CharSequence> {

    private static final String TAG = "MarkdownLoader";

    private final ImageGetter imageGetter;

    private final Repo repository;

    private final String raw;

    private boolean encode;

    /**
     * @param context
     * @param repository
     * @param raw
     * @param imageGetter
     * @param encode
     */
    public MarkdownLoader(Context context, Repo repository,
            String raw, ImageGetter imageGetter, boolean encode) {
        super(context);

        this.repository = repository;
        this.raw = raw;
        this.imageGetter = imageGetter;
        this.encode = encode;
    }

    @Override
    protected CharSequence getAccountFailureData() {
        return null;
    }

    @Override
    public CharSequence load(Account account) {
        GetMarkdownClient markdownClient = new GetMarkdownClient(RequestUtils.markdown(raw));
        String html = markdownClient.observable().toBlocking().first();
/*            if (repository != null)
                html = service.getRepositoryHtml(repository, raw);
            else
                html = service.getHtml(raw, MODE_GFM);*/

        if (encode)
            return HtmlUtils.encode(html, imageGetter);
        else
            return html;
    }
}
