package com.example.expensly.expensly;

/**
 * Model class representing a single expense entry.
 */
public class Expense {

    private long id;
    private String title;
    private double amount;
    private String category;   // e.g. "Food", "Transport", "Entertainment"
    private String date;       // stored as ISO-8601 string: "YYYY-MM-DD"
    private String note;       // optional free-text note

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Used when creating a new expense (id not yet assigned by DB). */
    public Expense(String title, double amount, String category, String date, String note) {
        this.title    = title;
        this.amount   = amount;
        this.category = category;
        this.date     = date;
        this.note     = note;
    }

    /** Used when reading an expense back from the database. */
    public Expense(long id, String title, double amount,
                   String category, String date, String note) {
        this.id       = id;
        this.title    = title;
        this.amount   = amount;
        this.category = category;
        this.date     = date;
        this.note     = note;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public long getId()                     { return id; }
    public void setId(long id)              { this.id = id; }

    public String getTitle()                { return title; }
    public void setTitle(String title)      { this.title = title; }

    public double getAmount()               { return amount; }
    public void setAmount(double amount)    { this.amount = amount; }

    public String getCategory()             { return category; }
    public void setCategory(String cat)     { this.category = cat; }

    public String getDate()                 { return date; }
    public void setDate(String date)        { this.date = date; }

    public String getNote()                 { return note; }
    public void setNote(String  note)        { this.note = note; }

    // ── Utility ───────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Expense{id=" + id
                + ", title='" + title + '\''
                + ", amount=" + amount
                + ", category='" + category + '\''
                + ", date='" + date + '\''
                + ", note='" + note + '\''
                + '}';
    }
}
