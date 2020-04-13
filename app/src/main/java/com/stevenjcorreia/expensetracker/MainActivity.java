package com.stevenjcorreia.expensetracker;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

// TODO - Query Room database asynchronously to avoid UI lock up
// TODO - Sort the newly added expense automatically so user doesn't have to re-select sort from popup dialog
public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static ArrayList<ExpenseItem> expenseList = new ArrayList<>();
    private Comparator<ExpenseItem> sortType = null;
    private ExpenseItemDatabase database = null;
    private Context context = this;
    private int backCount = 0;

    private RecyclerView recyclerView;
    private ExpenseItemAdapter adapter;

    MenuItem cancelFilter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case Utils.CREATE_FILE:
                Uri directoryPath = data != null ? data.getData() : null;
                ExpenseItem.exportExpenses(expenseList, directoryPath, context);
                break;
            case Utils.PICK_FILE:
                Uri filePath = data != null ? data.getData() : null;
                final ArrayList<ExpenseItem> importedExpenses = ExpenseItem.importExpenses(filePath, context);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_import_list, null);

                alertBuilder.setView(view);
                final AlertDialog dialog = alertBuilder.create();
                dialog.show();

                RecyclerView importedRecyclerView;
                Button okay, cancel;
                importedRecyclerView = dialog.findViewById(R.id.importedRecyclerView);
                okay = dialog.findViewById(R.id.okay);
                cancel = dialog.findViewById(R.id.cancelImport);

                importedRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                ExpenseItemAdapter importedAdapter = new ExpenseItemAdapter(context, importedExpenses, true, null);
                importedRecyclerView.setAdapter(importedAdapter);

                System.out.println(importedExpenses.size());

                assert okay != null;
                okay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (ExpenseItem expense: importedExpenses) {
                            database.expenseItemDao().insertExpenseItem(expense);
                        }

                        refreshAdapterDataSet();

                        dialog.dismiss();
                    }
                });

                assert cancel != null;
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
        }
    }

    @Override
    public void onBackPressed() {
        switch (backCount) {
            case 0:
                backCount++;
                Toast.makeText(context, "Press back again to go home.", Toast.LENGTH_LONG).show();
                break;
            case 1:
                backCount = 0;
                super.onBackPressed();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sortType = getSortType();

        database = Room.databaseBuilder(getApplicationContext(), ExpenseItemDatabase.class, "Expense").allowMainThreadQueries().build();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        expenseList = (ArrayList<ExpenseItem>) database.expenseItemDao().getExpenses();

        adapter = new ExpenseItemAdapter(context, expenseList, false, sortType);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final ExpenseItem temp = expenseList.get(position);

                expenseList.remove(temp);
                database.expenseItemDao().deleteExpense(temp);

                adapter.notifyItemRemoved(position);

                Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), " removed expense.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.restoreItem(temp, position);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        }).attachToRecyclerView(recyclerView);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, AddActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        cancelFilter = menu.findItem(R.id.action_cancel_filter);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                expenseList.clear();
                expenseList.addAll(database.expenseItemDao().getExpenses());
                adapter.notifyDataSetChanged();

                for (int i = 0; i < expenseList.size(); i++) {
                    if (!expenseList.get(i).getCategory().toLowerCase().contains(query.toLowerCase())) {
                        expenseList.remove(i);
                        adapter.notifyItemRemoved(i);

                        i--;
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_cancel_filter:
                expenseList.clear();
                expenseList.addAll(database.expenseItemDao().getExpenses());

                adapter.notifyDataSetChanged();

                cancelFilter.setVisible(false);

                break;
            case R.id.action_categories:
                startActivity(new Intent(context, CategoryActivity.class));
                break;
            case R.id.action_delete_expenses:
                if (expenseList.size() > 0) {
                    showDeleteExpensesDialog();
                } else {
                    Toast.makeText(context, "There are no expenses to delete.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_export:
                if (expenseList.size() > 0) {
                    getExportDirectory();
                } else {
                    Toast.makeText(context, "There are no expenses to export.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_filter:
                if (database.expenseItemDao().getExpenseCount() > 0) {
                    showFilterDialog();
                } else {
                    Toast.makeText(context, "There are no expenses to filter.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_import:
                getImportDirectory();
                break;
            case R.id.action_sort:
                if (expenseList.size() > 0) {
                    showSortDialog();
                } else {
                    Toast.makeText(context, "There are no expenses to sort.", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAllExpenses() {
        int count = database.expenseItemDao().getExpenseCount();

        database.expenseItemDao().deleteExpenses();
        expenseList.clear();
        adapter.notifyDataSetChanged();

        Toast.makeText(context, count + (count == 1 ? " expense has " : " expenses have ") + "been deleted.", Toast.LENGTH_LONG).show();
    }

    private void getExportDirectory() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Utils.getInitialDirectory());

        intent.putExtra(Intent.EXTRA_TITLE, String.format(Locale.US, "/%s_Expenses.csv", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date())));

        startActivityForResult(intent, Utils.CREATE_FILE);
    }

    private void getImportDirectory() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Utils.getInitialDirectory());

        startActivityForResult(Intent.createChooser(intent, "Select File"), Utils.PICK_FILE);
    }

    private Comparator<ExpenseItem> getSortType() {
        SharedPreferences sp = getSharedPreferences(getApplicationContext().getPackageName().concat(".SortType"), Context.MODE_PRIVATE);
        int sortTypeID = sp.getInt(ExpenseItem.class.getSimpleName(),-1);

        return ExpenseItem.getSortType(sortTypeID);
    }

    private void refreshAdapter() {
        adapter = new ExpenseItemAdapter(context, expenseList, false, sortType);
        recyclerView.setAdapter(adapter);
    }

    private void refreshAdapterDataSet() {
        expenseList.clear();
        expenseList.addAll(database.expenseItemDao().getExpenses());
        adapter.notifyDataSetChanged();
    }

    private void setSortType(Comparator<ExpenseItem> sortType) {
        this.sortType = sortType;

        SharedPreferences sp = getSharedPreferences(getApplicationContext().getPackageName().concat(".SortType"), Context.MODE_PRIVATE);
        sp.edit().putInt(ExpenseItem.class.getSimpleName(), ExpenseItem.getSortTypeID(sortType)).apply();
    }

    private void showDeleteExpensesDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Delete Expenses")
                .setMessage("Are you sure you want to delete all expenses?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllExpenses();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showFilterDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);

        alertBuilder.setView(view);
        final AlertDialog filterDialog = alertBuilder.create();
        filterDialog.show();

        final TextView filterDate, filterPrice;
        filterDate = filterDialog.findViewById(R.id.filterDate);
        filterPrice = filterDialog.findViewById(R.id.filterPrice);

        assert filterDate != null;
        filterDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.hide();

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_filter_date, null);

                alertBuilder.setView(view);
                final AlertDialog dialog = alertBuilder.create();
                dialog.show();

                final TextView errorMessage;
                final Button cancelDateRange, selectFromDate, selectToDate, submitDateRange;

                errorMessage = dialog.findViewById(R.id.errorMessage);
                cancelDateRange = dialog.findViewById(R.id.cancelDateRange);
                selectFromDate = dialog.findViewById(R.id.selectFromDate);
                selectToDate = dialog.findViewById(R.id.selectToDate);
                submitDateRange = dialog.findViewById(R.id.submitDateRange);

                String oldestExpense = database.expenseItemDao().getOldestExpense();

                final Calendar toCalendar = Calendar.getInstance();
                final Calendar fromCalendar = Calendar.getInstance();
                fromCalendar.set(Calendar.YEAR, Integer.parseInt(oldestExpense.split(", ")[1]));
                fromCalendar.set(Calendar.MONTH, Utils.getMonth(oldestExpense.substring(0, 3)));
                fromCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(oldestExpense.substring(4, oldestExpense.indexOf(","))));

                selectFromDate.setText(DateFormat.getDateInstance().format(fromCalendar.getTime()));
                selectToDate.setText(DateFormat.getDateInstance().format(toCalendar.getTime()));


                selectFromDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, MainActivity.this, fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();

                        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                fromCalendar.set(Calendar.YEAR, year);
                                fromCalendar.set(Calendar.MONTH, month);
                                fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                selectFromDate.setText(DateFormat.getDateInstance().format(fromCalendar.getTime()));
                            }
                        });
                    }
                });

                selectToDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, MainActivity.this, toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();

                        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                toCalendar.set(Calendar.YEAR, year);
                                toCalendar.set(Calendar.MONTH, month);
                                toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                selectToDate.setText(DateFormat.getDateInstance().format(toCalendar.getTime()));
                            }
                        });
                    }
                });

                assert cancelDateRange != null;
                cancelDateRange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        filterDialog.show();
                    }
                });

                assert submitDateRange != null;
                submitDateRange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO - Verify date range is valid

                        ArrayList<ExpenseItem> filteredExpenses = new ArrayList<>();
                        Date oldest = null;
                        Date newest = null;

                        try {
                            oldest = new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(selectFromDate.getText().toString());
                            newest = new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(selectToDate.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (oldest.after(newest)) {
                            errorMessage.setText("Date range incorrect.");
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            errorMessage.setVisibility(View.GONE);
                        }

                        if (oldest.equals(newest)) {
                            for (int i = 0; i < expenseList.size(); i++) {
                                try {
                                    Date tempDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(expenseList.get(i).getDate());
                                    if (tempDate.equals(oldest)) {
                                        filteredExpenses.add(expenseList.get(i));
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            for (int i = 0; i < expenseList.size(); i++) {
                                try {
                                    Date tempDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(expenseList.get(i).getDate());
                                    if ((tempDate.before(newest) || tempDate.equals(newest)) && (tempDate.after(oldest) || tempDate.equals(oldest))) { // TODO - Fix this logic
                                        filteredExpenses.add(expenseList.get(i));
                                        Log.e("DEBUG", "onClick: " + expenseList.get(i).getPrice() + " WITHIN RANGE");
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        expenseList.clear();
                        expenseList.addAll(filteredExpenses);

                        adapter.notifyDataSetChanged();

                        cancelFilter.setVisible(true);

                        dialog.dismiss();
                    }
                });
            }
        });

        assert filterPrice != null;
        filterPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.hide();

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                View view = getLayoutInflater().inflate(R.layout.dialog_filter_price, null);

                alertBuilder.setView(view);
                final AlertDialog dialog = alertBuilder.create();
                dialog.show();

                Button cancelPriceRange, submitPriceRange;
                final RangeSeekBar dateRange;
                dateRange = dialog.findViewById(R.id.dateRange);
                cancelPriceRange = dialog.findViewById(R.id.cancelPriceRange);
                submitPriceRange = dialog.findViewById(R.id.submitPriceRange);

                DecimalFormat format = new DecimalFormat("0");
                format.setRoundingMode(RoundingMode.CEILING);

                int max = Integer.parseInt(format.format(database.expenseItemDao().getMostExpensiveExpense()));

                dateRange.setRangeValues(0, max);

                dateRange.setTextAboveThumbsColor(Color.BLACK);

                assert cancelPriceRange != null;
                cancelPriceRange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        filterDialog.show();
                    }
                });

                assert submitPriceRange != null;
                submitPriceRange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int min = Integer.parseInt(dateRange.getSelectedMinValue().toString());
                        int max = Integer.parseInt(dateRange.getSelectedMaxValue().toString());

                        ArrayList<ExpenseItem> filteredExpenses = new ArrayList<>();

                        if (min == max) {
                            for (int i = 0; i < expenseList.size(); i++) {
                                if (expenseList.get(i).getPrice() == min) {
                                    filteredExpenses.add(expenseList.get(i));
                                }
                            }
                        } else {
                            for (int i = 0; i < expenseList.size(); i++) {
                                if ((expenseList.get(i).getPrice() > min || expenseList.get(i).getPrice() == min) && ((expenseList.get(i).getPrice() < max) || expenseList.get(i).getPrice() == max)) {
                                    filteredExpenses.add(expenseList.get(i));
                                }
                            }
                        }

                        expenseList.clear();
                        expenseList.addAll(filteredExpenses);

                        adapter.notifyDataSetChanged();

                        cancelFilter.setVisible(true);

                        dialog.dismiss();
                    }
                });
            }
        });
    }

    // TODO - Implement shared preferences for persistence of sortType
    private void showSortDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_sort_expense, null);

        alertBuilder.setView(view);
        final AlertDialog dialog = alertBuilder.create();
        dialog.show();

        RadioButton categoryAscending, categoryDescending, priceAscending, priceDescending, dateAscending, dateDescending;
        categoryAscending = dialog.findViewById(R.id.categoryAscendingRadio);
        categoryDescending = dialog.findViewById(R.id.categoryDescendingRadio);
        priceAscending = dialog.findViewById(R.id.priceAscendingRadio);
        priceDescending = dialog.findViewById(R.id.priceDescendingRadio);
        dateAscending = dialog.findViewById(R.id.dateAscendingRadio);
        dateDescending = dialog.findViewById(R.id.dateDescendingRadio);

        if (sortType == null) {
            categoryAscending.setChecked(false);
            categoryDescending.setChecked(false);
            priceAscending.setChecked(false);
            priceDescending.setChecked(false);
            dateAscending.setChecked(false);
            dateDescending.setChecked(false);
        } else if (sortType == ExpenseItem.CATEGORY_ASCENDING) {
            categoryAscending.setChecked(true);
        } else if (sortType == ExpenseItem.CATEGORY_DESCENDING) {
            categoryDescending.setChecked(true);
        } else if (sortType == ExpenseItem.PRICE_ASCENDING) {
            priceAscending.setChecked(true);
        } else if (sortType == ExpenseItem.PRICE_DESCENDING) {
            priceDescending.setChecked(true);
        } else if (sortType == ExpenseItem.DATE_ASCENDING) {
            dateAscending.setChecked(true);
        } else if (sortType == ExpenseItem.DATE_DESCENDING) {
            dateDescending.setChecked(true);
        }

        categoryAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(ExpenseItem.CATEGORY_ASCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });

        categoryDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(ExpenseItem.CATEGORY_DESCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });

        priceAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(ExpenseItem.PRICE_ASCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });

        priceDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(ExpenseItem.PRICE_DESCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });

        dateAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(ExpenseItem.DATE_ASCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });

        dateDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(ExpenseItem.DATE_DESCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });
    }
}
