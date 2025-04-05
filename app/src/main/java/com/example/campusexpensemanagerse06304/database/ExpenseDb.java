package com.example.campusexpensemanagerse06304.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;
import com.example.campusexpensemanagerse06304.model.RecurringExpense;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseDb extends SQLiteOpenHelper {
    private static final String TAG = "ExpenseDb";
    private static final String DB_NAME = "campus expenses";
    private static final int DB_VERSION = 2; // Increased version number!

    // Category table
    private static final String TABLE_CATEGORY = "categories";
    private static final String CAT_ID_COL = "id";
    private static final String CAT_NAME_COL = "name";
    private static final String CAT_DESC_COL = "description";
    private static final String CAT_ICON_COL = "icon";
    private static final String CAT_COLOR_COL = "color";

    // Expense table
    private static final String TABLE_EXPENSE = "expenses";
    private static final String EXP_ID_COL = "id";
    private static final String EXP_USER_ID_COL = "user_id";
    private static final String EXP_CAT_ID_COL = "category_id";
    private static final String EXP_AMOUNT_COL = "amount";
    private static final String EXP_DESC_COL = "description";
    private static final String EXP_DATE_COL = "date";
    private static final String EXP_PAYMENT_METHOD_COL = "payment_method";
    private static final String EXP_IS_RECURRING_COL = "is_recurring";
    private static final String EXP_RECURRING_ID_COL = "recurring_expense_id";
    private static final String EXP_CREATED_AT = "created_at";
    private static final String EXP_UPDATED_AT = "updated_at";

    // Budget table
    private static final String TABLE_BUDGET = "budgets";
    private static final String BUD_ID_COL = "id";
    private static final String BUD_USER_ID_COL = "user_id";
    private static final String BUD_CAT_ID_COL = "category_id";
    private static final String BUD_AMOUNT_COL = "amount";
    private static final String BUD_PERIOD_COL = "period";
    private static final String BUD_START_DATE_COL = "start_date";
    private static final String BUD_END_DATE_COL = "end_date";
    private static final String BUD_CREATED_AT = "created_at";
    private static final String BUD_UPDATED_AT = "updated_at";

    // Recurring Expense table
    private static final String TABLE_RECURRING = "recurring_expenses";
    private static final String REC_ID_COL = "id";
    private static final String REC_USER_ID_COL = "user_id";
    private static final String REC_CAT_ID_COL = "category_id";
    private static final String REC_AMOUNT_COL = "amount";
    private static final String REC_DESC_COL = "description";
    private static final String REC_FREQUENCY_COL = "frequency";
    private static final String REC_START_DATE_COL = "start_date";
    private static final String REC_END_DATE_COL = "end_date";
    private static final String REC_LAST_CHARGED_COL = "last_charged";
    private static final String REC_NEXT_CHARGE_COL = "next_charge";
    private static final String REC_CREATED_AT = "created_at";
    private static final String REC_UPDATED_AT = "updated_at";

    // Total Budget table
    private static final String TABLE_TOTAL_BUDGET = "total_budget";
    private static final String TOT_ID_COL = "id";
    private static final String TOT_USER_ID_COL = "user_id";
    private static final String TOT_AMOUNT_COL = "amount";
    private static final String TOT_PERIOD_COL = "period";
    private static final String TOT_START_DATE_COL = "start_date";
    private static final String TOT_END_DATE_COL = "end_date";
    private static final String TOT_CREATED_AT = "created_at";
    private static final String TOT_UPDATED_AT = "updated_at";

    public ExpenseDb(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create Category table
            String createCategoryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY + " ( "
                    + CAT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CAT_NAME_COL + " TEXT NOT NULL, "
                    + CAT_DESC_COL + " TEXT, "
                    + CAT_ICON_COL + " TEXT, "
                    + CAT_COLOR_COL + " TEXT )";

            // Create Expense table
            String createExpenseTable = "CREATE TABLE IF NOT EXISTS " + TABLE_EXPENSE + " ( "
                    + EXP_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + EXP_USER_ID_COL + " INTEGER NOT NULL, "
                    + EXP_CAT_ID_COL + " INTEGER NOT NULL, "
                    + EXP_AMOUNT_COL + " REAL NOT NULL, "
                    + EXP_DESC_COL + " TEXT, "
                    + EXP_DATE_COL + " DATE NOT NULL, "
                    + EXP_PAYMENT_METHOD_COL + " TEXT, "
                    + EXP_IS_RECURRING_COL + " INTEGER DEFAULT 0, "
                    + EXP_RECURRING_ID_COL + " INTEGER, "
                    + EXP_CREATED_AT + " DATETIME, "
                    + EXP_UPDATED_AT + " DATETIME )";

            // Create Budget table
            String createBudgetTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " ( "
                    + BUD_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + BUD_USER_ID_COL + " INTEGER NOT NULL, "
                    + BUD_CAT_ID_COL + " INTEGER NOT NULL, "
                    + BUD_AMOUNT_COL + " REAL NOT NULL, "
                    + BUD_PERIOD_COL + " TEXT NOT NULL, "
                    + BUD_START_DATE_COL + " DATE NOT NULL, "
                    + BUD_END_DATE_COL + " DATE, "
                    + BUD_CREATED_AT + " DATETIME, "
                    + BUD_UPDATED_AT + " DATETIME )";

            // Create Recurring Expense table
            String createRecurringTable = "CREATE TABLE IF NOT EXISTS " + TABLE_RECURRING + " ( "
                    + REC_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + REC_USER_ID_COL + " INTEGER NOT NULL, "
                    + REC_CAT_ID_COL + " INTEGER NOT NULL, "
                    + REC_AMOUNT_COL + " REAL NOT NULL, "
                    + REC_DESC_COL + " TEXT, "
                    + REC_FREQUENCY_COL + " TEXT NOT NULL, "
                    + REC_START_DATE_COL + " DATE NOT NULL, "
                    + REC_END_DATE_COL + " DATE, "
                    + REC_LAST_CHARGED_COL + " DATE, "
                    + REC_NEXT_CHARGE_COL + " DATE, "
                    + REC_CREATED_AT + " DATETIME, "
                    + REC_UPDATED_AT + " DATETIME )";

            // Create Total Budget table
            String createTotalBudgetTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TOTAL_BUDGET + " ( "
                    + TOT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TOT_USER_ID_COL + " INTEGER NOT NULL, "
                    + TOT_AMOUNT_COL + " REAL NOT NULL, "
                    + TOT_PERIOD_COL + " TEXT NOT NULL, "
                    + TOT_START_DATE_COL + " DATE NOT NULL, "
                    + TOT_END_DATE_COL + " DATE, "
                    + TOT_CREATED_AT + " DATETIME, "
                    + TOT_UPDATED_AT + " DATETIME )";

            // Execute table creation
            db.execSQL(createCategoryTable);
            db.execSQL(createExpenseTable);
            db.execSQL(createBudgetTable);
            db.execSQL(createRecurringTable);
            db.execSQL(createTotalBudgetTable);

            // Insert default categories
            insertDefaultCategories(db);

            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        try {
            if (oldVersion < 2) {
                // Create the new total_budget table without dropping existing tables
                String createTotalBudgetTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TOTAL_BUDGET + " ( "
                        + TOT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TOT_USER_ID_COL + " INTEGER NOT NULL, "
                        + TOT_AMOUNT_COL + " REAL NOT NULL, "
                        + TOT_PERIOD_COL + " TEXT NOT NULL, "
                        + TOT_START_DATE_COL + " DATE NOT NULL, "
                        + TOT_END_DATE_COL + " DATE, "
                        + TOT_CREATED_AT + " DATETIME, "
                        + TOT_UPDATED_AT + " DATETIME )";
                db.execSQL(createTotalBudgetTable);
                Log.d(TAG, "Created total_budget table during upgrade");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during database upgrade: " + e.getMessage());
        }
    }

    // Helper method to create missing tables
    public void ensureTablesExist() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Check if total_budget table exists
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{TABLE_TOTAL_BUDGET});

            boolean totalBudgetExists = cursor.getCount() > 0;
            cursor.close();

            if (!totalBudgetExists) {
                Log.d(TAG, "Total budget table does not exist, creating it now");
                String createTotalBudgetTable = "CREATE TABLE IF NOT EXISTS " + TABLE_TOTAL_BUDGET + " ( "
                        + TOT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TOT_USER_ID_COL + " INTEGER NOT NULL, "
                        + TOT_AMOUNT_COL + " REAL NOT NULL, "
                        + TOT_PERIOD_COL + " TEXT NOT NULL, "
                        + TOT_START_DATE_COL + " DATE NOT NULL, "
                        + TOT_END_DATE_COL + " DATE, "
                        + TOT_CREATED_AT + " DATETIME, "
                        + TOT_UPDATED_AT + " DATETIME )";
                db.execSQL(createTotalBudgetTable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring tables exist: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    // Helper method to insert default categories
    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] categories = {"Housing", "Food", "Transportation", "Entertainment", "Education", "Health", "Personal", "Utilities", "Other"};
        String[] colors = {"#FF5722", "#4CAF50", "#2196F3", "#9C27B0", "#FFC107", "#E91E63", "#3F51B5", "#009688", "#607D8B"};
        String[] icons = {"home", "restaurant", "directions_car", "movie", "school", "local_hospital", "person", "power", "more_horiz"};

        for (int i = 0; i < categories.length; i++) {
            ContentValues values = new ContentValues();
            values.put(CAT_NAME_COL, categories[i]);
            values.put(CAT_DESC_COL, categories[i] + " expenses");
            values.put(CAT_ICON_COL, icons[i]);
            values.put(CAT_COLOR_COL, colors[i]);
            db.insert(TABLE_CATEGORY, null, values);
        }
    }

    // CRUD operations for Category
    public long insertCategory(String name, String description, String icon, String color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CAT_NAME_COL, name);
        values.put(CAT_DESC_COL, description);
        values.put(CAT_ICON_COL, icon);
        values.put(CAT_COLOR_COL, color);
        long id = db.insert(TABLE_CATEGORY, null, values);
        db.close();
        return id;
    }

    public boolean updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CAT_NAME_COL, category.getName());
        values.put(CAT_DESC_COL, category.getDescription());
        values.put(CAT_ICON_COL, category.getIcon());
        values.put(CAT_COLOR_COL, category.getColor());

        String whereClause = CAT_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(category.getId())};

        int rowsAffected = db.update(TABLE_CATEGORY, values, whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First check if category is in use
        String checkQuery = "SELECT COUNT(*) FROM " + TABLE_EXPENSE + " WHERE " + EXP_CAT_ID_COL + " = ?";
        Cursor cursor = db.rawQuery(checkQuery, new String[]{String.valueOf(categoryId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count > 0) {
            // Category is in use, can't delete
            db.close();
            return false;
        }

        // Delete the category
        String whereClause = CAT_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(categoryId)};

        int rowsAffected = db.delete(TABLE_CATEGORY, whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    @SuppressLint("Range")
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CATEGORY, null, null,
                null, null, null, CAT_NAME_COL + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndex(CAT_ID_COL)));
                category.setName(cursor.getString(cursor.getColumnIndex(CAT_NAME_COL)));
                category.setDescription(cursor.getString(cursor.getColumnIndex(CAT_DESC_COL)));
                category.setIcon(cursor.getString(cursor.getColumnIndex(CAT_ICON_COL)));
                category.setColor(cursor.getString(cursor.getColumnIndex(CAT_COLOR_COL)));

                categoryList.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categoryList;
    }

    // CRUD operations for Expenses
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertExpense(int userId, int categoryId, double amount, String description,
                              String date, String paymentMethod, boolean isRecurring, Integer recurringExpenseId) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now();
        String currentDate = dtf.format(zoneDt);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(EXP_USER_ID_COL, userId);
        values.put(EXP_CAT_ID_COL, categoryId);
        values.put(EXP_AMOUNT_COL, amount);
        values.put(EXP_DESC_COL, description);
        values.put(EXP_DATE_COL, date);
        values.put(EXP_PAYMENT_METHOD_COL, paymentMethod);
        values.put(EXP_IS_RECURRING_COL, isRecurring ? 1 : 0);

        if (recurringExpenseId != null) {
            values.put(EXP_RECURRING_ID_COL, recurringExpenseId);
        }

        values.put(EXP_CREATED_AT, currentDate);
        values.put(EXP_UPDATED_AT, currentDate);

        long id = db.insert(TABLE_EXPENSE, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public List<Expense> getExpensesByUser(int userId) {
        List<Expense> expenseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = EXP_USER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_EXPENSE, null, selection,
                selectionArgs, null, null, EXP_DATE_COL + " DESC");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(cursor.getColumnIndex(EXP_ID_COL)));
                expense.setUserId(cursor.getInt(cursor.getColumnIndex(EXP_USER_ID_COL)));
                expense.setCategoryId(cursor.getInt(cursor.getColumnIndex(EXP_CAT_ID_COL)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndex(EXP_AMOUNT_COL)));
                expense.setDescription(cursor.getString(cursor.getColumnIndex(EXP_DESC_COL)));

                try {
                    String dateStr = cursor.getString(cursor.getColumnIndex(EXP_DATE_COL));
                    expense.setDate(dateFormat.parse(dateStr));
                } catch (ParseException e) {
                    expense.setDate(new Date()); // Default to current date if parsing fails
                }

                expense.setPaymentMethod(cursor.getString(cursor.getColumnIndex(EXP_PAYMENT_METHOD_COL)));
                expense.setRecurring(cursor.getInt(cursor.getColumnIndex(EXP_IS_RECURRING_COL)) == 1);

                expenseList.add(expense);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenseList;
    }

    @SuppressLint("Range")
    public List<Expense> getExpensesByMonth(int userId, String yearMonth) {
        List<Expense> expenseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = EXP_USER_ID_COL + " = ? AND " + EXP_DATE_COL + " LIKE ?";
        String[] selectionArgs = {String.valueOf(userId), yearMonth + "%"};

        Cursor cursor = db.query(TABLE_EXPENSE, null, selection,
                selectionArgs, null, null, EXP_DATE_COL + " DESC");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(cursor.getColumnIndex(EXP_ID_COL)));
                expense.setUserId(cursor.getInt(cursor.getColumnIndex(EXP_USER_ID_COL)));
                expense.setCategoryId(cursor.getInt(cursor.getColumnIndex(EXP_CAT_ID_COL)));
                expense.setAmount(cursor.getDouble(cursor.getColumnIndex(EXP_AMOUNT_COL)));
                expense.setDescription(cursor.getString(cursor.getColumnIndex(EXP_DESC_COL)));

                try {
                    String dateStr = cursor.getString(cursor.getColumnIndex(EXP_DATE_COL));
                    expense.setDate(dateFormat.parse(dateStr));
                } catch (ParseException e) {
                    expense.setDate(new Date()); // Default to current date if parsing fails
                }

                expense.setPaymentMethod(cursor.getString(cursor.getColumnIndex(EXP_PAYMENT_METHOD_COL)));
                expense.setRecurring(cursor.getInt(cursor.getColumnIndex(EXP_IS_RECURRING_COL)) == 1);

                expenseList.add(expense);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenseList;
    }

    // CRUD operations for Budget
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertBudget(int userId, int categoryId, double amount, String period,
                             String startDate, String endDate) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now();
        String currentDate = dtf.format(zoneDt);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BUD_USER_ID_COL, userId);
        values.put(BUD_CAT_ID_COL, categoryId);
        values.put(BUD_AMOUNT_COL, amount);
        values.put(BUD_PERIOD_COL, period);
        values.put(BUD_START_DATE_COL, startDate);

        if (endDate != null && !endDate.isEmpty()) {
            values.put(BUD_END_DATE_COL, endDate);
        }

        values.put(BUD_CREATED_AT, currentDate);
        values.put(BUD_UPDATED_AT, currentDate);

        long id = db.insert(TABLE_BUDGET, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public List<Budget> getBudgetsByUser(int userId) {
        List<Budget> budgetList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = BUD_USER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_BUDGET, null, selection,
                selectionArgs, null, null, BUD_START_DATE_COL + " DESC");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (cursor.moveToFirst()) {
            do {
                Budget budget = new Budget();
                budget.setId(cursor.getInt(cursor.getColumnIndex(BUD_ID_COL)));
                budget.setUserId(cursor.getInt(cursor.getColumnIndex(BUD_USER_ID_COL)));
                budget.setCategoryId(cursor.getInt(cursor.getColumnIndex(BUD_CAT_ID_COL)));
                budget.setAmount(cursor.getDouble(cursor.getColumnIndex(BUD_AMOUNT_COL)));
                budget.setPeriod(cursor.getString(cursor.getColumnIndex(BUD_PERIOD_COL)));

                try {
                    String startDateStr = cursor.getString(cursor.getColumnIndex(BUD_START_DATE_COL));
                    budget.setStartDate(dateFormat.parse(startDateStr));

                    String endDateStr = cursor.getString(cursor.getColumnIndex(BUD_END_DATE_COL));
                    if (endDateStr != null) {
                        budget.setEndDate(dateFormat.parse(endDateStr));
                    }
                } catch (ParseException e) {
                    budget.setStartDate(new Date()); // Default to current date if parsing fails
                }

                budgetList.add(budget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return budgetList;
    }

    // CRUD operations for RecurringExpense
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertRecurringExpense(int userId, int categoryId, double amount, String description,
                                       String frequency, String startDate, String endDate) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now();
        String currentDate = dtf.format(zoneDt);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(REC_USER_ID_COL, userId);
        values.put(REC_CAT_ID_COL, categoryId);
        values.put(REC_AMOUNT_COL, amount);
        values.put(REC_DESC_COL, description);
        values.put(REC_FREQUENCY_COL, frequency);
        values.put(REC_START_DATE_COL, startDate);

        if (endDate != null && !endDate.isEmpty()) {
            values.put(REC_END_DATE_COL, endDate);
        }

        // Calculate next charge date based on frequency and start date
        values.put(REC_NEXT_CHARGE_COL, startDate); // Simplified for now

        values.put(REC_CREATED_AT, currentDate);
        values.put(REC_UPDATED_AT, currentDate);

        long id = db.insert(TABLE_RECURRING, null, values);
        db.close();
        return id;
    }

    @SuppressLint("Range")
    public List<RecurringExpense> getRecurringExpensesByUser(int userId) {
        List<RecurringExpense> recurringList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = REC_USER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_RECURRING, null, selection,
                selectionArgs, null, null, REC_NEXT_CHARGE_COL + " ASC");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (cursor.moveToFirst()) {
            do {
                RecurringExpense recurring = new RecurringExpense();
                recurring.setId(cursor.getInt(cursor.getColumnIndex(REC_ID_COL)));
                recurring.setUserId(cursor.getInt(cursor.getColumnIndex(REC_USER_ID_COL)));
                recurring.setCategoryId(cursor.getInt(cursor.getColumnIndex(REC_CAT_ID_COL)));
                recurring.setAmount(cursor.getDouble(cursor.getColumnIndex(REC_AMOUNT_COL)));
                recurring.setDescription(cursor.getString(cursor.getColumnIndex(REC_DESC_COL)));
                recurring.setFrequency(cursor.getString(cursor.getColumnIndex(REC_FREQUENCY_COL)));

                try {
                    String startDateStr = cursor.getString(cursor.getColumnIndex(REC_START_DATE_COL));
                    recurring.setStartDate(dateFormat.parse(startDateStr));

                    String endDateStr = cursor.getString(cursor.getColumnIndex(REC_END_DATE_COL));
                    if (endDateStr != null) {
                        recurring.setEndDate(dateFormat.parse(endDateStr));
                    }

                    String lastChargedStr = cursor.getString(cursor.getColumnIndex(REC_LAST_CHARGED_COL));
                    if (lastChargedStr != null) {
                        recurring.setLastCharged(dateFormat.parse(lastChargedStr));
                    }

                    String nextChargeStr = cursor.getString(cursor.getColumnIndex(REC_NEXT_CHARGE_COL));
                    if (nextChargeStr != null) {
                        recurring.setNextCharge(dateFormat.parse(nextChargeStr));
                    }
                } catch (ParseException e) {
                    recurring.setStartDate(new Date()); // Default to current date if parsing fails
                }

                recurringList.add(recurring);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return recurringList;
    }

    // Monthly summary methods
    @SuppressLint("Range")
    public double getTotalExpensesByMonth(int userId, String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;

        String query = "SELECT SUM(" + EXP_AMOUNT_COL + ") as total FROM " + TABLE_EXPENSE +
                " WHERE " + EXP_USER_ID_COL + " = ? AND " + EXP_DATE_COL + " LIKE ?";
        String[] selectionArgs = {String.valueOf(userId), yearMonth + "%"};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndex("total"));
        }

        cursor.close();
        db.close();
        return total;
    }

    @SuppressLint("Range")
    public double getTotalExpensesByCategoryAndMonth(int userId, int categoryId, String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;

        try {
            // If we want all categories (categoryId = -1), don't filter by category
            String query;
            String[] selectionArgs;

            if (categoryId == -1) {
                // Sum all expenses for the month regardless of category
                query = "SELECT SUM(" + EXP_AMOUNT_COL + ") as total FROM " + TABLE_EXPENSE +
                        " WHERE " + EXP_USER_ID_COL + " = ? AND " + EXP_DATE_COL + " LIKE ?";
                selectionArgs = new String[]{String.valueOf(userId), yearMonth + "%"};
            } else {
                // Sum expenses for specific category and month
                query = "SELECT SUM(" + EXP_AMOUNT_COL + ") as total FROM " + TABLE_EXPENSE +
                        " WHERE " + EXP_USER_ID_COL + " = ? AND " + EXP_CAT_ID_COL + " = ? AND " +
                        EXP_DATE_COL + " LIKE ?";
                selectionArgs = new String[]{String.valueOf(userId), String.valueOf(categoryId), yearMonth + "%"};
            }

            Cursor cursor = db.rawQuery(query, selectionArgs);

            if (cursor.moveToFirst() && !cursor.isNull(cursor.getColumnIndex("total"))) {
                total = cursor.getDouble(cursor.getColumnIndex("total"));
            }

            cursor.close();
            Log.d("ExpenseDb", "Total expenses for user " + userId + " in month " + yearMonth +
                    " and category " + categoryId + ": " + total);
        } catch (Exception e) {
            Log.e("ExpenseDb", "Error getting expenses", e);
        } finally {
            db.close();
        }

        return total;
    }

    // Budget status methods
    @SuppressLint("Range")
    public double getBudgetRemainingForCategory(int userId, int categoryId, String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();
        double budgetAmount = 0;
        double expenseTotal = 0;

        // Get the budget amount for this category
        String budgetQuery = "SELECT " + BUD_AMOUNT_COL + " FROM " + TABLE_BUDGET +
                " WHERE " + BUD_USER_ID_COL + " = ? AND " + BUD_CAT_ID_COL + " = ? " +
                " AND " + BUD_START_DATE_COL + " <= ? " +
                " AND (" + BUD_END_DATE_COL + " IS NULL OR " + BUD_END_DATE_COL + " >= ?)";

        String currentMonth = yearMonth + "-01";
        String[] budgetArgs = {String.valueOf(userId), String.valueOf(categoryId), currentMonth, currentMonth};

        Cursor budgetCursor = db.rawQuery(budgetQuery, budgetArgs);

        if (budgetCursor.moveToFirst()) {
            budgetAmount = budgetCursor.getDouble(budgetCursor.getColumnIndex(BUD_AMOUNT_COL));
        }

        budgetCursor.close();

        // Get the expense total for this category in this month
        expenseTotal = getTotalExpensesByCategoryAndMonth(userId, categoryId, yearMonth);

        db.close();
        return budgetAmount - expenseTotal;
    }

    // Update and delete methods for all entities
    public int updateExpense(int id, int categoryId, double amount, String description,
                             String date, String paymentMethod) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(EXP_CAT_ID_COL, categoryId);
        values.put(EXP_AMOUNT_COL, amount);
        values.put(EXP_DESC_COL, description);
        values.put(EXP_DATE_COL, date);
        values.put(EXP_PAYMENT_METHOD_COL, paymentMethod);

        String whereClause = EXP_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int rowsAffected = db.update(TABLE_EXPENSE, values, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }

    public int deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = EXP_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int rowsAffected = db.delete(TABLE_EXPENSE, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }

    public int updateBudget(int id, int categoryId, double amount, String period,
                            String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BUD_CAT_ID_COL, categoryId);
        values.put(BUD_AMOUNT_COL, amount);
        values.put(BUD_PERIOD_COL, period);
        values.put(BUD_START_DATE_COL, startDate);

        if (endDate != null && !endDate.isEmpty()) {
            values.put(BUD_END_DATE_COL, endDate);
        }

        String whereClause = BUD_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int rowsAffected = db.update(TABLE_BUDGET, values, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }

    public int deleteBudget(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = BUD_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int rowsAffected = db.delete(TABLE_BUDGET, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }

    public boolean deleteRecurringExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String whereClause = REC_ID_COL + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int rowsAffected = db.delete(TABLE_RECURRING, whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    // Total Budget methods
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertOrUpdateTotalBudget(int userId, double amount, String period, String startDate, String endDate) {
        ensureTablesExist(); // Make sure the table exists first

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now();
        String currentDate = dtf.format(zoneDt);

        SQLiteDatabase db = this.getWritableDatabase();

        // Check if a total budget already exists for this user and period
        String query = "SELECT " + TOT_ID_COL + " FROM " + TABLE_TOTAL_BUDGET +
                " WHERE " + TOT_USER_ID_COL + " = ? AND " +
                TOT_PERIOD_COL + " = ?";

        long result = -1;
        try {
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), period});

            if (cursor.moveToFirst()) {
                // Update existing total budget
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(TOT_ID_COL));
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(TOT_AMOUNT_COL, amount);
                values.put(TOT_START_DATE_COL, startDate);
                if (endDate != null && !endDate.isEmpty()) {
                    values.put(TOT_END_DATE_COL, endDate);
                }
                values.put(TOT_UPDATED_AT, currentDate);

                String whereClause = TOT_ID_COL + " = ?";
                String[] whereArgs = {String.valueOf(id)};

                result = db.update(TABLE_TOTAL_BUDGET, values, whereClause, whereArgs);
            } else {
                // Insert new total budget
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(TOT_USER_ID_COL, userId);
                values.put(TOT_AMOUNT_COL, amount);
                values.put(TOT_PERIOD_COL, period);
                values.put(TOT_START_DATE_COL, startDate);
                if (endDate != null && !endDate.isEmpty()) {
                    values.put(TOT_END_DATE_COL, endDate);
                }
                values.put(TOT_CREATED_AT, currentDate);
                values.put(TOT_UPDATED_AT, currentDate);

                result = db.insert(TABLE_TOTAL_BUDGET, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting/updating total budget: " + e.getMessage());
        } finally {
            db.close();
        }

        return result;
    }

    @SuppressLint("Range")
    public double getTotalBudget(int userId, String period) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalBudget = 0;

        try {
            // Check if the table exists first
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='total_budget'",
                    null
            );
            boolean tableExists = tableCheck.moveToFirst();
            tableCheck.close();

            if (tableExists) {
                String query = "SELECT " + TOT_AMOUNT_COL + " FROM " + TABLE_TOTAL_BUDGET +
                        " WHERE " + TOT_USER_ID_COL + " = ? AND " +
                        TOT_PERIOD_COL + " = ? " +
                        "ORDER BY " + TOT_ID_COL + " DESC LIMIT 1";

                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), period});

                if (cursor.moveToFirst()) {
                    totalBudget = cursor.getDouble(cursor.getColumnIndex(TOT_AMOUNT_COL));
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e("ExpenseDb", "Error getting total budget: " + e.getMessage());
        } finally {
            db.close();
        }

        return totalBudget;
    }

    // Method to check if category budget is within limits of total budget
    public boolean validateCategoryBudget(int userId, double newBudgetAmount) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isValid = true;

        try {
            // Check if total_budget table exists
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='total_budget'",
                    null
            );
            boolean tableExists = tableCheck.moveToFirst();
            tableCheck.close();

            if (!tableExists) {
                // If table doesn't exist, allow any budget (no constraint)
                db.close();
                return true;
            }

            // Get total budget
            double totalBudget = getTotalBudget(userId, "monthly");

            // If no total budget is set, allow any category budget
            if (totalBudget <= 0) {
                db.close();
                return true;
            }

            // Get sum of existing category budgets excluding the current category
            String query = "SELECT SUM(" + BUD_AMOUNT_COL + ") as total FROM " + TABLE_BUDGET +
                    " WHERE " + BUD_USER_ID_COL + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            double currentTotalCategoryBudgets = 0;
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") double sum = cursor.getDouble(cursor.getColumnIndex("total"));
                currentTotalCategoryBudgets = sum;
            }

            cursor.close();

            // Check if adding this budget would exceed the total budget
            isValid = (currentTotalCategoryBudgets + newBudgetAmount) <= totalBudget;
        } catch (Exception e) {
            Log.e("ExpenseDb", "Error validating category budget: " + e.getMessage());
            isValid = true; // Allow budget if there's an error checking
        } finally {
            db.close();
        }

        return isValid;
    }

    // Method to check category balance before adding expense
    public boolean checkCategoryBudgetBalance(int userId, int categoryId, double expenseAmount) {
        if (categoryId == -1) return true; // Special case for "All Categories"

        SQLiteDatabase db = this.getReadableDatabase();
        boolean hasBalance = true;

        try {
            // Check if total_budget table exists
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='total_budget'",
                    null
            );
            boolean tableExists = tableCheck.moveToFirst();
            tableCheck.close();

            if (!tableExists) {
                // If budget features aren't enabled yet, allow all expenses
                return true;
            }

            // Rest of the method remains the same...
            // Get current month in format YYYY-MM
            Calendar cal = Calendar.getInstance();
            String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            // Get category budget amount
            String budgetQuery = "SELECT " + BUD_AMOUNT_COL + " FROM " + TABLE_BUDGET +
                    " WHERE " + BUD_USER_ID_COL + " = ? AND " +
                    BUD_CAT_ID_COL + " = ?";

            Cursor budgetCursor = db.rawQuery(budgetQuery,
                    new String[]{String.valueOf(userId), String.valueOf(categoryId)});

            double budgetAmount = 0;
            if (budgetCursor.moveToFirst()) {
                @SuppressLint("Range") double amount = budgetCursor.getDouble(budgetCursor.getColumnIndex(BUD_AMOUNT_COL));
                budgetAmount = amount;
            }
            budgetCursor.close();

            // Get current spending for this category in this month
            double spent = getTotalExpensesByCategoryAndMonth(userId, categoryId, currentMonth);

            // Check if adding this expense would exceed the category budget
            hasBalance = (spent + expenseAmount) <= budgetAmount;
        } catch (Exception e) {
            Log.e("ExpenseDb", "Error checking category budget balance: " + e.getMessage());
            hasBalance = true; // Allow expense if error checking
        } finally {
            db.close();
        }

        return hasBalance;
    }

    // Helper method to get remaining budget amount for a category
    public double getRemainingCategoryBudget(int userId, int categoryId) {
        // Get current month
        Calendar cal = Calendar.getInstance();
        String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

        // Get category budget
        double budget = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT " + BUD_AMOUNT_COL + " FROM " + TABLE_BUDGET +
                    " WHERE " + BUD_USER_ID_COL + " = ? AND " +
                    BUD_CAT_ID_COL + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(categoryId)});

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(BUD_AMOUNT_COL));
                budget = amount;
            }
            cursor.close();

            // Get expenses for this category
            double spent = getTotalExpensesByCategoryAndMonth(userId, categoryId, currentMonth);

            return budget - spent;
        } catch (Exception e) {
            Log.e(TAG, "Error getting remaining category budget: " + e.getMessage());
            return 0; // Default to 0 in case of error
        } finally {
            db.close();
        }
    }

    // Method to get remaining total budget
    public double getRemainingTotalBudget(int userId) {
        // Current implementation that could crash:
        // double totalBudget = getTotalBudget(userId, "monthly");
        // double totalSpent = getTotalExpensesByMonth(userId, currentMonth);
        // return totalBudget - totalSpent;

        // Revised implementation with safety check:
        double totalBudget = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Check if table exists first
            Cursor tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='total_budget'",
                    null
            );
            boolean tableExists = tableCheck.moveToFirst();
            tableCheck.close();

            if (tableExists) {
                // Only try to get total budget if table exists
                totalBudget = getTotalBudget(userId, "monthly");
            }

            // Get current month
            Calendar cal = Calendar.getInstance();
            String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            // Get total expenses for this month
            double totalSpent = getTotalExpensesByMonth(userId, currentMonth);

            db.close();
            return totalBudget - totalSpent;
        } catch (Exception e) {
            Log.e("ExpenseDb", "Error calculating remaining budget: " + e.getMessage());
            db.close();
            return 0; // Return 0 as fallback
        }
    }

    // Method for updating an existing budget with a specific ID
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long updateOrInsertBudget(int userId, int categoryId, double amount, String period, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Check if a budget already exists for this user, category, and period
            String selection = BUD_USER_ID_COL + " = ? AND " +
                    BUD_CAT_ID_COL + " = ? AND " +
                    BUD_PERIOD_COL + " = ?";
            String[] selectionArgs = {String.valueOf(userId), String.valueOf(categoryId), period};

            Cursor cursor = db.query(TABLE_BUDGET, new String[]{BUD_ID_COL}, selection, selectionArgs, null, null, null);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zoneDt = ZonedDateTime.now();
            String currentDate = dtf.format(zoneDt);

            long result;
            if (cursor.moveToFirst()) {
                // Budget exists, update it
                @SuppressLint("Range") int budgetId = cursor.getInt(cursor.getColumnIndex(BUD_ID_COL));
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(BUD_AMOUNT_COL, amount);
                values.put(BUD_START_DATE_COL, startDate);
                if (endDate != null && !endDate.isEmpty()) {
                    values.put(BUD_END_DATE_COL, endDate);
                }
                values.put(BUD_UPDATED_AT, currentDate);

                String whereClause = BUD_ID_COL + " = ?";
                String[] whereArgs = {String.valueOf(budgetId)};

                result = db.update(TABLE_BUDGET, values, whereClause, whereArgs);
            } else {
                // Budget doesn't exist, insert a new one
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(BUD_USER_ID_COL, userId);
                values.put(BUD_CAT_ID_COL, categoryId);
                values.put(BUD_AMOUNT_COL, amount);
                values.put(BUD_PERIOD_COL, period);
                values.put(BUD_START_DATE_COL, startDate);
                if (endDate != null && !endDate.isEmpty()) {
                    values.put(BUD_END_DATE_COL, endDate);
                }
                values.put(BUD_CREATED_AT, currentDate);
                values.put(BUD_UPDATED_AT, currentDate);

                result = db.insert(TABLE_BUDGET, null, values);
            }

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error updating/inserting budget: " + e.getMessage());
            return -1;
        } finally {
            db.close();
        }
    }
}