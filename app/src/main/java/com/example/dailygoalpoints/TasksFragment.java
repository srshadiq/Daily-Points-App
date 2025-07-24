package com.example.dailygoalpoints;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private TextView tvDailyGoalTarget, tvGoalDescription;
    private TextView tvCurrentPoints, tvDailyTarget, tvProgressMessage, tvPenaltyMessage;
    private ProgressBar progressDailyGoal;
    private MaterialButton btnEditGoal, btnAddTask;
    private RecyclerView rvTasks;
    private LinearLayout layoutEmptyState;
    private CardView cardPenaltyInfo;

    private DatabaseHelper databaseHelper;
    private TaskAdapter taskAdapter;
    private OnDataChangeListener dataChangeListener;

    public interface OnDataChangeListener {
        void onDataChanged();
    }

    public void setDataChangeListener(OnDataChangeListener listener) {
        this.dataChangeListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        databaseHelper = new DatabaseHelper(getContext());
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initializeViews(View view) {
        tvDailyGoalTarget = view.findViewById(R.id.tv_daily_goal_target);
        tvGoalDescription = view.findViewById(R.id.tv_goal_description);
        tvCurrentPoints = view.findViewById(R.id.tv_current_points);
        tvDailyTarget = view.findViewById(R.id.tv_daily_target);
        tvProgressMessage = view.findViewById(R.id.tv_progress_message);
        progressDailyGoal = view.findViewById(R.id.progress_daily_goal);
        btnEditGoal = view.findViewById(R.id.btn_edit_goal);
        btnAddTask = view.findViewById(R.id.btn_add_task);
        rvTasks = view.findViewById(R.id.rv_tasks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
//        cardPenaltyInfo = view.findViewById(R.id.card_penalty_info);
//        tvPenaltyMessage = view.findViewById(R.id.tv_penalty_message);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskToggle(Task task) {
                databaseHelper.toggleTaskCompletion(task.getId());
                loadData();
                notifyDataChange();
                WidgetUpdateHelper.updateAllWidgets(getContext());
            }

            @Override
            public void onTaskEdit(Task task) {
                showEditTaskDialog(task);
            }

            @Override
            public void onTaskDelete(Task task) {
                showDeleteTaskDialog(task);
            }
        });

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupClickListeners() {
        if (btnAddTask != null) {
            btnAddTask.setOnClickListener(v -> showAddTaskDialog());
        }
        if (btnEditGoal != null) {
            btnEditGoal.setOnClickListener(v -> showEditGoalDialog());
        }
        
        // Long press on add task button to test penalty system (for development)
        if (btnAddTask != null) {
            btnAddTask.setOnLongClickListener(v -> {
                showPenaltyTestDialog();
                return true;
            });
        }
    }

    private void loadData() {
        // Add null check for safety
        if (databaseHelper == null) {
            return;
        }
        
        // Load current goal
        Goal currentGoal = databaseHelper.getCurrentGoal();
        if (currentGoal != null && tvDailyGoalTarget != null) {
            tvDailyGoalTarget.setText(String.valueOf(currentGoal.getDailyTarget()));
            tvGoalDescription.setText(currentGoal.getDescription());
            tvDailyTarget.setText(String.valueOf(currentGoal.getDailyTarget()));
        }

        // Load current points and update progress
        int currentPoints = databaseHelper.getTodayPoints();
        if (tvCurrentPoints != null) {
            tvCurrentPoints.setText(String.valueOf(currentPoints));
            
            // Style negative current points differently
            if (currentPoints < 0) {
                tvCurrentPoints.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            } else {
                tvCurrentPoints.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            }
        }

        // Update progress bar
        int dailyTarget = databaseHelper.getDailyTarget();
        int progressPercentage;
        if (currentPoints < 0) {
            progressPercentage = 0; // Don't show negative progress
        } else {
            progressPercentage = Math.min(100, (currentPoints * 100) / dailyTarget);
        }
        if (progressDailyGoal != null) {
            progressDailyGoal.setProgress(progressPercentage);
        }

        // Update progress message
        updateProgressMessage(currentPoints, dailyTarget);

        // Load tasks
        List<Task> tasks = databaseHelper.getTodaysTasks();
        if (taskAdapter != null) {
            taskAdapter.updateTasks(tasks);
        }

        // Show/hide empty state
        if (tasks.isEmpty()) {
            if (rvTasks != null) rvTasks.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
//            if (cardPenaltyInfo != null) cardPenaltyInfo.setVisibility(View.GONE);
        } else {
            if (rvTasks != null) rvTasks.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            
            // Show penalty warning only if there are incomplete tasks
//            updatePenaltyMessage(tasks);
        }
    }

    private void updateProgressMessage(int currentPoints, int dailyTarget) {
        if (tvProgressMessage == null) return;
        
        String message;
        if (currentPoints < 0) {
            message = "⚠️ Negative points! Complete tasks to recover!";
        } else if (currentPoints >= dailyTarget) {
            message = "🎉 Amazing! Goal achieved for today!";
        } else if (currentPoints >= dailyTarget * 0.8) {
            message = "🔥 Almost there! Keep pushing!";
        } else if (currentPoints >= dailyTarget * 0.5) {
            message = "💪 Great progress! You're halfway there!";
        } else if (currentPoints > 0) {
            message = "🚀 Good start! Keep going!";
        } else {
            message = "⭐ Ready to start your day? Let's go!";
        }
        tvProgressMessage.setText(message);
    }
    
    private void updatePenaltyMessage(List<Task> tasks) {
        if (tvPenaltyMessage == null || cardPenaltyInfo == null) return;
        
        int incompleteCount = 0;
        int totalPenaltyPoints = 0;
        
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                incompleteCount++;
                totalPenaltyPoints += task.getPoints();
            }
        }
        
        if (incompleteCount > 0) {
            String message = String.format("⚠️ %d incomplete task%s will result in -%d point%s penalty at day's end", 
                incompleteCount, 
                incompleteCount == 1 ? "" : "s",
                totalPenaltyPoints,
                totalPenaltyPoints == 1 ? "" : "s");
            tvPenaltyMessage.setText(message);
            cardPenaltyInfo.setVisibility(View.VISIBLE);
        } else {
            cardPenaltyInfo.setVisibility(View.GONE);
        }
    }

    private void showAddTaskDialog() {
        AddTaskDialog dialog = new AddTaskDialog(getContext(), task -> {
            databaseHelper.addTask(task);
            loadData();
            notifyDataChange();
        });
        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
        EditTaskDialog dialog = new EditTaskDialog(getContext(), task, updatedTask -> {
            // Update task in database
            databaseHelper.updateTask(updatedTask);
            loadData();
            notifyDataChange();
        });
        dialog.show();
    }

    private void showDeleteTaskDialog(Task task) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteTask(task.getId());
                    loadData();
                    notifyDataChange();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditGoalDialog() {
        Goal currentGoal = databaseHelper.getCurrentGoal();
        if (currentGoal == null) return;

        EditGoalDialog dialog = new EditGoalDialog(getContext(), currentGoal.getDailyTarget(), updatedGoalValue -> {
            currentGoal.setDailyTarget(updatedGoalValue);
            databaseHelper.updateGoal(currentGoal);
            loadData();
            notifyDataChange();
        });
        dialog.show();
    }

    private void notifyDataChange() {
        if (dataChangeListener != null) {
            dataChangeListener.onDataChanged();
        }
    }

    public void refreshData() {
        if (databaseHelper != null) {
            loadData();
        }
    }
    
    private void showPenaltyTestDialog() {
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("🧪 Development Tools")
            .setMessage("Choose a testing option:")
            .setPositiveButton("Apply Penalties", (dialog, which) -> {
                // Get yesterday's date
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                String yesterday = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
                
                // Apply penalties
                databaseHelper.manualProcessEndOfDay(yesterday);
                
                // Show result
                android.widget.Toast.makeText(getContext(), "Penalties applied for " + yesterday, android.widget.Toast.LENGTH_LONG).show();
                
                // Refresh data
                loadData();
                if (dataChangeListener != null) {
                    dataChangeListener.onDataChanged();
                }
            })
            .setNeutralButton("Reset Dialog Flag", (dialog, which) -> {
                // Reset the penalty dialog flag to allow showing it again today
                android.content.SharedPreferences prefs = getContext().getSharedPreferences("daily_dialogs", getContext().MODE_PRIVATE);
                prefs.edit().remove("penalty_dialog_last_shown").apply();
                android.widget.Toast.makeText(getContext(), "Penalty dialog flag reset - dialog will show on next app restart", android.widget.Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
