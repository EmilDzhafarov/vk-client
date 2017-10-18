package ua.nure.dzhafarov.vkontakte.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ua.nure.dzhafarov.vkontakte.receivers.NetworkChangeBroadcastReceiver;

public abstract class BaseActivity extends AppCompatActivity {
    
    public static NetworkChangeBroadcastReceiver broadcastReceiver = new NetworkChangeBroadcastReceiver();
    
    protected abstract Integer getHostId();
    
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(getHostId(), fragment);
        
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        
        fragmentTransaction.commit();
    }
}
