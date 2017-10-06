package ua.nure.dzhafarov.vkontakte.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "MessageBase.db";
    
    public static final String TABLE_NAME = "messages";
    public static final String ID = "_id";
    public static final String MESSAGE_ID = "message_id";
    public static final String USER_ID = "user_id";
    public static final String FROM_ID = "from_id";
    public static final String TEXT = "text";
    public static final String TIME = "time";
    public static final String TS = "ts";
    public static final String READ_STATE = "read_state";
    public static final String SEND_STATE = "send_state";
    
    public MessageBaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
                        + MESSAGE_ID + ", "
                        + USER_ID + ", "
                        + FROM_ID + ", "
                        + TEXT + ", " 
                        + TIME + ", " 
                        + TS + ", "
                        + READ_STATE + ", "
                        + SEND_STATE + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
