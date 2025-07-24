package com.example.dailygoalpoints;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvCurrentPoints, tvGoalTitle, tvDailyTarget, tvProgressMessage;
    private TextView tvTotalPoints, tvStreakCount, tvAveragePoints, tvTasksCompleted, tvChartPeriod;
    private ProgressBar progressDailyGoal;
    private MaterialButton btnAddPoint, btnSubtractPoint, btnViewAllTasks;
    private ImageView ivChartMenu;
    private LineChart lineChart;
    private RecyclerView rvTaskPreview;

    private DatabaseHelper databaseHelper;
    private TaskPreviewAdapter taskPreviewAdapter;
    private OnPageNavigationListener navigationListener;
    private OnDataChangeListener dataChangeListener;
    
    // Chart time period enum
    private enum TimePeriod {
        SEVEN_DAYS, ONE_MONTH, THREE_MONTHS, ONE_YEAR, ALL_TIME
    }
    
    private TimePeriod currentTimePeriod = TimePeriod.SEVEN_DAYS;

    public interface OnPageNavigationListener {
        void navigateToTasks();
    }

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public void setNavigationListener(OnPageNavigationListener listener) {
        this.navigationListener = listener;
    }

    public void setDataChangeListener(OnDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        
        databaseHelper = new DatabaseHelper(getContext());
        updateChartPeriodDisplay(); // Set initial period display
        loadData();
    }

    private void initializeViews(View view) {
        tvCurrentPoints = view.findViewById(R.id.tv_current_points);
        tvGoalTitle = view.findViewById(R.id.tv_goal_title);
        tvDailyTarget = view.findViewById(R.id.tv_daily_target);
        tvProgressMessage = view.findViewById(R.id.tv_progress_message);
        tvTotalPoints = view.findViewById(R.id.tv_total_points);
        tvStreakCount = view.findViewById(R.id.tv_streak_count);
        tvAveragePoints = view.findViewById(R.id.tv_average_points);
        tvTasksCompleted = view.findViewById(R.id.tv_tasks_completed);
        tvChartPeriod = view.findViewById(R.id.tv_chart_period);
        
        progressDailyGoal = view.findViewById(R.id.progress_daily_goal);
        
        btnAddPoint = view.findViewById(R.id.btn_add_point);
        btnSubtractPoint = view.findViewById(R.id.btn_subtract_point);
        btnViewAllTasks = view.findViewById(R.id.btn_view_all_tasks);
        
        // Chart menu icon
        ivChartMenu = view.findViewById(R.id.iv_chart_menu);
        
        lineChart = view.findViewById(R.id.line_chart);
        rvTaskPreview = view.findViewById(R.id.rv_task_preview);
    }

    private void setupRecyclerView() {
        taskPreviewAdapter = new TaskPreviewAdapter(new ArrayList<>(), task -> {
            databaseHelper.toggleTaskCompletion(task.getId());
            loadData(); // Refresh data
            if (dataChangeListener != null) {
                dataChangeListener.onDataChanged(); // Notify MainActivity to update TasksFragment
            }
            WidgetUpdateHelper.updateAllWidgets(getContext());
        });
        
        rvTaskPreview.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTaskPreview.setAdapter(taskPreviewAdapter);
        rvTaskPreview.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        btnAddPoint.setOnClickListener(v -> {
            databaseHelper.addPoint();
            loadData();
            if (dataChangeListener != null) {
                dataChangeListener.onDataChanged(); // Notify MainActivity
            }
            WidgetUpdateHelper.updateAllWidgets(getContext());
        });

        btnSubtractPoint.setOnClickListener(v -> {
            databaseHelper.subtractPoint();
            loadData();
            if (dataChangeListener != null) {
                dataChangeListener.onDataChanged(); // Notify MainActivity
            }
            WidgetUpdateHelper.updateAllWidgets(getContext());
        });

        btnViewAllTasks.setOnClickListener(v -> {
            if (navigationListener != null) {
                navigationListener.navigateToTasks();
            }
        });
        
        // Chart menu click listener
        ivChartMenu.setOnClickListener(v -> showChartPeriodMenu(v));
    }

    private void loadData() {
        // Load current goal
        Goal currentGoal = databaseHelper.getCurrentGoal();
        if (currentGoal != null) {
            tvGoalTitle.setText(currentGoal.getTitle());
            tvDailyTarget.setText("/ " + currentGoal.getDailyTarget());
        }

        // Load current points
        int currentPoints = databaseHelper.getTodayPoints();
        tvCurrentPoints.setText(String.valueOf(currentPoints));
        
        // Style negative current points differently
        if (currentPoints < 0) {
            tvCurrentPoints.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            tvCurrentPoints.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        }

        // Update progress
        int dailyTarget = databaseHelper.getDailyTarget();
        int progressPercentage;
        if (currentPoints < 0) {
            progressPercentage = 0; // Don't show negative progress
        } else {
            progressPercentage = Math.min(100, (currentPoints * 100) / dailyTarget);
        }
        progressDailyGoal.setProgress(progressPercentage);

        // Update progress message
        updateProgressMessage(currentPoints, dailyTarget);

        // Load total points
        int totalPoints = databaseHelper.getTotalPoints();
        tvTotalPoints.setText(String.valueOf(totalPoints));
        
        // Style negative points differently
        if (totalPoints < 0) {
            tvTotalPoints.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            tvTotalPoints.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }

        // Calculate and display streak
        int streak = calculateStreak();
        tvStreakCount.setText(String.valueOf(streak));

        // Load tasks preview
        List<Task> tasks = databaseHelper.getTodaysTasks();
        int completedTasks = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) completedTasks++;
        }
        tvTasksCompleted.setText(completedTasks + "/" + tasks.size() + " completed");

        // Show only first 3 tasks in preview
        List<Task> previewTasks = tasks.size() > 3 ? tasks.subList(0, 3) : tasks;
        taskPreviewAdapter.updateTasks(previewTasks);

        // Load chart
        loadChart();
    }

    private void updateProgressMessage(int currentPoints, int dailyTarget) {
        // Check if there are recent penalties to inform the user
        List<DatabaseHelper.PenaltySummary> recentPenalties = databaseHelper.getRecentPenalties(1);
        boolean hasRecentPenalty = !recentPenalties.isEmpty();
        
        String message;
        if (currentPoints < 0) {
            if (hasRecentPenalty) {
                message = "⚠️ Negative points from task penalties! Complete today's tasks to recover!";
            } else {
                message = "⚠️ Negative points! Complete tasks to recover!";
            }
        } else if (currentPoints >= dailyTarget) {
            message = "🎉 Amazing! Goal achieved for today!";
        } else if (currentPoints >= dailyTarget * 0.8) {
            message = "🔥 Almost there! Keep pushing!";
        } else if (currentPoints >= dailyTarget * 0.5) {
            message = "💪 Great progress! You're halfway there!";
        } else if (currentPoints > 0) {
            if (hasRecentPenalty) {
                message = "🚀 Good start! Remember: incomplete tasks result in penalties!";
            } else {
                message = "🚀 Good start! Keep going!";
            }
        } else {
            if (hasRecentPenalty) {
                message = "⭐ New day, fresh start! Complete tasks to avoid penalties!";
            } else {
                message = "⭐ Ready to start your day? Let's go!";
            }
        }
        tvProgressMessage.setText(message);
    }

    private int calculateStreak() {
        List<DailyPoint> points = databaseHelper.getAllPoints();
        if (points.isEmpty()) return 0;

        int streak = 0;
        int dailyTarget = databaseHelper.getDailyTarget();
        
        for (int i = points.size() - 1; i >= 0; i--) {
            if (points.get(i).getPoints() >= dailyTarget) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private void showChartPeriodMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        
        // Add menu items
        popup.getMenu().add(0, 0, 0, "7 Days");
        popup.getMenu().add(0, 1, 1, "1 Month");
        popup.getMenu().add(0, 2, 2, "3 Months");
        popup.getMenu().add(0, 3, 3, "1 Year");
        popup.getMenu().add(0, 4, 4, "All Time");
        
        // Set current selection as checked
        MenuItem currentItem = null;
        switch (currentTimePeriod) {
            case SEVEN_DAYS:
                currentItem = popup.getMenu().getItem(0);
                break;
            case ONE_MONTH:
                currentItem = popup.getMenu().getItem(1);
                break;
            case THREE_MONTHS:
                currentItem = popup.getMenu().getItem(2);
                break;
            case ONE_YEAR:
                currentItem = popup.getMenu().getItem(3);
                break;
            case ALL_TIME:
                currentItem = popup.getMenu().getItem(4);
                break;
        }
        if (currentItem != null) {
            currentItem.setChecked(true);
        }
        
        // Set click listener
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    currentTimePeriod = TimePeriod.SEVEN_DAYS;
                    break;
                case 1:
                    currentTimePeriod = TimePeriod.ONE_MONTH;
                    break;
                case 2:
                    currentTimePeriod = TimePeriod.THREE_MONTHS;
                    break;
                case 3:
                    currentTimePeriod = TimePeriod.ONE_YEAR;
                    break;
                case 4:
                    currentTimePeriod = TimePeriod.ALL_TIME;
                    break;
            }
            updateChartPeriodDisplay();
            loadChart();
            return true;
        });
        
        popup.show();
    }

    private void updateChartPeriodDisplay() {
        String periodText;
        switch (currentTimePeriod) {
            case SEVEN_DAYS:
                periodText = "7 Days";
                break;
            case ONE_MONTH:
                periodText = "1 Month";
                break;
            case THREE_MONTHS:
                periodText = "3 Months";
                break;
            case ONE_YEAR:
                periodText = "1 Year";
                break;
            case ALL_TIME:
                periodText = "All Time";
                break;
            default:
                periodText = "7 Days";
                break;
        }
        tvChartPeriod.setText(periodText);
    }

    private void loadChart() {
        List<DailyPoint> dailyPoints = getPointsForCurrentPeriod();
        
        if (dailyPoints.isEmpty()) {
            lineChart.clear();
            tvAveragePoints.setText("Avg: 0.0");
            return;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        double totalPoints = 0;

        for (int i = 0; i < dailyPoints.size(); i++) {
            DailyPoint point = dailyPoints.get(i);
            entries.add(new Entry(i, point.getPoints()));
            totalPoints += point.getPoints();

            // Format date based on time period
            dates.add(formatDateForPeriod(point.getDate()));
        }

        // Calculate and display average
        double average = totalPoints / dailyPoints.size();
        tvAveragePoints.setText(String.format(Locale.getDefault(), "Avg: %.1f", average));

        LineDataSet dataSet = new LineDataSet(entries, "Daily Points");
        dataSet.setColor(getResources().getColor(R.color.primary_color));
        dataSet.setCircleColor(getResources().getColor(R.color.primary_color));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_color));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        
        // Adjust label count based on data size
        if (dailyPoints.size() > 30) {
            xAxis.setLabelCount(Math.min(10, dailyPoints.size()), false);
        } else {
            xAxis.setLabelCount(Math.min(dailyPoints.size(), 10), false);
        }

        // Customize chart
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getLegend().setEnabled(false);

        // Refresh chart
        lineChart.invalidate();
    }

    private List<DailyPoint> getPointsForCurrentPeriod() {
        switch (currentTimePeriod) {
            case SEVEN_DAYS:
                return databaseHelper.getLast7DaysPoints();
            case ONE_MONTH:
                return databaseHelper.getLastNMonthsPoints(1);
            case THREE_MONTHS:
                return databaseHelper.getLastNMonthsPoints(3);
            case ONE_YEAR:
                return databaseHelper.getLastNYearsPoints(1);
            case ALL_TIME:
                return databaseHelper.getAllTimePoints();
            default:
                return databaseHelper.getLast7DaysPoints();
        }
    }

    private String formatDateForPeriod(String date) {
        try {
            String[] dateParts = date.split("-");
            
            switch (currentTimePeriod) {
                case SEVEN_DAYS:
                    // For 7 days, show MM/dd format
                    return dateParts[1] + "/" + dateParts[2];
                case ONE_MONTH:
                    // For 1 month, show MM/dd format
                    return dateParts[1] + "/" + dateParts[2];
                case THREE_MONTHS:
                case ONE_YEAR:
                    // For longer periods, show MMM dd format
                    String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                     "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    int month = Integer.parseInt(dateParts[1]);
                    return months[month] + " " + dateParts[2];
                case ALL_TIME:
                    // For all time, show yyyy/MM format
                    return dateParts[0] + "/" + dateParts[1];
                default:
                    return dateParts[1] + "/" + dateParts[2];
            }
        } catch (Exception e) {
            return date;
        }
    }

    public void refreshData() {
        if (databaseHelper != null) {
            loadData();
        }
    }
}
