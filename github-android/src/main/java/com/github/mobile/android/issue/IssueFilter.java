package com.github.mobile.android.issue;

import static org.eclipse.egit.github.core.service.IssueService.DIRECTION_DESCENDING;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_DIRECTION;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_SORT;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_ASSIGNEE;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_LABELS;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_MILESTONE;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_STATE;
import static org.eclipse.egit.github.core.service.IssueService.SORT_CREATED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_CLOSED;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Milestone;

/**
 * Issue filter containing at least one valid query
 */
public class IssueFilter implements Serializable, Iterable<Map<String, String>>, Cloneable {

    /** serialVersionUID */
    private static final long serialVersionUID = 7310646589186299063L;

    private Set<String> labels;

    private Milestone milestone;

    private String assignee;

    private boolean open;

    private boolean closed;

    /**
     * Create filter
     */
    public IssueFilter() {
        open = true;
    }

    /**
     * Set all issues to be returned
     *
     * @return this filter
     */
    public IssueFilter setAll() {
        open = true;
        closed = true;
        return this;
    }

    /**
     * Set only closed issues to be returned
     *
     * @return this filter
     */
    public IssueFilter setClosedOnly() {
        open = false;
        closed = true;
        return this;
    }

    /**
     * Set only open issues to be returned
     *
     * @return this filter
     */
    public IssueFilter setOpenOnly() {
        open = true;
        closed = false;
        return this;
    }

    /**
     * Add label to filter
     *
     * @param label
     * @return this filter
     */
    public IssueFilter addLabel(String label) {
        if (label == null || label.length() == 0)
            return this;
        if (labels == null)
            labels = new HashSet<String>();
        labels.add(label);
        return this;
    }

    /**
     * @param labels
     * @return this filter
     */
    public IssueFilter setLabels(Set<String> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * @return labels
     */
    public Set<String> getLabels() {
        return labels;
    }

    /**
     * @param milestone
     * @return this filter
     */
    public IssueFilter setMilestone(Milestone milestone) {
        this.milestone = milestone;
        return this;
    }

    /**
     * @return milestone
     */
    public Milestone getMilestone() {
        return milestone;
    }

    /**
     * @param assignee
     * @return this filter
     */
    public IssueFilter setAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    /**
     * Are all issues returned?
     *
     * @return true if all ,false otherwise
     */
    public boolean isAll() {
        return open && closed;
    }

    /**
     * Are only open issues returned?
     *
     * @return true if open only, false otherwise
     */
    public boolean isOpenOnly() {
        return open && !closed;
    }

    /**
     * Are only closed issues returned?
     *
     * @return true if closed only, false otherwise
     */
    public boolean isClosedOnly() {
        return !open && closed;
    }

    /**
     * @return assignee
     */
    public String getAssignee() {
        return assignee;
    }

    public Iterator<Map<String, String>> iterator() {

        final Map<String, String> base = new HashMap<String, String>();

        base.put(FIELD_SORT, SORT_CREATED);
        base.put(FIELD_DIRECTION, DIRECTION_DESCENDING);

        if (assignee != null && assignee.length() > 0)
            base.put(FILTER_ASSIGNEE, assignee);

        if (milestone != null)
            base.put(FILTER_MILESTONE, Integer.toString(milestone.getNumber()));

        if (labels != null && !labels.isEmpty()) {
            StringBuilder labelsQuery = new StringBuilder();
            for (String label : labels)
                labelsQuery.append(label).append(',');
            base.put(FILTER_LABELS, labelsQuery.toString());
        }

        List<String> states = new ArrayList<String>();
        if (open)
            states.add(STATE_OPEN);
        if (closed)
            states.add(STATE_CLOSED);
        final Iterator<String> statesIter = states.iterator();
        return new Iterator<Map<String, String>>() {

            public boolean hasNext() {
                return statesIter.hasNext();
            }

            public Map<String, String> next() {
                HashMap<String, String> stateMap = new HashMap<String, String>(base);
                stateMap.put(FILTER_STATE, statesIter.next());
                return stateMap;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Get display {@link CharSequence} representing this filter
     *
     * @return display
     */
    public CharSequence toDisplay() {
        List<String> segments = new ArrayList<String>();
        if (assignee != null)
            segments.add("Assignee: " + assignee);

        if (milestone != null)
            segments.add("Milestone: " + milestone.getTitle());

        if (labels != null && !labels.isEmpty()) {
            StringBuilder builder = new StringBuilder("Labels: ");
            builder.append("Labels: ");
            for (String label : labels)
                builder.append(label).append(',').append(' ');
            builder.deleteCharAt(builder.length() - 1);
            builder.deleteCharAt(builder.length() - 1);
            segments.add(builder.toString());
        }

        if (segments.isEmpty())
            return "";

        StringBuilder all = new StringBuilder();
        for (String segment : segments)
            all.append(segment).append(',').append(' ');
        all.deleteCharAt(all.length() - 1);
        all.deleteCharAt(all.length() - 1);
        return all;
    }

    @Override
    public IssueFilter clone() {
        try {
            return (IssueFilter) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since this class implements Cloneable
            throw new IllegalArgumentException(e);
        }
    }
}
