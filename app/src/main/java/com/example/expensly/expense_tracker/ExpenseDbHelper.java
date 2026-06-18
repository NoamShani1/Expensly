package com.example.expensly.expense_tracker;

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
 *
 * Use {@link ExpenseRepository} for all app-level access — it wraps this class.
 */
public class ExpenseDbHelper extends SQLiteOpenHelper {

    // ── Database metadata ─────────────────────────────────────────────────────
    private static final String DB_NAME    = "expense_tracker.db";
    private static final int    DB_VERSION = 2; // Incremented for budget table

    // ── Table & column names ──────────────────────────────────────────────────
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

    // ── Singleton ─────────────────────────────────────────────────────────────
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExpensesTable =
                "CREATE TABLE " + TABLE_EXPENSES + " ("
                + COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE    + " TEXT NOT NULL, "
                + COL_AMOUNT   + " REAL NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_DATE     + " TEXT NOT NULL, "
                + COL_NOTE     + " TEXT"
                + ");";
        db.execSQL(createExpensesTable);

        String createBudgetTable =
                "CREATE TABLE " + TABLE_BUDGET + " ("
                + COL_BUDGET_AMT + " REAL NOT NULL, "
                + COL_BUDGET_PER + " TEXT NOT NULL"
                + ");";
        db.execSQL(createBudgetTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createBudgetTable =
                    "CREATE TABLE " + TABLE_BUDGET + " ("
                    + COL_BUDGET_AMT + " REAL NOT NULL, "
                    + COL_BUDGET_PER + " TEXT NOT NULL"
                    + ");";
            db.execSQL(createBudgetTable);
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = toContentValues(expense);
        return db.insert(TABLE_EXPENSES, null, cv);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                null, null, null, null,
                COL_DATE + " DESC"
        )) {
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
        try (Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToExpense(cursor);
            }
        }
        return null;
    }

    public List<Expense> getExpensesByCategory(String category) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                COL_CATEGORY + " = ?",
                new String[]{category},
                null, null,
                COL_DATE + " DESC"
        )) {
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
        String query = "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") " +
                "FROM " + TABLE_EXPENSES + " " +
                "GROUP BY " + COL_CATEGORY;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    double total    = cursor.getDouble(1);
                    totals.put(category, total);
                } while (cursor.moveToNext());
            }
        }
        return totals;
    }

    public double getTotalAmount() {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        try (Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        }
        return total;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = toContentValues(expense);
        return db.update(
                TABLE_EXPENSES,
                cv,
                COL_ID + " = ?",
                new String[]{String.valueOf(expense.getId())}
        );
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public int deleteExpense(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                TABLE_EXPENSES,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
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

    // ── Private helpers ───────────────────────────────────────────────────────

    private ContentValues toContentValues(Expense e) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE,    e.getTitle());
        cv.put(COL_AMOUNT,   e.getAmount());
        cv.put(COL_CATEGORY, e.getCategory());
        cv.put(COL_DATE,     e.getDate());
        cv.put(COL_NOTE,     e.getNote() != null ? e.getNote() : "");
        return cv;
    }

    private Expense cursorToExpense(Cursor cursor) {
        long   id       = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        String title    = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        double amount   = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
        String date     = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        String note     = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE));
        return new Expense(id, title, amount, category, date, note);
    }
}
