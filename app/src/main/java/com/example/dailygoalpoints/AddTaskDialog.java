package com.example.dailygoalpoints;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

public class AddTaskDialog extends Dialog {

    private EditText etTaskTitle, etTaskDescription, etTaskPoints;
    private Spinner spinnerCategory, spinnerPriority, spinnerFrequency;
    private LinearLayout layoutCustomDays;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private MaterialButton btnCancel, btnSave;
    
    private OnTaskCreatedListener listener;

    public interface OnTaskCreatedListener {
        void onTaskCreated(Task task);
    }

    public AddTaskDialog(@NonNull Context context, OnTaskCreatedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_task);
        
        initializeViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeViews() {
        etTaskTitle = findViewById(R.id.et_task_title);
        etTaskDescription = findViewById(R.id.et_task_description);
        etTaskPoints = findViewById(R.id.et_task_points);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerPriority = findViewById(R.id.spinner_priority);
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        layoutCustomDays = findViewById(R.id.layout_custom_days);
        cbMonday = findViewById(R.id.cb_monday);
        cbTuesday = findViewById(R.id.cb_tuesday);
        cbWednesday = findViewById(R.id.cb_wednesday);
        cbThursday = findViewById(R.id.cb_thursday);
        cbFriday = findViewById(R.id.cb_friday);
        cbSaturday = findViewById(R.id.cb_saturday);
        cbSunday = findViewById(R.id.cb_sunday);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupSpinners() {
        // Category spinner
        String[] categories = {"General", "Work", "Health", "Personal", "Learning", "Exercise"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Priority spinner
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default to Medium

        // Frequency spinner
        String[] frequencies = {"Once", "Daily", "Custom Days"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);
        
        // Setup frequency change listener
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) { // Custom Days selected
                    layoutCustomDays.setVisibility(View.VISIBLE);
                } else {
                    layoutCustomDays.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutCustomDays.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                Task task = createTaskFromInput();
                if (listener != null) {
                    listener.onTaskCreated(task);
                }
                dismiss();
            }
        });
    }

    private boolean validateInput() {
        String title = etTaskTitle.getText().toString().trim();
        String pointsText = etTaskPoints.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskTitle.setError("Title is required");
            return false;
        }

        if (pointsText.isEmpty()) {
            etTaskPoints.setError("Points are required");
            return false;
        }

        try {
            int points = Integer.parseInt(pointsText);
            if (points <= 0) {
                etTaskPoints.setError("Points must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            etTaskPoints.setError("Invalid number");
            return false;
        }

        return true;
    }

    private Task createTaskFromInput() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        int points = Integer.parseInt(etTaskPoints.getText().toString().trim());
        String category = spinnerCategory.getSelectedItem().toString();
        
        int priority;
        switch (spinnerPriority.getSelectedItemPosition()) {
            case 0: priority = 1; break; // High
            case 1: priority = 2; break; // Medium
            case 2: priority = 3; break; // Low
            default: priority = 2; break;
        }

        // Handle frequency
        String frequency;
        String customDays = "";
        
        switch (spinnerFrequency.getSelectedItemPosition()) {
            case 0: frequency = "once"; break;
            case 1: frequency = "daily"; break;
            case 2: 
                frequency = "custom";
                customDays = getSelectedCustomDays();
                break;
            default: frequency = "once"; break;
        }

        return new Task(title, description, points, category, priority, frequency, customDays);
    }

    private String getSelectedCustomDays() {
        StringBuilder days = new StringBuilder();
        
        if (cbMonday.isChecked()) days.append("2,");      // Monday = 2 in Calendar
        if (cbTuesday.isChecked()) days.append("3,");     // Tuesday = 3
        if (cbWednesday.isChecked()) days.append("4,");   // Wednesday = 4
        if (cbThursday.isChecked()) days.append("5,");    // Thursday = 5
        if (cbFriday.isChecked()) days.append("6,");      // Friday = 6
        if (cbSaturday.isChecked()) days.append("7,");    // Saturday = 7
        if (cbSunday.isChecked()) days.append("1,");      // Sunday = 1
        
        // Remove trailing comma
        if (days.length() > 0) {
            days.setLength(days.length() - 1);
        }
        
        return days.toString();
    }
}
