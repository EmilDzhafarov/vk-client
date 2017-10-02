package ua.nure.dzhafarov.vkontakte.utils;

import android.view.View;

public interface OnUserClickListener<T> {
    void onUserClicked(T result, View view);
}
