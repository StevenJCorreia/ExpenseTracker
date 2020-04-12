package com.stevenjcorreia.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

// TODO - Implement analytics regarding clicked category (category's total/max/min/average)
public class CategoryStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_stats);

        String category = getIntent().getStringExtra("category");
    }
}
