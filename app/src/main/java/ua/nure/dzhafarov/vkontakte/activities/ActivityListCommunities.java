package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;

import ua.nure.dzhafarov.vkontakte.fragments.FragmentListCommunities;

public class ActivityListCommunities extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FragmentListCommunities();
    }
}
