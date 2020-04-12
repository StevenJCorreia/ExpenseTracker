package com.stevenjcorreia.expensetracker;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExpenseItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ExpenseClickListener expenseClickListener;
    TextView expensePrice, expenseCategory, expenseDate;

    ExpenseItemHolder(@NonNull View view) {
        super(view);
        expensePrice = view.findViewById(R.id.expensePrice);
        expenseCategory = view.findViewById(R.id.expenseCategory);
        expenseDate = view.findViewById(R.id.expenseDate);

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
