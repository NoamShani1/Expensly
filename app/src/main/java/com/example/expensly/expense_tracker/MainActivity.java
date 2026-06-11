package com.example.expensly.expense_tracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensly.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Main screen showing:
 *  - An animated pie chart summarising spending by category
 *  - A RecyclerView listing all expenses
 *  - FAB to add a new expense
 *  - Swipe-to-delete on list items
 *  - Tap an item to edit it
 *
 * Layout file expected: res/layout/activity_main.xml
 * Required views:
 *   - PieChartView  : id="pieChartView"
 *   - RecyclerView  : id="recyclerView"
 *   - FAB           : id="fabAddExpense"
 *   - TextView      : id="tvTotalAmount"  (shows grand total)
 *   - TextView      : id="tvEmptyState"   (shown when list is empty)
 */
public class MainActivity extends AppCompatActivity {

    // ── Category options (extend as needed) ───────────────────────────────────
    private static final String[] CATEGORIES = {
            "Food", "Transport", "Entertainment",
            "Health", "Shopping", "Bills", "Other"
    };

    // ── Views ─────────────────────────────────────────────────────────────────
    private PieChartView      pieChartView;
    private RecyclerView      recyclerView;
    private ExpenseAdapter    adapter;
    private android.widget.TextView tvTotalAmount;
    private android.widget.TextView tvEmptyState;

    // ── Data layer ────────────────────────────────────────────────────────────
    private ExpenseRepository repository;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository    = ExpenseRepository.getInstance(this);
        pieChartView  = findViewById(R.id.pieChartView);
        recyclerView  = findViewById(R.id.recyclerView);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvEmptyState  = findViewById(R.id.tvEmptyState);

        setupRecyclerView();
        setupFab();
        refreshUI();
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExpenseAdapter(
                repository.getAllExpenses(),
                /* onItemClick  */ this::showEditDialog,
                /* onItemDelete */ expense -> {
                    repository.deleteExpense(expense.getId());
                    refreshUI();
                    Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                }
        );
        recyclerView.setAdapter(adapter);

        // Swipe-to-delete via ItemTouchHelper
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(RecyclerView rv,
                    RecyclerView.ViewHolder vh, RecyclerView.ViewHolder tgt) {
                return false;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                Expense expense = adapter.getExpenseAt(vh.getAdapterPosition());
                repository.deleteExpense(expense.getId());
                refreshUI();
                Toast.makeText(MainActivity.this,
                        "Expense deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> showAddDialog());
    }

    // ── UI refresh ────────────────────────────────────────────────────────────

    /**
     * Reloads data from the repository and refreshes every UI element.
     * Call this after any add / edit / delete operation.
     */
    private void refreshUI() {
        List<Expense> expenses = repository.getAllExpenses();

        // List
        adapter.updateData(expenses);

        // Empty state
        tvEmptyState.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);

        // Total
        double total = repository.getTotalAmount();
        tvTotalAmount.setText(String.format(Locale.getDefault(), "Total: €%.2f", total));

        // Pie chart — always animate on refresh so the user sees the update
        pieChartView.setData(repository.getCategoryTotals());
        pieChartView.startAnimation();
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    /** Shows a dialog to add a brand-new expense. */
    private void showAddDialog() {
        showExpenseDialog(null);
    }

    /** Shows a dialog pre-populated with the given expense's data for editing. */
    private void showEditDialog(Expense expense) {
        showExpenseDialog(expense);
    }

    /**
     * Shared dialog for Add (expense == null) and Edit (expense != null).
     *
     * Inflates dialog_expense.xml which must contain:
     *   EditText : id="etTitle"
     *   EditText : id="etAmount"
     *   Spinner  : id="spinnerCategory"
     *   EditText : id="etDate"   (tap to open DatePicker)
     *   EditText : id="etNote"
     */
    private void showExpenseDialog(Expense existing) {
        boolean isEdit = (existing != null);

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_expense, null);

        EditText etTitle    = dialogView.findViewById(R.id.etTitle);
        EditText etAmount   = dialogView.findViewById(R.id.etAmount);
        Spinner  spinner    = dialogView.findViewById(R.id.spinnerCategory);
        EditText etDate     = dialogView.findViewById(R.id.etDate);
        EditText etNote     = dialogView.findViewById(R.id.etNote);

        // Populate category spinner
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(catAdapter);

        // Pre-fill when editing
        if (isEdit) {
            etTitle.setText(existing.getTitle());
            etAmount.setText(String.valueOf(existing.getAmount()));
            etDate.setText(existing.getDate());
            etNote.setText(existing.getNote());
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(existing.getCategory())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        } else {
            // Default date to today
            Calendar cal = Calendar.getInstance();
            etDate.setText(String.format(Locale.getDefault(),
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)));
        }

        // Tap on date field opens a DatePicker
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (dp, year, month, day) ->
                            etDate.setText(String.format(Locale.getDefault(),
                                    "%04d-%02d-%02d", year, month + 1, day)),
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Expense" : "Add Expense")
                .setView(dialogView)
                .setPositiveButton("Save", (dlg, which) -> {
                    String title    = etTitle.getText().toString().trim();
                    String amtStr   = etAmount.getText().toString().trim();
                    String date     = etDate.getText().toString().trim();
                    String note     = etNote.getText().toString().trim();
                    String category = (String) spinner.getSelectedItem();

                    if (title.isEmpty() || amtStr.isEmpty() || date.isEmpty()) {
                        Toast.makeText(this,
                                "Title, amount and date are required.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amtStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this,
                                "Invalid amount.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        boolean ok = repository.updateExpense(
                                existing.getId(), title, amount, category, date, note);
                        Toast.makeText(this,
                                ok ? "Expense updated" : "Update failed",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Expense created = repository.addExpense(
                                title, amount, category, date, note);
                        Toast.makeText(this,
                                created != null ? "Expense added" : "Add failed",
                                Toast.LENGTH_SHORT).show();
                    }
                    refreshUI();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
