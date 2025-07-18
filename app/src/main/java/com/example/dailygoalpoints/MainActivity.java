package com.example.dailygoalpoints;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView currentPointsText;
    private TextView todayPointsText;
    private Button plusButton;
    private Button minusButton;
    private LineChart lineChart;

    private DatabaseHelper dbHelper;
    private int todayPoints = 0;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Get current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = sdf.format(new Date());

        // Initialize views
        initializeViews();

        // Load today's points
        loadTodayPoints();

        // Setup button listeners
        setupButtonListeners();

        // Load and display graph
        loadGraph();
    }

    private void initializeViews() {
        currentPointsText = findViewById(R.id.currentPointsText);
        todayPointsText = findViewById(R.id.todayPointsText);
        plusButton = findViewById(R.id.plusButton);
        minusButton = findViewById(R.id.minusButton);
        lineChart = findViewById(R.id.lineChart);
    }

    private void loadTodayPoints() {
        todayPoints = dbHelper.getTodayPoints(currentDate);
        updatePointsDisplay();
    }

    private void setupButtonListeners() {
        plusButton.setOnClickListener(v -> {
            todayPoints++;
            updatePointsInDatabase();
            updatePointsDisplay();
            loadGraph();
            // Update all widgets when points change
            WidgetUpdateHelper.updateAllWidgets(this);
        });

        minusButton.setOnClickListener(v -> {
            todayPoints--;
            updatePointsInDatabase();
            updatePointsDisplay();
            loadGraph();
            // Update all widgets when points change
            WidgetUpdateHelper.updateAllWidgets(this);
        });
    }

    private void updatePointsInDatabase() {
        dbHelper.insertOrUpdateDailyPoints(currentDate, todayPoints);
    }

    private void updatePointsDisplay() {
        todayPointsText.setText("Today's Points: " + todayPoints);

        // Calculate total points from all days
        int totalPoints = dbHelper.getTotalPoints();
        currentPointsText.setText("Total Points: " + totalPoints);
    }

    private void loadGraph() {
        List<DailyPoint> dailyPoints = dbHelper.getLast30DaysPoints();

        if (dailyPoints.isEmpty()) {
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        for (int i = 0; i < dailyPoints.size(); i++) {
            DailyPoint point = dailyPoints.get(i);
            entries.add(new Entry(i, point.getPoints()));

            // Format date for display (MM-dd)
            String[] dateParts = point.getDate().split("-");
            dates.add(dateParts[1] + "-" + dateParts[2]);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Points");
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);

        // Customize chart
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Refresh chart
        lineChart.invalidate();
    }
}