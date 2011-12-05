package com.github.mobile.android.gist;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to display an existing Gist
 */
public class ViewGistActivity extends RoboActivity {

    /**
     * Intent key representing the id of the Gist to display
     */
    public static final String GIST_ID = "gist";

    /**
     * Create intent to view Gist
     *
     * @param context
     * @param gist
     * @return intent
     */
    public static final Intent createIntent(Context context, Gist gist) {
        return createIntent(context, gist.getId());
    }

    /**
     * Create intent to view Gist
     *
     * @param context
     * @param gistId
     * @return intent
     */
    public static final Intent createIntent(Context context, String gistId) {
        Intent intent = new Intent(context, ViewGistActivity.class);
        intent.putExtra(GIST_ID, gistId);
        return intent;
    }

    @InjectView(id.tv_gist_id)
    private TextView gistId;

    @InjectView(id.tv_gist_description)
    private TextView description;

    @InjectView(id.elv_gist_files)
    private ExpandableListView files;

    @Inject
    private ContextScopedProvider<GistService> gistServiceProvider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.gist_view);
        gistId.setVisibility(INVISIBLE);
        description.setVisibility(INVISIBLE);

        final String id = getIntent().getStringExtra("gist");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(string.loading_gist));
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Gist>(this) {

            public Gist call() throws Exception {
                return gistServiceProvider.get(ViewGistActivity.this).getGist(id);
            }

            protected void onSuccess(Gist gist) throws Exception {
                progress.cancel();
                displayGist(gist);
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.cancel();
                Toast.makeText(ViewGistActivity.this, e.getMessage(), 5000).show();
            }
        }.execute();
    }

    private void displayGist(Gist gist) {
        gistId.setVisibility(VISIBLE);
        description.setVisibility(VISIBLE);
        gistId.setText(getString(string.gist) + " " + gist.getId());
        description.setText(gist.getDescription());
        GistFile[] gistFiles = gist.getFiles().values().toArray(new GistFile[gist.getFiles().size()]);
        files.setAdapter(new GistFileListAdapter(gistFiles, getLayoutInflater()));
    }
}
