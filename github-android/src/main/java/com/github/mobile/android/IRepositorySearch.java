package com.github.mobile.android;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * Interface facade to searching repositories
 */
public interface IRepositorySearch {

    /**
     * Search for repositories matching query
     *
     * @param query
     * @return list of repositories matching query
     * @throws IOException
     */
    List<SearchRepository> search(String query) throws IOException;
}
