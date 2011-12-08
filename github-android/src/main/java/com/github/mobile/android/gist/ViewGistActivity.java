package com.github.mobile.android.gist;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.Avatar;
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
     * Intent key representing the Gist to display
     */
    public static final String GIST = "gist";

    /**
     * Intent key representing the id of the Gist to display
     */
    public static final String GIST_ID = "gistId";

    /**
     * Create intent to view Gist
     *
     * @param context
     * @param gist
     * @return intent
     */
    public static final Intent createIntent(Context context, Gist gist) {
        Intent intent = new Intent(context, ViewGistActivity.class);
        intent.putExtra(GIST, gist);
        return intent;
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

    @InjectView(id.iv_gravatar)
    private ImageView gravatar;

    @InjectView(id.tv_gist_created)
    private TextView created;

    @InjectView(id.tv_gist_author)
    private TextView author;

    @InjectView(id.tv_gist_description)
    private TextView description;

    @InjectView(id.lv_gist_files)
    private ListView files;

    @Inject
    private ContextScopedProvider<GistService> gistServiceProvider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.gist_view);
        final Gist gist = (Gist) getIntent().getSerializableExtra(GIST);
        if (gist == null) {
            gistId.setVisibility(INVISIBLE);
            created.setVisibility(INVISIBLE);
            description.setVisibility(INVISIBLE);
            author.setVisibility(INVISIBLE);
            gravatar.setVisibility(INVISIBLE);

            final String id = getIntent().getStringExtra(GIST_ID);
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage(getString(string.loading_gist));
            progress.setIndeterminate(true);
            progress.show();
            new RoboAsyncTask<Gist>(this) {

                public Gist call() throws Exception {
                    Gist gist = gistServiceProvider.get(ViewGistActivity.this).getGist(id);
                    updateGravatar(gist);
                    return gist;
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
        } else {
            displayGist(gist);
            updateGravatar(gist);
        }
    }

    private void updateGravatar(final Gist gist) {
        Avatar.bind(this, gravatar, gist.getUser().getAvatarUrl());
    }

    private void displayGist(final Gist gist) {
        gistId.setText(getString(string.gist) + " " + gist.getId());
        description.setText(gist.getDescription());
        created.setText(DateUtils.getRelativeTimeSpanString(gist.getCreatedAt().getTime()));
        if (gist.getUser() != null) {
            author.setText(gist.getUser().getLogin());
            author.setVisibility(VISIBLE);
        }
        gistId.setVisibility(VISIBLE);
        description.setVisibility(VISIBLE);
        created.setVisibility(VISIBLE);
        GistFile[] gistFiles = gist.getFiles().values().toArray(new GistFile[gist.getFiles().size()]);
        files.setAdapter(new GistFileListAdapter(this, gistFiles));
        files.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                GistFile file = (GistFile) view.getItemAtPosition(position);
                startActivity(ViewGistFileActivity.createIntent(ViewGistActivity.this, gist, file));
            }
        });
    }
}
