package com.github.mobile.gist;

import static com.github.mobile.util.GitHubIntents.EXTRA_GIST_FILE;
import static com.github.mobile.util.GitHubIntents.EXTRA_GIST_ID;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.async.AuthenticatedUserTask;
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

    @InjectView(id.wv_gist_content)
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
            public GistFile run() throws Exception {
                gist = store.refreshGist(gistId);
                Map<String, GistFile> files = gist.getFiles();
                if (files == null)
                    throw new IOException();
                GistFile loadedFile = files.get(file.getFilename());
                if (loadedFile == null)
                    throw new IOException();
                return loadedFile;
            }

            protected void onException(Exception e) throws RuntimeException {
                ToastUtils.show(getActivity(), e, string.error_gist_file_load);
            }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.gist_view_content_item, null);
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
