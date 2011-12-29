package com.github.mobile.android.issue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Issue filter
 */
public class IssueFilter implements Serializable, Iterable<Map<String, String>> {

    /** serialVersionUID */
    private static final long serialVersionUID = 7310646589186299063L;

    private Set<String> labels;

    private Milestone milestone;

    private String assignee;

    private Set<String> states;

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
     * Add state to filter
     *
     * @param state
     * @return this filter
     */
    public IssueFilter addState(String state) {
        if (state == null || state.length() == 0)
            return this;
        if (states == null)
            states = new HashSet<String>();
        states.add(state);
        return this;
    }

    /**
     * @param states
     * @return this filter
     */
    public IssueFilter setStates(Set<String> states) {
        this.states = states;
        return this;
    }

    /**
     * @return states
     */
    public Set<String> getStates() {
        return states;
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
     * Does this filter contain the given state?
     *
     * @param state
     * @return true if contains the state, false otherwise
     */
    public boolean containsState(String state) {
        if (state == null || state.length() == 0)
            return false;
        return states != null && states.contains(state);
    }

    /**
     * Remove state from filter
     *
     * @param state
     * @return this filter
     */
    public IssueFilter removeState(String state) {
        if (state != null && state.length() != 0 && states != null)
            states.remove(state);
        return this;
    }

    /**
     * Is the filter valid?
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return states != null && !states.isEmpty();
    }

    /**
     * @return assignee
     */
    public String getAssignee() {
        return assignee;
    }

    public Iterator<Map<String, String>> iterator() {
        if (states == null || states.isEmpty())
            return new Iterator<Map<String, String>>() {

                public boolean hasNext() {
                    return false;
                }

                public Map<String, String> next() {
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new IllegalStateException();
                }
            };

        final Map<String, String> base = new HashMap<String, String>();

        if (assignee != null && assignee.length() > 0)
            base.put(IssueService.FILTER_ASSIGNEE, assignee);

        if (milestone != null)
            base.put(IssueService.FILTER_MILESTONE, Integer.toString(milestone.getNumber()));

        if (labels != null && !labels.isEmpty()) {
            StringBuilder labelsQuery = new StringBuilder();
            for (String label : labels)
                labelsQuery.append(label).append(',');
            base.put(IssueService.FILTER_LABELS, labelsQuery.toString());
        }

        final Iterator<String> statesIter = states.iterator();
        return new Iterator<Map<String, String>>() {

            public boolean hasNext() {
                return statesIter.hasNext();
            }

            public Map<String, String> next() {
                HashMap<String, String> stateMap = new HashMap<String, String>(base);
                stateMap.put(IssueService.FILTER_STATE, statesIter.next());
                return stateMap;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
