package com.stevenjcorreia.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExpenseItemAdapter extends RecyclerView.Adapter<ExpenseItemHolder> {
    private Context context;
    private ArrayList<ExpenseItem> expenseItems;
    private ExpenseItemDatabase database;

    ExpenseItemAdapter(Context context, ArrayList<ExpenseItem> expenseItems, Comparator<ExpenseItem> sortType) {
        this.context = context;
        this.expenseItems = expenseItems;

        if (sortType != null) {
            Collections.sort(this.expenseItems, sortType);
        }

        database = Room.databaseBuilder(context, ExpenseItemDatabase.class, "Expense").allowMainThreadQueries().build();
    }

    @Override
    public int getItemCount() {
        return expenseItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ExpenseItemHolder holder, int position) {
        holder.expensePrice.setText("$".concat(String.valueOf(expenseItems.get(position).getPrice())));
        holder.expenseCategory.setText(expenseItems.get(position).getCategory());
        holder.expenseDate.setText(expenseItems.get(position).getDate());

        holder.setItemClickListener(new ExpenseClickListener() {
            @Override
            public void onExpenseClickListener(View v, int position) {
                Intent intent = new Intent(context, AddActivity.class);
                intent.putExtra("item", expenseItems.get(position));
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ExpenseItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseItemHolder(view);
    }

    void restoreItem(ExpenseItem deletedModel, int deletedPosition) {
        expenseItems.add(deletedPosition, deletedModel);
        database.expenseItemDao().deleteExpense(deletedModel);
        notifyItemInserted(deletedPosition);
    }
}
