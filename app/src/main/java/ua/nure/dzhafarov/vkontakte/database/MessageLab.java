package ua.nure.dzhafarov.vkontakte.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import ua.nure.dzhafarov.vkontakte.models.Message;

import static ua.nure.dzhafarov.vkontakte.database.MessageBaseHelper.ID;
import static ua.nure.dzhafarov.vkontakte.database.MessageBaseHelper.TABLE_NAME;

public class MessageLab {
    
    private static MessageLab instance;
    
    private SQLiteDatabase database;
    
    public static synchronized MessageLab getInstance(Context context) {
        if (instance == null) {
            instance = new MessageLab(context.getApplicationContext());
        }
        
        return instance;
    }
    
    private MessageLab(Context context) {
        database = new MessageBaseHelper(context).getWritableDatabase();
    }
    
    private List<Message> getMessagesFromCursor(MessageCursorWrapper cursor) {
        List<Message> messages = new ArrayList<>();

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                messages.add(cursor.getMessage());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        
        return messages;
    }
    
    public List<Message> getAllUnreadMessagesWithUser(Integer peerId) {
        MessageCursorWrapper cursor = queryMessages(
                "SELECT * FROM messages WHERE from_id = " + peerId + " AND read_state = 0");
        
        return getMessagesFromCursor(cursor);
    }
    
    public List<Message> getAllUnsentMessages(Integer fromId) {
        MessageCursorWrapper cursor = queryMessages(
                "SELECT * FROM messages WHERE from_id = " + fromId + " AND send_state = 0 ORDER BY time");
        
        return getMessagesFromCursor(cursor);
    }
    
    public List<Message> getMessagesByFromId(Integer fromId, Long ts) {
        MessageCursorWrapper cursor = queryMessages(
                "SELECT * FROM messages WHERE from_id = " + fromId + " AND ts >= " + ts);
        
        return getMessagesFromCursor(cursor);
    }

    private static ContentValues getContentValues(Message message) {
        ContentValues values = new ContentValues();

        values.put(MessageBaseHelper.ID, message.getId());
        values.put(MessageBaseHelper.MESSAGE_ID, message.getMessageId());
        values.put(MessageBaseHelper.USER_ID, message.getUserId());
        values.put(MessageBaseHelper.FROM_ID, message.getFromId());
        values.put(MessageBaseHelper.TEXT, message.getText());
        values.put(MessageBaseHelper.TIME, message.getTime());
        values.put(MessageBaseHelper.TS, message.getTs());
        values.put(MessageBaseHelper.READ_STATE, message.getReadState());
        values.put(MessageBaseHelper.SEND_STATE, message.getSendState());

        return values;
    }

    private static ContentValues getContentValuesWithoutId(Message message) {
        ContentValues values = new ContentValues();
        
        values.put(MessageBaseHelper.MESSAGE_ID, message.getMessageId());
        values.put(MessageBaseHelper.USER_ID, message.getUserId());
        values.put(MessageBaseHelper.FROM_ID, message.getFromId());
        values.put(MessageBaseHelper.TEXT, message.getText());
        values.put(MessageBaseHelper.TIME, message.getTime());
        values.put(MessageBaseHelper.TS, message.getTs());
        values.put(MessageBaseHelper.READ_STATE, message.getReadState());
        values.put(MessageBaseHelper.SEND_STATE, message.getSendState());

        return values;
    }
    
    private MessageCursorWrapper queryMessages(String query) {
        Cursor cursor = database.rawQuery(query, null);

        return new MessageCursorWrapper(cursor);
    }
    
    
    public void addMessage(Message message) {
        if (message.getId() < 0) {
            ContentValues values = getContentValuesWithoutId(message);
            long id = database.insert(TABLE_NAME, null, values);

            message.setId(id);
        }
    }
    
    public void updateMessage(Message message) {
        ContentValues values = getContentValues(message);
        Long id = message.getId();
        
        database.update(TABLE_NAME, values, ID + " = ?", new String[] {id.toString()});
    }
    
    public void deleteMessage(Message message) {
        Long id = message.getId();
        
        database.delete(TABLE_NAME, ID + " = ?", new String[] {id.toString()});
    }
}
