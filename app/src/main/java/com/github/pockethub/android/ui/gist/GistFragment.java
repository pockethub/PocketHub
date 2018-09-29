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
package com.github.pockethub.android.ui.gist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.core.OnLoadListener;
import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.core.gist.RefreshGistTaskFactory;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.DialogResultListener;
import com.github.pockethub.android.ui.base.BaseFragment;
import com.github.pockethub.android.ui.comment.DeleteCommentListener;
import com.github.pockethub.android.ui.comment.EditCommentListener;
import com.github.pockethub.android.ui.item.GitHubCommentItem;
import com.github.pockethub.android.ui.item.LoadingItem;
import com.github.pockethub.android.ui.item.gist.GistFileItem;
import com.github.pockethub.android.ui.item.gist.GistHeaderItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GistFile;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.gists.GistCommentService;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.Section;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.pockethub.android.Intents.EXTRA_COMMENT;
import static com.github.pockethub.android.Intents.EXTRA_GIST_ID;
import static com.github.pockethub.android.RequestCodes.*;

/**
 * Activity to display an existing Gist
 */
public class GistFragment extends BaseFragment implements OnItemClickListener, DialogResultListener {

    private static final String TAG = "GistFragment";

    private String gistId;

    private List<GitHubComment> comments;

    private Gist gist;

    @BindView(R.id.list)
    protected RecyclerView list;

    @BindView(R.id.pb_loading)
    protected ProgressBar progress;

    @Inject
    protected GistStore store;

    @Inject
    protected RefreshGistTaskFactory refreshGistTaskFactory;

    @Inject
    protected HttpImageGetter imageGetter;

    private GroupAdapter adapter = new GroupAdapter();

    private Section mainSection = new Section();

    private Section filesSection = new Section();

    private Section commentsSection = new Section();

    private boolean starred;

    private boolean loadFinished;

    @Inject
    protected AvatarLoader avatars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gistId = getArguments().getString(EXTRA_GIST_ID);
        gist = store.getGist(gistId);

        mainSection.add(filesSection);
        mainSection.add(commentsSection);
        adapter.add(mainSection);

