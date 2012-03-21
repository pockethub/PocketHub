package com.github.mobile.android.gist;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.widget.Toast.LENGTH_LONG;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.HomeActivity;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.util.RoboAsyncTask;

/**
 * Fragment to display a list of Gists
 */
public abstract class GistsFragment extends ListLoadingFragment<Gist> implements Comparator<Gist> {

    /**
     * Create Gist request code
     */
    protected static final int REQUEST_CREATE = 1;

    /**
     * View Gist request code
     */
    protected static final int REQUEST_VIEW = REQUEST_CREATE + 1;

    @Inject
    private Context context;

    /**
     * Gist service
     */
    @Inject
    protected GistService service;

    /**
     * Gist store
     */
    @Inject
    protected GistStore store;

    /**
     * Gist id field
     */
    protected TextView gistId;

    /**
     * Width of id column of in Gist list
     */
    protected AtomicReference<Integer> idWidth = new AtomicReference<Integer>();

    private void randomGist() {
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage(getString(string.random_gist));
        progress.show();
        new RoboAsyncTask<Gist>(context) {

            public Gist call() throws Exception {
                PageIterator<Gist> pages = service.pagePublicGists(1);
                pages.next();
                int randomPage = 1 + (int) (Math.random() * ((pages.getLastPage() - 1) + 1));
                Collection<Gist> gists = service.pagePublicGists(randomPage, 1).next();
                if (gists.isEmpty())
                    throw new IllegalArgumentException("No Gists found");
                return service.getGist(gists.iterator().next().getId());
            }

            protected void onSuccess(Gist gist) throws Exception {
                progress.cancel();
                startActivity(ViewGistActivity.createIntent(gist));
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.cancel();
                Toast.makeText(context, e.getMessage(), LENGTH_LONG).show();
            }
        }.execute();
    }

    private void openGist() {
        Builder prompt = new Builder(context);

        prompt.setTitle("Open Gist");
        prompt.setMessage("Enter id:");

        final EditText id = new EditText(context);
        prompt.setView(id);

        prompt.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                startActivityForResult(ViewGistActivity.createIntent(id.getText().toString().trim()), REQUEST_VIEW);
            }
        });
        prompt.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.gists, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.open_gist:
            openGist();
            return true;
        case id.random_gist:
            randomGist();
            return true;
        case id.create_gist:
            startActivityForResult(new Intent(context, ShareGistActivity.class), REQUEST_CREATE);
            return true;
        case android.R.id.home:
            Intent intent = new Intent(context, HomeActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Gist gist = (Gist) l.getItemAtPosition(position);
        startActivityForResult(ViewGistActivity.createIntent(gist), REQUEST_VIEW);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_gists));
        getListView().setFastScrollEnabled(true);
        gistId = (TextView) getLayoutInflater(savedInstanceState).inflate(layout.gist_list_item, null).findViewById(
                id.tv_gist_id);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIEW && (RESULT_OK == resultCode || RESULT_CANCELED == resultCode)) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onLoadFinished(Loader<List<Gist>> loader, List<Gist> items) {
        Exception exception = getException(loader);
        if (exception != null) {
            showError(exception, string.error_gists_load);
            showList();
            return;
        }
        idWidth.set(GistViewHolder.computeIdWidth(items, gistId));
        super.onLoadFinished(loader, items);
    }

    @Override
    public int compare(final Gist g1, final Gist g2) {
        return g2.getCreatedAt().compareTo(g1.getCreatedAt());
    }
}
