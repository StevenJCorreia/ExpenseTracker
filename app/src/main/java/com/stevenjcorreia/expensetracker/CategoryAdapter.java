package com.stevenjcorreia.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryHolder> {
    private Comparator<String> sortType;
    private ArrayList<String> categoryItems;
    private Context context;

    CategoryAdapter(Context context, ArrayList<String> categoryItems, Comparator<String> sortType) {
        this.context = context;
        this.categoryItems = categoryItems;
        this.sortType = sortType;

        if (sortType != null) {
            Collections.sort(this.categoryItems, sortType);
        }
    }

    @Override
    public int getItemCount() {
        return categoryItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        holder.categoryName.setText(categoryItems.get(position));

        holder.setItemClickListener(new ExpenseClickListener() {
            @Override
            public void onExpenseClickListener(View v, int position) {
                Intent intent = new Intent(context, CategoryStatsActivity.class);
                intent.putExtra("category", categoryItems.get(position));
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryHolder(view);
    }

    void restoreItem(String temp, int position) {
        categoryItems.add(position, temp);
        Category.addCategoryToFile(temp, context);

        notifyItemInserted(position);
    }
}
