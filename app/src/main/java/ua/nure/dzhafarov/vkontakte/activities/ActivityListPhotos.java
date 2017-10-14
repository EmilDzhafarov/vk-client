package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;

import ua.nure.dzhafarov.vkontakte.fragments.FragmentListPhotos;

public class ActivityListPhotos extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FragmentListPhotos();
    }
}
