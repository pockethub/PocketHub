package com.github.mobile.ui.user;

import com.github.mobile.util.TypefaceUtils;

import org.eclipse.egit.github.core.event.Event;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IconAndViewTextManagerTest {

    @Test
    public void when_event_type_is_commit_comment_then_icon_should_be_comment() throws Exception {
        // Arrange
        IconAndViewTextManager iconAndViewTextManager = new TestingIconAndViewTextManager(null);
        Event event = new Event();
        event.setType(Event.TYPE_COMMIT_COMMENT);

        // Act
        String icon = iconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_COMMENT, icon);
    }
}