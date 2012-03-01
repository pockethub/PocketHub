package com.github.mobile.android.gist;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENTS;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import android.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.ConfirmDialogFragment;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.comment.CreateCommentActivity;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.Time;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to display an existing Gist
 */
public class ViewGistActivity extends DialogFragmentActivity implements LoaderCallbacks<List<Comment>> {

    /**
     * Result if the Gist was deleted
     */
    public static final int RESULT_DELETED = RESULT_FIRST_USER;

    private static final int REQUEST_CODE_COMMENT = 1;

    private static final int REQUEST_CONFIRM_DELETE = REQUEST_CODE_COMMENT + 1;

    /**
     * Create intent to view Gist
     *
     * @param gist
     * @return intent
     */
    public static final Intent createIntent(Gist gist) {
        return new Builder("gist.VIEW").add(EXTRA_GIST, gist).add(EXTRA_GIST_ID, gist.getId()).toIntent();
    }

    /**
     * Create intent to view Gist
     *
     * @param gistId
     * @return intent
     */
    public static final Intent createIntent(String gistId) {
        return new Builder("gist.VIEW").add(EXTRA_GIST_ID, gistId).toIntent();
    }

    @InjectView(id.iv_gravatar)
    private ImageView gravatar;

    @InjectView(id.tv_gist_creation)
    private TextView created;

    @InjectView(id.tv_gist_description)
    private TextView description;

    @InjectExtra(EXTRA_GIST_ID)
    private String gistId;

    @Inject
    private AvatarHelper avatarHelper;

    private GistFragment gistFragment;

    private MenuItem deleteItem;

    @Inject
    private ContextScopedProvider<GistService> gistServiceProvider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.gist_view);
        setTitle(getString(string.gist) + " " + gistId);

        gistFragment = (GistFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        if (gistFragment == null) {
            gistFragment = new GistFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list, gistFragment).commit();
        }
        gistFragment.setId(gistId).setLoadListener(this);

        showGist(getGist());
    }

    private Gist getGist() {
        return (Gist) getIntent().getSerializableExtra(EXTRA_GIST);
    }

    private boolean isOwner() {
        Gist gist = getGist();
        if (gist == null)
            return false;
        User user = gist.getUser();
        if (user == null)
            return false;
        return gistServiceProvider.get(this).getClient().getUser().equals(user.getLogin());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.gist_view, options);
        deleteItem = options.findItem(id.gist_delete);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        deleteItem.setEnabled(isOwner());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.gist_comment:
            startActivityForResult(CreateCommentActivity.createIntent(), REQUEST_CODE_COMMENT);
            return true;
        case id.gist_delete:
            ConfirmDialogFragment.show(this, REQUEST_CONFIRM_DELETE, "Confirm Delete",
                    "Are you sure you want to delete this Gist?");
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (REQUEST_CONFIRM_DELETE == requestCode && RESULT_OK == resultCode) {
            final ProgressDialog progress = new ProgressDialog(ViewGistActivity.this);
            progress.setIndeterminate(true);
            progress.setMessage("Deleting Gist...");
            progress.show();
            new RoboAsyncTask<Gist>(ViewGistActivity.this) {

                public Gist call() throws Exception {
                    gistServiceProvider.get(getContext()).deleteGist(gistId);
                    return null;
                }

                protected void onSuccess(Gist gist) throws Exception {
                    progress.dismiss();
                    setResult(RESULT_DELETED);
                    finish();
                }

                protected void onException(Exception e) throws RuntimeException {
                    progress.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), LENGTH_LONG).show();
                }
            }.execute();
            return;
        }

        super.onDialogResult(requestCode, resultCode, arguments);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && REQUEST_CODE_COMMENT == requestCode && data != null) {
            String comment = data.getStringExtra(EXTRA_COMMENT_BODY);
            if (comment != null && comment.length() > 0) {
                createComment(comment);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createComment(final String comment) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Creating comment...");
        progress.setIndeterminate(true);
        progress.show();
        new RoboAsyncTask<Comment>(this) {

            public Comment call() throws Exception {
                return gistServiceProvider.get(ViewGistActivity.this).createComment(gistId, comment);
            }

            protected void onSuccess(Comment comment) throws Exception {
                progress.dismiss();
                gistFragment.refresh();
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.dismiss();
                Toast.makeText(ViewGistActivity.this, e.getMessage(), LENGTH_LONG).show();
            }
        }.execute();

    }

    public Loader<List<Comment>> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    private void showGist(final Gist gist) {
        if (gist != null) {
            getIntent().putExtra(EXTRA_GIST, gist);
            User user = gist.getUser();
            if (user != null) {
                gravatar.setVisibility(VISIBLE);
                avatarHelper.bind(gravatar, user);
                created.setText(Html.fromHtml("<b>" + user.getLogin() + "</b> "
                        + Time.relativeTimeFor(gist.getCreatedAt())));
                created.setVisibility(VISIBLE);
            } else {
                created.setVisibility(GONE);
                gravatar.setVisibility(GONE);
            }

            String desc = gist.getDescription();
            if (desc != null && desc.length() > 0)
                description.setText(desc);
            else
                description.setText(Html.fromHtml("<i>No description</i>"));
            description.setVisibility(VISIBLE);

            created.setVisibility(VISIBLE);
        } else {
            gravatar.setVisibility(GONE);
            description.setVisibility(GONE);
            created.setVisibility(GONE);
        }
    }

    public void onLoadFinished(Loader<List<Comment>> loader, List<Comment> gists) {
        FullGist fullGist = (FullGist) gists;
        final Gist gist = fullGist.getGist();
        showGist(gist);
        getIntent().putExtra(EXTRA_COMMENTS, fullGist);
    }

    public void onLoaderReset(Loader<List<Comment>> loader) {
    }
}
