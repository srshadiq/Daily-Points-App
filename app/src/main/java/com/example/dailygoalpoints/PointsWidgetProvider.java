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

import java.util.List;

public class PointsWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_ADD_POINT = "com.example.dailygoalpoints.ADD_POINT";
    private static final String ACTION_SUBTRACT_POINT = "com.example.dailygoalpoints.SUBTRACT_POINT";

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
                new android.content.ComponentName(context, PointsWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        // Get current points
        int currentPoints = databaseHelper.getTodayPoints();

        // Create RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_points);

        // Update points display
        views.setTextViewText(R.id.widget_points_text, String.valueOf(currentPoints));

        // Set color based on points
        if (currentPoints > 0) {
            views.setTextColor(R.id.widget_points_text, Color.parseColor("#4CAF50")); // Green
        } else if (currentPoints < 0) {
            views.setTextColor(R.id.widget_points_text, Color.parseColor("#F44336")); // Red
        } else {
            views.setTextColor(R.id.widget_points_text, Color.parseColor("#2196F3")); // Blue
        }

        // Create mini graph
        Bitmap graphBitmap = createMiniGraph(context, databaseHelper);
        if (graphBitmap != null) {
            views.setImageViewBitmap(R.id.widget_mini_graph, graphBitmap);
        }

        // Set up click intents for buttons
        Intent addIntent = new Intent(context, PointsWidgetProvider.class);
        addIntent.setAction(ACTION_ADD_POINT);
        addIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent addPendingIntent = PendingIntent.getBroadcast(context, 
                appWidgetId * 1000 + 1, addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_plus, addPendingIntent);

        Intent subtractIntent = new Intent(context, PointsWidgetProvider.class);
        subtractIntent.setAction(ACTION_SUBTRACT_POINT);
        subtractIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent subtractPendingIntent = PendingIntent.getBroadcast(context, 
                appWidgetId * 1000 + 2, subtractIntent,
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

    private static Bitmap createMiniGraph(Context context, DatabaseHelper databaseHelper) {
        List<DailyPoint> pointsList = databaseHelper.getAllPoints();

        if (pointsList.isEmpty()) {
            return null;
        }

        // Create bitmap for mini graph
        int width = 200;
        int height = 100;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Clear canvas with transparent background
        canvas.drawColor(Color.TRANSPARENT);

        // Paint for the line
        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#2196F3"));
        linePaint.setStrokeWidth(3f);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);

        // Paint for points
        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#2196F3"));
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        if (pointsList.size() < 2) {
            // If only one point, draw a circle
            float x = width / 2f;
            float y = height / 2f;
            canvas.drawCircle(x, y, 4f, pointPaint);
            return bitmap;
        }

        // Calculate min and max points for scaling
        int minPoints = pointsList.get(0).getPoints();
        int maxPoints = pointsList.get(0).getPoints();

        for (DailyPoint point : pointsList) {
            minPoints = Math.min(minPoints, point.getPoints());
            maxPoints = Math.max(maxPoints, point.getPoints());
        }

        // Add some padding to the range
        int range = maxPoints - minPoints;
        if (range == 0) range = 1;

        // Create path for the line
        Path path = new Path();
        boolean first = true;

        // Take only the last 7 days for the mini graph
        int startIndex = Math.max(0, pointsList.size() - 7);
        List<DailyPoint> recentPoints = pointsList.subList(startIndex, pointsList.size());

        for (int i = 0; i < recentPoints.size(); i++) {
            DailyPoint point = recentPoints.get(i);

            float x = (float) i / (recentPoints.size() - 1) * (width - 20) + 10;
            float y = height - 10 - ((float) (point.getPoints() - minPoints) / range * (height - 20));

            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }

            // Draw point
            canvas.drawCircle(x, y, 3f, pointPaint);
        }

        // Draw the line
        canvas.drawPath(path, linePaint);

        return bitmap;
    }
}
