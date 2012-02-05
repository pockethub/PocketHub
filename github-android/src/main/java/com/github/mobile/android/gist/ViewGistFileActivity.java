package com.github.mobile.android.gist;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_FILE;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.SourceEditor;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity to view a file in a Gist
 */
public class ViewGistFileActivity extends RoboActivity {

    /**
     * Create intent to show file
     *
     * @param gist
     * @param file
     * @return intent
     */
    public static Intent createIntent(Gist gist, GistFile file) {
        return new Builder("gist.file.VIEW").add(EXTRA_GIST, gist).add(EXTRA_GIST_FILE, file).toIntent();
    }

    @InjectView(id.tv_gist_id)
    private TextView gistId;

    @InjectView(id.wv_gist_content)
    private WebView webView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.gist_view_content_item);

        final Gist gist = (Gist) getIntent().getSerializableExtra(EXTRA_GIST);
        final GistFile file = (GistFile) getIntent().getSerializableExtra(EXTRA_GIST_FILE);

        setTitle(file.getFilename());
        gistId.setText(getString(string.gist) + " " + gist.getId());

        SourceEditor.showSource(webView, file.getFilename(), new Object() {
            public String toString() {
                return file.getContent();
            }
        });
    }
}
