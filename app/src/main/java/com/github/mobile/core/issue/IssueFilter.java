/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.core.issue;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;

/**
 * Issue filter containing at least one valid query
 */
public class IssueFilter implements Serializable, Cloneable {

    /** serialVersionUID */
    private static final long serialVersionUID = 7310646589186299063L;

    private final Repository repository;

    private Set<String> labels;

    private Milestone milestone;

    private String assignee;

    private boolean open;

    /**
     * Create filter
     *
     * @param repository
     */
    public IssueFilter(final Repository repository) {
        this.repository = repository;
        open = true;
    }

    /**
     * Set only open issues to be returned
     *
     * @param open
     *            true for open issues, false for closed issues
     * @return this filter
     */
    public IssueFilter setOpen(final boolean open) {
        this.open = open;
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
     * @return repository
     */
    public Repository getRepository() {
        return repository;
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
     * Are only open issues returned?
     *
     * @return true if open only, false if closed only
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * @return assignee
     */
    public String getAssignee() {
        return assignee;
    }

    /**
     * Create a map of all the request parameters represented by this filter
     *
     * @return non-null map of filter request parameters
     */
    public Map<String, String> toFilterMap() {
        final Map<String, String> filter = new HashMap<String, String>();

        filter.put(FIELD_SORT, SORT_CREATED);
        filter.put(FIELD_DIRECTION, DIRECTION_DESCENDING);

        if (assignee != null && assignee.length() > 0)
            filter.put(FILTER_ASSIGNEE, assignee);

        if (milestone != null)
            filter.put(FILTER_MILESTONE, Integer.toString(milestone.getNumber()));

        if (labels != null && !labels.isEmpty()) {
            StringBuilder labelsQuery = new StringBuilder();
            for (String label : labels)
                labelsQuery.append(label).append(',');
            filter.put(FILTER_LABELS, labelsQuery.toString());
        }

        if (open)
            filter.put(FILTER_STATE, STATE_OPEN);
        else
            filter.put(FILTER_STATE, STATE_CLOSED);
        return filter;
    }

    /**
     * Get display {@link CharSequence} representing this filter
     *
     * @return display
     */
    public CharSequence toDisplay() {
        List<String> segments = new ArrayList<String>();
        if (open)
            segments.add("Open issues");
        else
            segments.add("Closed issues");

        if (assignee != null)
            segments.add("Assignee: " + assignee);

        if (milestone != null)
            segments.add("Milestone: " + milestone.getTitle());

        if (labels != null && !labels.isEmpty()) {
            StringBuilder builder = new StringBuilder("Labels: ");
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
    public int hashCode() {
        return Arrays.hashCode(new Object[] { open, assignee, milestone, assignee,
                repository != null ? repository.generateId() : null, labels });
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        return a != null && a.equals(b);
    }

    private boolean isEqual(Milestone a, Milestone b) {
        if (a == null && b == null)
            return true;
        return a != null && b != null && a.getNumber() == b.getNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof IssueFilter))
            return false;

        IssueFilter other = (IssueFilter) o;
        return open == other.open && isEqual(milestone, other.milestone) && isEqual(assignee, other.assignee)
                && isEqual(repository, repository) && isEqual(labels, other.labels);
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
