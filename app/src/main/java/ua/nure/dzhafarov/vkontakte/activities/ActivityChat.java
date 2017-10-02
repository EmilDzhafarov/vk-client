package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;
import ua.nure.dzhafarov.vkontakte.fragments.FragmentChat;

public class ActivityChat extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FragmentChat();
    }
}
