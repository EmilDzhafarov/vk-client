package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;

import ua.nure.dzhafarov.vkontakte.fragments.FragmentListDialogs;

public class ActivityListDialogs extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FragmentListDialogs();
    }
}
