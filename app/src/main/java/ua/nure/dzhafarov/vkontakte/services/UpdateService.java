package ua.nure.dzhafarov.vkontakte.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class UpdateService extends Service {

    private VKManager vkManager;

    public static final String SEND_MESSAGES_VK = "send_messages_vk";
    
    @Override
    public void onCreate() {
        super.onCreate();
        vkManager = VKManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doWork();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void doWork() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            Long curr = vkManager.getCurrentLongPoll().getTs();
                            curr = vkManager.connectToLongPollServer(curr, new OperationListener<Message>() {
                                @Override
                                public void onSuccess(Message object) {
                                    passMessageToActivity(object);
                                }
                            });
                            System.out.println("LONG POLL TS ===> " + curr);
                        }
                    }
                }
        ).start();
    }

    private void passMessageToActivity(Message message){
        Intent intent = new Intent();
        intent.setAction(SEND_MESSAGES_VK);
        intent.putExtra(SEND_MESSAGES_VK, message);
        sendBroadcast(intent);
    }
}
