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
package com.github.pockethub.android.util;

import android.content.Context;
import android.text.Html.ImageGetter;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.util.HtmlUtils;
import com.meisolsson.githubsdk.model.request.RequestMarkdown;
import com.meisolsson.githubsdk.service.misc.MarkdownService;

import io.reactivex.Single;
import retrofit2.Response;

/**
 * Markdown loader.
 */
public class MarkdownLoader {

    /**
     * Fetches html
     * @param context
     * @param raw
     * @param repository
     * @param imageGetter
     * @param encode
     * @return
     */
    public static Single<CharSequence> load(Context context, String raw, Repository repository,
                              ImageGetter imageGetter, boolean encode) {
        RequestMarkdown markdown = RequestMarkdown.builder()
                .mode(RequestMarkdown.MODE_GFM)
                .text(raw)
                .context(repository != null ? String.format("%s/%s", repository.owner().login(), repository.name()) : null)
                .build();

        return ServiceGenerator.createService(context, MarkdownService.class)
                .renderMarkdown(markdown)
                .map(Response::body)
                .map(html -> encode ? HtmlUtils.encode(html, imageGetter) : html);
    }
}
