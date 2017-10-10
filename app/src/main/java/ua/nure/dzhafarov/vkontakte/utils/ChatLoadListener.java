package ua.nure.dzhafarov.vkontakte.utils;

import java.util.List;

import ua.nure.dzhafarov.vkontakte.models.Message;

public interface ChatLoadListener {
    void onChatLoaded(List<Message> messages, Message lastMessage);
    void onFailure(String message);
}