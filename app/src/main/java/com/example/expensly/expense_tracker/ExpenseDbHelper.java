package com.example.expensly.expense_tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Low-level SQLite helper.
 * Handles database creation, upgrades, and raw CRUD operations.
 *
 * Use {@link ExpenseRepository} for all app-level access — it wraps this class.
 */
public class ExpenseDbHelper extends SQLiteOpenHelper {

    // ── Database metadata ─────────────────────────────────────────────────────
    private static final String DB_NAME    = "expense_tracker.db";
    private static final int    DB_VERSION = 1;

    // ── Table & column names ──────────────────────────────────────────────────
    public static final String TABLE_EXPENSES  = "expenses";
    public static final String COL_ID          = "_id";
    public static final String COL_TITLE       = "title";
    public static final String COL_AMOUNT      = "amount";
    public static final String COL_CATEGORY    = "category";
    public static final String COL_DATE        = "date";
    public static final String COL_NOTE        = "note";

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
        String createTable =
                "CREATE TABLE " + TABLE_EXPENSES + " ("
                + COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE    + " TEXT NOT NULL, "
                + COL_AMOUNT   + " REAL NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_DATE     + " TEXT NOT NULL, "
                + COL_NOTE     + " TEXT"
                + ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple migration: drop & recreate (extend with ALTER TABLE for production)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Inserts a new expense row.
     *
     * @return the row id of the newly inserted expense, or -1 on failure.
     */
    public long insertExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = toContentValues(expense);
        long newId = db.insert(TABLE_EXPENSES, null, cv);
        db.close();
        return newId;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Returns all expenses ordered by date descending.
     */
    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                null, null, null, null,
                COL_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToExpense(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    /**
     * Returns a single expense by its id, or null if not found.
     */
    public Expense getExpenseById(long id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Expense expense = null;
        if (cursor != null && cursor.moveToFirst()) {
            expense = cursorToExpense(cursor);
            cursor.close();
        }
        db.close();
        return expense;
    }

    /**
     * Returns all expenses that belong to a given category.
     */
    public List<Expense> getExpensesByCategory(String category) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                COL_CATEGORY + " = ?",
                new String[]{category},
                null, null,
                COL_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToExpense(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    /**
     * Returns the total sum of all expense amounts.
     */
    public double getTotalAmount() {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES, null);

        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        db.close();
        return total;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing expense row.
     *
     * @return number of rows affected (should be 1 on success, 0 if not found).
     */
    public int updateExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv  = toContentValues(expense);

        int rows = db.update(
                TABLE_EXPENSES,
                cv,
                COL_ID + " = ?",
                new String[]{String.valueOf(expense.getId())}
        );
        db.close();
        return rows;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes an expense by id.
     *
     * @return number of rows deleted (should be 1 on success).
     */
    public int deleteExpense(long id) {
        SQLiteDatabase db = getWritableDatabase();

        int rows = db.delete(
                TABLE_EXPENSES,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        return rows;
    }

    /**
     * Deletes ALL expense records. Use with caution.
     *
     * @return number of rows deleted.
     */
    public int deleteAllExpenses() {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_EXPENSES, null, null);
        db.close();
        return rows;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Maps an Expense object to a ContentValues map (excludes id). */
    private ContentValues toContentValues(Expense e) {
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE,    e.getTitle());
        cv.put(COL_AMOUNT,   e.getAmount());
        cv.put(COL_CATEGORY, e.getCategory());
        cv.put(COL_DATE,     e.getDate());
        cv.put(COL_NOTE,     e.getNote() != null ? e.getNote() : "");
        return cv;
    }

    /** Maps a Cursor row to an Expense object. */
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
