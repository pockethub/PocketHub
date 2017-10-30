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
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pockethub.android.rx.RxProgress;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GistFile;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.core.OnLoadListener;
import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.core.gist.RefreshGistTask;
import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.HeaderFooterListAdapter;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.comment.CommentListAdapter;
import com.github.pockethub.android.ui.comment.DeleteCommentListener;
import com.github.pockethub.android.ui.comment.EditCommentListener;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.gists.GistCommentService;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.pockethub.android.Intents.EXTRA_COMMENT;
import static com.github.pockethub.android.Intents.EXTRA_GIST_ID;
import static com.github.pockethub.android.RequestCodes.COMMENT_CREATE;
import static com.github.pockethub.android.RequestCodes.COMMENT_DELETE;
import static com.github.pockethub.android.RequestCodes.COMMENT_EDIT;

/**
 * Activity to display an existing Gist
 */
public class GistFragment extends DialogFragment implements OnItemClickListener {

    private static final String TAG = "GistFragment";

    private String gistId;

    private List<GitHubComment> comments;

    private Gist gist;

    private ListView list;

    private ProgressBar progress;

    @Inject
    private GistStore store;

    @Inject
    private HttpImageGetter imageGetter;

    private View headerView;

    private View footerView;

    private TextView created;

    private TextView updated;

    private TextView description;

    private View loadingView;

    private HeaderFooterListAdapter<CommentListAdapter> adapter;

    private boolean starred;

    private boolean loadFinished;

    @Inject
    private AvatarLoader avatars;

    private List<View> fileHeaders = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gistId = getArguments().getString(EXTRA_GIST_ID);
        gist = store.getGist(gistId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comment_list, container, false);

        headerView = inflater.inflate(R.layout.gist_header, null);
        created = (TextView) headerView.findViewById(R.id.tv_gist_creation);
        updated = (TextView) headerView.findViewById(R.id.tv_gist_updated);
        description = (TextView) headerView
                .findViewById(R.id.tv_gist_description);

        loadingView = inflater.inflate(R.layout.loading_item, null);
        ((TextView) loadingView.findViewById(R.id.tv_loading))
                .setText(R.string.loading_comments);

        footerView = inflater.inflate(R.layout.footer_separator, null);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = (ListView) view.findViewById(android.R.id.list);
        progress = (ProgressBar) view.findViewById(R.id.pb_loading);

        Activity activity = getActivity();
        User user = gist.owner();
        String userName = null;
        if(user != null) {
            userName = user.login();
        }

        adapter = new HeaderFooterListAdapter<>(list,
                new CommentListAdapter(activity.getLayoutInflater(), null, avatars,
                        imageGetter, editCommentListener, deleteCommentListener, userName, isOwner(), null));
        list.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setOnItemClickListener(this);
        adapter.addHeader(headerView);
        adapter.addFooter(footerView);

        if (gist != null) {
            updateHeader(gist);
            updateFiles(gist);
        }

        if (gist == null || (gist.comments() > 0 && comments == null)) {
            adapter.addHeader(loadingView, null, false);
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
        Date createdAt = gist.createdAt();
        if (createdAt != null) {
            StyledText text = new StyledText();
            text.append(getString(R.string.prefix_created));
            text.append(createdAt);
            created.setText(text);
            created.setVisibility(VISIBLE);
        } else {
            created.setVisibility(GONE);
        }

        Date updatedAt = gist.updatedAt();
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            StyledText text = new StyledText();
            text.append(getString(R.string.prefix_updated));
            text.append(updatedAt);
            updated.setText(text);
            updated.setVisibility(VISIBLE);
        } else {
            updated.setVisibility(GONE);
        }

        String desc = gist.description();
        if (!TextUtils.isEmpty(desc)) {
            description.setText(desc);
        } else {
            description.setText(R.string.no_description_given);
        }

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
                .compose(this.bindToLifecycle())
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
                .compose(this.bindToLifecycle())
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
                        Integer.valueOf(lhs.id()).compareTo(rhs.id()));
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

        for (View header : fileHeaders) {
            adapter.removeHeader(header);
        }
        fileHeaders.clear();

        Map<String, GistFile> files = gist.files();
        if (files == null || files.isEmpty()) {
            return;
        }

        final LayoutInflater inflater = activity.getLayoutInflater();
        for (GistFile file : files.values()) {
            View fileView = inflater.inflate(R.layout.gist_file_item, null);
            ((TextView) fileView.findViewById(R.id.tv_file)).setText(file.filename());
            adapter.addHeader(fileView, file, true);
            fileHeaders.add(fileView);
        }
    }

    private void updateList(Gist gist, List<GitHubComment> comments) {
        adapter.getWrappedAdapter().setItems(
                comments.toArray(new GitHubComment[comments.size()]));
        adapter.removeHeader(loadingView);

        headerView.setVisibility(VISIBLE);
        updateHeader(gist);

        updateFiles(gist);
    }

    private void refreshGist() {
        new RefreshGistTask(getActivity(), gistId, imageGetter)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(fullGist -> isUsable())
                .compose(this.bindToLifecycle())
                .subscribe(fullGist -> {
                    FragmentActivity activity = getActivity();
                    if (activity instanceof OnLoadListener) {
                        ((OnLoadListener<Gist>) activity)
                                .loaded(fullGist.getGist());
                    }

                    starred = fullGist.isStarred();
                    loadFinished = true;
                    gist = fullGist.getGist();
                    comments = fullGist.getComments();
                    updateList(fullGist.getGist(), fullGist.getComments());
                }, e -> ToastUtils.show(getActivity(), e, R.string.error_gist_load));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof GistFile) {
            startActivity(GistFilesViewActivity
                    .createIntent(gist, position - 1));
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
                    .compose(this.bindToLifecycle())
                    .compose(RxProgress.bindToLifecycle(getActivity(), R.string.deleting_comment))
                    .subscribe(response -> {
                        // Update comment list
                        if (comments != null) {
                            int position = Collections.binarySearch(comments,
                                    comment, (lhs, rhs) -> Integer.valueOf(lhs.id())
                                            .compareTo(rhs.id()));
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
