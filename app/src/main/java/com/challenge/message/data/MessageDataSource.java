package com.challenge.message.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.challenge.message.model.Message;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by kbw815 on 8/19/15.
 */
public class MessageDataSource {
    private SQLiteDatabase mDb;
    private MessageDataHelper mDataHelper;

    public MessageDataSource(Context context) {
        mDataHelper = new MessageDataHelper(context);
    }

    public void open() throws SQLException {
        mDb = mDataHelper.getWritableDatabase();
    }

    public void close() {
        mDataHelper.close();
    }

    public long addMessage(String message) {
        ContentValues values = new ContentValues();
        values.put(MessageContract.MessageEntry.COLUMN_NAME_CONTENT, message);

        return mDb.insert(
                MessageContract.MessageEntry.TABLE_NAME,
                null,
                values);
    }

    public void deleteMessage(long id) {
        mDb.delete(MessageContract.MessageEntry.TABLE_NAME, MessageContract.MessageEntry._ID + " = " + id, null);
    }

    public void updateMessage(long id, String message) {
        ContentValues values = new ContentValues();
        values.put(MessageContract.MessageEntry.COLUMN_NAME_CONTENT, message);
        values.put(MessageContract.MessageEntry.COLUMN_NAME_UPDATED_AT, getDateTime());
        String selection = MessageContract.MessageEntry._ID + "= ?";
        String[] selectionArgs = { String.valueOf(id) };
        mDb.update(
                MessageContract.MessageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public Message getMessage(long id) {
        String[] projection = {
                MessageContract.MessageEntry._ID,
                MessageContract.MessageEntry.COLUMN_NAME_CONTENT,
                MessageContract.MessageEntry.COLUMN_NAME_UPDATED_AT
        };
        String selection = MessageContract.MessageEntry._ID + "= ?";
        String[] selectionArgs = { String.valueOf(id) };
        Cursor c = mDb.query(
                MessageContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();
        Message message = new Message();
        message.setId(c.getLong(0));
        message.setContent(c.getString(1));
        message.setUpdatedAt(formatDateTime(c.getString(2)));

        return message;
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>();
        String[] projection = {
                MessageContract.MessageEntry._ID,
                MessageContract.MessageEntry.COLUMN_NAME_CONTENT,
                MessageContract.MessageEntry.COLUMN_NAME_UPDATED_AT
        };

        String sortOrder =
                MessageContract.MessageEntry.COLUMN_NAME_CREATED_AT + " ASC";

        Cursor c = mDb.query(
                MessageContract.MessageEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        c.moveToFirst();
        while (c.moveToNext()) {
            Message message = new Message();
            message.setId(c.getLong(0));
            message.setContent(c.getString(1));
            message.setUpdatedAt(formatDateTime(c.getString(2)));
            messages.add(message);
        }

        return messages;
    }

    public Date formatDateTime(String timeToFormat) {
        Date finalDateTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = sdf.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                finalDateTime = new Date(when + TimeZone.getDefault().getOffset(when));
            }
        }
        return finalDateTime;
    }
}
