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
package com.github.pockethub.tests;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.Team;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.dto.response.events.EventType;
import com.alorma.github.sdk.bean.dto.response.events.payload.Payload;
import com.github.pockethub.R.id;
import com.github.pockethub.api.FollowEventPayload;
import com.github.pockethub.api.GistEventPayload;
import com.github.pockethub.api.MemberEventPayload;
import com.github.pockethub.ui.user.NewsListAdapter;
import com.github.pockethub.util.AvatarLoader;

/**
 * Tests of the news text rendering
 */
public class NewsEventTextTest extends InstrumentationTestCase {

    private NewsListAdapter adapter;

    private TextView text;

    private User actor;

    private Repo repo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        actor = new User();
        actor.login = "user";
        repo = new Repo();
        repo.name = "user/repo";

        Context context = getInstrumentation().getTargetContext();
        adapter = new NewsListAdapter(LayoutInflater.from(context),
                new AvatarLoader(context));
    }

    private GithubEvent createEvent(EventType type) {
        GithubEvent event = new GithubEvent();
        event.created_at = "2015-01-01T00:00:00Z";
        event.type = type;
        event.actor = actor;
        event.repo = repo;
        return event;
    }

    private void verify(String expected) {
        CharSequence actual = text.getText();
        assertNotNull(actual);
        assertEquals(expected, actual.toString());
    }

    private void updateView(GithubEvent event) {
        adapter.setItems(new Object[] { event });
        View view = adapter.getView(0, null, null);
        assertNotNull(view);
        text = (TextView) view.findViewById(id.tv_event);
        assertNotNull(text);
    }

    /**
     * Verify text of commit comment event
     */
    @UiThreadTest
    public void testCommitCommentEvent() {
        GithubEvent event = createEvent(EventType.CommitCommentEvent);
        event.payload = new Payload();
        updateView(event);

        verify("user commented on user/repo");
    }

    /**
     * Verify text of create event
     */
    @UiThreadTest
    public void testCreateRepositoryEvent() {
        GithubEvent event = createEvent(EventType.CreateEvent);
        Payload payload = new Payload();
        payload.ref_type = "repository";
        event.payload = payload;
        updateView(event);

        verify("user created repository repo");
    }

    /**
     * Verify text of create event
     */
    @UiThreadTest
    public void testCreateBranchEvent() {
        GithubEvent event = createEvent(EventType.CreateEvent);
        Payload payload = new Payload();
        payload.ref_type = "branch";
        payload.ref = "b1";
        event.payload = payload;
        updateView(event);

        verify("user created branch b1 at user/repo");
    }

    /**
     * Verify text of delete event
     */
    @UiThreadTest
    public void testDelete() {
        GithubEvent event = createEvent(EventType.DeleteEvent);
        Payload payload = new Payload();
        payload.ref_type = "branch";
        payload.ref = "b1";
        event.payload = payload;
        updateView(event);

        verify("user deleted branch b1 at user/repo");
    }

    /**
     * Verify text of follow event
     */
    @UiThreadTest
    public void testFollow() {
        GithubEvent event = createEvent(EventType.FollowEvent);
        FollowEventPayload payload = new FollowEventPayload();

        User target = new User();
        target.login = "user2";
        payload.target = target;

        event.payload = payload;
        updateView(event);

        verify("user started following user2");
    }

    /**
     * Verify text of Gist event
     */
    @UiThreadTest
    public void testGist() {
        GithubEvent event = createEvent(EventType.GistEvent);
        GistEventPayload payload = new GistEventPayload();
        payload.action = "create";
        Gist gist = new Gist();
        gist.id = "1";
        payload.gist = gist;
        event.payload = payload;
        updateView(event);

        verify("user created Gist 1");
    }

    /**
     * Verify text of wiki event
     */
    @UiThreadTest
    public void testWiki() {
        GithubEvent event = createEvent(EventType.GollumEvent);
        updateView(event);

        verify("user updated the wiki in user/repo");
    }

    /**
     * Verify text of issue comment event
     */
    @UiThreadTest
    public void testIssueComment() {
        GithubEvent event = createEvent(EventType.IssueCommentEvent);
        Payload payload = new Payload();
        Issue issue = new Issue();
        issue.number = 5;
        payload.issue = issue;
        event.payload = payload;
        updateView(event);

        verify("user commented on issue 5 on user/repo");
    }

    /**
     * Verify text of issue event
     */
    @UiThreadTest
    public void testIssue() {
        GithubEvent event = createEvent(EventType.IssuesEvent);
        Payload payload = new Payload();
        payload.action = "closed";
        Issue issue = new Issue();
        issue.number = 8;
        payload.issue = issue;
        event.payload = payload;
        updateView(event);

        verify("user closed issue 8 on user/repo");
    }

    /**
     * Verify text of member event
     */
    @UiThreadTest
    public void testAddMember() {
        GithubEvent event = createEvent(EventType.MemberEvent);
        MemberEventPayload payload = new MemberEventPayload();
        User user = new User();
        user.login = "person";
        payload.member = user;
        event.payload = payload;
        updateView(event);

        verify("user added person as a collaborator to user/repo");
    }

    /**
     * Verify text of open sourced event
     */
    @UiThreadTest
    public void testOpenSourced() {
        GithubEvent event = createEvent(EventType.PublicEvent);
        updateView(event);

        verify("user open sourced repository user/repo");
    }

    /**
     * Verify text of watch event
     */
    @UiThreadTest
    public void testWatch() {
        GithubEvent event = createEvent(EventType.WatchEvent);
        updateView(event);

        verify("user starred user/repo");
    }

    /**
     * Verify text of pull request event
     */
    @UiThreadTest
    public void testPullRequest() {
        GithubEvent event = createEvent(EventType.PullRequestEvent);
        Payload payload = new Payload();
        payload.number = 30;
        payload.action = "merged";
        event.payload = payload;
        updateView(event);

        verify("user merged pull request 30 on user/repo");
    }

    /**
     * Verify text of push event
     */
    @UiThreadTest
    public void testPush() {
        GithubEvent event = createEvent(EventType.PushEvent);
        Payload payload = new Payload();
        payload.ref = "refs/heads/master";
        event.payload = payload;
        updateView(event);

        verify("user pushed to master at user/repo");
    }

    /**
     * Verify text of push event
     */
    @UiThreadTest
    public void testTeamAdd() {
        GithubEvent event = createEvent(EventType.TeamAddEvent);
        Payload payload = new Payload();

        Team team = new Team();
        team.name = "t1";

        Repo repo = new Repo();
        repo.name = "r2";

        payload.team = team;
        payload.repository = repo;

        event.payload = payload;

        updateView(event);

        verify("user added r2 to team t1");
    }
}
