package com.challenge.message.data;

import android.provider.BaseColumns;

/**
 * Created by kbw815 on 8/18/15.
 */
public final class MessageContract {
    public MessageContract() {

    }

    public static abstract class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
    }
}
