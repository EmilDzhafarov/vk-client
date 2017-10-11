package ua.nure.dzhafarov.vkontakte.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import ua.nure.dzhafarov.vkontakte.database.MessageLab;
import ua.nure.dzhafarov.vkontakte.models.LongPoll;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

public class UpdateService extends Service {

    private VKManager vkManager;

    public static final String ACTION_SEND_MESSAGE_VK = "action_send_message_vk";
    public static final String ACTION_USER_READ_MESSAGE = "action_user_read_message";
    public static final String ACTION_USER_TYPES_MESSAGE = "action_user_types_message";

    public static final String CURRENT_USER_ID = "current_user_id";
    public static final String MESSAGE_LOCAL_ID = "message_local_id";

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

        if (currTs == 0) {
            vkManager.initLongPoll(new OperationListener<LongPoll>() {
                @Override
                public void onSuccess(LongPoll object) {
                     startConnectingLongPollService();  
                }

                @Override
                public void onFailure(String message) {

                }
            });
            
        } else {
            vkManager.connectToLongPollServer(currTs, new OperationListener<JSONArray>() {
                @Override
                public void onSuccess(JSONArray object) {
                    try {

                        for (int i = 0; i < object.length(); i++) {
                            JSONArray event = object.getJSONArray(i);
                            int eventCode = event.getInt(0);

                            if (eventCode == 4) {
                                sendBroadcastToShowNewMessage(event);
                            } else if (eventCode == 7) {
                                sendBroadcastToShowReadingEvent(event);
                            } else if (eventCode == 61) {
                                sendBroadcastToShowUserTypes(event);
                            }
                        }

                        startConnectingLongPollService();

                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String message) {
                    
                }
            });    
        }
    }

    private void sendBroadcastToShowUserTypes(JSONArray data) throws JSONException {
        int userId = data.getInt(1);
        
        Intent intent = new Intent();
        intent.putExtra(CURRENT_USER_ID, userId);
        intent.setAction(ACTION_USER_TYPES_MESSAGE);
        sendBroadcast(intent);
    }
    
    private void sendBroadcastToShowReadingEvent(JSONArray data) throws JSONException {
        int userId = data.getInt(1);
        int localId = data.getInt(2);

        Intent intent = new Intent();
        intent.putExtra(CURRENT_USER_ID, userId);
        intent.putExtra(MESSAGE_LOCAL_ID, localId);
        intent.setAction(ACTION_USER_READ_MESSAGE);
        sendBroadcast(intent);
    }

    private void sendBroadcastToShowNewMessage(JSONArray event) throws JSONException {
        Message message = new Message();
        
        message.setMessageId(event.getInt(1));
        message.setTime(event.getLong(4) * 1000);
        message.setText(event.getString(5));
        message.setTs(vkManager.getCurrentLongPoll().getTs());

        int mask = event.getInt(2);
        int peerId = event.getInt(3);

        if (mask == 35) {
            message.setUserId(peerId);
            message.setFromId(VKManager.getInstance().getCurrentUser().getId());
        }

        if (mask == 49) {
            message.setFromId(peerId);
            message.setUserId(VKManager.getInstance().getCurrentUser().getId());
            message.setReadState(0);
        }

        MessageLab.getInstance(this).addMessage(message);

        Intent intent = new Intent();
        intent.setAction(ACTION_SEND_MESSAGE_VK);
        sendBroadcast(intent);
    }
}
