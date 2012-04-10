package com.github.mobile.android.ui.user;

import com.github.mobile.android.ResourcePager;

import org.eclipse.egit.github.core.event.Event;

/**
 * Pager over events
 */
public abstract class EventPager extends ResourcePager<Event> {

    @Override
    protected String getId(Event resource) {
        return resource.getId();
    }
}
