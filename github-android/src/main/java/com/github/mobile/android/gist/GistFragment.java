package com.github.mobile.android.gist;

import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display a Gist's files and comments
 */
public class GistFragment extends ListLoadingFragment<Comment> {

    private String id;

    private LoaderCallbacks<List<Comment>> loadListener;

    @Inject
    private GistService service;

    @Inject
    private GistStore store;

    private Gist gist;

    private List<View> fileHeaders = new ArrayList<View>();

    @Inject
    private AvatarHelper avatarHelper;

    public void onListItemClick(ListView l, View v, int position, long id) {
        Object item = l.getItemAtPosition(position);
        if (item instanceof GistFile)
            startActivity(ViewGistFileActivity.createIntent(gist, (GistFile) item));
    }

    /**
     * @param loadListener
     * @return this fragment
     */
    public GistFragment setLoadListener(LoaderCallbacks<List<Comment>> loadListener) {
        this.loadListener = loadListener;
        return this;
    }

    /**
     * @param id
     * @return this fragment
     */
    public GistFragment setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Loader<List<Comment>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<Comment>>(getActivity()) {
            @Override
            public List<Comment> loadInBackground() {
                try {
                    Gist gist = store.addGist(service.getGist(id));
                    List<Comment> comments;
                    if (gist.getComments() > 0)
                        comments = service.getComments(id);
                    else
                        comments = Collections.emptyList();
                    return new FullGist(gist, service.isStarred(id), comments);
                } catch (IOException e) {
                    return new FullGist();
                }
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Comment> adapterFor(List<Comment> items) {
        return new ViewHoldingListAdapter<Comment>(items, viewInflatorFor(getActivity(), layout.comment_view_item),
                reflectiveFactoryFor(CommentViewHolder.class, avatarHelper));
    }

    public void onLoadFinished(Loader<List<Comment>> loader, List<Comment> items) {
        FullGist gist = (FullGist) items;
        this.gist = gist.getGist();
        ListView view = getListView();
        for (View header : fileHeaders)
            view.removeHeaderView(header);
        fileHeaders.clear();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (GistFile file : gist.getGist().getFiles().values()) {
            View fileView = inflater.inflate(layout.gist_view_file_item, null);
            new GistFileViewHolder(fileView).updateViewFor(file);
            view.addHeaderView(fileView, file, true);
            fileHeaders.add(fileView);
        }
        super.onLoadFinished(loader, items);
        if (loadListener != null)
            loadListener.onLoadFinished(loader, items);
    }

    public void onLoaderReset(Loader<List<Comment>> listLoader) {
        super.onLoaderReset(listLoader);
        if (loadListener != null)
            loadListener.onLoaderReset(listLoader);
    }
}