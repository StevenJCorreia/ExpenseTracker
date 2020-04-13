package com.stevenjcorreia.expensetracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = AddActivity.class.getName();

    private static ArrayList<String> categoryList = new ArrayList<>();
    private ExpenseItemDatabase expenseItemDatabase = null;
    private Context context = this;
    private boolean hasIntentExtras = false;

    private Spinner categories;
    private Button selectDate, submitExpense;
    private TextView amountValue;
    private ImageView addCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        addCategory = findViewById(R.id.addCategory);
        amountValue = findViewById(R.id.amountValue);
        categories = findViewById(R.id.categories);
        selectDate = findViewById(R.id.selectDate);
        submitExpense = findViewById(R.id.submitExpense);

        final ExpenseItem item = (ExpenseItem) getIntent().getSerializableExtra("item");

        expenseItemDatabase = Room.databaseBuilder(getApplicationContext(), ExpenseItemDatabase.class, "Expense").allowMainThreadQueries().build();

        refreshCategoryList();

        final ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, categoryList);
        categoriesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        categories.setAdapter(categoriesAdapter);

        if (item != null) {
            setTitle(R.string.edit_activity_title);
            autoFillData(item);
        } else {
            selectDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date()));
        }

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_add, null);

                alertBuilder.setView(view);
                final AlertDialog dialog = alertBuilder.create();
                dialog.show();

                final TextView categoryValue = dialog.findViewById(R.id.categoryValue);
                final TextView errorMessage = dialog.findViewById(R.id.errorMessage);
                Button submitCategory = dialog.findViewById(R.id.submitCategory);

                assert categoryValue != null;
                categoryValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (categoryValue.getText().toString().length() == 0) {
                            errorMessage.setText("Category item cannot be blank.");
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setVisibility(View.VISIBLE);
                        } else if (categoryList.indexOf(categoryValue.getText().toString()) != -1) {
                            errorMessage.setText("Category item \"" + categoryValue.getText().toString() + "\" already exists.");
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setVisibility(View.VISIBLE);
                        } else {
                            categoryValue.setTextColor(Color.BLACK);
                            errorMessage.setVisibility(View.GONE);
                        }
                    }
                });

                assert submitCategory != null;
                submitCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    if (categoryValue.getText().toString().length() == 0 || categoryList.indexOf(categoryValue.getText().toString()) != -1) {
                        return;
                    }

                    categoryList.add(categoryValue.getText().toString());
                    Category.addCategoryToFile(categoryValue.getText().toString(), context);
                    categoriesAdapter.notifyDataSetChanged();
                    categories.setSelection(categoryList.size() - 1);

                    dialog.dismiss();
                    }
                });
            }
        });

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        submitExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (!formValid()) {
                return;
            }

            if (hasIntentExtras) {
                item.setPrice(Double.parseDouble(amountValue.getText().toString()));
                item.setCategory(categories.getSelectedItem().toString());
                item.setDate(selectDate.getText().toString());

                expenseItemDatabase.expenseItemDao().updateExpenseItem(item);
            } else {
                ExpenseItem item = new ExpenseItem(Double.parseDouble(amountValue.getText().toString()), categories.getSelectedItem().toString(), selectDate.getText().toString());
                expenseItemDatabase.expenseItemDao().insertExpenseItem(item);
            }

            startActivity(new Intent(context, MainActivity.class));
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        selectDate.setText(DateFormat.getDateInstance().format(c.getTime()));
    }

    private void autoFillData(ExpenseItem item) {
        hasIntentExtras = true;

        amountValue.setText(String.valueOf(item.getPrice()));

        if (categoryList.indexOf(item.getCategory()) == -1) {
            categoryList.add(item.getCategory());
            Category.addCategoryToFile(item.getCategory(), context);
            categories.setSelection(Category.getCategoryList(context).size() - 1);
        } else {
            categories.setSelection(categoryList.indexOf(item.getCategory()));
        }

        selectDate.setText(item.getDate());
    }

    private boolean formValid() {
        if (categories.getSelectedItem() == null) {
            Log.d(TAG, "formValid: Category not selected.");
            return false;
        }

        if (amountValue.getText().toString().length() == 0) {
            Log.d(TAG, "formValid: Amount not defined.");
            return false;
        }

        if (!isDouble(amountValue.getText().toString())) {
            Log.d(TAG, "formValid: Incorrect amount format.");
            return false;
        }

        return true;
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, String.format("isDouble: %s is not a double.", s), e);
            return false;
        }
    }

    private void refreshCategoryList() {
        categoryList = Category.getCategoryList(context);
    }
}
