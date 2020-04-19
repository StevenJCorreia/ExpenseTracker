package com.stevenjcorreia.expensetracker;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExpenseItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private boolean imported;

    private ExpenseClickListener expenseClickListener;
    TextView expensePrice, expenseCategory, expenseDate;
    ImageView expenseImage;

    ExpenseItemHolder(@NonNull View view, boolean imported) {
        super(view);

        this.imported = imported;
        expensePrice = view.findViewById(R.id.expensePrice);
        expenseCategory = view.findViewById(R.id.expenseCategory);
        expenseDate = view.findViewById(R.id.expenseDate);
        expenseImage = view.findViewById(R.id.expenseImage);

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
