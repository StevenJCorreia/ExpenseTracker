package com.stevenjcorreia.expensetracker;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExpenseItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private boolean imported;

    private ExpenseClickListener expenseClickListener;
    TextView expensePrice, expenseCategory, expenseDate;

    ExpenseItemHolder(@NonNull View view, boolean imported) {
        super(view);
        expensePrice = view.findViewById(R.id.expensePrice);
        expenseCategory = view.findViewById(R.id.expenseCategory);
        expenseDate = view.findViewById(R.id.expenseDate);
        this.imported = imported;

        if (!imported) {
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (!imported) {
            this.expenseClickListener.onExpenseClickListener(v, getLayoutPosition());
        }
    }

    void setItemClickListener(ExpenseClickListener expenseClickListener) {
        this.expenseClickListener = expenseClickListener;
    }
}
