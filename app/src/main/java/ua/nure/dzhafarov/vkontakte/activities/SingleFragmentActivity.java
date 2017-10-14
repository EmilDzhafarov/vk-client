package ua.nure.dzhafarov.vkontakte.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ua.nure.dzhafarov.vkontakte.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_host);

        addFragmentToActivity(createFragment(), this, R.id.fragment_host);
    }

    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();

        if (fragments == 1) {
            finish();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 1) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    public static void addFragmentToActivity(Fragment fr, Activity activity, Integer hostId) {
        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
        
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(hostId, fr);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }
}
