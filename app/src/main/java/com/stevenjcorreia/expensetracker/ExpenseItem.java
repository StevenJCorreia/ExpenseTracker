package com.stevenjcorreia.expensetracker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    static void exportExpenses(ArrayList<ExpenseItem> expenseList, Uri directoryPath, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.getContentResolver().openOutputStream(directoryPath));

            for (ExpenseItem expense: expenseList) {
                outputStreamWriter.append(expense.toStringCSV());
            }

            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, String.format("exportExpenses: Could not write import expenses to \"%s\" directory.", directoryPath), e);
        }
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

    static ArrayList<ExpenseItem> importExpenses(Uri filePath, Context context) {
        ArrayList<ExpenseItem> output = new ArrayList<>();

        try (InputStream inputStream = context.getContentResolver().openInputStream(filePath)) {
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        output.add(parse(line));
                    }
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, String.format("importExpenses: Could not open file \"%s\", returning empty array list.", filePath), e);
        }

        return output;
    }

    private static ExpenseItem parse(String line) {
        // "3,Gas,Feb 8 2020,12.58"
        int ID = Integer.parseInt(line.split("[,]")[0]);

        String category = line.split("[,]")[1];

        String date = line.split("[,]")[2];
        date = date.substring(0, date.lastIndexOf(" ")) + "," + date.substring(date.lastIndexOf(" "));

        double price = Double.parseDouble(line.split("[,]")[3]);

        return new ExpenseItem(price, category, date);
    }

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

    private String toStringCSV() {
        return String.format(Locale.US,
                "%d,%s,%s,%s\n",
                this.ID,
                this.category,
                this.date.replace(",", ""),
                new DecimalFormat("0.00").format(this.price));
    }
}
