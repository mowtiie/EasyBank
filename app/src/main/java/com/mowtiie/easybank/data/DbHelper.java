package com.mowtiie.easybank.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "easybank.db";
    private static final int DATABASE_VERSION = 3;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, password TEXT, name TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE accounts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, number TEXT, type TEXT, balance INTEGER)");
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER, type TEXT, amount INTEGER, date TEXT, description TEXT, counterparty TEXT)");
        db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, title TEXT, message TEXT, date TEXT, is_read INTEGER)");
        seedDemo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS accounts");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS notifications");
        onCreate(db);
    }

    private void tx(SQLiteDatabase db, int account, String type, int amount, String date, String description, String counterparty) {
        db.execSQL("INSERT INTO transactions (account_id, type, amount, date, description, counterparty) VALUES (?, ?, ?, ?, ?, ?)", new Object[]{account, type, amount, date, description, counterparty});
    }

    private void notif(SQLiteDatabase db, int user, String title, String message, String date, int read) {
        db.execSQL("INSERT INTO notifications (user_id, title, message, date, is_read) VALUES (?, ?, ?, ?, ?)", new Object[]{user, title, message, date, read});
    }

    private void seedDemo(SQLiteDatabase db) {
        db.execSQL("INSERT INTO users (email, password, name, phone) VALUES (?, ?, ?, ?)", new Object[]{"demo@demo.com", "demo1234", "Juan Dela Cruz", "09171234567"});
        db.execSQL("INSERT INTO users (email, password, name, phone) VALUES (?, ?, ?, ?)", new Object[]{"other@demo.com", "demo1234", "Maria Santos", "09181234567"});

        // Balances are the result of the transactions below.
        db.execSQL("INSERT INTO accounts (user_id, number, type, balance) VALUES (?, ?, ?, ?)", new Object[]{1, "1234567890", Data.ACCOUNT_SAVINGS, 36300});
        db.execSQL("INSERT INTO accounts (user_id, number, type, balance) VALUES (?, ?, ?, ?)", new Object[]{1, "2345678901", Data.ACCOUNT_CHECKING, 16500});
        db.execSQL("INSERT INTO accounts (user_id, number, type, balance) VALUES (?, ?, ?, ?)", new Object[]{2, "3456789012", Data.ACCOUNT_SAVINGS, 400});

        // Account ids: 1 = demo Savings, 2 = demo Checking, 3 = Maria Savings. Inserted oldest first.
        tx(db, 1, Data.TYPE_DEPOSIT, 30000, "Sep 01, 2026 09:00 AM", "Salary deposit", "");
        tx(db, 1, Data.TYPE_BILL, 2200, "Sep 05, 2026 07:40 PM", "Meralco", "123456789");
        tx(db, 1, Data.TYPE_BILL, 1500, "Sep 08, 2026 06:15 PM", "Manila Water", "456789012");
        tx(db, 1, Data.TYPE_WITHDRAWAL, 3000, "Sep 12, 2026 12:10 PM", "Cash withdrawal", "");
        tx(db, 1, Data.TYPE_TRANSFER_OUT, 5000, "Sep 15, 2026 10:25 AM", "Transfer to Checking", "2345678901");
        tx(db, 2, Data.TYPE_TRANSFER_IN, 5000, "Sep 15, 2026 10:25 AM", "Transfer from Savings", "1234567890");
        tx(db, 2, Data.TYPE_DEPOSIT, 8000, "Sep 18, 2026 02:00 PM", "Cash deposit", "");
        tx(db, 1, Data.TYPE_BILL, 3500, "Sep 20, 2026 08:20 PM", "BDO", "5412345678");
        tx(db, 2, Data.TYPE_BILL, 1800, "Sep 25, 2026 05:50 PM", "Smart", "09171234567");
        tx(db, 1, Data.TYPE_DEPOSIT, 15000, "Sep 28, 2026 01:30 PM", "Cash deposit", "");
        tx(db, 1, Data.TYPE_DEPOSIT, 20000, "Oct 01, 2026 09:15 AM", "Salary deposit", "");
        tx(db, 2, Data.TYPE_DEPOSIT, 10000, "Oct 02, 2026 10:30 AM", "Cash deposit", "");
        tx(db, 3, Data.TYPE_DEPOSIT, 5000, "Oct 03, 2026 11:00 AM", "Cash deposit", "");
        tx(db, 1, Data.TYPE_BILL, 2500, "Oct 05, 2026 08:45 PM", "Meralco", "123456789");
        tx(db, 3, Data.TYPE_BILL, 800, "Oct 06, 2026 04:10 PM", "Globe", "09181234567");
        tx(db, 1, Data.TYPE_TRANSFER_OUT, 3000, "Oct 08, 2026 01:20 PM", "Transfer to Checking", "2345678901");
        tx(db, 2, Data.TYPE_TRANSFER_IN, 3000, "Oct 08, 2026 01:20 PM", "Transfer from Savings", "1234567890");
        tx(db, 1, Data.TYPE_WITHDRAWAL, 1500, "Oct 10, 2026 11:05 AM", "Cash withdrawal", "");
        tx(db, 2, Data.TYPE_BILL, 1200, "Oct 12, 2026 07:30 PM", "PLDT", "987654321");
        tx(db, 1, Data.TYPE_TRANSFER_OUT, 2000, "Oct 14, 2026 03:10 PM", "Transfer to Maria Santos", "3456789012");
        tx(db, 3, Data.TYPE_TRANSFER_IN, 2000, "Oct 14, 2026 03:10 PM", "Transfer from Juan Dela Cruz", "1234567890");
        tx(db, 2, Data.TYPE_BILL, 4500, "Oct 15, 2026 06:40 PM", "BPI", "4098765432");
        tx(db, 1, Data.TYPE_BILL, 1000, "Oct 16, 2026 09:05 PM", "Globe", "09171234567");
        tx(db, 3, Data.TYPE_TRANSFER_OUT, 1500, "Oct 17, 2026 10:50 AM", "Transfer to Juan Dela Cruz", "1234567890");
        tx(db, 1, Data.TYPE_TRANSFER_IN, 1500, "Oct 17, 2026 10:50 AM", "Transfer from Maria Santos", "3456789012");
        tx(db, 2, Data.TYPE_WITHDRAWAL, 2000, "Oct 18, 2026 09:30 AM", "Cash withdrawal", "");
        tx(db, 1, Data.TYPE_WITHDRAWAL, 5000, "Oct 18, 2026 12:15 PM", "Cash withdrawal", "");
        tx(db, 3, Data.TYPE_BILL, 4300, "Oct 18, 2026 05:45 PM", "Meralco", "246813579");

        notif(db, 1, "Welcome to EasyBank", "Your Savings and Checking accounts are ready to use.", "Sep 01, 2026 08:55 AM", 1);
        notif(db, 1, "Deposit successful", "You deposited " + Data.money(30000) + " to your Savings account.", "Sep 01, 2026 09:00 AM", 1);
        notif(db, 1, "Bill paid", "You paid " + Data.money(2200) + " to Meralco.", "Sep 05, 2026 07:40 PM", 1);
        notif(db, 1, "Transfer sent", "You sent " + Data.money(5000) + " to your Checking account.", "Sep 15, 2026 10:25 AM", 1);
        notif(db, 1, "Bill paid", "You paid " + Data.money(2500) + " to Meralco.", "Oct 05, 2026 08:45 PM", 1);
        notif(db, 1, "Transfer sent", "You sent " + Data.money(2000) + " to Maria Santos.", "Oct 14, 2026 03:10 PM", 0);
        notif(db, 1, "Transfer received", "You received " + Data.money(1500) + " from Maria Santos.", "Oct 17, 2026 10:50 AM", 0);
        notif(db, 1, "Withdrawal successful", "You withdrew " + Data.money(5000) + " from your Savings account.", "Oct 18, 2026 12:15 PM", 0);

        notif(db, 2, "Welcome to EasyBank", "Your Savings account is ready to use.", "Oct 03, 2026 10:55 AM", 1);
        notif(db, 2, "Transfer received", "You received " + Data.money(2000) + " from Juan Dela Cruz.", "Oct 14, 2026 03:10 PM", 1);
        notif(db, 2, "Low balance", "Your Savings account balance is below " + Data.money(Data.LOW_BALANCE_LIMIT) + ".", "Oct 18, 2026 05:45 PM", 0);
    }
}