        adapter.setOnItemClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration itemDecoration =
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_divider_5dp));

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(itemDecoration);
        list.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (gist != null) {
            updateHeader(gist);
            updateFiles(gist);
        }

        if (gist == null || (gist.comments() > 0 && comments == null)) {
            mainSection.setFooter(new LoadingItem(R.string.loading_comments));
        }

        if (gist != null && comments != null) {
            updateList(gist, comments);
        } else {
            refreshGist();
        }
    }

    private boolean isOwner() {
        if (gist == null) {
            return false;
        }
        User user = gist.owner();
        if (user == null) {
            return false;
        }
        String login = AccountUtils.getLogin(getActivity());
        return login != null && login.equals(user.login());
    }

    private void updateHeader(Gist gist) {
        mainSection.setHeader(new GistHeaderItem(getActivity(), gist));
        progress.setVisibility(GONE);
        list.setVisibility(VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu options, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_gist_view, options);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean owner = isOwner();
        if (!owner) {
            menu.removeItem(R.id.m_delete);
            MenuItem starItem = menu.findItem(R.id.m_star);
            starItem.setEnabled(loadFinished && !owner);
            if (starred) {
                starItem.setTitle(R.string.unstar);
            } else {
                starItem.setTitle(R.string.star);
            }
        } else {
            menu.removeItem(R.id.m_star);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (gist == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
        case R.id.m_comment:
            startActivityForResult(CreateCommentActivity.createIntent(gist),
                    COMMENT_CREATE);
            return true;
        case R.id.m_star:
            if (starred) {
                unstarGist();
            } else {
                starGist();
            }
            return true;
        case R.id.m_refresh:
            refreshGist();
            return true;
        case R.id.m_share:
            shareGist();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void starGist() {
        ToastUtils.show(getActivity(), R.string.starring_gist);
        ServiceGenerator.createService(getActivity(), GistService.class)
                .starGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> starred = response.code() == 204,
                        e -> ToastUtils.show((Activity) getContext(), e.getMessage()));
    }

    private void shareGist() {
        StringBuilder subject = new StringBuilder("Gist ");
        String id = gist.id();
        subject.append(id);
        User user = gist.owner();
        if (user != null && !TextUtils.isEmpty(user.login())) {
            subject.append(" by ").append(user.login());
        }
        startActivity(ShareUtils.create(subject, "https://gist.github.com/"
                + id));
    }

    private void unstarGist() {
        ToastUtils.show(getActivity(), R.string.unstarring_gist);
        ServiceGenerator.createService(getActivity(), GistService.class)
                .unstarGist(gistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> starred = !(response.code() == 204),
                        e -> ToastUtils.show((Activity) getContext(), e.getMessage()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode || data == null) {
            return;
        }

        switch (requestCode) {
        case COMMENT_CREATE:
            GitHubComment comment = data
                    .getParcelableExtra(EXTRA_COMMENT);
            if (comments != null) {
                comments.add(comment);
                gist = gist.toBuilder().comments(gist.comments() + 1).build();
                updateList(gist, comments);
            } else {
                refreshGist();
            }
            return;
        case COMMENT_EDIT:
            comment = data.getParcelableExtra(EXTRA_COMMENT);
            if (comments != null && comment != null) {
                int position = Collections.binarySearch(comments, comment, (lhs, rhs) ->
                        lhs.id().compareTo(rhs.id()));
                imageGetter.removeFromCache(comment.id());
                comments.set(position, comment);
                updateList(gist, comments);
            } else {
                refreshGist();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateFiles(Gist gist) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        Map<String, GistFile> files = gist.files();
        if (files == null || files.isEmpty()) {
            filesSection.update(Collections.emptyList());
            return;
        }

        List<GistFileItem> fileItems = new ArrayList<>();
        for (GistFile file : files.values()) {
            fileItems.add(new GistFileItem(file));
        }
        filesSection.update(fileItems);
    }

    private void updateList(Gist gist, List<GitHubComment> comments) {
        List<GitHubCommentItem> items = new ArrayList<>();
        String username = AccountUtils.getLogin(getActivity());
        boolean isOwner = isOwner();

        for (GitHubComment comment : comments) {
            items.add(
                    new GitHubCommentItem(avatars, imageGetter, editCommentListener,
                            deleteCommentListener, username, isOwner, comment)
            );
        }
        commentsSection.update(items);
        mainSection.removeFooter();

        updateHeader(gist);
        updateFiles(gist);
    }

    private void refreshGist() {
        refreshGistTaskFactory.create(getActivity(), gistId)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(fullGist -> isAdded())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(fullGist -> {
                    FragmentActivity activity = getActivity();
                    if (activity instanceof OnLoadListener) {
                        ((OnLoadListener<Gist>) activity).loaded(fullGist.getGist());
                    }

                    starred = fullGist.getStarred();
                    loadFinished = true;
                    gist = fullGist.getGist();
                    comments = fullGist.getComments();
                    updateList(fullGist.getGist(), fullGist.getComments());
                }, e -> ToastUtils.show(getActivity(), R.string.error_gist_load));
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof GistFileItem) {
            int position = adapter.getAdapterPosition(item);
            startActivity(GistFilesViewActivity.createIntent(gist, position - 1));
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
        case COMMENT_DELETE:
            final GitHubComment comment = arguments.getParcelable(EXTRA_COMMENT);
            ServiceGenerator.createService(getActivity(), GistCommentService.class)
                    .deleteGistComment(gistId, comment.id())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(RxProgress.bindToLifecycle(getActivity(), R.string.deleting_comment))
                    .as(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe(response -> {
                        // Update comment list
                        if (comments != null) {
                            int position = Collections.binarySearch(comments,
                                    comment, (lhs, rhs) -> lhs.id().compareTo(rhs.id()));
                            comments.remove(position);
                            updateList(gist, comments);
                        } else {
                            refreshGist();
                        }
                    }, e -> {
                        Log.d(TAG, "Exception deleting comment on gist", e);
                        ToastUtils.show((Activity) getContext(), e.getMessage());
                    });
            break;
        }
    }

    /**
     * Edit existing comment
     */
    final EditCommentListener editCommentListener = new EditCommentListener() {
        @Override
        public void onEditComment(GitHubComment comment) {
            startActivityForResult(
                    EditCommentActivity.createIntent(gist, comment),
                    COMMENT_EDIT);
        }
    };

    /**
     * Delete existing comment
     */
    final DeleteCommentListener deleteCommentListener = comment -> {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_COMMENT, comment);
        ConfirmDialogFragment.show(
                getActivity(),
                COMMENT_DELETE,
                getActivity()
                        .getString(R.string.confirm_comment_delete_title),
                getActivity().getString(
                        R.string.confirm_comment_delete_message), args);
    };
}
