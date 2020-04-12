package com.stevenjcorreia.expensetracker;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    TextView categoryName;
    private ExpenseClickListener expenseClickListener;

    CategoryHolder(@NonNull View view) {
        super(view);
        categoryName = view.findViewById(R.id.categoryName);

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.expenseClickListener.onExpenseClickListener(v, getLayoutPosition());
    }

    void setItemClickListener(ExpenseClickListener expenseClickListener) {
        this.expenseClickListener = expenseClickListener;
    }
}
