package com.github.pockethub.android.ui.item;

import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.util.AvatarLoader;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

public abstract class BaseDataItem<T, V extends ViewHolder> extends Item<V> {

    private AvatarLoader avatarLoader;

    private T data;

    public BaseDataItem(AvatarLoader avatarLoader, T dataItem, long id) {
        super(id);
        this.avatarLoader = avatarLoader;
        this.data = dataItem;
    }

    protected AvatarLoader getAvatarLoader() {
        return avatarLoader;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseDataItem) {
            return getData().equals(((BaseDataItem) obj).getData());
        }
        return super.equals(obj);
    }

    @NonNull
    @Override
    public abstract V createViewHolder(@NonNull View itemView);
}
