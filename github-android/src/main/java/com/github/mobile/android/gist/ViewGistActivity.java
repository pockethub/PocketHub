package com.github.mobile.android.gist;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENTS;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.comment.CreateCommentActivity;
import com.github.mobile.android.util.Avatar;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.mobile.android.util.HttpImageGetter;
import com.github.mobile.android.util.Time;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to display an existing Gist
 */
public class ViewGistActivity extends RoboFragmentActivity {

    /**
     * Result if the Gist was deleted
     */
    public static final int RESULT_DELETED = RESULT_FIRST_USER;

    private static final int REQUEST_CODE_COMMENT = 1;

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

    @InjectView(id.tv_gist_id)
    private TextView gistIdText;

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

    @InjectView(id.lv_gist_comments)
    private ListView comments;

    @InjectExtra(EXTRA_GIST_ID)
    private String gistId;

    private HttpImageGetter imageGetter;

    private MenuItem deleteItem;

    @Inject
    private ContextScopedProvider<GistService> gistServiceProvider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageGetter = new HttpImageGetter(this);
        setContentView(layout.gist_view);

        gistIdText.setText(getString(string.gist) + " " + gistId);

        loadGist(false);
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

    @SuppressWarnings("unchecked")
    private List<Comment> getComments() {
        return (List<Comment>) getIntent().getSerializableExtra(EXTRA_COMMENTS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(menu.gist_view, options);
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
            deleteGist();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void deleteGist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this Gist?");
        builder.setPositiveButton(android.R.string.yes, new OnClickListener() {

            public void onClick(DialogInterface dialog, int button) {
                dialog.dismiss();
                final ProgressDialog progress = new ProgressDialog(ViewGistActivity.this);
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
                        Toast.makeText(getContext(), e.getMessage(), 5000).show();
                    }
                }.execute();
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
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
                loadComments(true);
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.dismiss();
                Toast.makeText(ViewGistActivity.this, e.getMessage(), 5000).show();
            }
        }.execute();

    }

    private void loadGist(boolean force) {
        Gist current = getGist();
        if (force || current == null) {
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage(getString(string.loading_gist));
            progress.setIndeterminate(true);
            progress.show();
            new RoboAsyncTask<Gist>(this) {

                public Gist call() throws Exception {
                    return gistServiceProvider.get(ViewGistActivity.this).getGist(gistId);
                }

                protected void onSuccess(Gist gist) throws Exception {
                    getIntent().putExtra(EXTRA_GIST, gist);
                    if (deleteItem != null)
                        deleteItem.setEnabled(isOwner());
                    progress.cancel();
                    displayGist(gist);
                }

                protected void onException(Exception e) throws RuntimeException {
                    progress.cancel();
                    Toast.makeText(ViewGistActivity.this, e.getMessage(), 5000).show();
                }

            }.execute();
        } else
            displayGist(current);
    }

    private void loadComments(boolean force) {
        List<Comment> current = getComments();
        if (force || current == null)
            new RoboAsyncTask<List<Comment>>(this) {

                public List<Comment> call() throws Exception {
                    return gistServiceProvider.get(ViewGistActivity.this).getComments(gistId);
                }

                protected void onSuccess(List<Comment> gistComments) throws Exception {
                    getIntent().putExtra(EXTRA_COMMENTS, (Serializable) gistComments);
                    displayComments(gistComments);
                }

                protected void onException(Exception e) throws RuntimeException {
                    Toast.makeText(ViewGistActivity.this, e.getMessage(), 5000).show();
                }
            }.execute();
        else
            displayComments(current);
    }

    private void displayComments(List<Comment> gistComments) {
        comments.setAdapter(new ViewHoldingListAdapter<Comment>(gistComments, ViewInflator.viewInflatorFor(
                ViewGistActivity.this, layout.comment_view_item), reflectiveFactoryFor(CommentViewHolder.class,
                ViewGistActivity.this, imageGetter)));
    }

    private void displayGist(final Gist gist) {
        Avatar.bind(this, gravatar, gist.getUser());

        String desc = gist.getDescription();
        if (desc != null && desc.length() > 0)
            description.setText(desc);
        else
            description.setText(Html.fromHtml("<i>No description</i>"));

        created.setText(Time.relativeTimeFor(gist.getCreatedAt()));

        if (gist.getUser() != null) {
            author.setText(gist.getUser().getLogin());
            author.setVisibility(VISIBLE);
        } else
            author.setVisibility(INVISIBLE);

        files.setAdapter(new ViewHoldingListAdapter<GistFile>(new ArrayList<GistFile>(gist.getFiles().values()),
                ViewInflator.viewInflatorFor(this, layout.gist_view_file_item),
                reflectiveFactoryFor(GistFileViewHolder.class)));
        files.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                GistFile file = (GistFile) view.getItemAtPosition(position);
                startActivity(ViewGistFileActivity.createIntent(gist, file));
            }
        });

        loadComments(false);
    }
}
