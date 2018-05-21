package com.vilakshan.weathermaster.utils;

import android.database.Cursor;
import android.support.annotation.Nullable;


public class CursorUtils {

    public static void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public static int getCount(Cursor cursor) {
        if (isCursorEmpty(cursor)) {
            return 0;
        }
        return cursor.getCount();
    }

    public static boolean isCursorEmpty(@Nullable Cursor cursor) {
        return cursor == null || cursor.getCount() <= 0 || cursor.isClosed();
    }
}
