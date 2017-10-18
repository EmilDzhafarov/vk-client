package ua.nure.dzhafarov.vkontakte.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import ua.nure.dzhafarov.vkontakte.R;

public abstract class SingleFragmentActivity extends BaseActivity {

    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_host);
        
        addFragment(createFragment(), false);   
    }
}
