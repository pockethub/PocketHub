package com.github.mobile.repo;

import static com.github.mobile.util.TypefaceUtils.ICON_FORK;
import static com.github.mobile.util.TypefaceUtils.ICON_PRIVATE;
import static com.github.mobile.util.TypefaceUtils.ICON_PUBLIC;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * Adapter for a list of searched for repositories
 */
public class SearchRepositoryListAdapter extends ItemListAdapter<SearchRepository, SearchRepositoryItemView> {

    /**
     * Create list adapter for searched for repositories
     *
     * @param inflater
     * @param elements
     */
    public SearchRepositoryListAdapter(LayoutInflater inflater, SearchRepository[] elements) {
        super(layout.repo_search_list_item, inflater, elements);
    }

    /**
     *
     * Create list adapter for searched for repositories
     *
     * @param inflater
     */
    public SearchRepositoryListAdapter(LayoutInflater inflater) {
        this(inflater, null);
    }

    @Override
    protected void update(final SearchRepositoryItemView view, final SearchRepository repository) {
        if (repository.isPrivate())
            view.repoIcon.setText(Character.toString(ICON_PRIVATE));
        else if (repository.isFork())
            view.repoIcon.setText(Character.toString(ICON_FORK));
        else
            view.repoIcon.setText(Character.toString(ICON_PUBLIC));

        view.repoName.setText(repository.generateId());
        view.repoDescription.setText(repository.getDescription());
    }

    @Override
    protected SearchRepositoryItemView createView(View view) {
        return new SearchRepositoryItemView(view);
    }
}
