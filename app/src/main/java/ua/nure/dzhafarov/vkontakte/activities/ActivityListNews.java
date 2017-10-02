package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentListNews;

public class ActivityListNews extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FragmentListNews();
    }
    
}
