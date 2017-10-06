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
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import ua.nure.dzhafarov.vkontakte.R;
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
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, getActivity());
        recyclerView.setAdapter(messageAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        destUser = (User) getActivity().getIntent().getSerializableExtra(REQUEST_USER_PROFILE);
        getActivity().setTitle(destUser.getFirstName() + " " + destUser.getLastName());

        vkManager = VKManager.getInstance();
        messageLab = MessageLab.getInstance(getActivity());

        registerReceiver();
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

                                    messageAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
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
                
                vkManager.sendMessage(message, message.getUserId(), new OperationListener<Message>() {
                            @Override
                            public void onSuccess(final Message me) {
                                messageLab.updateMessage(me);
                            }
                        });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private void showNewMessageInUI(Message message) {
        messages.add(0, message);
        messageAdapter.notifyDataSetChanged();
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
        message.setTime(System.currentTimeMillis() / 1000);
        
        return message;
    }
    
    public void registerReceiver() {
        receiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdateService.SEND_MESSAGES_VK);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private class MessageReceiver extends BroadcastReceiver {

        public MessageReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(UpdateService.SEND_MESSAGES_VK)) {

                Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    List<Message> mess = messageLab.getMessagesByFromId(destUser.getId(),
                                            vkManager.getCurrentLongPoll().getTs()
                                    );

                                    messages.addAll(0, mess);

                                    OperationListener<Message> listener = new OperationListener<Message>() {
                                        @Override
                                        public void onSuccess(Message object) {
                                            object.setReadState(1);
                                            messageLab.updateMessage(object);
                                        }
                                    };
                                    
                                    for (Message m : mess) {
                                        vkManager.markMessageAsRead(m, listener);
                                    }

                                    recyclerView.scrollToPosition(0);
                                    messageAdapter.notifyDataSetChanged();
                                }
                            }
                    );
                }
            }
        }
    }
}
