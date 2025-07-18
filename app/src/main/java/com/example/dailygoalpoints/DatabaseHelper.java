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
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_DAILY_POINTS = "daily_points";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_POINTS = "points";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DAILY_POINTS_TABLE = "CREATE TABLE " + TABLE_DAILY_POINTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT UNIQUE,"
                + COLUMN_POINTS + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_DAILY_POINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_POINTS);
        onCreate(db);
    }

    // Add a point to today's total
    public synchronized void addPoint() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        try {
            // First get current points without closing the database
            int currentPoints = getTodayPointsWithDb(db, today);
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, today);
            values.put(COLUMN_POINTS, currentPoints + 1);

            db.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
    }

    // Subtract a point from today's total
    public synchronized void subtractPoint() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        try {
            // First get current points without closing the database
            int currentPoints = getTodayPointsWithDb(db, today);
            
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, today);
            values.put(COLUMN_POINTS, currentPoints - 1);

            db.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
    }

    // Get today's points
    public int getTodayPoints() {
        return getTodayPoints(getCurrentDate());
    }

    // Get points for a specific date
    public int getTodayPoints(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            return getTodayPointsWithDb(db, date);
        } finally {
            db.close();
        }
    }

    // Helper method to get points using an existing database connection
    private int getTodayPointsWithDb(SQLiteDatabase db, String date) {
        Cursor cursor = db.query(TABLE_DAILY_POINTS, new String[]{COLUMN_POINTS},
                COLUMN_DATE + "=?", new String[]{date}, null, null, null);

        int points = 0;
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0);
        }
        cursor.close();
        return points;
    }

    // Get all daily points for the graph
    public List<DailyPoint> getAllPoints() {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
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
        } finally {
            db.close();
        }
        return pointsList;
    }

    // Get last 30 days points for the graph
    public List<DailyPoint> getLast30DaysPoints() {
        List<DailyPoint> pointsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
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

        } finally {
            db.close();
        }
        return pointsList;
    }

    // Insert or update daily points
    public void insertOrUpdateDailyPoints(String date, int points) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_POINTS, points);

            db.insertWithOnConflict(TABLE_DAILY_POINTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
    }

    // Get total points from all days
    public int getTotalPoints() {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String selectQuery = "SELECT SUM(" + COLUMN_POINTS + ") FROM " + TABLE_DAILY_POINTS;
            Cursor cursor = db.rawQuery(selectQuery, null);

            int totalPoints = 0;
            if (cursor.moveToFirst()) {
                totalPoints = cursor.getInt(0);
            }
            cursor.close();
            return totalPoints;
        } finally {
            db.close();
        }
    }

    // Get current date in YYYY-MM-DD format
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}