package com.mowtiie.easybank;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class EasyBankDatabase extends SQLiteOpenHelper {

    public EasyBankDatabase(@Nullable Context context) {
        super(context, "easybank.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users(" +
                "userId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fullName TEXT, " +
                "email TEXT," +
                "password TEXT, " +
                "phone TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS accounts(" +
                "accountId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER, " +
                "accountType TEXT," +
                "accountName TEXT," +
                "balance REAL," +
                "createdAt REAL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS transactions(" +
                "transactionId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sourceId INTEGER, " +
                "receiverId INTEGER, " +
                "amount REAL," +
                "transactionType TEXT," +
                "createdAt REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users;");
        db.execSQL("DROP TABLE IF EXISTS accounts;");
        db.execSQL("DROP TABLE IF EXISTS transactions;");
    }
}
