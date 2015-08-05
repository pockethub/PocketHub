package com.github.pockethub.ui.user;

import com.github.pockethub.util.TypefaceUtils;

import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.IssuesPayload;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class IconAndViewTextManagerTest {

    @Test
    public void when_event_type_is_commit_comment_then_icon_should_be_comment_and_its_text_should_be_formatted() throws Exception {
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
    public void when_event_type_is_create_then_icon_should_be_create_and_its_text_should_be_formatted() throws Exception {
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

    @Test
    public void when_event_type_is_delete_then_icon_should_be_delete_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_DELETE);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatDelete(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_DELETE, icon);
        verify(spyIconAndViewTextManager).formatDelete(event, null, null);
    }

    @Test
    public void when_event_type_is_download_then_icon_should_be_upload_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_DOWNLOAD);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatDownload(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_UPLOAD, icon);
        verify(spyIconAndViewTextManager).formatDownload(event, null, null);
    }

    @Test
    public void when_event_type_is_follow_then_icon_should_be_follow_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_FOLLOW);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatFollow(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_FOLLOW, icon);
        verify(spyIconAndViewTextManager).formatFollow(event, null, null);
    }

    @Test
    public void when_event_type_is_fork_then_icon_should_be_fork_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_FORK);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatFork(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_FORK, icon);
        verify(spyIconAndViewTextManager).formatFork(event, null, null);
    }

    @Test
    public void when_event_type_is_gist_then_icon_should_be_gist_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_GIST);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatGist(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_GIST, icon);
        verify(spyIconAndViewTextManager).formatGist(event, null, null);
    }

    @Test
    public void when_event_type_is_gollum_then_icon_should_be_gollum_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_GOLLUM);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatWiki(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_WIKI, icon);
        verify(spyIconAndViewTextManager).formatWiki(event, null, null);
    }

    @Test
    public void when_event_type_is_issue_comment_then_icon_should_be_issue_comment_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_ISSUE_COMMENT);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssueComment(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_ISSUE_COMMENT, icon);
        verify(spyIconAndViewTextManager).formatIssueComment(event, null, null);
    }

    @Test
    public void when_event_type_is_issues_and_action_is_opened_then_icon_should_be_issue_open_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_ISSUES);
        IssuesPayload payload = new IssuesPayload();
        payload.setAction(IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_OPENED);
        event.setPayload(payload);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssues(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_ISSUE_OPEN, icon);
        verify(spyIconAndViewTextManager).formatIssues(event, null, null);
    }

    @Test
    public void when_event_type_is_issues_and_action_is_reopened_then_icon_should_be_issue_reopen_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_ISSUES);
        IssuesPayload payload = new IssuesPayload();
        payload.setAction(IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_REOPENED);
        event.setPayload(payload);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssues(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_ISSUE_REOPEN, icon);
        verify(spyIconAndViewTextManager).formatIssues(event, null, null);
    }

    @Test
    public void when_event_type_is_issues_and_action_is_closed_then_icon_should_be_issue_close_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_ISSUES);
        IssuesPayload payload = new IssuesPayload();
        payload.setAction(IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_CLOSED);
        event.setPayload(payload);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssues(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_ISSUE_CLOSE, icon);
        verify(spyIconAndViewTextManager).formatIssues(event, null, null);
    }

    @Test
    public void when_event_type_is_member_then_icon_should_be_add_member_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_MEMBER);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatAddMember(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_ADD_MEMBER, icon);
        verify(spyIconAndViewTextManager).formatAddMember(event, null, null);
    }

    @Test
    public void when_event_type_is_public_then_icon_should_be_null_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_PUBLIC);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatPublic(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(null, icon);
        verify(spyIconAndViewTextManager).formatPublic(event, null, null);
    }

    @Test
    public void when_event_type_is_pull_request_then_icon_should_be_pull_request_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_PULL_REQUEST);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatPullRequest(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_PULL_REQUEST, icon);
        verify(spyIconAndViewTextManager).formatPullRequest(event, null, null);
    }

    @Test
    public void when_event_type_is_pull_request_review_comment_then_icon_should_be_comment_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_PULL_REQUEST_REVIEW_COMMENT);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatReviewComment(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_COMMENT, icon);
        verify(spyIconAndViewTextManager).formatReviewComment(event, null, null);
    }

    @Test
    public void when_event_type_is_push_then_icon_should_be_push_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_PUSH);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatPush(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_PUSH, icon);
        verify(spyIconAndViewTextManager).formatPush(event, null, null);
    }

    @Test
    public void when_event_type_is_watch_then_icon_should_be_star_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        Event event = new Event();
        event.setType(Event.TYPE_WATCH);

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatWatch(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(TypefaceUtils.ICON_STAR, icon);
        verify(spyIconAndViewTextManager).formatWatch(event, null, null);
    }
}