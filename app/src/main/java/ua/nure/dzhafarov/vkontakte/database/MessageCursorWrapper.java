package ua.nure.dzhafarov.vkontakte.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import ua.nure.dzhafarov.vkontakte.models.Message;

public class MessageCursorWrapper extends CursorWrapper {
    
    public MessageCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Message getMessage() {
        
        Long id = getLong(getColumnIndex(MessageBaseHelper.ID));
        Integer messageId = getInt(getColumnIndex(MessageBaseHelper.MESSAGE_ID));
        Integer userId = getInt(getColumnIndex(MessageBaseHelper.USER_ID));
        Integer fromId = getInt(getColumnIndex(MessageBaseHelper.FROM_ID));
        String text = getString(getColumnIndex(MessageBaseHelper.TEXT));
        Long time = getLong(getColumnIndex(MessageBaseHelper.TIME));
        Integer readState = getInt(getColumnIndex(MessageBaseHelper.READ_STATE));
        Long ts = getLong(getColumnIndex(MessageBaseHelper.TS));
        Integer sendState = getInt(getColumnIndex(MessageBaseHelper.SEND_STATE));
        
        Message message = new Message();
        
        message.setId(id);
        message.setMessageId(messageId);
        message.setUserId(userId);
        message.setFromId(fromId);
        message.setText(text);
        message.setTime(time);
        message.setTs(ts);
        message.setReadState(readState);
        message.setSendState(sendState);
        
        return message;
    }
}
