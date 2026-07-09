package com.example.expensly.expensly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-level SQLite helper.
 * Handles database creation, upgrades, and raw CRUD operations.
 */
public class ExpenseDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "expense_tracker.db";
    private static final int    DB_VERSION = 3; // Incremented for users table

    // ── Tables & Columns ──────────────────────────────────────────────────────
    public static final String TABLE_EXPENSES  = "expenses";
    public static final String COL_ID          = "_id";
    public static final String COL_TITLE       = "title";
    public static final String COL_AMOUNT      = "amount";
    public static final String COL_CATEGORY    = "category";
    public static final String COL_DATE        = "date";
    public static final String COL_NOTE        = "note";

    public static final String TABLE_BUDGET    = "budget";
    public static final String COL_BUDGET_AMT  = "amount";
    public static final String COL_BUDGET_PER  = "period";

    public static final String TABLE_USERS     = "users";
    public static final String COL_USER_EMAIL  = "email";
    public static final String COL_USER_FNAME  = "fname";
    public static final String COL_USER_LNAME  = "lname";
    public static final String COL_USER_PASS   = "password";

    private static ExpenseDbHelper instance;

    public static synchronized ExpenseDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ExpenseDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " ("
                + COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE    + " TEXT NOT NULL, "
                + COL_AMOUNT   + " REAL NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_DATE     + " TEXT NOT NULL, "
                + COL_NOTE     + " TEXT"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_BUDGET + " ("
                + COL_BUDGET_AMT + " REAL NOT NULL, "
                + COL_BUDGET_PER + " TEXT NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_EMAIL + " TEXT PRIMARY KEY, "
                + COL_USER_FNAME + " TEXT NOT NULL, "
                + COL_USER_LNAME + " TEXT NOT NULL, "
                + COL_USER_PASS  + " TEXT NOT NULL"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE " + TABLE_BUDGET + " ("
                    + COL_BUDGET_AMT + " REAL NOT NULL, "
                    + COL_BUDGET_PER + " TEXT NOT NULL"
                    + ");");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                    + COL_USER_EMAIL + " TEXT PRIMARY KEY, "
                    + COL_USER_FNAME + " TEXT NOT NULL, "
                    + COL_USER_LNAME + " TEXT NOT NULL, "
                    + COL_USER_PASS  + " TEXT NOT NULL"
                    + ");");
        }
    }

    // ── EXPENSES ──────────────────────────────────────────────────────────────

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(COL_TITLE,    expense.getTitle());
        cv.put(COL_AMOUNT,   expense.getAmount());
        cv.put(COL_CATEGORY, expense.getCategory());
        cv.put(COL_DATE,     expense.getDate());
        cv.put(COL_NOTE,     expense.getNote());
        return db.insert(TABLE_EXPENSES, null, cv);
    }

    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_EXPENSES, null, null, null, null, null, COL_DATE + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToExpense(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public Expense getExpenseById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_EXPENSES, null, COL_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursorToExpense(cursor);
        }
        return null;
    }

    public List<Expense> getExpensesByCategory(String category) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_EXPENSES, null, COL_CATEGORY + " = ?", new String[]{category}, null, null, COL_DATE + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToExpense(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public Map<String, Double> getCategoryTotals() {
        Map<String, Double> totals = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES + " GROUP BY " + COL_CATEGORY, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    totals.put(cursor.getString(0), cursor.getDouble(1));
                } while (cursor.moveToNext());
            }
        }
        return totals;
    }

    public double getTotalAmount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getDouble(0);
        }
        return 0;
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = new ContentValues();
        cv.put(COL_TITLE,    expense.getTitle());
        cv.put(COL_AMOUNT,   expense.getAmount());
        cv.put(COL_CATEGORY, expense.getCategory());
        cv.put(COL_DATE,     expense.getDate());
        cv.put(COL_NOTE,     expense.getNote());
        return db.update(TABLE_EXPENSES, cv, COL_ID + " = ?", new String[]{String.valueOf(expense.getId())});
    }

    public int deleteExpense(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EXPENSES, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteAllExpenses() {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EXPENSES, null, null);
    }

    // ── BUDGET ────────────────────────────────────────────────────────────────

    public long setBudget(double amount, String period) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_BUDGET, null, null);
        ContentValues cv = new ContentValues();
        cv.put(COL_BUDGET_AMT, amount);
        cv.put(COL_BUDGET_PER, period);
        return db.insert(TABLE_BUDGET, null, cv);
    }

    public Cursor getBudget() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_BUDGET, null, null, null, null, null, null);
    }

    // ── USERS ─────────────────────────────────────────────────────────────────

    public long insertUser(String fname, String lname, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_FNAME, fname);
        cv.put(COL_USER_LNAME, lname);
        cv.put(COL_USER_EMAIL, email.toLowerCase());
        cv.put(COL_USER_PASS, password);
        return db.insert(TABLE_USERS, null, cv);
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_USERS, null, COL_USER_EMAIL + " = ?", new String[]{email.toLowerCase()}, null, null, null);
    }

    public int updateUserNames(String email, String fname, String lname) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_FNAME, fname);
        cv.put(COL_USER_LNAME, lname);
        return db.update(TABLE_USERS, cv, COL_USER_EMAIL + " = ?", new String[]{email.toLowerCase()});
    }

    public int updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_PASS, newPassword);
        return db.update(TABLE_USERS, cv, COL_USER_EMAIL + " = ?", new String[]{email.toLowerCase()});
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Expense cursorToExpense(Cursor cursor) {
        return new Expense(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE))
        );
    }
}
