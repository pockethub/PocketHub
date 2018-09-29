package com.github.pockethub.android.rx;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

public class AutoDisposeUtils {

    public static <T> AutoDisposeConverter<T> bindToLifecycle(LifecycleOwner lifecycleOwner) {
        return bindToLifecycle(lifecycleOwner.getLifecycle());
    }

    public static <T> AutoDisposeConverter<T> bindToLifecycle(Lifecycle lifecycle) {
        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle));
    }

    public static <T> AutoDisposeConverter<T> bindToLifecycle(LifecycleOwner lifecycleOwner,
                                                          Lifecycle.Event untilEvent) {
        return bindToLifecycle(lifecycleOwner.getLifecycle(), untilEvent);
    }

    public static <T> AutoDisposeConverter<T> bindToLifecycle(Lifecycle lifecycle,
                                                          Lifecycle.Event event) {

        return AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle, event));
    }
}
