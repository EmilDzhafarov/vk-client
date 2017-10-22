package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.activities.BaseActivity;
import ua.nure.dzhafarov.vkontakte.adapters.MessageAdapter;
import ua.nure.dzhafarov.vkontakte.database.MessageLab;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.models.Message;
import ua.nure.dzhafarov.vkontakte.services.UpdateService;
import ua.nure.dzhafarov.vkontakte.utils.ChatLoadListener;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

import static ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile.REQUEST_USER_PROFILE;

public class FragmentChat extends Fragment implements View.OnClickListener {

    private User destUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private EditText messageTypeField;
    private ImageButton sendMessageButton;
    private TextView userTypesMessageTextView;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private VKManager vkManager;
    private MessageLab messageLab;
    private MessageReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout_messages);
        swipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingMessages(messages.get(messages.size() - 1));
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.chat_messages_recycler_view);
        messageTypeField = (EditText) view.findViewById(R.id.send_body_message);

        sendMessageButton = (ImageButton) view.findViewById(R.id.send_button_message);
        sendMessageButton.setOnClickListener(this);
        userTypesMessageTextView = (TextView) view.findViewById(R.id.user_types_text_view);
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, getActivity());
        recyclerView.setAdapter(messageAdapter);

        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if ( bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.smoothScrollToPosition(0);
                        }
                    }, 500);
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                
                
            }
        });
        
        destUser = (User) getActivity().getIntent().getSerializableExtra(REQUEST_USER_PROFILE);
        Activity activity = getActivity();

        if (destUser != null && activity != null) {
            activity.setTitle(
                    getString(
                            R.string.user_chat_title,
                            destUser.getFirstName(),
                            destUser.getLastName()
                    )
            );

            userTypesMessageTextView.setText(
                    getString(R.string.user_typing,
                            destUser.getFirstName()
                    )
            );
        }

        vkManager = VKManager.getInstance();
        messageLab = MessageLab.getInstance(getActivity());
        
        loadingMessages(null);
    }

    private void loadingMessages(Message curr) {
        swipeRefreshLayout.setRefreshing(true);
        vkManager.loadMessages(destUser, curr, new ChatLoadListener() {
            @Override
            public void onChatLoaded(final List<Message> mess, final Message curr) {
                Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (curr == null) {
                                        recyclerView.scrollToPosition(0);
                                        messages.addAll(mess);
                                    } else {
                                        messages.addAll(mess.subList(1, mess.size()));
                                        recyclerView.scrollToPosition(messages.size() - mess.size() - 1);
                                    }

                                    showNewUnreadMessages();

                                    messageAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                    );
                }
            }

            @Override
            public void onFailure(final String message) {
                Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                    Toast.makeText(
                                            getActivity(),
                                            message,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    );
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_button_message) {
            String body = messageTypeField.getText().toString();

            if (!body.trim().isEmpty()) {
                final Message message = createMessageFromString(body);
                showNewMessageInUI(message);

                sendMessage(message);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver();
        vkManager.registerUnsentMessagesListener(new OperationListener<List<Message>>() {
            @Override
            public void onSuccess(final List<Message> object) {
                Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    for (Message message : object) {
                                        int pos = messages.indexOf(message);
                                        messages.set(pos, message);
                                        messageAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                    );
                }
            }

            @Override
            public void onFailure(String message) {
                // skip this
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(receiver);
        vkManager.unregisterUnsentMessagesListener();
    }

    private void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdateService.ACTION_SEND_MESSAGE_VK);
        intentFilter.addAction(UpdateService.ACTION_USER_READ_MESSAGE);
        intentFilter.addAction(UpdateService.ACTION_USER_TYPES_MESSAGE);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void showNewMessageInUI(Message message) {
        messages.add(0, message);
        messageAdapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);
        messageTypeField.setText("");
    }

    private Message createMessageFromString(String body) {
        Message message = new Message();

        message.setText(body);
        message.setSendState(0);
        message.setUserId(destUser.getId());
        message.setFromId(vkManager.getCurrentUser().getId());
        message.setReadState(0);
        message.setTs(vkManager.getCurrentLongPoll().getTs());
        message.setTime(System.currentTimeMillis());

        return message;
    }

    private void showNewUnreadMessages() {
        List<Message> messages = messageLab.getAllUnreadMessagesWithUser(destUser.getId());

        for (Message m : messages) {
            showNewMessageInUI(m);
        }

        markMessagesAsRead(messages);
    }
    
    private void sendMessage(Message message) {
        if (vkManager.isNetworksAvailable(getActivity())) {
            vkManager.sendMessage(message, destUser.getId(), new OperationListener<Message>() {
                @Override
                public void onSuccess(final Message me) {
                    Activity activity = getActivity();

                    if (activity != null) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        messageLab.updateMessage(me);
                                        int pos = messages.indexOf(me);
                                        messages.set(pos, me);
                                        messageAdapter.notifyItemChanged(pos);
                                    }
                                }
                        );
                    }
                }

                @Override
                public void onFailure(final String message) {
                    Activity activity = getActivity();

                    if (activity != null) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                getActivity(),
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    }
                }
            });    
        } else {
            messageLab.addMessage(message);   
        }
    }

    private final OperationListener<Message> listener = new OperationListener<Message>() {
        @Override
        public void onSuccess(Message object) {
            object.setReadState(1);
            messageLab.updateMessage(object);
        }

        @Override
        public void onFailure(String message) {
            // skip this method here
        }
    };

    private void markMessagesAsRead(List<Message> messages) {
        for (Message m : messages) {
            vkManager.markMessageAsRead(m, listener);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {

        private Animation animation = new AlphaAnimation(0.0f, 1.0f);

        public MessageReceiver() {
            super();

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    userTypesMessageTextView.setVisibility(View.VISIBLE);
                    recyclerView.scrollToPosition(0);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    userTypesMessageTextView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            animation.setDuration(5000);
            animation.setStartOffset(20);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            final Activity activity = getActivity();

            if (activity != null) {
                if (intent.getAction().equals(UpdateService.ACTION_SEND_MESSAGE_VK)) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    List<Message> mess = messageLab.getMessagesByFromId(
                                            destUser.getId(),
                                            vkManager.getCurrentLongPoll().getTs()
                                    );

                                    messages.addAll(0, mess);
                                    markMessagesAsRead(mess);

                                    recyclerView.scrollToPosition(0);
                                    messageAdapter.notifyDataSetChanged();
                                }
                            }
                    );
                }

                if (intent.getAction().equals(UpdateService.ACTION_USER_READ_MESSAGE)) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    int userId = intent.getIntExtra(UpdateService.CURRENT_USER_ID, -1);
                                    int localMessageId = intent.getIntExtra(UpdateService.MESSAGE_LOCAL_ID, -1);

                                    if (userId != -1 && localMessageId != -1) {
                                        int pos = 0;
                                        for (int i = 0; i < messages.size(); i++) {
                                            if (messages.get(i).getMessageId() == localMessageId) {
                                                pos = i;
                                                break;
                                            }
                                        }

                                        for (int j = pos; j < messages.size(); j++) {
                                            messages.get(j).setReadState(1);
                                        }

                                        messageAdapter.notifyItemRangeChanged(pos, messages.size() - pos - 1);
                                    }
                                }
                            }
                    );
                }

                if (intent.getAction().equals(UpdateService.ACTION_USER_TYPES_MESSAGE)) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    int userId = intent.getIntExtra(UpdateService.CURRENT_USER_ID, -1);

                                    if (userId != -1 && userId == destUser.getId()) {
                                        userTypesMessageTextView.startAnimation(animation);
                                    }
                                }
                            }
                    );
                }
            }
        }
    }
}
