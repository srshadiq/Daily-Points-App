package com.example.dailygoalpoints;

public class Task {
    private int id;
    private String title;
    private String description;
    private int points;
    private boolean isCompleted;
    private String dateCreated;
    private String category;
    private int priority; // 1-High, 2-Medium, 3-Low
    private String frequency; // "once", "daily", "custom"
    private String customDays; // JSON string for custom days like "1,2,3,4,5" (Mon-Fri)

    // Constructors
    public Task() {}

    public Task(String title, String description, int points, String category, int priority) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
        this.frequency = "once"; // Default frequency
        this.customDays = "";
    }

    public Task(String title, String description, int points, String category, int priority, 
                String frequency, String customDays) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
        this.frequency = frequency;
        this.customDays = customDays;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getPriorityText() {
        switch (priority) {
            case 1: return "High";
            case 2: return "Medium";
            case 3: return "Low";
            default: return "Medium";
        }
    }

    public String getFrequency() {
        return frequency != null ? frequency : "once";
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getCustomDays() {
        return customDays != null ? customDays : "";
    }

    public void setCustomDays(String customDays) {
        this.customDays = customDays;
    }

    public String getFrequencyText() {
        switch (getFrequency()) {
            case "once": return "Once";
            case "daily": return "Daily";
            case "custom": return "Custom Days";
            default: return "Once";
        }
    }

    // Helper method to check if task should be visible on a specific day
    public boolean isVisibleOnDay(int dayOfWeek) {
        String freq = getFrequency();
        switch (freq) {
            case "once":
                return true; // Always visible until completed/expired
            case "daily":
                return true; // Visible every day
            case "custom":
                String[] days = getCustomDays().split(",");
                for (String day : days) {
                    try {
                        if (Integer.parseInt(day.trim()) == dayOfWeek) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid day numbers
                    }
                }
                return false;
            default:
                return true;
        }
    }
}
