package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.view.OcticonTextView;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.payload.PushPayload;

import java.text.NumberFormat;
import java.util.List;

public class PushEventItem extends NewsItem {

    public PushEventItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem);
    }

    @Override
    public void bind(@NonNull NewsItem.ViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.icon.setText(OcticonTextView.ICON_PUSH);

        PushPayload payload = (PushPayload) getData().payload();

        StyledText main = new StyledText();
        boldActor(main, getData());

        main.append(" pushed to ");
        String ref = payload.ref();
        if (ref.startsWith("refs/heads/")) {
            ref = ref.substring(11);
        }
        main.bold(ref);
        main.append(" at ");

        boldRepo(main, getData());
        viewHolder.event.setText(main);

        StyledText details = new StyledText();
        final List<GitCommit> commits = payload.commits();
        int size = commits.size();
        if (size > 0) {
            if (size != 1) {
                NumberFormat numberFormat = NumberFormat.getIntegerInstance();
                details.append(numberFormat.format(size)).append(" new commits");
            } else {
                details.append("1 new commit");
            }

            int max = 3;
            int appended = 0;
            for (GitCommit commit : commits) {
                if (commit == null) {
                    continue;
                }

                String sha = commit.sha();
                if (TextUtils.isEmpty(sha)) {
                    continue;
                }

                details.append('\n');
                if (sha.length() > 7) {
                    details.monospace(sha.substring(0, 7));
                } else {
                    details.monospace(sha);
                }

                String message = commit.message();
                if (!TextUtils.isEmpty(message)) {
                    details.append(' ');
                    int newline = message.indexOf('\n');
                    if (newline > 0) {
                        details.append(message.subSequence(0, newline));
                    } else {
                        details.append(message);
                    }
                }

                appended++;
                if (appended == max) {
                    break;
                }
            }
        }

        if (TextUtils.isEmpty(details)) {
            viewHolder.details.setVisibility(View.GONE);
        } else {
            viewHolder.details.setText(details);
        }
    }
}
