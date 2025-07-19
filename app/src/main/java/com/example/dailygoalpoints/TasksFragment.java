package com.example.dailygoalpoints;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TasksFragment extends Fragment {

    private TextView tvDailyGoalTarget, tvGoalDescription;
    private MaterialButton btnEditGoal, btnAddTask;
    private RecyclerView rvTasks;
    private LinearLayout layoutEmptyState;

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
        btnEditGoal = view.findViewById(R.id.btn_edit_goal);
        btnAddTask = view.findViewById(R.id.btn_add_task);
        rvTasks = view.findViewById(R.id.rv_tasks);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(databaseHelper.getAllActiveTasks(), new TaskAdapter.OnTaskActionListener() {
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
        btnAddTask.setOnClickListener(v -> showAddTaskDialog());
        btnEditGoal.setOnClickListener(v -> showEditGoalDialog());
    }

    private void loadData() {
        // Load current goal
        Goal currentGoal = databaseHelper.getCurrentGoal();
        if (currentGoal != null) {
            tvDailyGoalTarget.setText(String.valueOf(currentGoal.getDailyTarget()));
            tvGoalDescription.setText(currentGoal.getDescription());
        }

        // Load tasks
        List<Task> tasks = databaseHelper.getAllActiveTasks();
        taskAdapter.updateTasks(tasks);

        // Show/hide empty state
        if (tasks.isEmpty()) {
            rvTasks.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTasks.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
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
            // Update task in database (you'll need to add an updateTask method to DatabaseHelper)
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
}
