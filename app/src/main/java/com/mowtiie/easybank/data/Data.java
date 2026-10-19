package com.mowtiie.easybank.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Data {

    public static final String TYPE_DEPOSIT = "DEPOSIT";
    public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
    public static final String TYPE_TRANSFER_IN = "TRANSFER_IN";
    public static final String TYPE_TRANSFER_OUT = "TRANSFER_OUT";
    public static final String TYPE_BILL = "BILL";

    public static final String ACCOUNT_SAVINGS = "SAVINGS";
    public static final String ACCOUNT_CHECKING = "CHECKING";
    public static final String ACCOUNT_PAYROLL = "PAYROLL";
    public static final String ACCOUNT_TIME_DEPOSIT = "TIME_DEPOSIT";
    public static final String ACCOUNT_STUDENT = "STUDENT";
    // Types a user can open from Profile.
    public static final String[] ACCOUNT_TYPES = {ACCOUNT_SAVINGS, ACCOUNT_CHECKING, ACCOUNT_PAYROLL, ACCOUNT_TIME_DEPOSIT, ACCOUNT_STUDENT};

    public static final String CATEGORY_UTILITY = "UTILITY";
    public static final String CATEGORY_CREDIT_CARD = "CREDIT_CARD";
    public static final String CATEGORY_OTHER = "OTHER";

    // {name, category}
    public static final String[][] BILLERS = {
            {"Meralco", CATEGORY_UTILITY},
            {"Manila Water", CATEGORY_UTILITY},
            {"PLDT", CATEGORY_UTILITY},
            {"BDO", CATEGORY_CREDIT_CARD},
            {"BPI", CATEGORY_CREDIT_CARD},
            {"Metrobank", CATEGORY_CREDIT_CARD},
            {"Globe", CATEGORY_OTHER},
            {"Smart", CATEGORY_OTHER},
            {"Tuition (STI)", CATEGORY_OTHER}
    };

    public static final String CURRENCY = "₱";
    public static final int MAX_AMOUNT = 50000;
    public static final int LOW_BALANCE_LIMIT = 500;
    public static final String DATE_PATTERN = "MMM dd, yyyy hh:mm a";

    public static int USER_ID;
    public static String USER_EMAIL;
    public static String USER_NAME;
    public static String USER_PHONE;

    private static DbHelper helper;

    public static SQLiteDatabase db(Context context) {
        if (helper == null) helper = new DbHelper(context.getApplicationContext());
        return helper.getWritableDatabase();
    }

    public static String money(int amount) {
        return CURRENCY + NumberFormat.getIntegerInstance(Locale.US).format(amount);
    }

    public static String now() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(new Date());
    }

    // "TRANSFER_IN" -> "Transfer In"
    public static String label(String constant) {
        StringBuilder sb = new StringBuilder();
        for (String word : constant.split("_")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(word.charAt(0)).append(word.substring(1).toLowerCase(Locale.US));
        }
        return sb.toString();
    }

    public static boolean isDebit(String type) {
        return type.equals(TYPE_WITHDRAWAL) || type.equals(TYPE_TRANSFER_OUT) || type.equals(TYPE_BILL);
    }

    public static ArrayList<String[]> rows(Context context, String sql, String... args) {
        ArrayList<String[]> list = new ArrayList<>();
        try (Cursor cursor = db(context).rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                String[] row = new String[cursor.getColumnCount()];
                for (int i = 0; i < row.length; i++) row[i] = cursor.getString(i);
                list.add(row);
            }
        }
        return list;
    }

    public static boolean login(Context context, String email, String password) {
        ArrayList<String[]> rows = rows(context, "SELECT id, email, name, phone FROM users WHERE email = ? COLLATE NOCASE AND password = ?", email, password);
        if (rows.isEmpty()) return false;
        String[] user = rows.get(0);
        USER_ID = Integer.parseInt(user[0]);
        USER_EMAIL = user[1];
        USER_NAME = user[2];
        USER_PHONE = user[3];
        return true;
    }

    public static void logout() {
        USER_ID = 0;
        USER_EMAIL = null;
        USER_NAME = null;
        USER_PHONE = null;
    }

    public static boolean emailExists(Context context, String email) {
        return DatabaseUtils.longForQuery(db(context), "SELECT COUNT(*) FROM users WHERE email = ? COLLATE NOCASE", new String[]{email}) > 0;
    }

    public static void addNotification(SQLiteDatabase db, int userId, String title, String message) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("message", message);
        values.put("date", now());
        values.put("is_read", 0);
        db.insert("notifications", null, values);
    }

    // Creates the user, a SAVINGS account with 0 balance and a welcome notification, then logs the user in.
    public static void signUp(Context context, String name, String email, String phone, String password) {
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            ContentValues user = new ContentValues();
            user.put("email", email);
            user.put("password", password);
            user.put("name", name);
            user.put("phone", phone);
            int userId = (int) db.insert("users", null, user);

            String number = newAccountNumber(db);

            ContentValues account = new ContentValues();
            account.put("user_id", userId);
            account.put("number", number);
            account.put("type", ACCOUNT_SAVINGS);
            account.put("balance", 0);
            db.insert("accounts", null, account);

            addNotification(db, userId, "Welcome to EasyBank", "Your Savings account " + number + " is ready to use.");
            db.setTransactionSuccessful();

            USER_ID = userId;
            USER_EMAIL = email;
            USER_NAME = name;
            USER_PHONE = phone;
        } finally {
            db.endTransaction();
        }
    }

    private static String newAccountNumber(SQLiteDatabase db) {
        String number;
        do {
            number = String.valueOf(1000000000L + (long) (Math.random() * 9000000000L));
        } while (DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM accounts WHERE number = ?", new String[]{number}) > 0);
        return number;
    }

    // Opens a 0 balance account of the given ACCOUNT_* type for the logged-in user and returns its number.
    public static String openAccount(Context context, String type) {
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            String number = newAccountNumber(db);
            ContentValues account = new ContentValues();
            account.put("user_id", USER_ID);
            account.put("number", number);
            account.put("type", type);
            account.put("balance", 0);
            db.insert("accounts", null, account);
            addNotification(db, USER_ID, "Account opened", "Your new " + label(type) + " account " + number + " is ready to use.");
            db.setTransactionSuccessful();
            return number;
        } finally {
            db.endTransaction();
        }
    }

    // {id, number, type, balance}
    public static ArrayList<String[]> accounts(Context context) {
        return rows(context, "SELECT id, number, type, balance FROM accounts WHERE user_id = ? ORDER BY id", String.valueOf(USER_ID));
    }

    public static int totalBalance(Context context) {
        return (int) DatabaseUtils.longForQuery(db(context), "SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE user_id = ?", new String[]{String.valueOf(USER_ID)});
    }

    // {id, type, amount, date, description, counterparty}
    public static ArrayList<String[]> recentTransactions(Context context, int limit) {
        return rows(context, "SELECT t.id, t.type, t.amount, t.date, t.description, t.counterparty FROM transactions t JOIN accounts a ON a.id = t.account_id WHERE a.user_id = ? ORDER BY t.id DESC LIMIT " + limit, String.valueOf(USER_ID));
    }

    public static int unreadCount(Context context) {
        return (int) DatabaseUtils.longForQuery(db(context), "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0", new String[]{String.valueOf(USER_ID)});
    }

    // {id, user_id, number, type, owner name, balance}
    private static String[] accountInfo(Context context, String where, String arg) {
        ArrayList<String[]> rows = rows(context, "SELECT a.id, a.user_id, a.number, a.type, u.name, a.balance FROM accounts a JOIN users u ON u.id = a.user_id WHERE " + where, arg);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static String[] accountById(Context context, String id) {
        return accountInfo(context, "a.id = ?", id);
    }

    public static String[] accountByNumber(Context context, String number) {
        return accountInfo(context, "a.number = ?", number);
    }

    public static ArrayList<String> accountLabels(ArrayList<String[]> accounts) {
        ArrayList<String> labels = new ArrayList<>();
        for (String[] a : accounts) {
            labels.add(label(a[2]) + " • " + a[1] + " • " + money(Integer.parseInt(a[3])));
        }
        return labels;
    }

    public static void failure(Context context, String title, String message) {
        addNotification(db(context), USER_ID, title, message);
    }

    private static void adjust(SQLiteDatabase db, String accountId, int delta) {
        db.execSQL("UPDATE accounts SET balance = balance + ? WHERE id = ?", new Object[]{delta, accountId});
    }

    private static void writeTx(SQLiteDatabase db, String accountId, String type, int amount, String description, String counterparty) {
        ContentValues values = new ContentValues();
        values.put("account_id", accountId);
        values.put("type", type);
        values.put("amount", amount);
        values.put("date", now());
        values.put("description", description);
        values.put("counterparty", counterparty);
        db.insert("transactions", null, values);
    }

    private static void checkLow(SQLiteDatabase db, String accountId) {
        long balance = DatabaseUtils.longForQuery(db, "SELECT balance FROM accounts WHERE id = ?", new String[]{accountId});
        if (balance < LOW_BALANCE_LIMIT) {
            String number = DatabaseUtils.stringForQuery(db, "SELECT number FROM accounts WHERE id = ?", new String[]{accountId});
            addNotification(db, USER_ID, "Low balance", "Account " + number + " balance is " + money((int) balance) + ", below " + money(LOW_BALANCE_LIMIT) + ".");
        }
    }

    public static void cash(Context context, String accountId, boolean deposit, int amount) {
        String number = accountById(context, accountId)[2];
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            adjust(db, accountId, deposit ? amount : -amount);
            writeTx(db, accountId, deposit ? TYPE_DEPOSIT : TYPE_WITHDRAWAL, amount, deposit ? "Cash deposit" : "Cash withdrawal", "");
            addNotification(db, USER_ID, deposit ? "Deposit successful" : "Withdrawal successful",
                    (deposit ? "You deposited " : "You withdrew ") + money(amount) + (deposit ? " to account " : " from account ") + number + ".");
            if (!deposit) checkLow(db, accountId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void transfer(Context context, String fromId, String toId, int amount) {
        String[] from = accountById(context, fromId);
        String[] to = accountById(context, toId);
        boolean own = to[1].equals(String.valueOf(USER_ID));
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            adjust(db, fromId, -amount);
            adjust(db, toId, amount);
            writeTx(db, fromId, TYPE_TRANSFER_OUT, amount, "Transfer to " + (own ? label(to[3]) : to[4]), to[2]);
            writeTx(db, toId, TYPE_TRANSFER_IN, amount, "Transfer from " + (own ? label(from[3]) : USER_NAME), from[2]);
            addNotification(db, USER_ID, "Transfer sent", "You sent " + money(amount) + " to " + (own ? "your " + label(to[3]) + " account" : to[4]) + ".");
            if (!own) {
                addNotification(db, Integer.parseInt(to[1]), "Transfer received", "You received " + money(amount) + " from " + USER_NAME + ".");
            }
            checkLow(db, fromId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void payBill(Context context, String accountId, String biller, String reference, int amount) {
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            adjust(db, accountId, -amount);
            writeTx(db, accountId, TYPE_BILL, amount, biller, reference);
            addNotification(db, USER_ID, "Bill paid", "You paid " + money(amount) + " to " + biller + ".");
            checkLow(db, accountId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // accountId null = all accounts, no types = all types. Row: {id, type, amount, date, description, counterparty, account number}
    public static ArrayList<String[]> transactions(Context context, String accountId, String... types) {
        StringBuilder sql = new StringBuilder("SELECT t.id, t.type, t.amount, t.date, t.description, t.counterparty, a.number FROM transactions t JOIN accounts a ON a.id = t.account_id WHERE a.user_id = ?");
        ArrayList<String> args = new ArrayList<>();
        args.add(String.valueOf(USER_ID));
        if (accountId != null) {
            sql.append(" AND a.id = ?");
            args.add(accountId);
        }
        if (types.length > 0) {
            sql.append(" AND t.type IN (");
            for (int i = 0; i < types.length; i++) {
                sql.append(i == 0 ? "?" : ",?");
                args.add(types[i]);
            }
            sql.append(")");
        }
        sql.append(" ORDER BY t.id DESC");
        return rows(context, sql.toString(), args.toArray(new String[0]));
    }

    // {id, title, message, date, is_read}
    public static ArrayList<String[]> notifications(Context context) {
        return rows(context, "SELECT id, title, message, date, is_read FROM notifications WHERE user_id = ? ORDER BY id DESC", String.valueOf(USER_ID));
    }

    public static void markNotificationsRead(Context context) {
        db(context).execSQL("UPDATE notifications SET is_read = 1 WHERE user_id = ?", new Object[]{USER_ID});
    }

    public static void updateProfile(Context context, String name, String email, String phone) {
        SQLiteDatabase db = db(context);
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("phone", phone);
        db.update("users", values, "id = ?", new String[]{String.valueOf(USER_ID)});
        USER_NAME = name;
        USER_EMAIL = email;
        USER_PHONE = phone;
        addNotification(db, USER_ID, "Profile updated", "Your profile details were updated.");
    }

    public static boolean passwordMatches(Context context, String password) {
        return DatabaseUtils.longForQuery(db(context), "SELECT COUNT(*) FROM users WHERE id = ? AND password = ?", new String[]{String.valueOf(USER_ID), password}) > 0;
    }

    public static void changePassword(Context context, String password) {
        SQLiteDatabase db = db(context);
        db.execSQL("UPDATE users SET password = ? WHERE id = ?", new Object[]{password, USER_ID});
        addNotification(db, USER_ID, "Password changed", "Your password was changed.");
    }

    public static void resetPassword(Context context, String email, String password) {
        SQLiteDatabase db = db(context);
        db.execSQL("UPDATE users SET password = ? WHERE email = ? COLLATE NOCASE", new Object[]{password, email});
        int userId = (int) DatabaseUtils.longForQuery(db, "SELECT id FROM users WHERE email = ? COLLATE NOCASE", new String[]{email});
        addNotification(db, userId, "Password reset", "Your password was reset.");
    }
}
