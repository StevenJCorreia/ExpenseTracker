package com.stevenjcorreia.expensetracker;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;

public class CategoryActivity extends AppCompatActivity {
    private static ArrayList<String> categoryList = new ArrayList<>();
    private Comparator<String> sortType = null;
    private Context context = this;

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sortType = getSortType();

        recyclerView = findViewById(R.id.categoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        categoryList = Category.getCategoryList(context);

        adapter = new CategoryAdapter(context, categoryList, sortType);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final String temp = categoryList.get(position);
                categoryList.remove(temp);
                Category.removeCategoryFromFile(temp, context);

                adapter.notifyItemRemoved(position);

                Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), String.format("Removed category %s.", temp), Snackbar.LENGTH_LONG);
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

                        refreshAdapter();

                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) CategoryActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(CategoryActivity.this.getComponentName()));
        }

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                categoryList.clear();
                categoryList.addAll(Category.getCategoryList(context));
                adapter.notifyDataSetChanged();

                for (int i = 0; i < categoryList.size(); i++) {
                    if (!categoryList.get(i).toLowerCase().contains(query.toLowerCase())) {
                        categoryList.remove(i);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int size = categoryList.size();

        switch (id) {
            case R.id.action_back_to_expenses:
                Log.d("DEBUG", "onOptionsItemSelected: Back button clicked!");
                finish();
                break;
            case R.id.action_sort:
                if (size > 0) {
                    if (size == 1) {
                        Toast.makeText(context, "There is only one category to sort.", Toast.LENGTH_LONG).show();
                        break;
                    }

                    showSortDialog();
                } else {
                    Toast.makeText(context, "There are no categories to sort.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_delete_categories:
                if (size > 0) {
                    showDeleteDialog();
                } else {
                    Toast.makeText(context, "There are no categories to delete.", Toast.LENGTH_LONG).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAllCategories() {
        int count = categoryList.size();

        Category.removeCategoriesFromFile(context);
        categoryList.clear();

        adapter.notifyDataSetChanged();

        Toast.makeText(context, count + (count == 1 ? " category has " : " categories have ") + "been deleted.", Toast.LENGTH_LONG).show();
    }

    private Comparator<String> getSortType() {
        SharedPreferences sp = getSharedPreferences(getApplicationContext().getPackageName().concat(".SortType"), Context.MODE_PRIVATE);
        int sortTypeID = sp.getInt(Category.class.getSimpleName(),-1);

        return sortTypeID == 0 ? Category.CATEGORY_ASCENDING : Category.CATEGORY_DESCENDING;
    }

    private void refreshAdapter() {
        adapter = new CategoryAdapter(context, categoryList, sortType);
        recyclerView.setAdapter(adapter);
    }

    private void setSortType(Comparator<String> sortType) {
        this.sortType = sortType;

        SharedPreferences sp = getSharedPreferences(getApplicationContext().getPackageName().concat(".SortType"), Context.MODE_PRIVATE);
        sp.edit().putInt(Category.class.getSimpleName(), sortType == Category.CATEGORY_ASCENDING ? 0 : 1).apply();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Delete Categories")
                .setMessage("Are you sure you want to delete all categories?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllCategories();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showSortDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.dialog_sort_category, null);

        alertBuilder.setView(view);
        final AlertDialog dialog = alertBuilder.create();
        dialog.show();

        RadioButton categoryAscending, categoryDescending;
        categoryAscending = dialog.findViewById(R.id.categoryAscendingRadio);
        categoryDescending = dialog.findViewById(R.id.categoryDescendingRadio);

        if (sortType == null) {
            categoryAscending.setChecked(false);
            categoryDescending.setChecked(false);
        } else if (sortType == Category.CATEGORY_ASCENDING) {
            categoryAscending.setChecked(true);
        } else if (sortType == Category.CATEGORY_DESCENDING) {
            categoryDescending.setChecked(true);
        }

        assert categoryAscending != null;
        categoryAscending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(Category.CATEGORY_ASCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });

        assert categoryDescending != null;
        categoryDescending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortType(Category.CATEGORY_DESCENDING);

                refreshAdapter();

                dialog.dismiss();
            }
        });
    }
}
