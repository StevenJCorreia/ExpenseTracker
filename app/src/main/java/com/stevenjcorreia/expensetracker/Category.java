package com.stevenjcorreia.expensetracker;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;

// TODO - Grab categories from imported or accidentally deleted categories?
class Category {
    private static final String TAG = Category.class.getName();

    private static final String CATEGORY_FILE = "categories.txt";

    static void addCategoryToFile(String category, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(CATEGORY_FILE, Context.MODE_APPEND | Context.MODE_PRIVATE));
            outputStreamWriter.append(category);
            outputStreamWriter.append("\n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, String.format("addCategoryToFile: Could not write \"%s\" to file.", category), e);
        }
    }

    static final Comparator<String> CATEGORY_ASCENDING = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    static final Comparator<String> CATEGORY_DESCENDING = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    };

    static ArrayList<String> getCategoryList(Context context) {
        ArrayList<String> output = new ArrayList<>();

        try {
            InputStream inputStream = context.openFileInput(CATEGORY_FILE);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        output.add(line);
                    }
                }

                inputStream.close();
            }
        }
        catch (IOException e) {
            Log.e(TAG, "getCategoryList: No category file.", e);
        }

        return output;
    }

    static void removeCategoriesFromFile(Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(CATEGORY_FILE, Context.MODE_PRIVATE));
            outputStreamWriter.write("");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "removeCategoriesFromFile: Could not remove categories from file.", e);
        }
    }

    static void removeCategoryFromFile(String target, Context context) {
        ArrayList<String> output = getCategoryList(context);

        removeCategoriesFromFile(context);

        output.remove(target);

        for (String category: output) {
            addCategoryToFile(category, context);
        }
    }
}
