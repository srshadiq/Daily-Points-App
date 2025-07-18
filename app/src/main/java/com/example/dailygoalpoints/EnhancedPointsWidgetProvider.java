package com.example.dailygoalpoints;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnhancedPointsWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_ADD_POINT = "com.example.dailygoalpoints.ADD_POINT_ENHANCED";
    private static final String ACTION_SUBTRACT_POINT = "com.example.dailygoalpoints.SUBTRACT_POINT_ENHANCED";
    private static final int DAILY_GOAL = 10; // Default daily goal

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_ADD_POINT.equals(intent.getAction())) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.addPoint();
            // Update all widgets using the helper
            WidgetUpdateHelper.updateAllWidgets(context);
        } else if (ACTION_SUBTRACT_POINT.equals(intent.getAction())) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.subtractPoint();
            // Update all widgets using the helper
            WidgetUpdateHelper.updateAllWidgets(context);
        }
    }

    private void updateAllWidgets(Context context, AppWidgetManager appWidgetManager) {
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new android.content.ComponentName(context, EnhancedPointsWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        // Get current points
        int currentPoints = databaseHelper.getTodayPoints();

        // Create RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_points_enhanced);

        // Update current date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        views.setTextViewText(R.id.widget_date_text, currentDate);

        // Update points display
        views.setTextViewText(R.id.widget_points_text, String.valueOf(currentPoints));

        // Set color based on points
        int pointsColor;
        if (currentPoints >= DAILY_GOAL) {
            pointsColor = Color.parseColor("#4CAF50"); // Green - goal achieved
        } else if (currentPoints > 0) {
            pointsColor = Color.parseColor("#2196F3"); // Blue - positive progress
        } else if (currentPoints < 0) {
            pointsColor = Color.parseColor("#F44336"); // Red - negative
        } else {
            pointsColor = Color.parseColor("#757575"); // Gray - neutral
        }
        views.setTextColor(R.id.widget_points_text, pointsColor);

        // Update progress bar
        int progressPercentage = Math.max(0, Math.min(100, (currentPoints * 100) / DAILY_GOAL));
        views.setProgressBar(R.id.widget_progress_bar, 100, progressPercentage, false);

        // Update goal text
        views.setTextViewText(R.id.widget_goal_text, String.valueOf(DAILY_GOAL));

        // Create mini graph
        Bitmap graphBitmap = createEnhancedMiniGraph(context, databaseHelper);
        if (graphBitmap != null) {
            views.setImageViewBitmap(R.id.widget_mini_graph, graphBitmap);
        }

        // Update trend text
        String trendText = calculateTrendText(databaseHelper);
        views.setTextViewText(R.id.widget_trend_text, trendText);

        // Set up click intents for buttons
        Intent addIntent = new Intent(context, EnhancedPointsWidgetProvider.class);
        addIntent.setAction(ACTION_ADD_POINT);
        addIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent addPendingIntent = PendingIntent.getBroadcast(context, 
                appWidgetId * 2000 + 1, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_plus, addPendingIntent);

        Intent subtractIntent = new Intent(context, EnhancedPointsWidgetProvider.class);
        subtractIntent.setAction(ACTION_SUBTRACT_POINT);
        subtractIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent subtractPendingIntent = PendingIntent.getBroadcast(context, 
                appWidgetId * 2000 + 2, subtractIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_minus, subtractPendingIntent);

        // Set up click intent to open main app
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent);

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static Bitmap createEnhancedMiniGraph(Context context, DatabaseHelper databaseHelper) {
        List<DailyPoint> pointsList = databaseHelper.getAllPoints();

        if (pointsList.isEmpty()) {
            return createEmptyGraphBitmap();
        }

        // Create bitmap for mini graph
        int width = 240;
        int height = 120;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Clear canvas with transparent background
        canvas.drawColor(Color.TRANSPARENT);

        // Paints
        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#2196F3"));
        linePaint.setStrokeWidth(3f);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);

        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.parseColor("#402196F3")); // Semi-transparent blue
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);

        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#2196F3"));
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        Paint goalLinePaint = new Paint();
        goalLinePaint.setColor(Color.parseColor("#FF9800"));
        goalLinePaint.setStrokeWidth(2f);
        goalLinePaint.setAntiAlias(true);
        goalLinePaint.setStyle(Paint.Style.STROKE);
        goalLinePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{5, 5}, 0));

        if (pointsList.size() < 2) {
            // If only one point, draw a circle
            float x = width / 2f;
            float y = height / 2f;
            canvas.drawCircle(x, y, 6f, pointPaint);
            return bitmap;
        }

        // Calculate min and max points for scaling
        int minPoints = pointsList.get(0).getPoints();
        int maxPoints = pointsList.get(0).getPoints();

        for (DailyPoint point : pointsList) {
            minPoints = Math.min(minPoints, point.getPoints());
            maxPoints = Math.max(maxPoints, point.getPoints());
        }

        // Include goal line in scaling
        minPoints = Math.min(minPoints, 0);
        maxPoints = Math.max(maxPoints, DAILY_GOAL);

        // Add some padding to the range
        int range = maxPoints - minPoints;
        if (range == 0) range = 1;

        // Take only the last 7 days for the mini graph
        int startIndex = Math.max(0, pointsList.size() - 7);
        List<DailyPoint> recentPoints = pointsList.subList(startIndex, pointsList.size());

        // Create paths
        Path linePath = new Path();
        Path fillPath = new Path();
        boolean first = true;

        // Draw goal line
        float goalY = height - 10 - ((float) (DAILY_GOAL - minPoints) / range * (height - 20));
        canvas.drawLine(10, goalY, width - 10, goalY, goalLinePaint);

        for (int i = 0; i < recentPoints.size(); i++) {
            DailyPoint point = recentPoints.get(i);

            float x = (float) i / (recentPoints.size() - 1) * (width - 20) + 10;
            float y = height - 10 - ((float) (point.getPoints() - minPoints) / range * (height - 20));

            if (first) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height - 10);
                fillPath.lineTo(x, y);
                first = false;
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            // Draw point with different colors based on goal achievement
            Paint currentPointPaint = new Paint(pointPaint);
            if (point.getPoints() >= DAILY_GOAL) {
                currentPointPaint.setColor(Color.parseColor("#4CAF50")); // Green for goal achieved
            } else if (point.getPoints() < 0) {
                currentPointPaint.setColor(Color.parseColor("#F44336")); // Red for negative
            }
            
            canvas.drawCircle(x, y, 4f, currentPointPaint);
        }

        // Complete fill path
        fillPath.lineTo(width - 10, height - 10);
        fillPath.close();

        // Draw fill area and line
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        return bitmap;
    }

    private static Bitmap createEmptyGraphBitmap() {
        int width = 240;
        int height = 120;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        canvas.drawColor(Color.TRANSPARENT);
        
        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#757575"));
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("No data yet", width / 2f, height / 2f, textPaint);
        
        return bitmap;
    }

    private static String calculateTrendText(DatabaseHelper databaseHelper) {
        List<DailyPoint> pointsList = databaseHelper.getAllPoints();
        
        if (pointsList.size() < 2) {
            return "Start tracking!";
        }
        
        // Calculate trend over last 7 days
        int days = Math.min(7, pointsList.size());
        int startIndex = pointsList.size() - days;
        
        double sum = 0;
        for (int i = startIndex; i < pointsList.size(); i++) {
            sum += pointsList.get(i).getPoints();
        }
        
        double average = sum / days;
        
        if (average >= DAILY_GOAL) {
            return "Excellent progress! 🎯";
        } else if (average >= DAILY_GOAL * 0.7) {
            return "Good progress 📈";
        } else if (average > 0) {
            return "Keep going! 💪";
        } else {
            return "Time to boost! 🚀";
        }
    }
}
