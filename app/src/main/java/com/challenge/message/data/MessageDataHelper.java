package com.challenge.message.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by kbw815 on 8/18/15.
 */
public class MessageDataHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String DEFAULT = " DEFAULT";
    private static final String CURRENT_TIMESTAMP = " CURRENT_TIMESTAMP";
    private static final String COMMA_SEP = ",";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Message.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MessageContract.MessageEntry.TABLE_NAME + " (" +
                    MessageContract.MessageEntry._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT," +
                    MessageContract.MessageEntry.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    MessageContract.MessageEntry.COLUMN_NAME_CREATED_AT + INTEGER_TYPE + DEFAULT + CURRENT_TIMESTAMP + COMMA_SEP +
                    MessageContract.MessageEntry.COLUMN_NAME_UPDATED_AT + INTEGER_TYPE + DEFAULT + CURRENT_TIMESTAMP +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessageContract.MessageEntry.TABLE_NAME;
    public MessageDataHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
