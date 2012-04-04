package com.github.mobile.android.gist;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static java.util.Collections.sort;
import android.app.AlertDialog.Builder;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

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
        new RandomGistTask(getActivity()).start();
    }

    private void openGist() {
        Builder prompt = new Builder(context);

        prompt.setTitle(getString(string.open_gist_title));
        prompt.setMessage(getString(string.enter_id_message));

        final EditText id = new EditText(context);
        prompt.setView(id);

        prompt.setPositiveButton(string.open, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                new OpenGistTask(getActivity(), id.getText().toString().trim()).start();
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
        startActivityForResult(ViewGistsActivity.createIntent(listItems, position), REQUEST_VIEW);
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

    List<Gist> storeAndSort(Collection<Gist> gists) {
        List<Gist> gistList = new ArrayList<Gist>(gists.size());
        for (Gist gist : gists)
            gistList.add(store.addGist(gist));
        sort(gistList, this);
        return gistList;
    }
}
