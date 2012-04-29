/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package com.github.mobile.core.gist;

import com.github.mobile.core.ResourcePager;

import org.eclipse.egit.github.core.Gist;

/**
 * Pager over Gists
 */
public abstract class GistPager extends ResourcePager<Gist> {

    private final GistStore store;

    /**
     * Create pager
     *
     * @param store
     */
    public GistPager(final GistStore store) {
        this.store = store;
    }

    @Override
    protected Object getId(Gist resource) {
        return resource.getId();
    }

    @Override
    protected Gist register(Gist resource) {
        return store.addGist(resource);
    }
}
