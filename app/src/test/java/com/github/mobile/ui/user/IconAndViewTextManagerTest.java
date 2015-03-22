package com.github.mobile.ui.user;

import com.github.mobile.util.TypefaceUtils;

import org.eclipse.egit.github.core.event.Event;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class IconAndViewTextManagerTest {

    @Test
    public void when_event_type_is_commit_comment_then_icon_should_be_comment_and_commit_comment_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_COMMIT_COMMENT);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatCommitComment(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_COMMENT, icon);
        verify(spyIconAndViewTextManager).formatCommitComment(event, null, null);
    }

    @Test
    public void when_event_type_is_create_then_icon_should_be_create_and_create_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_CREATE);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatCreate(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_CREATE, icon);
        verify(spyIconAndViewTextManager).formatCreate(event, null, null);
    }
}