package ua.nure.dzhafarov.vkontakte.utils;

import java.util.List;

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
    
    public static synchronized VKManager getInstance() {
        if (instance == null) {
            instance = new VKManager();
        }

        return instance;
    }

    public synchronized void initialize(String token, OperationListener<LongPoll> listener) {
        if (fetcher == null) {
            currentUser = new User();
            longPoll = new LongPoll();
            fetcher = new VKFetcher(token);
            initLongPoll(listener);
        }
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
    
    public void markMessageAsRead(final Message message) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        fetcher.markMessageAsRead(message);      
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
                        fetcher.sendMessageToUser(message.getBody(), id);
                        listener.onSuccess(message);
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

    public Long connectToLongPollServer(final Long ts, OperationListener<Message> listener) {
        Long newTs = fetcher.connectToLongPollServer(ts, listener);
        longPoll.setTs(newTs);
        return newTs;
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
