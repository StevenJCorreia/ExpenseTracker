package com.stevenjcorreia.expensetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseItemDao {

    @Delete
    void deleteExpense(ExpenseItem item);

    @Query("DELETE FROM ExpenseItem")
    void deleteExpenses();

    @Query("SELECT COUNT(*) FROM ExpenseItem")
    int getExpenseCount();

    @Query("SELECT * FROM ExpenseItem")
    List<ExpenseItem> getExpenses();

    @Insert
    void insertExpenseItem(ExpenseItem item);

    @Update
    void updateExpenseItem(ExpenseItem item);
}
