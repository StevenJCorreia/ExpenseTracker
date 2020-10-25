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

// TODO - Add description to Espense items
// TODO - Add more robust string parser that can take on many formats (ask user for string format before parse?)
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

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    private byte[] image;

    ExpenseItem(double price, String category, String date, byte[] image) {
        this.price = price;
        this.category = category;
        this.date = date;
        this.image = image;
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

    String getDate() {
        return date;
    }

    int getID() {
        return ID;
    }

    public byte[] getImage() {
        return image;
    }

    double getPrice() {
        return price;
    }

    static Comparator<ExpenseItem> getSortType(int sortTypeID) {
        switch (sortTypeID) {
            case 0:
                return CATEGORY_ASCENDING;
            case 1:
                return CATEGORY_DESCENDING;
            case 2:
                return DATE_ASCENDING;
            case 3:
                return DATE_DESCENDING;
            case 4:
                return PRICE_ASCENDING;
            case 5:
                return PRICE_DESCENDING;
        }

        return null;
    }

    static int getSortTypeID(Comparator<ExpenseItem> sortType) {
        if (sortType == CATEGORY_ASCENDING) {
            return 0;
        } else if (sortType == CATEGORY_DESCENDING) {
            return 1;
        } else if (sortType == DATE_ASCENDING) {
            return 2;
        } else if (sortType == DATE_DESCENDING) {
            return 3;
        } else if (sortType == PRICE_ASCENDING) {
            return 4;
        } else if (sortType == PRICE_DESCENDING) {
            return 5;
        }

        return -1;
    }

    static ArrayList<ExpenseItem> importExpenses(Uri filePath, Context context) {
        ArrayList<ExpenseItem> output = new ArrayList<>();

        try (InputStream inputStream = context.getContentResolver().openInputStream(filePath)) {
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

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
        String category = line.split("[,]")[1];

        String date = line.split("[,]")[2];
        date = date.substring(0, date.lastIndexOf(" ")) + "," + date.substring(date.lastIndexOf(" "));

        double price = Double.parseDouble(line.split("[,]")[3]);

        return new ExpenseItem(price, category, date, null);
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

    public void setCategory(String category) {
        this.category = category;
    }

    void setDate(String date) {
        this.date = date;
    }

    void setID(int ID) {
        this.ID = ID;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    void setPrice(double price) {
        this.price = price;
    }

    private String toStringCSV() {
        return String.format(Locale.US,
                "%s,%s,%s\n",
                this.category,
                this.date.replace(",", ""),
                new DecimalFormat("0.00").format(this.price));
    }
}
