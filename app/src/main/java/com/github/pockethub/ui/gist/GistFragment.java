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
package com.github.pockethub.ui.gist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
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

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GistFile;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.accounts.AccountUtils;
import com.github.pockethub.core.OnLoadListener;
import com.github.pockethub.core.gist.FullGist;
import com.github.pockethub.core.gist.GistStore;
import com.github.pockethub.core.gist.RefreshGistTask;
import com.github.pockethub.core.gist.StarGistTask;
import com.github.pockethub.core.gist.UnstarGistTask;
import com.github.pockethub.ui.ConfirmDialogFragment;
import com.github.pockethub.ui.DialogFragment;
import com.github.pockethub.ui.HeaderFooterListAdapter;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.ui.comment.CommentListAdapter;
import com.github.pockethub.ui.comment.DeleteCommentListener;
import com.github.pockethub.ui.comment.EditCommentListener;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.HttpImageGetter;
import com.github.pockethub.util.ShareUtils;
import com.github.pockethub.util.TimeUtils;
import com.github.pockethub.util.ToastUtils;
import com.github.pockethub.util.TypefaceUtils;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.pockethub.Intents.EXTRA_COMMENT;
import static com.github.pockethub.Intents.EXTRA_GIST_ID;
import static com.github.pockethub.RequestCodes.COMMENT_CREATE;
import static com.github.pockethub.RequestCodes.COMMENT_DELETE;
import static com.github.pockethub.RequestCodes.COMMENT_EDIT;

/**
 * Activity to display an existing Gist
 */
public class GistFragment extends DialogFragment implements OnItemClickListener {

    private String gistId;

    private List<GithubComment> comments;

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

        list = finder.find(android.R.id.list);
        progress = finder.find(R.id.pb_loading);

        Activity activity = getActivity();
        User user = gist.user;
        String userName = null;
        if(user != null) userName = user.login;
        adapter = new HeaderFooterListAdapter<>(list,
                new CommentListAdapter(activity.getLayoutInflater(), null, avatars,
                        imageGetter, editCommentListener, deleteCommentListener, userName, false, null));
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

        if (gist == null || (gist.comments > 0 && comments == null))
            adapter.addHeader(loadingView, null, false);

