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

    @Query("SELECT AVG(price) FROM ExpenseItem WHERE category = :category")
    double getAvgExpenseByCategory(String category);

    @Query("SELECT COUNT(*) FROM ExpenseItem")
    int getExpenseCount();

    @Query("SELECT COUNT(*) FROM ExpenseItem WHERE category = :category")
    int getExpenseCountByCategory(String category);

    @Query("SELECT * FROM ExpenseItem")
    List<ExpenseItem> getExpenses();

    @Query("SELECT SUM(price) FROM ExpenseItem WHERE SUBSTR(date, -4) = :year")
    double getExpenseTotalByYear(String year);

    @Query("SELECT MAX(price) FROM ExpenseItem WHERE category = :category")
    double getMaxExpenseByCategory(String category);

    @Query("SELECT MIN(price) FROM ExpenseItem WHERE category = :category")
    double getMinExpenseByCategory(String category);

    @Query("SELECT MAX(price) FROM ExpenseItem")
    double getMostExpensiveExpense();

    @Query("SELECT MIN(date) FROM ExpenseItem")
    String getOldestExpense();

    @Query("SELECT SUM(price) FROM ExpenseItem WHERE category = :category")
    double getTotalPriceByCategory(String category);

    @Query("SELECT DISTINCT SUBSTR(date, -4) FROM ExpenseItem")
    List<String> getYears();

    @Insert
    void insertExpenseItem(ExpenseItem item);

    @Update
    void updateExpenseItem(ExpenseItem item);
}
