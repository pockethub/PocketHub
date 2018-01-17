package com.github.pockethub.android.dagger;

import com.github.pockethub.android.ui.notification.NotificationListFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface NotificationFragmentProvider {

    @ContributesAndroidInjector
    NotificationListFragment notificationListFragment();

}
