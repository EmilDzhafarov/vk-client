package ua.nure.dzhafarov.vkontakte.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.List;

import ua.nure.dzhafarov.vkontakte.database.MessageLab;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class UpdateService extends Service {

    private VKManager vkManager;

    public static final String SEND_MESSAGES_VK = "send_messages_vk";
    public static final String USER_READ_MESSAGES = "user_read_messages";

    public static final String EVENT_DATA = "event_data";
    
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
                        startConnectingLongPollService();
                    }
                }
        ).start();
    }

    private void startConnectingLongPollService() {
        final Long currTs = vkManager.getCurrentLongPoll().getTs();

        vkManager.connectToLongPollServer(currTs,
                new OperationListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (!messages.isEmpty()) {
                            MessageLab.getInstance(UpdateService.this).addAllMessages(messages);
                            sendBroadcastToShowMessages(messages);
                        }

                        startConnectingLongPollService();
                    }
                },
                new OperationListener<int[]>() {
                    @Override
                    public void onSuccess(int[] object) {
                        if (object.length == 3) {
                            sendBroadcastToShowEvents(object);
                        }
                    }
                });
    }
    
    private void sendBroadcastToShowEvents(int[] obj) {
        Intent intent = new Intent();
        intent.putExtra(EVENT_DATA, obj);
        intent.setAction(USER_READ_MESSAGES);
        sendBroadcast(intent);
    }
    
    private void sendBroadcastToShowMessages(List<Message> messages) {
        if (!messages.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(SEND_MESSAGES_VK);
            sendBroadcast(intent);
        }
    }
}