        if (gist != null && comments != null)
            updateList(gist, comments);
        else
            refreshGist();
    }

    private boolean isOwner() {
        if (gist == null)
            return false;
        User user = gist.user;
        if (user == null)
            return false;
        String login = AccountUtils.getLogin(getActivity());
        return login != null && login.equals(user.login);
    }

    private void updateHeader(Gist gist) {
        Date createdAt = TimeUtils.stringToDate(gist.created_at);
        if (createdAt != null) {
            StyledText text = new StyledText();
            text.append(getString(R.string.prefix_created));
            text.append(createdAt);
            created.setText(text);
            created.setVisibility(VISIBLE);
        } else
            created.setVisibility(GONE);

        Date updatedAt = TimeUtils.stringToDate(gist.updated_at);
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            StyledText text = new StyledText();
            text.append(getString(R.string.prefix_updated));
            text.append(updatedAt);
            updated.setText(text);
            updated.setVisibility(VISIBLE);
        } else
            updated.setVisibility(GONE);

        String desc = gist.description;
        if (!TextUtils.isEmpty(desc))
            description.setText(desc);
        else
            description.setText(R.string.no_description_given);

        ViewUtils.setGone(progress, true);
        ViewUtils.setGone(list, false);
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
            if (starred)
                starItem.setTitle(R.string.unstar);
            else
                starItem.setTitle(R.string.star);
        } else
            menu.removeItem(R.id.m_star);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (gist == null)
            return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case R.id.m_comment:
            startActivityForResult(CreateCommentActivity.createIntent(gist),
                    COMMENT_CREATE);
            return true;
        case R.id.m_star:
            if (starred)
                unstarGist();
            else
                starGist();
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

        new StarGistTask(getActivity(), gistId) {

            @Override
            protected void onSuccess(Gist gist) throws Exception {
                super.onSuccess(gist);

                starred = true;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                ToastUtils.show((Activity) getContext(), e.getMessage());
            }

        }.execute();
    }

    private void shareGist() {
        StringBuilder subject = new StringBuilder("Gist ");
        String id = gist.id;
        subject.append(id);
        User user = gist.user;
        if (user != null && !TextUtils.isEmpty(user.login))
            subject.append(" by ").append(user.login);
        startActivity(ShareUtils.create(subject, "https://gist.github.com/"
                + id));
    }

    private void unstarGist() {
        ToastUtils.show(getActivity(), R.string.unstarring_gist);

        new UnstarGistTask(getActivity(), gistId) {

            @Override
            protected void onSuccess(Gist gist) throws Exception {
                super.onSuccess(gist);

                starred = false;
            }

            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                ToastUtils.show((Activity) getContext(), e.getMessage());
            }

        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode || data == null)
            return;

        switch (requestCode) {
        case COMMENT_CREATE:
            GithubComment comment = (GithubComment) data
                    .getParcelableExtra(EXTRA_COMMENT);
            if (comments != null) {
                comments.add(comment);
                gist.comments = gist.comments + 1;
                updateList(gist, comments);
            } else
                refreshGist();
            return;
        case COMMENT_EDIT:
            comment = (GithubComment) data.getParcelableExtra(EXTRA_COMMENT);
            if (comments != null && comment != null) {
                int position = Collections.binarySearch(comments, comment,
                        new Comparator<GithubComment>() {
                            public int compare(GithubComment lhs, GithubComment rhs) {
                                return String.valueOf(lhs.id).compareTo(rhs.id);
                            }
                        });
                imageGetter.removeFromCache(comment.id);
                comments.set(position, comment);
                updateList(gist, comments);
            } else
                refreshGist();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateFiles(Gist gist) {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        for (View header : fileHeaders)
            adapter.removeHeader(header);
        fileHeaders.clear();

        Map<String, GistFile> files = gist.files;
        if (files == null || files.isEmpty())
            return;

        final LayoutInflater inflater = activity.getLayoutInflater();
        final Typeface octicons = TypefaceUtils.getOcticons(activity);
        for (GistFile file : files.values()) {
            View fileView = inflater.inflate(R.layout.gist_file_item, null);
            ((TextView) fileView.findViewById(R.id.tv_file)).setText(file.filename);
            ((TextView) fileView.findViewById(R.id.tv_file_icon))
                    .setTypeface(octicons);
            adapter.addHeader(fileView, file, true);
            fileHeaders.add(fileView);
        }
    }

    private void updateList(Gist gist, List<GithubComment> comments) {
        adapter.getWrappedAdapter().setItems(
                comments.toArray(new GithubComment[comments.size()]));
        adapter.removeHeader(loadingView);

        headerView.setVisibility(VISIBLE);
        updateHeader(gist);

        updateFiles(gist);
    }

    private void refreshGist() {
        new RefreshGistTask(getActivity(), gistId, imageGetter) {

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
                ToastUtils.show(getActivity(), e, R.string.error_gist_load);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onSuccess(FullGist fullGist) throws Exception {
                super.onSuccess(fullGist);

                if (!isUsable())
                    return;

                FragmentActivity activity = getActivity();
                if (activity instanceof OnLoadListener)
                    ((OnLoadListener<Gist>) activity)
                            .loaded(fullGist.getGist());

                starred = fullGist.isStarred();
                loadFinished = true;
                gist = fullGist.getGist();
                comments = fullGist;
                updateList(fullGist.getGist(), fullGist);
            }

        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof GistFile)
            startActivity(GistFilesViewActivity
                    .createIntent(gist, position - 1));
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case COMMENT_DELETE:
            final GithubComment comment = arguments.getParcelable(EXTRA_COMMENT);
            new DeleteCommentTask(getActivity(), gist.id, comment) {
                @Override
                protected void onSuccess(GithubComment comment) throws Exception {
                    super.onSuccess(comment);

                    // Update comment list
                    if (comments != null && comment != null) {
                        int position = Collections.binarySearch(comments,
                                comment, new Comparator<GithubComment>() {
                                    public int compare(GithubComment lhs, GithubComment rhs) {
                                        return String.valueOf(lhs.id)
                                                .compareTo(rhs.id);
                                    }
                                });
                        comments.remove(position);
                        updateList(gist, comments);
                    } else
                        refreshGist();
                }
            }.start();
            break;
        }
    }

    /**
     * Edit existing comment
     */
    final EditCommentListener editCommentListener = new EditCommentListener() {
        public void onEditComment(GithubComment comment) {
            startActivityForResult(
                    EditCommentActivity.createIntent(gist, comment),
                    COMMENT_EDIT);
        }
    };

    /**
     * Delete existing comment
     */
    final DeleteCommentListener deleteCommentListener = new DeleteCommentListener() {
        public void onDeleteComment(GithubComment comment) {
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_COMMENT, comment);
            ConfirmDialogFragment.show(
                    getActivity(),
                    COMMENT_DELETE,
                    getActivity()
                            .getString(R.string.confirm_comment_delete_title),
                    getActivity().getString(
                            R.string.confirm_comment_delete_message), args);
        }
    };
}
