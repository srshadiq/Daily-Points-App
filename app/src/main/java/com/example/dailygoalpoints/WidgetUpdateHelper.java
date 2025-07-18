package com.example.dailygoalpoints;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

public class WidgetUpdateHelper {

    /**
     * Updates all instances of all widget types on the home screen
     * Call this method whenever points are updated to refresh all widgets
     */
    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        // Update original points widgets
        ComponentName originalWidgetComponent = new ComponentName(context, PointsWidgetProvider.class);
        int[] originalAppWidgetIds = appWidgetManager.getAppWidgetIds(originalWidgetComponent);
        for (int appWidgetId : originalAppWidgetIds) {
            PointsWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        
        // Update enhanced points widgets
        try {
            ComponentName enhancedWidgetComponent = new ComponentName(context, EnhancedPointsWidgetProvider.class);
            int[] enhancedAppWidgetIds = appWidgetManager.getAppWidgetIds(enhancedWidgetComponent);
            for (int appWidgetId : enhancedAppWidgetIds) {
                EnhancedPointsWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        } catch (Exception e) {
            // Enhanced widget might not be available
        }
        
        // Update graph widgets
        try {
            ComponentName graphWidgetComponent = new ComponentName(context, GraphWidgetProvider.class);
            int[] graphAppWidgetIds = appWidgetManager.getAppWidgetIds(graphWidgetComponent);
            for (int appWidgetId : graphAppWidgetIds) {
                GraphWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        } catch (Exception e) {
            // Graph widget might not be available
        }
    }
}
