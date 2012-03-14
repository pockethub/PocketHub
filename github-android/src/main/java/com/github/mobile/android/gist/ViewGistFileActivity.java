package com.github.mobile.android.gist;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_FILE;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.SourceEditor;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

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
        return new Builder("gist.file.VIEW").add(EXTRA_GIST, gist).add(EXTRA_GIST_FILE, file).toIntent();
    }

    @InjectView(id.tv_gist_id)
    private TextView gistId;

    @InjectView(id.wv_gist_content)
    private WebView webView;

    @InjectExtra(EXTRA_GIST)
    private Gist gist;

    @InjectExtra(EXTRA_GIST_FILE)
    private GistFile file;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.gist_view_content_item);
        setTitle(file.getFilename());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gistId.setText(getString(string.gist) + " " + gist.getId());

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
