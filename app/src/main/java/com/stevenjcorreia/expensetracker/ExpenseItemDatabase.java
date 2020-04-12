package com.stevenjcorreia.expensetracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ExpenseItem.class}, version = 1, exportSchema = false)
public abstract class ExpenseItemDatabase extends RoomDatabase {
    public abstract ExpenseItemDao expenseItemDao();
}
