package ua.nure.dzhafarov.vkontakte.utils;

import android.content.Context;

import java.util.List;

import ua.nure.dzhafarov.vkontakte.database.MessageLab;
import ua.nure.dzhafarov.vkontakte.models.Community;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.LongPoll;
import ua.nure.dzhafarov.vkontakte.models.Message;

public class VKManager {

    private static VKManager instance;

    private VKManager() {}

    private VKFetcher fetcher;
    private LongPoll longPoll;
    private User currentUser;
    private MessageLab messageLab;
    
    public static synchronized VKManager getInstance() {
        if (instance == null) {
            instance = new VKManager();
        }

        return instance;
    }

    public synchronized void initialize(Context context, String token, OperationListener<LongPoll> listener) {
        if (fetcher == null) {
            fetcher = new VKFetcher(token);
        }
        
        currentUser = new User();
        longPoll = new LongPoll();
        initLongPoll(listener);
        messageLab = MessageLab.getInstance(context.getApplicationContext());
    }
    
    public void loadUsers(final OperationListener<List<User>> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        List<User> users = fetcher.getAllUsers();
                        listener.onSuccess(users);
                    }
                }
        ).start();
    }
    
    public void loadCommunities(final OperationListener<List<Community>> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        List<Community> communities = fetcher.getAllCommunities();
                        listener.onSuccess(communities);
                    }
                }
        ).start();
    }

    public void loadMessages(final User user, final Message curr, final ChatLoadListener listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        List<Message> messages = fetcher.getChatWith(user, curr);
                        listener.onChatLoaded(messages, curr);
                    }
                }
        ).start();
    }

    public void sendMessage(final String message, final int id, final OperationListener<Void> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        fetcher.sendMessageToUser(message, id);
                        listener.onSuccess(null);
                    }
                }
        ).start();
    }
    
    public void markMessageAsRead(final Message message, final OperationListener<Message> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Integer resultCode = fetcher.markMessageAsRead(message);
                        
                        if (resultCode != -1) {
                            listener.onSuccess(message);   
                        }
                    }
                }
        ).start();
    }
    
    private void initLongPoll(final OperationListener<LongPoll> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        LongPoll curr = fetcher.getLongPollServer();

                        longPoll.setKey(curr.getKey());
                        longPoll.setServer(curr.getServer());
                        longPoll.setTs(curr.getTs());

                        listener.onSuccess(curr);
                    }
                }
        ).start();
    }

    public void sendMessage(final Message message, final int id, final OperationListener<Message> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        messageLab.addMessage(message);
                        Integer messageId = fetcher.sendMessageToUser(message.getText(), id);
                        
                        if (messageId != 0) {
                            message.setMessageId(messageId);
                            message.setSendState(1);
                            message.setTime(System.currentTimeMillis() / 1000);
                            
                            listener.onSuccess(message);
                        }
                    }
                }
        ).start();
    }

    public void removeFriend(final User user, final OperationListener<User> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        boolean isRemoved = fetcher.removeUser(user.getId());

                        if (isRemoved) {
                            listener.onSuccess(user);
                        }
                    }
                }
        ).start();
    }

    public void connectToLongPollServer(final Long ts, OperationListener<List<Message>> listener, OperationListener<int[]> eventListener) {
        fetcher.connectToFromLongPollServer(ts, listener, eventListener);
    }
    
    public void loadCurrentUser(final OperationListener<User> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        User temp = fetcher.getCurrentUser();

                        currentUser.setId(temp.getId());
                        currentUser.setFirstName(temp.getFirstName());
                        currentUser.setLastName(temp.getLastName());
                        currentUser.setLastSeen(temp.getLastSeen());
                        currentUser.setOnline(temp.isOnline());
                        currentUser.setPhotoURL(temp.getPhotoURL());
                        
                        listener.onSuccess(temp);
                    }
                }
        ).start();
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("User has not been initialized yet!");
        }

        return currentUser;
    }

    public LongPoll getCurrentLongPoll() {
        if (longPoll == null) {
            throw new IllegalStateException("Long poll doesn't initialized!");
        }

        return longPoll;
    }
}
