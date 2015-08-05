package com.github.pockethub.ui.user;

import android.text.TextUtils;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.core.issue.IssueUtils;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.TimeUtils;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Download;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.CommitCommentPayload;
import org.eclipse.egit.github.core.event.CreatePayload;
import org.eclipse.egit.github.core.event.DeletePayload;
import org.eclipse.egit.github.core.event.DownloadPayload;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventRepository;
import org.eclipse.egit.github.core.event.FollowPayload;
import org.eclipse.egit.github.core.event.GistPayload;
import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.eclipse.egit.github.core.event.IssuesPayload;
import org.eclipse.egit.github.core.event.MemberPayload;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.event.PullRequestReviewCommentPayload;
import org.eclipse.egit.github.core.event.PushPayload;
import org.eclipse.egit.github.core.event.TeamAddPayload;

import java.util.List;

import static com.github.kevinsawicki.wishlist.ViewUpdater.FORMAT_INT;

public class IconAndViewTextManager {
    public static final String ISSUES_PAYLOAD_ACTION_OPENED = "opened";
    public static final String ISSUES_PAYLOAD_ACTION_REOPENED = "reopened";
    public static final String ISSUES_PAYLOAD_ACTION_CLOSED = "closed";
    private final NewsListAdapter newsListAdapter;

    public IconAndViewTextManager(NewsListAdapter newsListAdapter) {
        this.newsListAdapter = newsListAdapter;
    }

    private void appendComment(final StyledText details,
            final Comment comment) {
        if (comment != null)
            appendText(details, comment.getBody());
    }

    private void appendCommitComment(final StyledText details,
            final CommitComment comment) {
        if (comment == null)
            return;

        String id = comment.getCommitId();
        if (!TextUtils.isEmpty(id)) {
            if (id.length() > 10)
                id = id.substring(0, 10);
            appendText(details, "Comment in");
            details.append(' ');
            details.monospace(id);
            details.append(':').append('\n');
        }
        appendComment(details, comment);
    }

    private void appendText(final StyledText details, String text) {
        if (text == null)
            return;
        text = text.trim();
        if (text.length() == 0)
            return;

        details.append(text);
    }

    private StyledText boldActor(final StyledText text, final Event event) {
        return boldUser(text, event.getActor());
    }

    private StyledText boldUser(final StyledText text, final User user) {
        if (user != null)
            text.bold(user.getLogin());
        return text;
    }

    private StyledText boldRepo(final StyledText text, final Event event) {
        EventRepository repo = event.getRepo();
        if (repo != null)
            text.bold(repo.getName());
        return text;
    }

    private StyledText boldRepoName(final StyledText text,
            final Event event) {
        EventRepository repo = event.getRepo();
        if (repo != null) {
            String name = repo.getName();
            if (!TextUtils.isEmpty(name)) {
                int slash = name.indexOf('/');
                if (slash != -1 && slash + 1 < name.length())
                    text.bold(name.substring(slash + 1));
            }
        }
        return text;
    }

    void formatCommitComment(Event event, StyledText main,
                                    StyledText details) {
        boldActor(main, event);
        main.append(" commented on ");
        boldRepo(main, event);

        CommitCommentPayload payload = (CommitCommentPayload) event
                .getPayload();
        appendCommitComment(details, payload.getComment());
    }

    void formatDownload(Event event, StyledText main,
                               StyledText details) {
        boldActor(main, event);
        main.append(" uploaded a file to ");
        boldRepo(main, event);

        DownloadPayload payload = (DownloadPayload) event.getPayload();
        Download download = payload.getDownload();
        if (download != null)
            appendText(details, download.getName());
    }

    void formatCreate(Event event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        main.append(" created ");
        CreatePayload payload = (CreatePayload) event.getPayload();
        String refType = payload.getRefType();
        main.append(refType);
        main.append(' ');
        if (!"repository".equals(refType)) {
            main.append(payload.getRef());
            main.append(" at ");
            boldRepo(main, event);
        } else
            boldRepoName(main, event);
    }

    void formatDelete(Event event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        DeletePayload payload = (DeletePayload) event.getPayload();
        main.append(" deleted ");
        main.append(payload.getRefType());
        main.append(' ');
        main.append(payload.getRef());
        main.append(" at ");

        boldRepo(main, event);
    }

    void formatFollow(Event event, StyledText main,
                             StyledText details) {
        boldActor(main, event);
        main.append(" started following ");
        boldUser(main, ((FollowPayload) event.getPayload()).getTarget());
    }

    void formatFork(Event event, StyledText main,
                           StyledText details) {
        boldActor(main, event);
        main.append(" forked repository ");
        boldRepo(main, event);
    }

    void formatGist(Event event, StyledText main,
                           StyledText details) {
        boldActor(main, event);

        GistPayload payload = (GistPayload) event.getPayload();

        main.append(' ');
        String action = payload.getAction();
        if ("create".equals(action))
            main.append("created");
        else if ("update".equals(action))
            main.append("updated");
        else
            main.append(action);
        main.append(" Gist ");
        main.append(payload.getGist().getId());
    }

    void formatWiki(Event event, StyledText main,
                           StyledText details) {
        boldActor(main, event);
        main.append(" updated the wiki in ");
        boldRepo(main, event);
    }

