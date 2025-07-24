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

public class EditTaskDialog extends Dialog {

    private EditText etTaskTitle, etTaskDescription, etTaskPoints;
    private Spinner spinnerCategory, spinnerPriority, spinnerFrequency;
    private LinearLayout layoutCustomDays;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private MaterialButton btnCancel, btnSave;
    
    private Task task;
    private OnTaskUpdatedListener listener;

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task task);
    }

    public EditTaskDialog(@NonNull Context context, Task task, OnTaskUpdatedListener listener) {
        super(context);
        this.task = task;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_task);
        
        initializeViews();
        setupSpinners();
        populateFields();
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

    private void populateFields() {
        if (task != null) {
            etTaskTitle.setText(task.getTitle());
            etTaskDescription.setText(task.getDescription());
            etTaskPoints.setText(String.valueOf(task.getPoints()));
            
            // Set category selection
            String[] categories = {"General", "Work", "Health", "Personal", "Learning", "Exercise"};
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(task.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            
            // Set priority selection
            int priorityIndex;
            switch (task.getPriority()) {
                case 1: priorityIndex = 0; break; // High
                case 2: priorityIndex = 1; break; // Medium
                case 3: priorityIndex = 2; break; // Low
                default: priorityIndex = 1; break;
            }
            spinnerPriority.setSelection(priorityIndex);
            
            // Set frequency selection
            int frequencyIndex;
            switch (task.getFrequency()) {
                case "once": frequencyIndex = 0; break;
                case "daily": frequencyIndex = 1; break;
                case "custom": frequencyIndex = 2; break;
                default: frequencyIndex = 0; break;
            }
            spinnerFrequency.setSelection(frequencyIndex);
            
            // If custom frequency, populate the custom days
            if ("custom".equals(task.getFrequency()) && task.getCustomDays() != null) {
                layoutCustomDays.setVisibility(View.VISIBLE);
                populateCustomDays(task.getCustomDays());
            } else {
                layoutCustomDays.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                updateTaskFromInput();
                if (listener != null) {
                    listener.onTaskUpdated(task);
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

    private void updateTaskFromInput() {
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

        task.setTitle(title);
        task.setDescription(description);
        task.setPoints(points);
        task.setCategory(category);
        task.setPriority(priority);
        task.setFrequency(frequency);
        task.setCustomDays(customDays);
    }

    private void populateCustomDays(String customDays) {
        // Reset all checkboxes first
        cbMonday.setChecked(false);
        cbTuesday.setChecked(false);
        cbWednesday.setChecked(false);
        cbThursday.setChecked(false);
        cbFriday.setChecked(false);
        cbSaturday.setChecked(false);
        cbSunday.setChecked(false);
        
        if (customDays != null && !customDays.isEmpty()) {
            String[] days = customDays.split(",");
            for (String day : days) {
                switch (day.trim()) {
                    case "1": cbSunday.setChecked(true); break;    // Sunday = 1
                    case "2": cbMonday.setChecked(true); break;    // Monday = 2
                    case "3": cbTuesday.setChecked(true); break;   // Tuesday = 3
                    case "4": cbWednesday.setChecked(true); break; // Wednesday = 4
                    case "5": cbThursday.setChecked(true); break;  // Thursday = 5
                    case "6": cbFriday.setChecked(true); break;    // Friday = 6
                    case "7": cbSaturday.setChecked(true); break;  // Saturday = 7
                }
            }
        }
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
