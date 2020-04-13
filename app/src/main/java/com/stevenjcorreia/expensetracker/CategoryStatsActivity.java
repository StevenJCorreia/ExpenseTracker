package com.stevenjcorreia.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.TextView;

public class CategoryStatsActivity extends AppCompatActivity {
    ExpenseItemDatabase database = null;
    String category = null;

    TextView categoryExpenseCount, categoryTotalPrice, categoryMaximumPrice, categoryMinimumPrice, categoryAveragePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_stats);

        database = Room.databaseBuilder(getApplicationContext(), ExpenseItemDatabase.class, "Expense").allowMainThreadQueries().build();

        category = getIntent().getStringExtra("category");

        setTitle(category);

        categoryExpenseCount = findViewById(R.id.categoryExpenseCount);
        categoryTotalPrice = findViewById(R.id.categoryTotalPrice);
        categoryMaximumPrice = findViewById(R.id.categoryMaximumPrice);
        categoryMinimumPrice = findViewById(R.id.categoryMinimumPrice);
        categoryAveragePrice = findViewById(R.id.categoryAveragePrice);

        categoryExpenseCount.setText(categoryExpenseCount.getText().toString().concat(String.valueOf(database.expenseItemDao().getExpenseCountByCategory(category))));
        categoryTotalPrice.setText(categoryTotalPrice.getText().toString().concat(String.valueOf(database.expenseItemDao().getTotalPriceByCategory(category))));
        categoryMaximumPrice.setText(categoryMaximumPrice.getText().toString().concat((String.valueOf(database.expenseItemDao().getMaxExpenseByCategory(category)))));
        categoryMinimumPrice.setText(categoryMinimumPrice.getText().toString().concat((String.valueOf(database.expenseItemDao().getMinExpenseByCategory(category)))));
        categoryAveragePrice.setText(categoryAveragePrice.getText().toString().concat((String.valueOf(database.expenseItemDao().getAvgExpenseByCategory(category)))));
    }
}
