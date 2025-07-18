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
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphWidgetProvider extends AppWidgetProvider {

    private static final int DAILY_GOAL = 10; // Default daily goal

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        // Get current points
        int currentPoints = databaseHelper.getTodayPoints();
        
        // Create RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_graph);

        // Update current points display
        views.setTextViewText(R.id.graph_widget_current_points, String.valueOf(currentPoints));

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
        views.setTextColor(R.id.graph_widget_current_points, pointsColor);

        // Calculate and display statistics
        updateStatistics(views, databaseHelper);

        // Create main graph
        Bitmap graphBitmap = createMainGraph(context, databaseHelper);
        if (graphBitmap != null) {
            views.setImageViewBitmap(R.id.graph_widget_main_chart, graphBitmap);
        }

        // Set up click intent to open main app
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.graph_widget_container, openAppPendingIntent);

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void updateStatistics(RemoteViews views, DatabaseHelper databaseHelper) {
        List<DailyPoint> pointsList = databaseHelper.getAllPoints();
        
        if (pointsList.isEmpty()) {
            views.setTextViewText(R.id.graph_widget_avg_text, "0.0");
            views.setTextViewText(R.id.graph_widget_streak_text, "0");
            return;
        }

        // Calculate average
        double sum = 0;
        for (DailyPoint point : pointsList) {
            sum += point.getPoints();
        }
        double average = sum / pointsList.size();
        views.setTextViewText(R.id.graph_widget_avg_text, String.format(Locale.getDefault(), "%.1f", average));

        // Calculate streak (consecutive days with positive points)
        int streak = 0;
        for (int i = pointsList.size() - 1; i >= 0; i--) {
            if (pointsList.get(i).getPoints() > 0) {
                streak++;
            } else {
                break;
            }
        }
        views.setTextViewText(R.id.graph_widget_streak_text, String.valueOf(streak));
    }

    private static Bitmap createMainGraph(Context context, DatabaseHelper databaseHelper) {
        List<DailyPoint> pointsList = databaseHelper.getAllPoints();

        if (pointsList.isEmpty()) {
            return createEmptyMainGraphBitmap();
        }

        // Create bitmap for main graph
        int width = 320;
        int height = 200;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Clear canvas
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
        goalLinePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{8, 4}, 0));

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAntiAlias(true);
        gridPaint.setStyle(Paint.Style.STROKE);

        if (pointsList.size() < 2) {
            // If only one point, draw a circle
            float x = width / 2f;
            float y = height / 2f;
            canvas.drawCircle(x, y, 8f, pointPaint);
            return bitmap;
        }

        // Calculate min and max points for scaling
        int minPoints = pointsList.get(0).getPoints();
        int maxPoints = pointsList.get(0).getPoints();

        for (DailyPoint point : pointsList) {
            minPoints = Math.min(minPoints, point.getPoints());
            maxPoints = Math.max(maxPoints, point.getPoints());
        }

        // Include goal line and zero line in scaling
        minPoints = Math.min(minPoints, 0);
        maxPoints = Math.max(maxPoints, DAILY_GOAL);

        // Add padding to the range
        int range = maxPoints - minPoints;
        if (range == 0) range = 1;
        
        // Add 20% padding
        int padding = range / 5;
        minPoints -= padding;
        maxPoints += padding;
        range = maxPoints - minPoints;

        // Take last 30 days or all available data
        int maxDays = 30;
        int startIndex = Math.max(0, pointsList.size() - maxDays);
        List<DailyPoint> recentPoints = pointsList.subList(startIndex, pointsList.size());

        // Draw grid lines
        int numGridLines = 5;
        for (int i = 0; i <= numGridLines; i++) {
            float y = 20 + (float) i / numGridLines * (height - 40);
            canvas.drawLine(20, y, width - 20, y, gridPaint);
        }

        // Draw goal line
        float goalY = height - 20 - ((float) (DAILY_GOAL - minPoints) / range * (height - 40));
        canvas.drawLine(20, goalY, width - 20, goalY, goalLinePaint);

        // Draw zero line if needed
        if (minPoints < 0 && maxPoints > 0) {
            float zeroY = height - 20 - ((float) (0 - minPoints) / range * (height - 40));
            Paint zeroLinePaint = new Paint();
            zeroLinePaint.setColor(Color.parseColor("#757575"));
            zeroLinePaint.setStrokeWidth(1f);
            zeroLinePaint.setAntiAlias(true);
            zeroLinePaint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(20, zeroY, width - 20, zeroY, zeroLinePaint);
        }

        // Create paths
        Path linePath = new Path();
        Path fillPath = new Path();
        boolean first = true;

        for (int i = 0; i < recentPoints.size(); i++) {
            DailyPoint point = recentPoints.get(i);

            float x = 20 + (float) i / (recentPoints.size() - 1) * (width - 40);
            float y = height - 20 - ((float) (point.getPoints() - minPoints) / range * (height - 40));

            if (first) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, height - 20);
                fillPath.lineTo(x, y);
                first = false;
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            // Draw point with different colors and sizes based on goal achievement
            Paint currentPointPaint = new Paint(pointPaint);
            float radius = 4f;
            
            if (point.getPoints() >= DAILY_GOAL) {
                currentPointPaint.setColor(Color.parseColor("#4CAF50")); // Green for goal achieved
                radius = 6f; // Larger for achievements
            } else if (point.getPoints() < 0) {
                currentPointPaint.setColor(Color.parseColor("#F44336")); // Red for negative
            }
            
            canvas.drawCircle(x, y, radius, currentPointPaint);
        }

        // Complete fill path
        if (recentPoints.size() > 1) {
            fillPath.lineTo(width - 20, height - 20);
            fillPath.close();

            // Draw fill area and line
            canvas.drawPath(fillPath, fillPaint);
            canvas.drawPath(linePath, linePaint);
        }

        return bitmap;
    }

    private static Bitmap createEmptyMainGraphBitmap() {
        int width = 320;
        int height = 200;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        canvas.drawColor(Color.TRANSPARENT);
        
        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#757575"));
        textPaint.setTextSize(32f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("Start tracking to see", width / 2f, height / 2f - 20, textPaint);
        canvas.drawText("your progress here!", width / 2f, height / 2f + 20, textPaint);
        
        return bitmap;
    }
}
