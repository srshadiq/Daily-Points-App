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
    private static final int DATABASE_VERSION = 3; // Updated version for new columns

    // Table names
    private static final String TABLE_DAILY_POINTS = "daily_points";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_GOALS = "goals";
    private static final String TABLE_TASK_COMPLETIONS = "task_completions";

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
                + TASK_IS_ACTIVE + " INTEGER DEFAULT 1"
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

        // Insert default goal
        insertDefaultGoal(db);
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
        values.put("manual_points", Math.max(0, currentManualPoints - 1)); // Don't go below 0
        values.put("task_points", currentTaskPoints);
        values.put(COLUMN_POINTS, Math.max(0, currentManualPoints - 1) + currentTaskPoints);

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

    // ==== TASK MANAGEMENT METHODS ====

    // Add a new task
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TASK_TITLE, task.getTitle());
            values.put(TASK_DESCRIPTION, task.getDescription());
            values.put(TASK_POINTS, task.getPoints());
            values.put(TASK_CATEGORY, task.getCategory());
            values.put(TASK_PRIORITY, task.getPriority());
            values.put(TASK_CREATED_DATE, getCurrentDate());
            values.put(TASK_IS_ACTIVE, 1);

            return db.insert(TABLE_TASKS, null, values);
        } finally {
            db.close();
        }
    }

    // Get all active tasks
    public List<Task> getAllActiveTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
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
                    
                    // Check if task is completed today
                    task.setCompleted(isTaskCompletedToday(task.getId()));
                    
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            db.close();
        }
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
        try {
            // Mark task as inactive instead of deleting
            ContentValues values = new ContentValues();
            values.put(TASK_IS_ACTIVE, 0);
            db.update(TABLE_TASKS, values, TASK_ID + "=?", new String[]{String.valueOf(taskId)});
        } finally {
            db.close();
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