package com.github.mobile.android.core.repo;

import static org.eclipse.egit.github.core.event.Event.TYPE_CREATE;
import static org.eclipse.egit.github.core.event.Event.TYPE_FORK;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventPayload;
import org.eclipse.egit.github.core.event.EventRepository;
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
        else if (TYPE_CREATE.equals(type)) {
            EventRepository repo = event.getRepo();
            if (repo != null) {
                String id = repo.getName();
                int slash = id.indexOf('/');
                if (slash > 0 && slash + 1 < id.length()) {
                    Repository full = new Repository();
                    full.setName(id.substring(slash + 1));
                    String login = id.substring(0, slash);
                    // Use actor if it matches login parsed from repository id
                    if (event.getActor() != null && login.equals(event.getActor().getLogin()))
                        full.setOwner(event.getActor());
                    else
                        full.setOwner(new User().setLogin(id.substring(0, slash)));
                    return full;
                }
            }
        }
        return null;
    }
}
