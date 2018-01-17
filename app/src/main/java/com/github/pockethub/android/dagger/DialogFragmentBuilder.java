package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.issue.AssigneeDialogFragment;
import com.github.pockethub.android.ui.issue.LabelsDialogFragment;
import com.github.pockethub.android.ui.issue.MilestoneDialogFragment;
import com.github.pockethub.android.ui.ref.RefDialogFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface DialogFragmentBuilder {

    @ContributesAndroidInjector
    LabelsDialogFragment labelsDialogFragment();

    @ContributesAndroidInjector
    AssigneeDialogFragment assigneeDialogFragment();

    @ContributesAndroidInjector
    MilestoneDialogFragment milestoneDialogFragment();

    @ContributesAndroidInjector
    RefDialogFragment refDialogFragment();

    @ContributesAndroidInjector
    ConfirmDialogFragment confirmDialogFragment();
}
