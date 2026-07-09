package com.example.expensly.expensly;

public class Budget {
    private double amount;
    private String period;

    public Budget(double amount, String period) {
        this.amount = amount;
        this.period = period;
    }

    public double getAmount() { return amount; }
    public String getPeriod() { return period; }
}
