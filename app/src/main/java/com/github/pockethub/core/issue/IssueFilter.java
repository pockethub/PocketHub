/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.core.issue;

import android.os.Parcel;
import android.os.Parcelable;

import com.alorma.github.sdk.bean.dto.response.Label;
import com.alorma.github.sdk.bean.dto.response.Milestone;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
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

/**
 * Issue filter containing at least one valid query
 */
public class IssueFilter implements Parcelable, Cloneable, Comparator<Label> {

    /** serialVersionUID */
    private static final long serialVersionUID = 7310646589186299063L;

    private final Repo repository;

    private List<Label> labels;

    private Milestone milestone;

    private User assignee;

    private boolean open;

    /**
     * Create filter
     *
     * @param repository
     */
    public IssueFilter(final Repo repository) {
        this.repository = repository;
        open = true;
    }

    protected IssueFilter(Parcel in) {
        repository = in.readParcelable(Repo.class.getClassLoader());
        labels = new ArrayList<>();
        in.readTypedList(labels, Label.CREATOR);
        milestone = in.readParcelable(Milestone.class.getClassLoader());
        assignee = in.readParcelable(User.class.getClassLoader());
        open = in.readByte() != 0;
    }

    public static final Creator<IssueFilter> CREATOR = new Creator<IssueFilter>() {
        @Override
        public IssueFilter createFromParcel(Parcel in) {
            return new IssueFilter(in);
        }

        @Override
        public IssueFilter[] newArray(int size) {
            return new IssueFilter[size];
        }
    };

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
    public IssueFilter addLabel(Label label) {
        if (label == null)
            return this;
        if (labels == null)
            labels = new ArrayList<>();
        labels.add(label);
        return this;
    }

    /**
     * @param labels
     * @return this filter
     */
    public IssueFilter setLabels(Collection<Label> labels) {
        if (labels != null && !labels.isEmpty()) {
            if (this.labels == null)
                this.labels = new ArrayList<>();
            else
                this.labels.clear();
            this.labels.addAll(labels);
        } else
            this.labels = null;
        return this;
    }

    /**
     * @return labels
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * @return repository
     */
    public Repo getRepository() {
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
    public IssueFilter setAssignee(User assignee) {
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
    public User getAssignee() {
        return assignee;
    }

    /**
     * Create a map of all the request parameters represented by this filter
     *
     * @return non-null map of filter request parameters
     */
    public Map<String, String> toFilterMap() {
        final Map<String, String> filter = new HashMap<>();

        filter.put(FIELD_SORT, SORT_CREATED);
        filter.put(FIELD_DIRECTION, DIRECTION_DESCENDING);

        if (assignee != null)
            filter.put(FILTER_ASSIGNEE, assignee.login);

        if (milestone != null)
            filter.put(FILTER_MILESTONE,
                    Integer.toString(milestone.number));

        if (labels != null && !labels.isEmpty()) {
            StringBuilder labelsQuery = new StringBuilder();
            for (Label label : labels)
                labelsQuery.append(label.name).append(',');
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
        List<String> segments = new ArrayList<>();
        if (open)
            segments.add("Open issues");
        else
            segments.add("Closed issues");

        if (assignee != null)
            segments.add("Assignee: " + assignee.login);

        if (milestone != null)
            segments.add("Milestone: " + milestone.title);

        if (labels != null && !labels.isEmpty()) {
            StringBuilder builder = new StringBuilder("Labels: ");
            for (Label label : labels)
                builder.append(label.name).append(',').append(' ');
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
        return Arrays.hashCode(new Object[] { open,
                assignee != null ? assignee.id : null,
                milestone != null ? milestone.number : null,
                assignee != null ? assignee.id : null,
                repository != null ? repository.id : null, labels });
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        return a != null && a.equals(b);
    }

    private boolean isEqual(Milestone a, Milestone b) {
        if (a == null && b == null)
            return true;
        return a != null && b != null && a.number == b.number;
    }

    private boolean isEqual(User a, User b) {
        if (a == null && b == null)
            return true;
        return a != null && b != null && a.id == b.id;
    }

    private boolean isEqual(Repo a, Repo b) {
        return a != null && b != null && a.id == b.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof IssueFilter))
            return false;

        IssueFilter other = (IssueFilter) o;
        return open == other.open && isEqual(milestone, other.milestone)
                && isEqual(assignee, other.assignee)
                && isEqual(repository, repository)
                && isEqual(labels, other.labels);
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

    @Override
    public int compare(Label lhs, Label rhs) {
        return CASE_INSENSITIVE_ORDER.compare(lhs.name, rhs.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(repository, flags);
        dest.writeTypedList(labels);
        dest.writeParcelable(milestone, flags);
        dest.writeParcelable(assignee, flags);
        dest.writeByte((byte) (open ? 1 : 0));
    }
}
