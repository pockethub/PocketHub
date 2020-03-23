package com.github.pockethub.android.ui.repo;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caverock.androidsvg.SVG;
import com.github.pockethub.android.R;
import com.github.pockethub.android.markwon.FontResolver;
import com.github.pockethub.android.markwon.MarkwonUtils;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.base.BaseFragment;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.Intents;
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HtmlBlock;

import io.noties.markwon.Markwon;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.SimpleEntry;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.prism4j.annotations.PrismBundle;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RepositoryReadmeFragment extends BaseFragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(inflater.getContext());
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view;

        final Repository repo = requireActivity().getIntent().getParcelableExtra(Intents.EXTRA_REPOSITORY);
        final String defaultBranch = repo.defaultBranch() == null ? "master" : repo.defaultBranch();
        final String baseString = String.format("https://github.com/%s/%s/%s/%s/", repo.owner().login(), repo.name(), "%s", defaultBranch);

        SVG.registerExternalFileResolver(new FontResolver(requireActivity().getAssets()));

        ServiceGenerator.createService(getActivity(), RepositoryContentService.class)
                .getReadmeRaw(repo.owner().login(), repo.name(), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> {
                    MarkwonAdapter adapter = MarkwonAdapter.builderTextViewIsRoot(R.layout.markwon_adapter)
                            .include(HtmlBlock.class, SimpleEntry.createTextViewIsRoot(R.layout.markwon_adapter_test))
                            .include(FencedCodeBlock.class, SimpleEntry.create(R.layout.adapter_fenced_code_block, R.id.text))
                            .include(TableBlock.class, TableEntry.create(builder ->
                                    builder
                                            .tableLayout(R.layout.markwon_table_block, R.id.table_layout)
                                            .textLayoutIsRoot(R.layout.markwon_table_cell))
                            )
                            .build();

                    Markwon markwon = MarkwonUtils.createMarkwon(requireContext(), baseString);
                    recyclerView.setAdapter(adapter);
                    adapter.setMarkdown(markwon, response.body());
                    adapter.notifyDataSetChanged();
                });
    }
}
