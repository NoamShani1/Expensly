package com.example.expensly.expensly;

import android.content.Context;
import android.database.Cursor;

import java.util.List;
import java.util.Map;

public class ExpenseRepository {

    private final ExpenseDbHelper dbHelper;
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

    // ── EXPENSES ──────────────────────────────────────────────────────────────

    public Expense addExpense(String title, double amount, String category, String date, String note) {
        Expense expense = new Expense(title, amount, category, date, note);
        long newId = dbHelper.insertExpense(expense);
        if (newId == -1) return null;
        expense.setId(newId);
        return expense;
    }

    public List<Expense> getAllExpenses() {
        return dbHelper.getAllExpenses();
    }

    public Expense getExpenseById(long id) {
        return dbHelper.getExpenseById(id);
    }

    public List<Expense> getExpensesByCategory(String category) {
        return dbHelper.getExpensesByCategory(category);
    }

    public Map<String, Double> getCategoryTotals() {
        return dbHelper.getCategoryTotals();
    }

    public double getTotalAmount() {
        return dbHelper.getTotalAmount();
    }

    public boolean updateExpense(long id, String title, double amount, String category, String date, String note) {
        Expense expense = new Expense(id, title, amount, category, date, note);
        return dbHelper.updateExpense(expense) > 0;
    }

    public boolean deleteExpense(long id) {
        return dbHelper.deleteExpense(id) > 0;
    }

    public int deleteAllExpenses() {
        return dbHelper.deleteAllExpenses();
    }

    // ── BUDGET ────────────────────────────────────────────────────────────────

    public boolean setBudget(double amount, String period) {
        return dbHelper.setBudget(amount, period) != -1;
    }

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

    public double getSavings() {
        Budget b = getBudget();
        if (b == null) return 0;
        return b.getAmount() - getTotalAmount();
    }
}
