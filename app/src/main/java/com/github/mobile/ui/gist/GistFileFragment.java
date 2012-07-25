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
package com.github.mobile.ui.gist;

import static com.github.mobile.Intents.EXTRA_GIST_FILE;
import static com.github.mobile.Intents.EXTRA_GIST_ID;
import android.accounts.Account;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.core.gist.GistStore;
import com.github.mobile.util.SourceEditor;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Map;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Fragment to display the content of a file in a Gist
 */
public class GistFileFragment extends RoboSherlockFragment {

    @InjectView(id.wv_code)
    private WebView webView;

    @InjectExtra(EXTRA_GIST_ID)
    private String gistId;

    private GistFile file;

    private Gist gist;

    @Inject
    private GistStore store;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        file = (GistFile) getArguments().get(EXTRA_GIST_FILE);
        gist = store.getGist(gistId);
        if (gist == null)
            gist = new Gist().setId(gistId);
    }

    private void loadSource() {
        new AuthenticatedUserTask<GistFile>(getActivity()) {

            @Override
            public GistFile run(Account account) throws Exception {
                gist = store.refreshGist(gistId);
                Map<String, GistFile> files = gist.getFiles();
                if (files == null)
                    throw new IOException();
                GistFile loadedFile = files.get(file.getFilename());
                if (loadedFile == null)
                    throw new IOException();
                return loadedFile;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                ToastUtils.show(getActivity(), e, string.error_gist_file_load);
            }

            @Override
            protected void onSuccess(GistFile loadedFile) throws Exception {
                if (loadedFile == null)
                    return;

                file = loadedFile;
                getArguments().putSerializable(EXTRA_GIST_FILE, file);
                if (file.getContent() != null)
                    showSource();
            }

        }.execute();
    }

    private void showSource() {
        SourceEditor.showSource(webView, file.getFilename(), new Object() {
            public String toString() {
                return file.getContent();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.gist_file_view, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (file.getContent() != null)
            showSource();
        else
            loadSource();
    }
}
