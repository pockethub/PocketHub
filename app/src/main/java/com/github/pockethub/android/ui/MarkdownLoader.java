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
package com.github.pockethub.android.ui;

import android.accounts.Account;
import android.app.Activity;
import android.text.Html.ImageGetter;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.accounts.AuthenticatedUserLoader;
import com.github.pockethub.android.util.HtmlUtils;
import com.meisolsson.githubsdk.model.request.RequestMarkdown;
import com.meisolsson.githubsdk.service.misc.MarkdownService;

/**
 * Markdown loader
 */
public class MarkdownLoader extends AuthenticatedUserLoader<CharSequence> {

    private static final String TAG = "MarkdownLoader";

    private final ImageGetter imageGetter;

    private final Repository repository;

    private final String raw;

    private boolean encode;

    /**
     * @param activity
     * @param repository
     * @param raw
     * @param imageGetter
     * @param encode
     */
    public MarkdownLoader(Activity activity, Repository repository,
                          String raw, ImageGetter imageGetter, boolean encode) {
        super(activity);

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
        RequestMarkdown markdown = RequestMarkdown.builder()
                .mode(RequestMarkdown.MODE_GFM)
                .text(raw)
                .context(repository != null ? String.format("%s/%s", repository.owner().login(), repository.name()) : null)
                .build();

        String html = ServiceGenerator.createService(activity, MarkdownService.class)
                .renderMarkdown(markdown)
                .blockingGet()
                .body();

        if (encode) {
            return HtmlUtils.encode(html, imageGetter);
        } else {
            return html;
        }
    }
}
