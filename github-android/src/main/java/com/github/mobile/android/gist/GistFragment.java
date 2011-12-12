package com.github.mobile.android.gist;

import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.fragment.RoboListFragment;
import roboguice.inject.ContextScopedProvider;
import roboguice.util.RoboAsyncTask;

/**
 * Fragment to display Gists
 */
public class GistFragment extends RoboListFragment implements LoaderCallbacks<List<Gist>> {

    private static final int REQUEST_CREATE = 1;

    @Inject
    private Context context;

    @Inject
    ContextScopedProvider<GistService> serviceProvider;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        return inflater.inflate(layout.gists, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button createButton = (Button) view.findViewById(id.createGistButton);
        createButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivityForResult(new Intent(context, ShareGistActivity.class), REQUEST_CREATE);
            }
        });

        Button randomButton = (Button) view.findViewById(id.randomGistButton);
        randomButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(context);
                progress.setMessage(getActivity().getString(R.string.random_gist));
                progress.show();
                new RoboAsyncTask<Gist>(context) {

                    public Gist call() throws Exception {
                        GistService service = serviceProvider.get(context);
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
                        startActivity(ViewGistActivity.createIntent(context, gist));
                    }

                    protected void onException(Exception e) throws RuntimeException {
                        progress.cancel();
                        Toast.makeText(context, e.getMessage(), 5000).show();
                    }
                }.execute();
            }
        });

        Button openButton = (Button) view.findViewById(id.openGistButton);
        openButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Builder prompt = new Builder(context);

                prompt.setTitle("Open Gist");
                prompt.setMessage("Enter id:");

                final EditText id = new EditText(context);
                prompt.setView(id);

                prompt.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String gistId = id.getText().toString();
                        startActivity(ViewGistActivity.createIntent(context, gistId));

                    }
                });
                prompt.show();
            }
        });
    }

    public Loader<List<Gist>> onCreateLoader(int id, Bundle bundle) {
        return new AsyncLoader<List<Gist>>(getActivity()) {
            @Override
            public List<Gist> loadInBackground() {
                GistService service = serviceProvider.get(context);
                List<Gist> gists;
                try {
                    gists = service.getGists(service.getClient().getUser());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Collections.sort(gists, new Comparator<Gist>() {

                    public int compare(Gist g1, Gist g2) {
                        return g2.getCreatedAt().compareTo(g1.getCreatedAt());
                    }
                });
                return gists;
            }
        };
    }

    /**
     * Get adapter for gist list
     *
     * @param items
     * @return list adapter
     */
    protected ListAdapter adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                new ViewHolderFactory<Gist>() {
                    public ViewHolder<Gist> createViewHolderFor(View view) {
                        return new GistViewHolder(view);
                    }
                });
    }

    public void onListItemClick(ListView list, View view, int position, long id) {
        Gist gist = (Gist) list.getItemAtPosition(position);
        startActivity(ViewGistActivity.createIntent(getActivity(), gist.getId()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    public void onLoadFinished(Loader<List<Gist>> loader, List<Gist> items) {
        setListAdapter(adapterFor(items));
    }

    public void onLoaderReset(Loader<List<Gist>> arg0) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE && ShareGistActivity.RESULT_CREATED == resultCode) {
            getLoaderManager().restartLoader(0, null, this);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
