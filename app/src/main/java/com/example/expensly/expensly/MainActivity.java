package com.example.expensly.expensly;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
 * The primary activity of the application that serves as the main dashboard for expense tracking.
 */
public class MainActivity extends AppCompatActivity {

    // ── Category options ──────────────────────────────────────────────────────
    private static final String[] CATEGORIES = {
            "Food", "Transport", "Entertainment",
            "Health", "Shopping", "Bills", "Other"
    };

    private static final String[] PERIODS = {
            "Weekly", "Monthly", "Yearly"
    };

    // ── Views ─────────────────────────────────────────────────────────────────
    private PieChartView      pieChartView;
    private RecyclerView      recyclerView;
    private ExpenseAdapter    adapter;
    private TextView          tvTotalAmount;
    private TextView          tvBudget;
    private TextView          tvSavings;
    private TextView          tvEmptyState;

    // ── Data layer ────────────────────────────────────────────────────────────
    private ExpenseRepository repository;
    private SessionManager    session;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository    = ExpenseRepository.getInstance(this);
        session       = new SessionManager(this);
        pieChartView  = findViewById(R.id.pieChartView);
        recyclerView  = findViewById(R.id.recyclerView);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvBudget      = findViewById(R.id.tvBudget);
        tvSavings     = findViewById(R.id.tvSavings);
        tvEmptyState  = findViewById(R.id.tvEmptyState);

        setupRecyclerView();
        setupFab();
        setupBudgetClick();
        refreshUI();

        if (repository.getBudget() == null) {
            showBudgetDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExpenseAdapter(
                repository.getAllExpenses(),
                /* onItemClick */ this::showEditDialog,
                /* onItemDelete */ null
        );
        recyclerView.setAdapter(adapter);

        // Swipe-to-delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder tgt) {
                return false;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                int position = vh.getAdapterPosition();
                Expense expense = adapter.getExpenseAt(position);
                repository.deleteExpense(expense.getId());
                refreshUI();
                Toast.makeText(MainActivity.this, R.string.expense_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void setupBudgetClick() {
        tvBudget.setOnClickListener(v -> showBudgetDialog());
    }

    // ── UI refresh ────────────────────────────────────────────────────────────

    private void refreshUI() {
        List<Expense> expenses = repository.getAllExpenses();

        // Update List
        adapter.updateData(expenses);

        // Empty state visibility
        tvEmptyState.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);

        // Update Total
        double total = repository.getTotalAmount();
        tvTotalAmount.setText(getString(R.string.total_label, total));

        // Update Budget & Savings
        Budget budget = repository.getBudget();
        if (budget != null) {
            tvBudget.setText(getString(R.string.budget_label, budget.getAmount(), budget.getPeriod()));
            tvSavings.setText(getString(R.string.savings_label, repository.getSavings()));
        }

        // Update Pie Chart - Now using the full list for per-expense colors and including savings
        pieChartView.setData(expenses, repository.getSavings());
        pieChartView.startAnimation();
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showAddDialog() {
        showExpenseDialog(null);
    }

    private void showEditDialog(Expense expense) {
        showExpenseDialog(expense);
    }

    private void showBudgetDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense, null);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spinner = dialogView.findViewById(R.id.spinnerCategory);

        // Hide unused fields for budget dialog
        dialogView.findViewById(R.id.etTitle).setVisibility(View.GONE);
        dialogView.findViewById(R.id.etDate).setVisibility(View.GONE);
        dialogView.findViewById(R.id.etNote).setVisibility(View.GONE);

        etAmount.setHint(R.string.budget_amount_hint);

        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, PERIODS);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(periodAdapter);

        Budget current = repository.getBudget();
        if (current != null) {
            etAmount.setText(String.valueOf(current.getAmount()));
            for (int i = 0; i < PERIODS.length; i++) {
                if (PERIODS[i].equals(current.getPeriod())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.set_budget_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String amtStr = etAmount.getText().toString().trim();
                    String period = (String) spinner.getSelectedItem();
                    if (!amtStr.isEmpty()) {
                        repository.setBudget(Double.parseDouble(amtStr), period);
                        refreshUI();
                        Toast.makeText(this, R.string.budget_saved, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showExpenseDialog(Expense existing) {
        boolean isEdit = (existing != null);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense, null);
        EditText etTitle    = dialogView.findViewById(R.id.etTitle);
        EditText etAmount   = dialogView.findViewById(R.id.etAmount);
        Spinner  spinner    = dialogView.findViewById(R.id.spinnerCategory);
        EditText etDate     = dialogView.findViewById(R.id.etDate);
        EditText etNote     = dialogView.findViewById(R.id.etNote);

        // Category Spinner
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(catAdapter);

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
            Calendar cal = Calendar.getInstance();
            etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
        }

        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (isEdit) {
                String[] parts = existing.getDate().split("-");
                if (parts.length == 3) {
                    cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                    cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                }
            }
            new DatePickerDialog(this, (dp, year, month, day) ->
                    etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isEdit ? R.string.dialog_edit_title : R.string.dialog_add_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null);

        if (isEdit) {
            builder.setNeutralButton("Delete", (dlg, which) -> {
                repository.deleteExpense(existing.getId());
                refreshUI();
                Toast.makeText(this, R.string.expense_deleted, Toast.LENGTH_SHORT).show();
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        saveBtn.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String amtStr   = etAmount.getText().toString().trim();
            String date     = etDate.getText().toString().trim();
            String note     = etNote.getText().toString().trim();
            String category = (String) spinner.getSelectedItem();

            if (performSave(isEdit, existing, title, amtStr, date, note, category)) {
                dialog.dismiss();
            }
        });
    }

    private boolean performSave(boolean isEdit, Expense existing, String title, String amtStr, String date, String note, String category) {
        if (title.isEmpty() || amtStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, R.string.validation_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Budget check
        Budget budget = repository.getBudget();
        if (budget != null) {
            double currentTotal = repository.getTotalAmount();
            double newTotal;
            if (isEdit) {
                newTotal = currentTotal - existing.getAmount() + amount;
            } else {
                newTotal = currentTotal + amount;
            }

            if (newTotal > budget.getAmount()) {
                Toast.makeText(this, R.string.budget_exceeded, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        boolean success;
        if (isEdit) {
            success = repository.updateExpense(existing.getId(), title, amount, category, date, note);
            Toast.makeText(this, success ? R.string.expense_updated : R.string.update_failed, Toast.LENGTH_SHORT).show();
        } else {
            Expense created = repository.addExpense(title, amount, category, date, note);
            success = (created != null);
            Toast.makeText(this, success ? R.string.expense_added : R.string.add_failed, Toast.LENGTH_SHORT).show();
        }

        if (success) {
            refreshUI();
        }
        return success;
    }
}
