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

package com.github.pockethub.ui.user;

import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.events.EventType;
import com.alorma.github.sdk.bean.dto.response.events.payload.Payload;
import com.github.pockethub.BuildConfig;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.TypefaceUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IconAndViewTextManagerTest {

    @Test
    public void when_event_type_is_commit_comment_then_icon_should_be_comment_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GithubEvent event = new GithubEvent();
        event.type = EventType.CommitCommentEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.CreateEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.DeleteEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.DownloadEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.FollowEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.ForkEvent;

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatFork(event, new StyledText(), null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, new StyledText(), null);

        // Assert
        assertEquals(TypefaceUtils.ICON_FORK, icon);
        verify(spyIconAndViewTextManager).formatFork(event, new StyledText(), null);
    }

    @Test
    public void when_event_type_is_gist_then_icon_should_be_gist_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GithubEvent event = new GithubEvent();
        event.type = EventType.GistEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.GollumEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.IssueCommentEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.IssuesEvent;
        Payload payload = new Payload();
        payload.action = IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_OPENED;
        event.payload = payload;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.IssuesEvent;
        Payload payload = new Payload();
        payload.action = IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_REOPENED;
        event.payload = payload;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.IssuesEvent;
        Payload payload = new Payload();
        payload.action = IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_CLOSED;
        event.payload = payload;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.MemberEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.PublicEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.PullRequestEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.PullRequestReviewCommentEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.PushEvent;

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
        GithubEvent event = new GithubEvent();
        event.type = EventType.WatchEvent;

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
