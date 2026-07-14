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
 * Lokale SQLite-Datenbankhilfe.
 * Verwaltet das Erstellen der Tabellen für Ausgaben und Budgets.
 */
public class ExpenseDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "expense_tracker.db";
    private static final int    DB_VERSION = 3;

    // ── Tabellen & Spalten ──────────────────────────────────────────────────────
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
        // Erstellt die Tabelle für Ausgaben
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " ("
                + COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE    + " TEXT NOT NULL, "
                + COL_AMOUNT   + " REAL NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_DATE     + " TEXT NOT NULL, "
                + COL_NOTE     + " TEXT"
                + ");");

        // Erstellt die Tabelle für das Budget
        db.execSQL("CREATE TABLE " + TABLE_BUDGET + " ("
                + COL_BUDGET_AMT + " REAL NOT NULL, "
                + COL_BUDGET_PER + " TEXT NOT NULL"
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
        // Bereinigung alter Tabellen falls nötig
    }

    // ── METHODEN FÜR AUSGABEN ──────────────────────────────────────────────────

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

    public double getTotalAmount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_EXPENSES, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getDouble(0);
        }
        return 0;
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
