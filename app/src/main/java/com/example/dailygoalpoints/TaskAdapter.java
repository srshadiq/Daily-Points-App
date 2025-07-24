package com.example.dailygoalpoints;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskActionListener onTaskActionListener;

    public interface OnTaskActionListener {
        void onTaskToggle(Task task);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskActionListener listener) {
        this.tasks = tasks;
        this.onTaskActionListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxCompleted;
        private TextView tvTaskTitle;
        private TextView tvTaskDescription;
        private TextView tvTaskPoints;
        private TextView tvTaskCategory;
        private TextView tvTaskPriority;
        private View priorityIndicator;
        private ImageView btnEdit;
        private ImageView btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskPoints = itemView.findViewById(R.id.tv_task_points);
            tvTaskCategory = itemView.findViewById(R.id.tv_task_category);
            tvTaskPriority = itemView.findViewById(R.id.tv_task_priority);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Task task) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskDescription.setText(task.getDescription());
            
            // Simple points display
            tvTaskPoints.setText("+" + task.getPoints() + " points");
            
            // Set color based on completion status
            if (task.isCompleted()) {
                tvTaskPoints.setTextColor(itemView.getContext().getResources().getColor(R.color.success_color));
            } else {
                tvTaskPoints.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
            }
            
            tvTaskCategory.setText(task.getCategory() != null ? task.getCategory() : "General");
            tvTaskPriority.setText(task.getPriorityText());
            checkboxCompleted.setChecked(task.isCompleted());

            // Set priority indicator color and priority text color
            int priorityColor;
            switch (task.getPriority()) {
                case 1: // High priority
                    priorityColor = itemView.getContext().getResources().getColor(R.color.priority_high);
                    break;
                case 2: // Medium priority
                    priorityColor = itemView.getContext().getResources().getColor(R.color.priority_medium);
                    break;
                case 3: // Low priority
                    priorityColor = itemView.getContext().getResources().getColor(R.color.priority_low);
                    break;
                default:
                    priorityColor = itemView.getContext().getResources().getColor(R.color.priority_medium);
                    break;
            }
            priorityIndicator.setBackgroundColor(priorityColor);
            tvTaskPriority.setTextColor(priorityColor);

            // Set click listeners
            checkboxCompleted.setOnClickListener(v -> {
                if (onTaskActionListener != null) {
                    onTaskActionListener.onTaskToggle(task);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (onTaskActionListener != null) {
                    onTaskActionListener.onTaskEdit(task);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (onTaskActionListener != null) {
                    onTaskActionListener.onTaskDelete(task);
                }
            });

            itemView.setOnClickListener(v -> {
                if (onTaskActionListener != null) {
                    onTaskActionListener.onTaskToggle(task);
                }
            });

            // Update UI based on completion status
            float alpha = task.isCompleted() ? 0.6f : 1.0f;
            tvTaskTitle.setAlpha(alpha);
            tvTaskDescription.setAlpha(alpha);
            tvTaskPoints.setAlpha(alpha);
            tvTaskCategory.setAlpha(alpha);
        }
    }
}
