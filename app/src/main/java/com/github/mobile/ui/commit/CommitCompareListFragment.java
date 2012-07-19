/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.commit;

import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_HEAD;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.R.color;
import com.github.mobile.R.id;
import com.github.mobile.core.commit.CommitCompareTask;
import com.github.mobile.ui.DialogFragment;
import com.github.mobile.ui.HeaderFooterListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.ViewUtils;
import com.viewpagerindicator.R.layout;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommitCompare;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * Fragment to display a list of commits being compared
 */
public class CommitCompareListFragment extends DialogFragment {

    @InjectView(android.R.id.list)
    private ListView list;

    @InjectView(id.pb_loading)
    private ProgressBar progress;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @InjectExtra(EXTRA_BASE)
    private String base;

    @InjectExtra(EXTRA_HEAD)
    private String head;

    private RepositoryCommitCompare compare;

    private Map<String, CharSequence> styledDiffs = new HashMap<String, CharSequence>();

    private HeaderFooterListAdapter<CommitFileListAdapter> adapter;

    @InjectResource(color.diff_marker)
    private int markerColor;

    @InjectResource(color.diff_add)
    private int addColor;

    @InjectResource(color.diff_remove)
    private int removeColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compareCommits();
    }

    private void compareCommits() {
        new CommitCompareTask(getActivity(), repository, base, head) {

            @Override
            protected RepositoryCommitCompare run() throws Exception {
                RepositoryCommitCompare compare = super.run();

                styledDiffs.clear();
                List<CommitFile> files = compare.getFiles();
                if (files != null) {
                    for (CommitFile file : files) {
                        String patch = file.getPatch();
                        if (TextUtils.isEmpty(patch))
                            continue;

                        int start = 0;
                        int end = patch.indexOf('\n');
                        StyledText styled = new StyledText();
                        while (end != -1) {
                            String line = patch.substring(start, end + 1);
                            switch (patch.charAt(start)) {
                            case '@':
                                styled.background(line, markerColor);
                                break;
                            case '+':
                                styled.background(line, addColor);
                                break;
                            case '-':
                                styled.background(line, removeColor);
                                break;
                            default:
                                styled.append(line);
                                break;
                            }
                            styledDiffs.put(file.getFilename(), styled);
                            start = end + 1;
                            end = patch.indexOf('\n', start);
                        }
                    }
                    Collections.sort(files, new Comparator<CommitFile>() {

                        public int compare(CommitFile lhs, CommitFile rhs) {
                            String lPath = lhs.getFilename();
                            String rPath = rhs.getFilename();
                            int lSlash = lPath.lastIndexOf('/');
                            if (lSlash != -1)
                                lPath = lPath.substring(lSlash + 1);
                            int rSlash = rPath.lastIndexOf('/');
                            if (rSlash != -1)
                                rPath = rPath.substring(rSlash + 1);
                            return CASE_INSENSITIVE_ORDER.compare(lPath, rPath);
                        }
                    });
                }
                return compare;
            }

            @Override
            protected void onSuccess(RepositoryCommitCompare compare)
                    throws Exception {
                super.onSuccess(compare);

                CommitCompareListFragment.this.compare = compare;
                updateList();
            }

        }.execute();
    }

    private void updateList() {
        ViewUtils.setGone(progress, true);
        ViewUtils.setGone(list, false);

        List<CommitFile> files = compare.getFiles();
        if (files != null && !files.isEmpty())
            adapter.getWrappedAdapter().setItems(
                    files.toArray(new CommitFile[files.size()]));
        else
            adapter.getWrappedAdapter().setItems(null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new HeaderFooterListAdapter<CommitFileListAdapter>(list,
                new CommitFileListAdapter(layout.commit_file_item,
                        getActivity().getLayoutInflater()) {

                    @Override
                    protected void update(int position, CommitFileView view,
                            CommitFile item) {
                        super.update(position, view, item);

                        view.diff.setText(styledDiffs.get(item.getFilename()));
                    }
                });
        list.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.item_list, container);
    }
}
