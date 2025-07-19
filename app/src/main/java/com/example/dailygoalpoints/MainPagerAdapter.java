package com.example.dailygoalpoints;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    private HomeFragment homeFragment;
    private TasksFragment tasksFragment;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        homeFragment = new HomeFragment();
        tasksFragment = new TasksFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return homeFragment;
            case 1:
                return tasksFragment;
            default:
                return homeFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Home and Tasks
    }

    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Home";
            case 1:
                return "Tasks";
            default:
                return "";
        }
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public TasksFragment getTasksFragment() {
        return tasksFragment;
    }
}
