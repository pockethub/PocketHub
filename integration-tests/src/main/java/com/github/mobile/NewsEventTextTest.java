package com.github.mobile;

import static org.eclipse.egit.github.core.event.Event.TYPE_COMMIT_COMMENT;
import static org.eclipse.egit.github.core.event.Event.TYPE_CREATE;
import static org.eclipse.egit.github.core.event.Event.TYPE_DELETE;
import static org.eclipse.egit.github.core.event.Event.TYPE_FOLLOW;
import static org.eclipse.egit.github.core.event.Event.TYPE_GIST;
import static org.eclipse.egit.github.core.event.Event.TYPE_GOLLUM;
import static org.eclipse.egit.github.core.event.Event.TYPE_ISSUES;
import static org.eclipse.egit.github.core.event.Event.TYPE_ISSUE_COMMENT;
import static org.eclipse.egit.github.core.event.Event.TYPE_MEMBER;
import static org.eclipse.egit.github.core.event.Event.TYPE_PUBLIC;
import static org.eclipse.egit.github.core.event.Event.TYPE_PULL_REQUEST;
import static org.eclipse.egit.github.core.event.Event.TYPE_PUSH;
import static org.eclipse.egit.github.core.event.Event.TYPE_TEAM_ADD;
import static org.eclipse.egit.github.core.event.Event.TYPE_WATCH;
import android.test.AndroidTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.ui.user.NewsEventViewHolder;
import com.github.mobile.util.AvatarHelper;

import java.util.Date;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.CreatePayload;
import org.eclipse.egit.github.core.event.DeletePayload;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventRepository;
import org.eclipse.egit.github.core.event.FollowPayload;
import org.eclipse.egit.github.core.event.GistPayload;
import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.eclipse.egit.github.core.event.IssuesPayload;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.event.PushPayload;
import org.eclipse.egit.github.core.event.TeamAddPayload;

/**
 * Tests of the news text rendering
 */
public class NewsEventTextTest extends AndroidTestCase {

	private NewsEventViewHolder holder;

	private TextView text;

	private User actor;

	private EventRepository repo;

	private Date date;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		date = new Date();
		actor = new User().setLogin("user");
		repo = new EventRepository().setName("user/repo");

		View view = LayoutInflater.from(mContext).inflate(layout.event_item,
				null);
		text = (TextView) view.findViewById(id.tv_event);
		assertNotNull(text);
		AvatarHelper helper = new AvatarHelper(mContext);
		holder = new NewsEventViewHolder(view, helper);
	}

	private Event createEvent(String type) {
		Event event = new Event();
		event.setCreatedAt(date);
		event.setType(type);
		event.setActor(actor);
		event.setRepo(repo);
		return event;
	}

	private void verify(String expected) {
		CharSequence actual = text.getText();
		assertNotNull(actual);
		assertEquals(expected, actual.toString());
	}

	/**
	 * Verify text of commit comment event
	 */
	public void testCommitCommentEvent() {
		Event event = createEvent(TYPE_COMMIT_COMMENT);
		holder.updateViewFor(event);

		CharSequence content = text.getText();
		assertNotNull(content);
		assertEquals("user commented on commit on user/repo",
				content.toString());
	}

	/**
	 * Verify text of create event
	 */
	public void testCreateRepositoryEvent() {
		Event event = createEvent(TYPE_CREATE);
		CreatePayload payload = new CreatePayload();
		payload.setRefType("repository");
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user created repository repo");
	}

	/**
	 * Verify text of create event
	 */
	public void testCreateBranchEvent() {
		Event event = createEvent(TYPE_CREATE);
		CreatePayload payload = new CreatePayload();
		payload.setRefType("branch");
		payload.setRef("b1");
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user created branch b1 at user/repo");
	}

	/**
	 * Verify text of deleve event
	 */
	public void testDelete() {
		Event event = createEvent(TYPE_DELETE);
		DeletePayload payload = new DeletePayload();
		payload.setRefType("branch");
		payload.setRef("b1");
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user deleted branch b1 at user/repo");
	}

	/**
	 * Verify text of follow event
	 */
	public void testFollow() {
		Event event = createEvent(TYPE_FOLLOW);
		FollowPayload payload = new FollowPayload();
		payload.setTarget(new User().setLogin("user2"));
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user started following user2");
	}

	/**
	 * Verify text of Gist event
	 */
	public void testGist() {
		Event event = createEvent(TYPE_GIST);
		GistPayload payload = new GistPayload();
		payload.setAction("create");
		payload.setGist(new Gist().setId("1"));
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user created Gist 1");
	}

	/**
	 * Verify text of wiki event
	 */
	public void testWiki() {
		Event event = createEvent(TYPE_GOLLUM);
		holder.updateViewFor(event);

		verify("user updated the wiki in user/repo");
	}

	/**
	 * Verify text of issue comment event
	 */
	public void testIssueComment() {
		Event event = createEvent(TYPE_ISSUE_COMMENT);
		IssueCommentPayload payload = new IssueCommentPayload();
		payload.setIssue(new Issue().setNumber(5));
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user commented on issue 5 on user/repo");
	}

	/**
	 * Verify text of issue event
	 */
	public void testIssue() {
		Event event = createEvent(TYPE_ISSUES);
		IssuesPayload payload = new IssuesPayload();
		payload.setAction("closed");
		payload.setIssue(new Issue().setNumber(8));
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user closed issue 8 on user/repo");
	}

	/**
	 * Verify text of member event
	 */
	public void testAddMember() {
		Event event = createEvent(TYPE_MEMBER);
		holder.updateViewFor(event);

		verify("user was added as a collaborator to user/repo");
	}

	/**
	 * Verify text of open sourced event
	 */
	public void testOpenSourced() {
		Event event = createEvent(TYPE_PUBLIC);
		holder.updateViewFor(event);

		verify("user open sourced repository user/repo");
	}

	/**
	 * Verify text of watch event
	 */
	public void testWatch() {
		Event event = createEvent(TYPE_WATCH);
		holder.updateViewFor(event);

		verify("user started watching user/repo");
	}

	/**
	 * Verify text of pull request event
	 */
	public void testPullRequest() {
		Event event = createEvent(TYPE_PULL_REQUEST);
		PullRequestPayload payload = new PullRequestPayload();
		payload.setNumber(30);
		payload.setAction("merged");
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user merged pull request 30 on user/repo");
	}

	/**
	 * Verify text of push event
	 */
	public void testPush() {
		Event event = createEvent(TYPE_PUSH);
		PushPayload payload = new PushPayload();
		payload.setRef("refs/heads/master");
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user pushed to master at user/repo");
	}

	/**
	 * Verify text of push event
	 */
	public void testTeamAdd() {
		Event event = createEvent(TYPE_TEAM_ADD);
		TeamAddPayload payload = new TeamAddPayload();
		payload.setTeam(new Team().setName("t1"));
		payload.setUser(new User().setLogin("u2"));
		event.setPayload(payload);
		holder.updateViewFor(event);

		verify("user added u2 to team t1");
	}
}
