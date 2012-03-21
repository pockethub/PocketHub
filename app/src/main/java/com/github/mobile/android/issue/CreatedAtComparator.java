package com.github.mobile.android.issue;

import java.util.Comparator;

import org.eclipse.egit.github.core.Issue;

/**
 * Comparators that sort issue's by created at date
 */
public class CreatedAtComparator implements Comparator<Issue> {

    /**
     * Descending order
     */
    public static final int DESC = -1;

    /**
     * Ascending order
     */
    public static final int ASC = 1;

    private final int direction;

    /**
     * Create comparator in descending order
     */
    public CreatedAtComparator() {
        this(DESC);
    }

    /**
     * Create comparator
     *
     * @param direction
     */
    public CreatedAtComparator(final int direction) {
        this.direction = direction;
    }

    public int compare(Issue issue1, Issue issue2) {
        return issue1.getCreatedAt().compareTo(issue2.getCreatedAt()) * direction;
    }
}
