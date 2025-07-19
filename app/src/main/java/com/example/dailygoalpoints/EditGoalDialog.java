package com.example.dailygoalpoints;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

public class EditGoalDialog extends Dialog {

    private EditText etDailyGoal;
    private MaterialButton btnCancel, btnSave;
    
    private int currentGoal;
    private OnGoalUpdatedListener listener;

    public interface OnGoalUpdatedListener {
        void onGoalUpdated(int newGoal);
    }

    public EditGoalDialog(@NonNull Context context, int currentGoal, OnGoalUpdatedListener listener) {
        super(context);
        this.currentGoal = currentGoal;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_goal);
        
        initializeViews();
        populateFields();
        setupClickListeners();
    }

    private void initializeViews() {
        etDailyGoal = findViewById(R.id.et_daily_goal);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void populateFields() {
        etDailyGoal.setText(String.valueOf(currentGoal));
        etDailyGoal.selectAll(); // Select all text for easy editing
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                int newGoal = Integer.parseInt(etDailyGoal.getText().toString().trim());
                if (listener != null) {
                    listener.onGoalUpdated(newGoal);
                }
                dismiss();
            }
        });
    }

    private boolean validateInput() {
        String goalText = etDailyGoal.getText().toString().trim();

        if (goalText.isEmpty()) {
            etDailyGoal.setError("Goal is required");
            return false;
        }

        try {
            int goal = Integer.parseInt(goalText);
            if (goal <= 0) {
                etDailyGoal.setError("Goal must be positive");
                return false;
            }
            if (goal > 10000) {
                etDailyGoal.setError("Goal too high");
                return false;
            }
        } catch (NumberFormatException e) {
            etDailyGoal.setError("Invalid number");
            return false;
        }

        return true;
    }
}
