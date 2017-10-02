package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentUserProfile;

public class ActivityUserProfile extends SingleFragmentActivity {

    public static final String REQUEST_USER_PROFILE = "request_user_profile";
    
    @Override
    protected Fragment createFragment() {
        return new FragmentUserProfile();
    }
}
