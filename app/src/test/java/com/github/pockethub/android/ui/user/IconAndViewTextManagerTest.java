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

package com.github.pockethub.android.ui.user;

import com.github.pockethub.android.BuildConfig;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.payload.IssuesPayload;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IconAndViewTextManagerTest {

    @Test
    public void when_event_type_is_commit_comment_then_icon_should_be_comment_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.CommitCommentEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatCommitComment(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_COMMENT, icon);
        verify(spyIconAndViewTextManager).formatCommitComment(event, null, null);
    }

    @Test
    public void when_event_type_is_create_then_icon_should_be_create_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.CreateEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatCreate(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_CREATE, icon);
        verify(spyIconAndViewTextManager).formatCreate(event, null, null);
    }

    @Test
    public void when_event_type_is_delete_then_icon_should_be_delete_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.DeleteEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatDelete(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_DELETE, icon);
        verify(spyIconAndViewTextManager).formatDelete(event, null, null);
    }

    @Test
    public void when_event_type_is_download_then_icon_should_be_upload_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.DownloadEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatDownload(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_UPLOAD, icon);
        verify(spyIconAndViewTextManager).formatDownload(event, null, null);
    }

    @Test
    public void when_event_type_is_follow_then_icon_should_be_follow_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.FollowEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatFollow(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_FOLLOW, icon);
        verify(spyIconAndViewTextManager).formatFollow(event, null, null);
    }

    @Test
    public void when_event_type_is_fork_then_icon_should_be_fork_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.ForkEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatFork(event, new StyledText(), null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, new StyledText(), null);

        // Assert
        assertEquals(OcticonTextView.ICON_FORK, icon);
        verify(spyIconAndViewTextManager).formatFork(event, new StyledText(), null);
    }

    @Test
    public void when_event_type_is_gist_then_icon_should_be_gist_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.GistEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatGist(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_GIST, icon);
        verify(spyIconAndViewTextManager).formatGist(event, null, null);
    }

    @Test
    public void when_event_type_is_gollum_then_icon_should_be_gollum_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.GollumEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatWiki(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_WIKI, icon);
        verify(spyIconAndViewTextManager).formatWiki(event, null, null);
    }

    @Test
    public void when_event_type_is_issue_comment_then_icon_should_be_issue_comment_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.IssueCommentEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssueComment(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_ISSUE_COMMENT, icon);
        verify(spyIconAndViewTextManager).formatIssueComment(event, null, null);
    }

    @Test
    public void when_event_type_is_issues_and_action_is_opened_then_icon_should_be_issue_open_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        IssuesPayload payload = IssuesPayload.builder()
                .action(IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_OPENED)
                .build();

        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.IssuesEvent)
                .payload(payload)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssues(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_ISSUE_OPEN, icon);
        verify(spyIconAndViewTextManager).formatIssues(event, null, null);
    }

    @Test
    public void when_event_type_is_issues_and_action_is_reopened_then_icon_should_be_issue_reopen_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        IssuesPayload payload = IssuesPayload.builder()
                .action(IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_REOPENED)
                .build();

        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.IssuesEvent)
                .payload(payload)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssues(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_ISSUE_REOPEN, icon);
        verify(spyIconAndViewTextManager).formatIssues(event, null, null);
    }

    @Test
    public void when_event_type_is_issues_and_action_is_closed_then_icon_should_be_issue_close_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        IssuesPayload payload = IssuesPayload.builder()
                .action(IconAndViewTextManager.ISSUES_PAYLOAD_ACTION_CLOSED)
                .build();

        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.IssuesEvent)
                .payload(payload)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatIssues(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_ISSUE_CLOSE, icon);
        verify(spyIconAndViewTextManager).formatIssues(event, null, null);
    }

    @Test
    public void when_event_type_is_member_then_icon_should_be_add_member_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.MemberEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatAddMember(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_ADD_MEMBER, icon);
        verify(spyIconAndViewTextManager).formatAddMember(event, null, null);
    }

    @Test
    public void when_event_type_is_public_then_icon_should_be_null_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.PublicEvent)
                .build();

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
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.PullRequestEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatPullRequest(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_PULL_REQUEST, icon);
        verify(spyIconAndViewTextManager).formatPullRequest(event, null, null);
    }

    @Test
    public void when_event_type_is_pull_request_review_comment_then_icon_should_be_comment_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.PullRequestReviewCommentEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatReviewComment(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_COMMENT, icon);
        verify(spyIconAndViewTextManager).formatReviewComment(event, null, null);
    }

    @Test
    public void when_event_type_is_push_then_icon_should_be_push_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.PushEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatPush(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_PUSH, icon);
        verify(spyIconAndViewTextManager).formatPush(event, null, null);
    }

    @Test
    public void when_event_type_is_watch_then_icon_should_be_star_and_its_text_should_be_formatted() throws Exception {
        // Arrange
        GitHubEvent event = GitHubEvent.builder()
                .type(GitHubEventType.WatchEvent)
                .build();

        IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(null);
        IconAndViewTextManager spyIconAndViewTextManager = spy(iconAndViewTextManager);
        doNothing().when(spyIconAndViewTextManager).formatWatch(event, null, null);

        // Act
        String icon = spyIconAndViewTextManager.setIconAndFormatStyledText(event, null, null);

        // Assert
        assertEquals(OcticonTextView.ICON_STAR, icon);
        verify(spyIconAndViewTextManager).formatWatch(event, null, null);
    }
}
