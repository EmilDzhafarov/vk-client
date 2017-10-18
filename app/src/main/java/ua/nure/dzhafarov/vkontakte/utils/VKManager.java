package ua.nure.dzhafarov.vkontakte.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.database.MessageBaseHelper;
import ua.nure.dzhafarov.vkontakte.database.MessageLab;
import ua.nure.dzhafarov.vkontakte.models.Community;
import ua.nure.dzhafarov.vkontakte.models.Photo;
import ua.nure.dzhafarov.vkontakte.models.PhotoAlbum;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.LongPoll;
import ua.nure.dzhafarov.vkontakte.models.Message;

public class VKManager {

    private static VKManager instance;

    private VKManager() {
    }

    private VKFetcher fetcher;
    private LongPoll longPoll;
    private User currentUser;
    private MessageLab messageLab;
    private Context context;
    private OperationListener<List<Message>> unsentMessageListener;
    
    public static synchronized VKManager getInstance() {
        if (instance == null) {
            instance = new VKManager();
        }

        return instance;
    }

    public synchronized void initialize(Context context, String token) {
        if (fetcher == null) {
            fetcher = new VKFetcher(token);
        }

        this.context = context.getApplicationContext();
        currentUser = new User();
        longPoll = new LongPoll();
        messageLab = MessageLab.getInstance(this.context);
    }

    public void registerUnsentMessagesListener(OperationListener<List<Message>> listener) {
        this.unsentMessageListener = listener;
    }
    
    public void loadFriends(final OperationListener<List<User>> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<User> friends = fetcher.getAllFriends();
                            listener.onSuccess(friends);
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public void loadCommunities(final OperationListener<List<Community>> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Community> communities = fetcher.getAllCommunities();
                            listener.onSuccess(communities);
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public void loadPhotosFromAlbum(final Integer ownerId, final Integer albumId, final OperationListener<List<Photo>> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Photo> photos = fetcher.getAllPhotosFromAlbum(ownerId, albumId);
                            listener.onSuccess(photos);
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public void loadPhotoAlbums(final Integer ownerId, final OperationListener<List<PhotoAlbum>> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<PhotoAlbum> result = fetcher.getAllPhotoAlbums(ownerId);
                            listener.onSuccess(result);
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public void loadMessages(final User user, final Message curr, final ChatLoadListener listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Message> messages = fetcher.getChatWith(user, curr);
                            listener.onChatLoaded(messages, curr);
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public void markMessageAsRead(final Message message, final OperationListener<Message> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Integer resultCode = fetcher.markMessageAsRead(message);

                            if (resultCode != -1) {
                                listener.onSuccess(message);
                            }
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }
    
    public void sendMessage(final Message message, final int id, final OperationListener<Message> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Integer messageId = fetcher.sendMessageToUser(message.getText(), id);

                            if (messageId != 0) {
                                message.setMessageId(messageId);
                                message.setSendState(1);
                                message.setTime(System.currentTimeMillis());

                                listener.onSuccess(message);
                            } else {
                                listener.onFailure(context.getString(R.string.error_send_message));
                            }
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public boolean isNetworksAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            return (netInfo != null && netInfo.isConnectedOrConnecting());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void sendUnsentMessages() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Message> messages = messageLab.getAllUnsentMessages(currentUser.getId());
                            
                            for (Message message : messages) {
                                Integer messageId = fetcher.sendMessageToUser(message.getText(), message.getUserId());
                                
                                if (messageId > 0) {
                                    message.setSendState(1);
                                    message.setMessageId(messageId);
                                    message.setTime(System.currentTimeMillis());
                                    messageLab.updateMessage(message);
                                } else {
                                    Log.e("VkManager", "Error with sending message " + message);
                                    break;
                                }
                            }
                            
                            if (unsentMessageListener != null) {
                                unsentMessageListener.onSuccess(messages);
                            }
                        } catch (IOException iex) {
                            iex.printStackTrace();
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
                        try {
                            boolean isRemoved = fetcher.removeUser(user.getId());

                            if (isRemoved) {
                                listener.onSuccess(user);
                            } else {
                                listener.onFailure(context.getString(R.string.error_remove_user));
                            }

                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public void connectToLongPollServer(final Long ts, OperationListener<JSONArray> eventListener) {
        try {
            fetcher.connectToLongPollServer(ts, eventListener);
        } catch (IOException iex) {
            eventListener.onFailure(context.getString(R.string.error_connect_server));
        }
    }

    public void loadCurrentUser(final OperationListener<User> listener) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            User temp = fetcher.getInfoAboutCurrentUser();

                            if (temp != null) {
                                currentUser.setId(temp.getId());
                                currentUser.setFirstName(temp.getFirstName());
                                currentUser.setLastName(temp.getLastName());
                                currentUser.setLastSeen(temp.getLastSeen());
                                currentUser.setOnline(temp.isOnline());
                                currentUser.setPhotoURL(temp.getPhotoURL());

                                listener.onSuccess(temp);
                            } else {
                                listener.onFailure(context.getString(R.string.error_load_current_user));
                            }
                        } catch (IOException iex) {
                            listener.onFailure(context.getString(R.string.error_connect_server));
                        }
                    }
                }
        ).start();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LongPoll getCurrentLongPoll() {
        return longPoll;
    }

    public void initLongPoll(final OperationListener<LongPoll> listener) {
        try {
            LongPoll curr = fetcher.getLongPollServer();

            longPoll.setKey(curr.getKey());
            longPoll.setServer(curr.getServer());
            longPoll.setTs(curr.getTs());

            listener.onSuccess(curr);

        } catch (IOException iex) {
            listener.onFailure(context.getString(R.string.error_connect_server));
        }
    }
}