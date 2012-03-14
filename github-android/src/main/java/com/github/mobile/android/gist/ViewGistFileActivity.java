package com.github.mobile.android.gist;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_FILE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.ErrorHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.SourceEditor;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Map;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to view a file in a Gist
 */
public class ViewGistFileActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent to show file
     *
     * @param gist
     * @param file
     * @return intent
     */
    public static Intent createIntent(Gist gist, GistFile file) {
        return new Builder("gist.file.VIEW").add(EXTRA_GIST_ID, gist.getId()).add(EXTRA_GIST_FILE, file).toIntent();
    }

    @InjectView(id.tv_gist_id)
    private TextView idText;

    @InjectView(id.wv_gist_content)
    private WebView webView;

    @InjectExtra(EXTRA_GIST_ID)
    private String gistId;

    @InjectExtra(EXTRA_GIST_FILE)
    private GistFile file;

    private Gist gist;

    @Inject
    private GistStore store;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.gist_view_content_item);
        setTitle(file.getFilename());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idText.setText(getString(string.gist) + " " + gistId);
        gist = store.getGist(gistId);
        if (gist == null)
            gist = new Gist().setId(gistId);

        if (file.getContent() != null)
            showSource();
        else
            loadSource();

    }

    private void loadSource() {
        new RoboAsyncTask<GistFile>(this) {
            public GistFile call() throws Exception {
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
                ErrorHelper.show(getApplication(), e, string.error_gist_file_load);
            }

            protected void onSuccess(GistFile loadedFile) throws Exception {
                if (loadedFile == null)
                    return;

                file = loadedFile;
                getIntent().putExtra(EXTRA_GIST_FILE, file);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = ViewGistActivity.createIntent(gist);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
