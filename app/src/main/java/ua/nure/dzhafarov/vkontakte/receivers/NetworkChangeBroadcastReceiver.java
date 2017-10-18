package ua.nure.dzhafarov.vkontakte.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class NetworkChangeBroadcastReceiver extends BroadcastReceiver {
    
    private VKManager vkManager;

    public NetworkChangeBroadcastReceiver() {
        super();
        vkManager = VKManager.getInstance();
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                && vkManager.getCurrentUser() != null) {
            
            if (vkManager.isNetworksAvailable(context)) {
                vkManager.sendUnsentMessages();
            }
        }
    }
}
