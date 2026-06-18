package com.example.expensly.expense_tracker;

/**
 * Simple model for a budget limit.
 */
public class Budget {
    private double amount;
    private String period; // e.g., "Monthly", "Weekly"

    public Budget(double amount, String period) {
        this.amount = amount;
        this.period = period;
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
