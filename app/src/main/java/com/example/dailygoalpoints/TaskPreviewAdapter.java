package com.example.dailygoalpoints;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskPreviewAdapter extends RecyclerView.Adapter<TaskPreviewAdapter.TaskPreviewViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener onTaskClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskPreviewAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.onTaskClickListener = listener;
    }

    @NonNull
    @Override
    public TaskPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_preview, parent, false);
        return new TaskPreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskPreviewViewHolder holder, int position) {
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

    class TaskPreviewViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxCompleted;
        private TextView tvTaskTitle;
        private TextView tvTaskPoints;
        private View priorityIndicator;

        public TaskPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskPoints = itemView.findViewById(R.id.tv_task_points);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
        }

        public void bind(Task task) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskPoints.setText("+" + task.getPoints() + " pts");
            checkboxCompleted.setChecked(task.isCompleted());

            // Set priority indicator color
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

            // Set click listeners
            checkboxCompleted.setOnClickListener(v -> {
                if (onTaskClickListener != null) {
                    onTaskClickListener.onTaskClick(task);
                }
            });

            itemView.setOnClickListener(v -> {
                if (onTaskClickListener != null) {
                    onTaskClickListener.onTaskClick(task);
                }
            });

            // Update UI based on completion status
            if (task.isCompleted()) {
                tvTaskTitle.setAlpha(0.6f);
                tvTaskPoints.setAlpha(0.6f);
            } else {
                tvTaskTitle.setAlpha(1.0f);
                tvTaskPoints.setAlpha(1.0f);
            }
        }
    }
}
