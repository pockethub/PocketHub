package com.github.mobile.ui.user;

import com.github.mobile.ui.StyledText;

import org.eclipse.egit.github.core.event.Event;

/**
 * Created by twer on 3/22/15.
 */
public class TestingIconAndViewTextManager extends IconAndViewTextManager {
    public TestingIconAndViewTextManager(NewsListAdapter newsListAdapter) {
        super(newsListAdapter);
    }

    @Override
    void formatCommitComment(Event event, StyledText main,
                             StyledText details) {

    }

}
