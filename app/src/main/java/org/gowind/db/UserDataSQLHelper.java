package org.gowind.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shiv.loka on 11/19/16.
 */

public class UserDataSQLHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "gowind.db";
    public static final int DATABASE_VERSION = 1;
    public static final String USER_TABLE = "user";
    public static final String USER_ID = "id";
    public static final String USER_NAME = "username";
    public static final String USER_FIRST_NAME = "first name";
    public static final String USER_LAST_NAME = "last name";
    public static final String USER_PHONE = "phone";
    public static final String USER_EMAIL = "email";

    private static final String CREATE_USER_TABLE = "create table "
            + USER_TABLE
            + "("
            + USER_ID + " integer primary key autoincrement, "
            + USER_NAME + "text not null, "
            + USER_FIRST_NAME + " text not null, "
            + USER_LAST_NAME + " text not null,"
            + USER_PHONE + " text, "
            + USER_EMAIL + " text not null"
            + ");";

//    private static final String SELECT_USER_BY_NAME=

    public static final String UPDATE_USER_TABLE = "drop table if exists " + USER_TABLE;

    public UserDataSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(UPDATE_USER_TABLE);
    }
}
