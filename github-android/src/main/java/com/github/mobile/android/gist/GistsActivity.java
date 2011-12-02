package com.github.mobile.android.gist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.google.inject.Inject;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Main activity for Gists
 */
public class GistsActivity extends RoboFragment {

    private static final int REQUEST_CREATE = 1;

    @Inject
    private Context context;

    @InjectView(id.createGistButton)
    private Button createButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.gists, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivityForResult(new Intent(context, ShareGistActivity.class), REQUEST_CREATE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE && ShareGistActivity.RESULT_CREATED == resultCode) {
            GistFragment fragment = (GistFragment) getFragmentManager().findFragmentById(id.gist_list);
            fragment.getLoaderManager().restartLoader(0, null, fragment);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
