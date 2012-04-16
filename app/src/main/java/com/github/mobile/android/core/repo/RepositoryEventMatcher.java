package com.github.mobile.android.core.repo;

import static org.eclipse.egit.github.core.event.Event.TYPE_FORK;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventPayload;
import org.eclipse.egit.github.core.event.ForkPayload;

/**
 * Helper to find a {@link RepositoryEventMatcher} to open for an event
 */
public class RepositoryEventMatcher {

    /**
     * Get {@link Repository} from event
     *
     * @param event
     * @return gist or null if event doesn't apply
     */
    public Repository getRepository(final Event event) {
        if (event == null)
            return null;
        EventPayload payload = event.getPayload();
        if (payload == null)
            return null;
        String type = event.getType();
        if (TYPE_FORK.equals(type))
            return ((ForkPayload) payload).getForkee();
        else
            return null;
    }
}