    void formatIssueComment(Event event, StyledText main,
                                   StyledText details) {
        boldActor(main, event);

        main.append(" commented on ");

        IssueCommentPayload payload = (IssueCommentPayload) event.getPayload();

        Issue issue = payload.getIssue();
        String number;
        if (IssueUtils.isPullRequest(issue))
            number = "pull request " + issue.getNumber();
        else
            number = "issue " + issue.getNumber();
        main.bold(number);

        main.append(" on ");

        boldRepo(main, event);

        appendComment(details, payload.getComment());
    }

    void formatIssues(Event event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        IssuesPayload payload = (IssuesPayload) event.getPayload();
        String action = payload.getAction();
        Issue issue = payload.getIssue();
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("issue " + issue.getNumber());
        main.append(" on ");

        boldRepo(main, event);

        appendText(details, issue.getTitle());
    }

    void formatAddMember(Event event, StyledText main,
                                StyledText details) {
        boldActor(main, event);
        main.append(" added ");
        User member = ((MemberPayload) event.getPayload()).getMember();
        if (member != null)
            main.bold(member.getLogin());
        main.append(" as a collaborator to ");
        boldRepo(main, event);
    }

    void formatPublic(Event event, StyledText main,
                             StyledText details) {
        boldActor(main, event);
        main.append(" open sourced repository ");
        boldRepo(main, event);
    }

    void formatWatch(Event event, StyledText main,
                            StyledText details) {
        boldActor(main, event);
        main.append(" starred ");
        boldRepo(main, event);
    }

    void formatReviewComment(Event event, StyledText main,
                                    StyledText details) {
        boldActor(main, event);
        main.append(" commented on ");
        boldRepo(main, event);

        PullRequestReviewCommentPayload payload = (PullRequestReviewCommentPayload) event
                .getPayload();
        appendCommitComment(details, payload.getComment());
    }

    void formatPullRequest(Event event, StyledText main,
                                  StyledText details) {
        boldActor(main, event);

        PullRequestPayload payload = (PullRequestPayload) event.getPayload();
        String action = payload.getAction();
        if ("synchronize".equals(action))
            action = "updated";
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("pull request " + payload.getNumber());
        main.append(" on ");

        boldRepo(main, event);

        if (ISSUES_PAYLOAD_ACTION_OPENED.equals(action) || "closed".equals(action)) {
            PullRequest request = payload.getPullRequest();
            if (request != null) {
                String title = request.getTitle();
                if (!TextUtils.isEmpty(title))
                    details.append(title);
            }
        }
    }

    void formatPush(Event event, StyledText main,
                           StyledText details) {
        boldActor(main, event);

        main.append(" pushed to ");
        PushPayload payload = (PushPayload) event.getPayload();
        String ref = payload.getRef();
        if (ref.startsWith("refs/heads/"))
            ref = ref.substring(11);
        main.bold(ref);
        main.append(" at ");

        boldRepo(main, event);

        final List<Commit> commits = payload.getCommits();
        int size = commits != null ? commits.size() : -1;
        if (size > 0) {
            if (size != 1)
                details.append(FORMAT_INT.format(size)).append(" new commits");
            else
                details.append("1 new commit");

            int max = 3;
            int appended = 0;
            for (Commit commit : commits) {
                if (commit == null)
                    continue;

                String sha = commit.getSha();
                if (TextUtils.isEmpty(sha))
                    continue;

                details.append('\n');
                if (sha.length() > 7)
                    details.monospace(sha.substring(0, 7));
                else
                    details.monospace(sha);

                String message = commit.getMessage();
                if (!TextUtils.isEmpty(message)) {
                    details.append(' ');
                    int newline = message.indexOf('\n');
                    if (newline > 0)
                        details.append(message.subSequence(0, newline));
                    else
                        details.append(message);
                }

                appended++;
                if (appended == max)
                    break;
            }
        }
    }

    void formatTeamAdd(Event event, StyledText main,
                              StyledText details) {
        boldActor(main, event);

        TeamAddPayload payload = (TeamAddPayload) event.getPayload();

        main.append(" added ");

        User user = payload.getUser();
        if (user != null)
            boldUser(main, user);
        else
            boldRepoName(main, event);

        main.append(" to team");

        Team team = payload.getTeam();
        String teamName = team != null ? team.getName() : null;
        if (teamName != null)
            main.append(' ').bold(teamName);
    }

    protected void update(int position, Event event) {
        newsListAdapter.getAvatars().bind(newsListAdapter.imageViewAgent(0), event.getActor());

        StyledText main = new StyledText();
        StyledText details = new StyledText();
        String icon = setIconAndFormatStyledText(event, main, details);

        if (icon != null)
            ViewUtils.setGone(newsListAdapter.setTextAgent(3, icon), false);
        else
            newsListAdapter.setGoneAgent(3, true);

        newsListAdapter.setTextAgent(1, main);

        if (!TextUtils.isEmpty(details))
            ViewUtils.setGone(newsListAdapter.setTextAgent(2, details), false);
        else
            newsListAdapter.setGoneAgent(2, true);

        newsListAdapter.setTextAgent(4, TimeUtils.getRelativeTime(event.getCreatedAt()));
    }

    String setIconAndFormatStyledText(Event event, StyledText main, StyledText details) {

        return EventType.valueOf(event.getType()).generateIconAndFormatStyledText(this, event, main, details);
    }
}