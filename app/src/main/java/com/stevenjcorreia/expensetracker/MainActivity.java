package com.stevenjcorreia.expensetracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;

// TODO - Query Room database asynchronously to avoid UI lock up
// TODO - Sort the newly added expense automatically so user doesn't have to re-select sort from popup dialog
public class MainActivity extends AppCompatActivity {
    private static ArrayList<ExpenseItem> expenseList = new ArrayList<>();
    private static Comparator<ExpenseItem> sortType = null;
    private ExpenseItemDatabase database = null;
    private Context context = this;

    private RecyclerView recyclerView;
    private ExpenseItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = Room.databaseBuilder(getApplicationContext(), ExpenseItemDatabase.class, "Expense").allowMainThreadQueries().build();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        expenseList = (ArrayList<ExpenseItem>) database.expenseItemDao().getExpenses();

        adapter = new ExpenseItemAdapter(this, expenseList, null);
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

                Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), " removed.", Snackbar.LENGTH_LONG);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
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

    private void refreshAdapter() {
        adapter = new ExpenseItemAdapter(context, expenseList, sortType);
        recyclerView.setAdapter(adapter);
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
                MainActivity.sortType = ExpenseItem.CATEGORY_ASCENDING;

                refreshAdapter();

                dialog.dismiss();
            }
        });

        categoryDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sortType = ExpenseItem.CATEGORY_DESCENDING;

                refreshAdapter();

                dialog.dismiss();
            }
        });

        priceAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sortType = ExpenseItem.PRICE_ASCENDING;

                refreshAdapter();

                dialog.dismiss();
            }
        });

        priceDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sortType = ExpenseItem.PRICE_DESCENDING;

                refreshAdapter();

                dialog.dismiss();
            }
        });

        dateAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sortType = ExpenseItem.DATE_ASCENDING;

                refreshAdapter();

                dialog.dismiss();
            }
        });

        dateDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sortType = ExpenseItem.DATE_DESCENDING;

                refreshAdapter();

                dialog.dismiss();
            }
        });
    }
}
