package com.example.dailygoalpoints;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "daily_points.db";
    private static final int DATABASE_VERSION = 5; // Updated version for frequency feature

    // Table names
    private static final String TABLE_DAILY_POINTS = "daily_points";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_GOALS = "goals";
    private static final String TABLE_TASK_COMPLETIONS = "task_completions";
    private static final String TABLE_APP_SETTINGS = "app_settings";

    // Column names for daily_points
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_POINTS = "points";

    // Column names for tasks
    private static final String TASK_ID = "task_id";
    private static final String TASK_TITLE = "task_title";
    private static final String TASK_DESCRIPTION = "task_description";
    private static final String TASK_POINTS = "task_points";
    private static final String TASK_CATEGORY = "task_category";
    private static final String TASK_PRIORITY = "task_priority";
    private static final String TASK_CREATED_DATE = "task_created_date";
    private static final String TASK_IS_ACTIVE = "task_is_active";
    private static final String TASK_FREQUENCY = "task_frequency";
    private static final String TASK_CUSTOM_DAYS = "task_custom_days";

    // Column names for goals
    private static final String GOAL_ID = "goal_id";
    private static final String GOAL_TITLE = "goal_title";
    private static final String GOAL_DESCRIPTION = "goal_description";
    private static final String GOAL_DAILY_TARGET = "goal_daily_target";
    private static final String GOAL_START_DATE = "goal_start_date";
    private static final String GOAL_END_DATE = "goal_end_date";
    private static final String GOAL_IS_ACTIVE = "goal_is_active";

    // Column names for task_completions
    private static final String COMPLETION_ID = "completion_id";
    private static final String COMPLETION_TASK_ID = "completion_task_id";
    private static final String COMPLETION_DATE = "completion_date";
    private static final String COMPLETION_STATUS = "completion_status"; // 1 for completed, 0 for missed

    // Column names for app_settings
    private static final String SETTING_ID = "setting_id";
    private static final String SETTING_KEY = "setting_key";
    private static final String SETTING_VALUE = "setting_value";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create daily_points table with separate manual and task points
        String CREATE_DAILY_POINTS_TABLE = "CREATE TABLE " + TABLE_DAILY_POINTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT UNIQUE,"
                + COLUMN_POINTS + " INTEGER DEFAULT 0,"
                + "manual_points INTEGER DEFAULT 0,"
                + "task_points INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_DAILY_POINTS_TABLE);

        // Create tasks table
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TASK_TITLE + " TEXT NOT NULL,"
                + TASK_DESCRIPTION + " TEXT,"
                + TASK_POINTS + " INTEGER NOT NULL,"
                + TASK_CATEGORY + " TEXT,"
                + TASK_PRIORITY + " INTEGER DEFAULT 2,"
                + TASK_CREATED_DATE + " TEXT NOT NULL,"
                + TASK_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + TASK_FREQUENCY + " TEXT DEFAULT 'once',"
                + TASK_CUSTOM_DAYS + " TEXT DEFAULT ''"
                + ")";
        db.execSQL(CREATE_TASKS_TABLE);

        // Create goals table
        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                + GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GOAL_TITLE + " TEXT NOT NULL,"
                + GOAL_DESCRIPTION + " TEXT,"
                + GOAL_DAILY_TARGET + " INTEGER NOT NULL,"
                + GOAL_START_DATE + " TEXT NOT NULL,"
                + GOAL_END_DATE + " TEXT,"
                + GOAL_IS_ACTIVE + " INTEGER DEFAULT 1"
                + ")";
        db.execSQL(CREATE_GOALS_TABLE);

        // Create task_completions table
        String CREATE_COMPLETIONS_TABLE = "CREATE TABLE " + TABLE_TASK_COMPLETIONS + "("
                + COMPLETION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COMPLETION_TASK_ID + " INTEGER NOT NULL,"
                + COMPLETION_DATE + " TEXT NOT NULL,"
                + COMPLETION_STATUS + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + COMPLETION_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + TASK_ID + "),"
                + "UNIQUE(" + COMPLETION_TASK_ID + ", " + COMPLETION_DATE + ")"
                + ")";
        db.execSQL(CREATE_COMPLETIONS_TABLE);

        // Create app_settings table
        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_APP_SETTINGS + "("
                + SETTING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SETTING_KEY + " TEXT NOT NULL UNIQUE,"
                + SETTING_VALUE + " TEXT NOT NULL"
                + ")";
        db.execSQL(CREATE_SETTINGS_TABLE);

        // Insert default goal and app start date
        insertDefaultGoal(db);
        setAppStartDate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Create new tables for version 2
            String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                    + TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TASK_TITLE + " TEXT NOT NULL,"
                    + TASK_DESCRIPTION + " TEXT,"
                    + TASK_POINTS + " INTEGER NOT NULL,"
                    + TASK_CATEGORY + " TEXT,"
                    + TASK_PRIORITY + " INTEGER DEFAULT 2,"
                    + TASK_CREATED_DATE + " TEXT NOT NULL,"
                    + TASK_IS_ACTIVE + " INTEGER DEFAULT 1"
                    + ")";
            db.execSQL(CREATE_TASKS_TABLE);

            String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                    + GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + GOAL_TITLE + " TEXT NOT NULL,"
                    + GOAL_DESCRIPTION + " TEXT,"
                    + GOAL_DAILY_TARGET + " INTEGER NOT NULL,"
                    + GOAL_START_DATE + " TEXT NOT NULL,"
                    + GOAL_END_DATE + " TEXT,"
                    + GOAL_IS_ACTIVE + " INTEGER DEFAULT 1"
                    + ")";
            db.execSQL(CREATE_GOALS_TABLE);

            String CREATE_COMPLETIONS_TABLE = "CREATE TABLE " + TABLE_TASK_COMPLETIONS + "("
                    + COMPLETION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COMPLETION_TASK_ID + " INTEGER NOT NULL,"
                    + COMPLETION_DATE + " TEXT NOT NULL,"
                    + COMPLETION_STATUS + " INTEGER NOT NULL,"
                    + "FOREIGN KEY(" + COMPLETION_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + TASK_ID + "),"
                    + "UNIQUE(" + COMPLETION_TASK_ID + ", " + COMPLETION_DATE + ")"
                    + ")";
            db.execSQL(CREATE_COMPLETIONS_TABLE);

            insertDefaultGoal(db);
        }
        
        if (oldVersion < 3) {
            // Add new columns for separating manual and task points
            try {
                db.execSQL("ALTER TABLE " + TABLE_DAILY_POINTS + " ADD COLUMN manual_points INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_DAILY_POINTS + " ADD COLUMN task_points INTEGER DEFAULT 0");
                
                // Migrate existing data: assume all current points are manual points
                db.execSQL("UPDATE " + TABLE_DAILY_POINTS + " SET manual_points = " + COLUMN_POINTS + ", task_points = 0");
            } catch (Exception e) {
                // Columns might already exist, ignore
            }
        }

        if (oldVersion < 4) {
            // Create app_settings table
            String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_APP_SETTINGS + "("
                    + SETTING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SETTING_KEY + " TEXT NOT NULL UNIQUE,"
                    + SETTING_VALUE + " TEXT NOT NULL"
                    + ")";
            db.execSQL(CREATE_SETTINGS_TABLE);
            
            // Set app start date
            setAppStartDate(db);
        }

        if (oldVersion < 5) {
            // Add frequency columns to tasks table
            try {
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + TASK_FREQUENCY + " TEXT DEFAULT 'once'");
                db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + TASK_CUSTOM_DAYS + " TEXT DEFAULT ''");
            } catch (Exception e) {
                // Columns might already exist, ignore
            }
        }
    }

    private void insertDefaultGoal(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(GOAL_TITLE, "Daily Progress");
        values.put(GOAL_DESCRIPTION, "Complete daily tasks to reach your goal");
        values.put(GOAL_DAILY_TARGET, 10);
        values.put(GOAL_START_DATE, getCurrentDate());
        values.put(GOAL_IS_ACTIVE, 1);
        db.insert(TABLE_GOALS, null, values);
    }

    private void setAppStartDate(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SETTING_KEY, "app_start_date");
        values.put(SETTING_VALUE, getCurrentDate());
        db.insertWithOnConflict(TABLE_APP_SETTINGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // Add a point to today's total
    public synchronized void addPoint() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        // Get current manual and task points
        int[] currentPoints = getManualAndTaskPoints(today);
        int currentManualPoints = currentPoints[0];
        int currentTaskPoints = currentPoints[1];
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, today);
        values.put("manual_points", currentManualPoints + 1);
        values.put("task_points", currentTaskPoints);
        values.put(COLUMN_POINTS, currentManualPoints + 1 + currentTaskPoints);

        db.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Subtract a point from today's total
    public synchronized void subtractPoint() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        // Get current manual and task points
        int[] currentPoints = getManualAndTaskPoints(today);
        int currentManualPoints = currentPoints[0];
        int currentTaskPoints = currentPoints[1];
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, today);
        values.put("manual_points", currentManualPoints - 1); // Allow negative points
        values.put("task_points", currentTaskPoints);
        values.put(COLUMN_POINTS, (currentManualPoints - 1) + currentTaskPoints);

        db.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Get today's points
    public int getTodayPoints() {
        return getTodayPoints(getCurrentDate());
    }

    // Get points for a specific date
    public int getTodayPoints(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAILY_POINTS, new String[]{COLUMN_POINTS},
                COLUMN_DATE + "=?", new String[]{date}, null, null, null);

        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        cursor.close();
        return points;
    }

    // Get manual and task points separately [manual_points, task_points]
    private int[] getManualAndTaskPoints(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAILY_POINTS, new String[]{"manual_points", "task_points"},
                COLUMN_DATE + "=?", new String[]{date}, null, null, null);

        int[] points = {0, 0}; // [manual, task]
        if (cursor.moveToFirst()) {
            points[0] = cursor.getInt(0); // manual_points
            points[1] = cursor.getInt(1); // task_points
        }
        cursor.close();
        return points;
    }

    // Get all daily points for the graph
    public List<DailyPoint> getAllPoints() {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DAILY_POINTS, null, null, null, null, null, COLUMN_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                DailyPoint point = new DailyPoint();
                point.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                point.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS)));
                pointsList.add(point);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pointsList;
    }

    // Get last 30 days points for the graph
    public List<DailyPoint> getLast30DaysPoints() {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get last 30 days of data, ordered by date descending, then reverse it
        String selectQuery = "SELECT * FROM " + TABLE_DAILY_POINTS +
                " ORDER BY " + COLUMN_DATE + " DESC LIMIT 30";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DailyPoint point = new DailyPoint();
                point.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                point.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS)));
                pointsList.add(point);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Reverse the list to get chronological order (oldest to newest)
        java.util.Collections.reverse(pointsList);

        return pointsList;
    }

    // Get last 7 days points
    public List<DailyPoint> getLast7DaysPoints() {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_DAILY_POINTS +
                " ORDER BY " + COLUMN_DATE + " DESC LIMIT 7";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DailyPoint point = new DailyPoint();
                point.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                point.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS)));
                pointsList.add(point);
            } while (cursor.moveToNext());
        }
        cursor.close();

        java.util.Collections.reverse(pointsList);
        return pointsList;
    }

    // Get last N months points
    public List<DailyPoint> getLastNMonthsPoints(int months) {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Calculate date N months ago
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, -months);
        String dateNMonthsAgo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        String selectQuery = "SELECT * FROM " + TABLE_DAILY_POINTS +
                " WHERE " + COLUMN_DATE + " >= '" + dateNMonthsAgo + "'" +
                " ORDER BY " + COLUMN_DATE + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DailyPoint point = new DailyPoint();
                point.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                point.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS)));
                pointsList.add(point);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return pointsList;
    }

    // Get last N years points
    public List<DailyPoint> getLastNYearsPoints(int years) {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Calculate date N years ago
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -years);
        String dateNYearsAgo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        String selectQuery = "SELECT * FROM " + TABLE_DAILY_POINTS +
                " WHERE " + COLUMN_DATE + " >= '" + dateNYearsAgo + "'" +
                " ORDER BY " + COLUMN_DATE + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DailyPoint point = new DailyPoint();
                point.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                point.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS)));
                pointsList.add(point);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return pointsList;
    }

    // Get all time points
    public List<DailyPoint> getAllTimePoints() {
        return getAllPoints(); // This already exists and gets all points
    }

    // Insert or update daily points
    public void insertOrUpdateDailyPoints(String date, int points) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_POINTS, points);

        db.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Get total points from all days
    public int getTotalPoints() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT SUM(" + COLUMN_POINTS + ") FROM " + TABLE_DAILY_POINTS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int totalPoints = 0;
        if (cursor.moveToFirst()) {
            totalPoints = cursor.getInt(0);
        }
        cursor.close();
        return totalPoints;
    }

    // Get current date in YYYY-MM-DD format
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ==== APP SETTINGS METHODS ====

    // Get app setting value
    public String getAppSetting(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APP_SETTINGS, new String[]{SETTING_VALUE},
                SETTING_KEY + "=?", new String[]{key}, null, null, null);
        
        String value = null;
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        cursor.close();
        return value;
    }

    // Set app setting value
    public void setAppSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SETTING_KEY, key);
        values.put(SETTING_VALUE, value);
        db.insertWithOnConflict(TABLE_APP_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Get app start date
    public String getAppStartDate() {
        String startDate = getAppSetting("app_start_date");
        return startDate != null ? startDate : getCurrentDate(); // Fallback to today if not set
    }

    // Check if a date is on or after the app start date
    public boolean isDateOnOrAfterAppStart(String date) {
        String appStartDate = getAppStartDate();
        return date.compareTo(appStartDate) >= 0;
    }

    // ==== TASK MANAGEMENT METHODS ====

    // Add a new task
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TASK_TITLE, task.getTitle());
        values.put(TASK_DESCRIPTION, task.getDescription());
        values.put(TASK_POINTS, task.getPoints());
        values.put(TASK_CATEGORY, task.getCategory());
        values.put(TASK_PRIORITY, task.getPriority());
        values.put(TASK_CREATED_DATE, getCurrentDate());
        values.put(TASK_IS_ACTIVE, 1);
        values.put(TASK_FREQUENCY, task.getFrequency());
        values.put(TASK_CUSTOM_DAYS, task.getCustomDays());

        return db.insert(TABLE_TASKS, null, values);
    }

    // Update an existing task
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TASK_TITLE, task.getTitle());
        values.put(TASK_DESCRIPTION, task.getDescription());
        values.put(TASK_POINTS, task.getPoints());
        values.put(TASK_CATEGORY, task.getCategory());
        values.put(TASK_PRIORITY, task.getPriority());
        values.put(TASK_FREQUENCY, task.getFrequency());
        values.put(TASK_CUSTOM_DAYS, task.getCustomDays());
        // Don't update created_date or is_active when updating

        return db.update(TABLE_TASKS, values, TASK_ID + "=?", 
                new String[]{String.valueOf(task.getId())});
    }

    // Get all active tasks
    public List<Task> getAllActiveTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, null, 
                TASK_IS_ACTIVE + "=?", new String[]{"1"}, 
                null, null, TASK_PRIORITY + " ASC, " + TASK_TITLE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TASK_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TASK_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TASK_DESCRIPTION)));
                task.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(TASK_POINTS)));
                task.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(TASK_CATEGORY)));
                task.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(TASK_PRIORITY)));
                task.setDateCreated(cursor.getString(cursor.getColumnIndexOrThrow(TASK_CREATED_DATE)));
                
                // Read frequency columns (with defaults for old records)
                int frequencyIndex = cursor.getColumnIndex(TASK_FREQUENCY);
                if (frequencyIndex != -1) {
                    task.setFrequency(cursor.getString(frequencyIndex));
                } else {
                    task.setFrequency("once"); // Default for old records
                }
                
                int customDaysIndex = cursor.getColumnIndex(TASK_CUSTOM_DAYS);
                if (customDaysIndex != -1) {
                    task.setCustomDays(cursor.getString(customDaysIndex));
                } else {
                    task.setCustomDays(""); // Default for old records
                }
                
                // Check if task is completed today
                task.setCompleted(isTaskCompletedToday(task.getId()));
                
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    // Mark task as completed/uncompleted for today
    public synchronized void toggleTaskCompletion(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        // Check if completion record exists
        Cursor cursor = db.query(TABLE_TASK_COMPLETIONS, null,
                COMPLETION_TASK_ID + "=? AND " + COMPLETION_DATE + "=?",
                new String[]{String.valueOf(taskId), today}, null, null, null);

        boolean recordExists = cursor.moveToFirst();
        boolean currentStatus = recordExists && cursor.getInt(cursor.getColumnIndexOrThrow(COMPLETION_STATUS)) == 1;
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COMPLETION_TASK_ID, taskId);
        values.put(COMPLETION_DATE, today);
        values.put(COMPLETION_STATUS, currentStatus ? 0 : 1);

        db.insertWithOnConflict(TABLE_TASK_COMPLETIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        // Update daily points
        updateDailyPointsFromTasks();
    }

    // Check if task is completed today
    public boolean isTaskCompletedToday(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();

        Cursor cursor = db.query(TABLE_TASK_COMPLETIONS, new String[]{COMPLETION_STATUS},
                COMPLETION_TASK_ID + "=? AND " + COMPLETION_DATE + "=?",
                new String[]{String.valueOf(taskId), today}, null, null, null);

        boolean isCompleted = false;
        if (cursor.moveToFirst()) {
            isCompleted = cursor.getInt(0) == 1;
        }
        cursor.close();
        return isCompleted;
    }

    // Update daily points based on task completions
    private void updateDailyPointsFromTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();

        // Get current manual points (preserve them)
        int[] currentPoints = getManualAndTaskPoints(today);
        int currentManualPoints = currentPoints[0];
        
        // Calculate new task points from completed tasks
        String query = "SELECT SUM(t." + TASK_POINTS + ") FROM " + TABLE_TASKS + " t " +
                "INNER JOIN " + TABLE_TASK_COMPLETIONS + " tc ON t." + TASK_ID + " = tc." + COMPLETION_TASK_ID + " " +
                "WHERE tc." + COMPLETION_DATE + " = ? AND tc." + COMPLETION_STATUS + " = 1 AND t." + TASK_IS_ACTIVE + " = 1";

        Cursor cursor = db.rawQuery(query, new String[]{today});
        int newTaskPoints = 0;
        if (cursor.moveToFirst()) {
            newTaskPoints = cursor.getInt(0);
        }
        cursor.close();

        // Update with preserved manual points + new task points
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, today);
        values.put("manual_points", currentManualPoints);
        values.put("task_points", newTaskPoints);
        values.put(COLUMN_POINTS, currentManualPoints + newTaskPoints);

        SQLiteDatabase writeDb = this.getWritableDatabase();
        writeDb.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Get today's task points only
    private int getTodayTaskPoints(String date) {
        int[] points = getManualAndTaskPoints(date);
        return points[1]; // Return task points
    }

    // Store today's task points (for tracking purposes)
    private void updateTodayTaskPoints(String date, int taskPoints) {
        // This method is now handled within updateDailyPointsFromTasks
        // Keeping for compatibility but no longer needed
    }

    // Delete a task
    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Mark task as inactive instead of deleting
        ContentValues values = new ContentValues();
        values.put(TASK_IS_ACTIVE, 0);
        db.update(TABLE_TASKS, values, TASK_ID + "=?", new String[]{String.valueOf(taskId)});
    }

    // Clean up "once" tasks at end of day (remove completed ones)
    public void cleanupOnceTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();
        
        // Find all "once" frequency tasks that are completed today
        String query = "SELECT DISTINCT t." + TASK_ID + " FROM " + TABLE_TASKS + " t " +
                "INNER JOIN " + TABLE_TASK_COMPLETIONS + " tc ON t." + TASK_ID + " = tc." + COMPLETION_TASK_ID + " " +
                "WHERE t." + TASK_FREQUENCY + " = 'once' AND tc." + COMPLETION_DATE + " = ? AND tc." + COMPLETION_STATUS + " = 1";
        
        Cursor cursor = db.rawQuery(query, new String[]{today});
        
        if (cursor.moveToFirst()) {
            do {
                int taskId = cursor.getInt(0);
                // Mark as inactive (delete)
                ContentValues values = new ContentValues();
                values.put(TASK_IS_ACTIVE, 0);
                db.update(TABLE_TASKS, values, TASK_ID + "=?", new String[]{String.valueOf(taskId)});
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Get tasks visible for today based on frequency
    public List<Task> getTodaysTasks() {
        List<Task> allTasks = getAllActiveTasks();
        List<Task> todaysTasks = new ArrayList<>();
        
        // Get current day of week (1=Sunday, 2=Monday, ..., 7=Saturday)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        
        for (Task task : allTasks) {
            if (task.isVisibleOnDay(dayOfWeek)) {
                todaysTasks.add(task);
            }
        }
        
        return todaysTasks;
    }

    // ==== END-OF-DAY PROCESSING ====

    // Process end-of-day penalties for uncompleted tasks
    public synchronized void processEndOfDayPenalties(String date) {
        // Only process dates on or after the app start date
        if (!isDateOnOrAfterAppStart(date)) {
            return; // Skip penalty processing for dates before app start
        }
        
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Get all active tasks that were NOT completed on the given date
        String query = "SELECT t." + TASK_ID + ", t." + TASK_POINTS + " FROM " + TABLE_TASKS + " t " +
                "WHERE t." + TASK_IS_ACTIVE + " = 1 " +
                "AND t." + TASK_ID + " NOT IN (" +
                "SELECT tc." + COMPLETION_TASK_ID + " FROM " + TABLE_TASK_COMPLETIONS + " tc " +
                "WHERE tc." + COMPLETION_DATE + " = ? AND tc." + COMPLETION_STATUS + " = 1)";

        Cursor cursor = db.rawQuery(query, new String[]{date});
        int totalPenalty = 0;

        if (cursor.moveToFirst()) {
            do {
                int taskPoints = cursor.getInt(1);
                totalPenalty += taskPoints; // Add points that will be deducted
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Apply penalty if there are uncompleted tasks
        if (totalPenalty > 0) {
            applyDailyPenalty(date, totalPenalty);
        }
    }

    // Apply penalty points for uncompleted tasks
    private void applyDailyPenalty(String date, int penaltyPoints) {
        // Get current manual and task points
        int[] currentPoints = getManualAndTaskPoints(date);
        int currentManualPoints = currentPoints[0];
        int currentTaskPoints = currentPoints[1];
        
        // Apply penalty to manual points (user's responsibility)
        int newManualPoints = currentManualPoints - penaltyPoints;
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put("manual_points", newManualPoints);
        values.put("task_points", currentTaskPoints);
        values.put(COLUMN_POINTS, newManualPoints + currentTaskPoints);

        SQLiteDatabase writeDb = this.getWritableDatabase();
        writeDb.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        
        // Log penalty for user feedback
        logPenaltyApplication(date, penaltyPoints);
    }
    
    // Log penalty application for user awareness
    private void logPenaltyApplication(String date, int penaltyPoints) {
        // This could be enhanced to show a notification or toast
        // For now, we'll just keep it simple
        System.out.println("Penalty applied for " + date + ": -" + penaltyPoints + " points for incomplete tasks");
    }

    // Check if end-of-day processing has been done for a date
    public boolean hasEndOfDayProcessingBeenDone(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Check if there are any tasks for this date
        Cursor taskCursor = db.query(TABLE_TASKS, new String[]{TASK_ID},
                TASK_IS_ACTIVE + "=1", null, null, null, null);
        boolean hasTasks = taskCursor.getCount() > 0;
        taskCursor.close();
        
        if (!hasTasks) {
            return true; // No tasks to process
        }
        
        // Check if the date has a daily points record
        Cursor cursor = db.query(TABLE_DAILY_POINTS, new String[]{"manual_points", "task_points"},
                COLUMN_DATE + "=?", new String[]{date}, null, null, null);

        boolean processed = false;
        if (cursor.moveToFirst()) {
            // If there's a record for this date, consider it processed
            // In a more robust system, we could add a "penalties_applied" flag
            processed = true;
        }
        cursor.close();
        return processed;
    }

    // Auto-process end-of-day penalties (can be called when app starts)
    public void autoProcessPreviousDays() {
        String today = getCurrentDate();
        String appStartDate = getAppStartDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        try {
            Date currentDate = sdf.parse(today);
            Date appStart = sdf.parse(appStartDate);
            Date processingDate = new Date(currentDate.getTime() - 24 * 60 * 60 * 1000); // Yesterday
            
            // Process up to 7 previous days, but not before app start date
            for (int i = 0; i < 7; i++) {
                String dateString = sdf.format(processingDate);
                
                // Only process if not already done, it's not today, and it's on or after app start
                if (!dateString.equals(today) && 
                    processingDate.compareTo(appStart) >= 0 && 
                    !hasEndOfDayProcessingBeenDone(dateString)) {
                    processEndOfDayPenalties(dateString);
                }
                
                // Move to previous day
                processingDate = new Date(processingDate.getTime() - 24 * 60 * 60 * 1000);
                
                // Stop if we've gone before the app start date
                if (processingDate.before(appStart)) {
                    break;
                }
            }
        } catch (Exception e) {
            // Handle date parsing errors
            e.printStackTrace();
        }
    }

    // Manually process end-of-day penalties for a specific date
    public void manualProcessEndOfDay(String date) {
        if (!date.equals(getCurrentDate()) && isDateOnOrAfterAppStart(date)) {
            processEndOfDayPenalties(date);
        }
    }

    // Get penalty points that would be applied for a given date (for preview)
    public int getPenaltyPreview(String date) {
        // Only show penalty for dates on or after app start
        if (!isDateOnOrAfterAppStart(date)) {
            return 0;
        }
        
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT t." + TASK_POINTS + " FROM " + TABLE_TASKS + " t " +
                "WHERE t." + TASK_IS_ACTIVE + " = 1 " +
                "AND t." + TASK_ID + " NOT IN (" +
                "SELECT tc." + COMPLETION_TASK_ID + " FROM " + TABLE_TASK_COMPLETIONS + " tc " +
                "WHERE tc." + COMPLETION_DATE + " = ? AND tc." + COMPLETION_STATUS + " = 1)";

        Cursor cursor = db.rawQuery(query, new String[]{date});
        int totalPenalty = 0;

        if (cursor.moveToFirst()) {
            do {
                int taskPoints = cursor.getInt(0);
                totalPenalty += taskPoints;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return totalPenalty;
    }
    
    // Get penalty summary for the last N days
    public List<PenaltySummary> getRecentPenalties(int days) {
        List<PenaltySummary> penalties = new ArrayList<>();
        String today = getCurrentDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        try {
            Date currentDate = sdf.parse(today);
            
            for (int i = 1; i <= days; i++) {
                Date checkDate = new Date(currentDate.getTime() - (i * 24 * 60 * 60 * 1000));
                String dateString = sdf.format(checkDate);
                
                // Only check dates on or after app start
                if (isDateOnOrAfterAppStart(dateString)) {
                    int penalty = getPenaltyPreview(dateString);
                    if (penalty > 0) {
                        penalties.add(new PenaltySummary(dateString, penalty));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return penalties;
    }
    
    // Helper class for penalty summary
    public static class PenaltySummary {
        private String date;
        private int penaltyPoints;
        
        public PenaltySummary(String date, int penaltyPoints) {
            this.date = date;
            this.penaltyPoints = penaltyPoints;
        }
        
        public String getDate() { return date; }
        public int getPenaltyPoints() { return penaltyPoints; }
        
        public String getFormattedDate() {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                Date date = inputFormat.parse(this.date);
                return outputFormat.format(date);
            } catch (Exception e) {
                return date;
            }
        }
    }

    // ==== GOAL MANAGEMENT METHODS ====

    // Add a new goal
    public long addGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(GOAL_TITLE, goal.getTitle());
            values.put(GOAL_DESCRIPTION, goal.getDescription());
            values.put(GOAL_DAILY_TARGET, goal.getDailyTarget());
            values.put(GOAL_START_DATE, getCurrentDate());
            values.put(GOAL_IS_ACTIVE, 1);

            return db.insert(TABLE_GOALS, null, values);
        } finally {
            db.close();
        }
    }

    // Get current active goal
    public Goal getCurrentGoal() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE_GOALS, null,
                    GOAL_IS_ACTIVE + "=?", new String[]{"1"},
                    null, null, GOAL_ID + " DESC", "1");

            Goal goal = null;
            if (cursor.moveToFirst()) {
                goal = new Goal();
                goal.setId(cursor.getInt(cursor.getColumnIndexOrThrow(GOAL_ID)));
                goal.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(GOAL_TITLE)));
                goal.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(GOAL_DESCRIPTION)));
                goal.setDailyTarget(cursor.getInt(cursor.getColumnIndexOrThrow(GOAL_DAILY_TARGET)));
                goal.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(GOAL_START_DATE)));
                goal.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(GOAL_END_DATE)));
                goal.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(GOAL_IS_ACTIVE)) == 1);
            }
            cursor.close();
            return goal;
        } finally {
            db.close();
        }
    }

    // Update goal
    public void updateGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(GOAL_TITLE, goal.getTitle());
            values.put(GOAL_DESCRIPTION, goal.getDescription());
            values.put(GOAL_DAILY_TARGET, goal.getDailyTarget());
            
            db.update(TABLE_GOALS, values, GOAL_ID + "=?", new String[]{String.valueOf(goal.getId())});
        } finally {
            db.close();
        }
    }

    // Get daily target from current goal
    public int getDailyTarget() {
        Goal currentGoal = getCurrentGoal();
        return currentGoal != null ? currentGoal.getDailyTarget() : 10; // Default to 10
    }
}