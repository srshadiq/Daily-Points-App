package com.example.dailygoalpoints;

public class DailyPoint {
    private String date;
    private int points;

    public DailyPoint() {
    }

    public DailyPoint(String date, int points) {
        this.date = date;
        this.points = points;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "DailyPoint{" +
                "date='" + date + '\'' +
                ", points=" + points +
                '}';
    }
}