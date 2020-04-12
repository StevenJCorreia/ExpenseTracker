package com.stevenjcorreia.expensetracker;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;

@Entity
public class ExpenseItem implements Serializable {
    private static final String TAG = ExpenseItem.class.getName();

    @PrimaryKey(autoGenerate = true)
    private int ID;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "price")
    private double price;

    ExpenseItem(double price, String category, String date) {
        this.category = category;
        this.date = date;
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    int getID() {
        return ID;
    }

    String getDate() {
        return date;
    }

    double getPrice() {
        return price;
    }

    void setID(int ID) {
        this.ID = ID;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    void setDate(String date) {
        this.date = date;
    }

    void setPrice(double price) {
        this.price = price;
    }

    static final Comparator<ExpenseItem> CATEGORY_ASCENDING = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem o1, ExpenseItem o2) {
            return o1.getCategory().compareTo(o2.getCategory());
        }
    };

    static final Comparator<ExpenseItem> CATEGORY_DESCENDING = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem o1, ExpenseItem o2) {
            return o2.getCategory().compareTo(o1.getCategory());
        }
    };

    static final Comparator<ExpenseItem> DATE_ASCENDING = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem o1, ExpenseItem o2) {
            try {
                return new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(o1.getDate()).compareTo(new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(o2.getDate()));
            } catch (ParseException e) {
                Log.e(TAG, "DATE_ASCENDING: Could not sort dates.", e);
                return 0;
            }
        }
    };

    static final Comparator<ExpenseItem> DATE_DESCENDING = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem o1, ExpenseItem o2) {
            try {
                return new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(o2.getDate()).compareTo(new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(o1.getDate()));
            } catch (ParseException e) {
                Log.e(TAG, "DATE_DESCENDING: Could not sort dates.", e);
                return 0;
            }
        }
    };

    static final Comparator<ExpenseItem> PRICE_ASCENDING = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem o1, ExpenseItem o2) {
            return Double.compare(o1.getPrice(), o2.getPrice());
        }
    };

    static final Comparator<ExpenseItem> PRICE_DESCENDING = new Comparator<ExpenseItem>() {
        @Override
        public int compare(ExpenseItem o1, ExpenseItem o2) {
            return Double.compare(o2.getPrice(), o1.getPrice());
        }
    };
}
