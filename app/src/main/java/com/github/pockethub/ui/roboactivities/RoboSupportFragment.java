package com.github.pockethub.ui.roboactivities;

import roboguice.RoboGuice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;


public abstract class RoboSupportFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoboGuice.getInjector(getActivity()).injectViewMembers(this);
    }
}
