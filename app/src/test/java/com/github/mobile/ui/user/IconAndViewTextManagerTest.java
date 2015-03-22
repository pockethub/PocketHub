package com.github.mobile.ui.user;

import com.github.mobile.util.TypefaceUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IconAndViewTextManagerTest {

    @Test
    public void when_event_type_is_commit_comment_then_icon_should_be_comment() throws Exception {
        // Arrange

        // Act
        String icon = iconAndViewTextManager.setIconAndFormatStyledText(event, main, details);

        // Assert
        assertEquals(TypefaceUtils.ICON_COMMENT, icon);
    }
}