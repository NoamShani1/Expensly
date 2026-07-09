package com.example.expensly.expensly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensly.R;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the expense list.
 *
 * Each row item inflates res/layout/item_expense.xml which must contain:
 *   TextView : id="tvExpenseTitle"
 *   TextView : id="tvExpenseCategory"
 *   TextView : id="tvExpenseDate"
 *   TextView : id="tvExpenseAmount"
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    // ── Callbacks ─────────────────────────────────────────────────────────────

    public interface OnItemClickListener  { void onItemClick(Expense expense); }
    public interface OnItemDeleteListener { void onItemDelete(Expense expense); }

    // ── Fields ────────────────────────────────────────────────────────────────

    private List<Expense>         expenses;
    private final OnItemClickListener  clickListener;
    private final OnItemDeleteListener deleteListener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ExpenseAdapter(List<Expense> expenses,
                          OnItemClickListener clickListener,
                          OnItemDeleteListener deleteListener) {
        this.expenses       = expenses;
        this.clickListener  = clickListener;
        this.deleteListener = deleteListener;
    }

    // ── RecyclerView.Adapter overrides ────────────────────────────────────────

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(expense);
        });
    }

    @Override
    public int getItemCount() {
        return expenses != null ? expenses.size() : 0;
    }

    // ── Public helpers ────────────────────────────────────────────────────────

    /** Replaces the dataset and refreshes the list. */
    public void updateData(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    /** Returns the expense at the given adapter position (used for swipe-to-delete). */
    public Expense getExpenseAt(int position) {
        return expenses.get(position);
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvDate;
        private final TextView tvAmount;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvExpenseTitle);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvDate     = itemView.findViewById(R.id.tvExpenseDate);
            tvAmount   = itemView.findViewById(R.id.tvExpenseAmount);
        }

        void bind(Expense expense) {
            tvTitle.setText(expense.getTitle());
            tvCategory.setText(expense.getCategory());
            tvDate.setText(expense.getDate());
            tvAmount.setText(String.format(Locale.getDefault(),
                    "€%.2f", expense.getAmount()));
        }
    }
}
