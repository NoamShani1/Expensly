package com.example.expensly.expensly;

import android.content.Context;
import android.database.Cursor;

import java.util.List;
import java.util.Map;

/**
 * Repository — the single source of truth for expense data.
 *
 * All Activities / Fragments should talk to this class, never to
 * {@link ExpenseDbHelper} directly. This keeps the UI code clean and
 * makes the data layer easy to swap out (e.g. Room, remote API) later.
 */
public class ExpenseRepository {

    private final ExpenseDbHelper dbHelper;

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static ExpenseRepository instance;

    public static synchronized ExpenseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseRepository(context.getApplicationContext());
        }
        return instance;
    }

    private ExpenseRepository(Context context) {
        dbHelper = ExpenseDbHelper.getInstance(context);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Adds a new expense and returns it with its database-assigned id populated.
     * Returns null if the insert failed.
     */
    public Expense addExpense(String title, double amount,
                              String category, String date, String note) {
        Expense expense = new Expense(title, amount, category, date, note);
        long newId = dbHelper.insertExpense(expense);
        if (newId == -1) return null;
        expense.setId(newId);
        return expense;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Returns every expense, sorted by date descending. */
    public List<Expense> getAllExpenses() {
        return dbHelper.getAllExpenses();
    }

    /** Returns a single expense by id, or null if not found. */
    public Expense getExpenseById(long id) {
        return dbHelper.getExpenseById(id);
    }

    /** Returns all expenses for a specific category. */
    public List<Expense> getExpensesByCategory(String category) {
        return dbHelper.getExpensesByCategory(category);
    }

    /**
     * Returns a map of { category -> total amount } for use by the pie chart.
     * Fetched directly via a SQL aggregate query for efficiency.
     */
    public Map<String, Double> getCategoryTotals() {
        return dbHelper.getCategoryTotals();
    }

    /** Returns the grand total of all expenses. */
    public double getTotalAmount() {
        return dbHelper.getTotalAmount();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing expense.
     *
     * @param id       id of the expense to update
     * @param title    new title
     * @param amount   new amount
     * @param category new category
     * @param date     new date (YYYY-MM-DD)
     * @param note     new note (may be null)
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateExpense(long id, String title, double amount,
                                  String category, String date, String note) {
        Expense expense = new Expense(id, title, amount, category, date, note);
        return dbHelper.updateExpense(expense) > 0;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes the expense with the given id.
     *
     * @return true if a row was deleted, false if no matching row was found.
     */
    public boolean deleteExpense(long id) {
        return dbHelper.deleteExpense(id) > 0;
    }

    /** Wipes all expenses. Returns the number of rows removed. */
    public int deleteAllExpenses() {
        return dbHelper.deleteAllExpenses();
    }

    // ── BUDGET ────────────────────────────────────────────────────────────────

    /** Sets or updates the current budget. */
    public boolean setBudget(double amount, String period) {
        return dbHelper.setBudget(amount, period) != -1;
    }

    /** Returns the current budget, or null if none set. */
    public Budget getBudget() {
        try (Cursor cursor = dbHelper.getBudget()) {
            if (cursor != null && cursor.moveToFirst()) {
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseDbHelper.COL_BUDGET_AMT));
                String period = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseDbHelper.COL_BUDGET_PER));
                return new Budget(amount, period);
            }
        }
        return null;
    }

    /** Returns the remaining budget (Budget - Total Expenses). */
    public double getSavings() {
        Budget b = getBudget();
        if (b == null) return 0;
        return b.getAmount() - getTotalAmount();
    }
}
