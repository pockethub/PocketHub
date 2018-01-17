package com.github.pockethub.android.util;

import android.view.View;

import butterknife.ButterKnife;

public class ButterKnifeUtils {

    public static final ButterKnife.Action<View> GONE =
            (view, index) -> view.setVisibility(View.GONE);
}
