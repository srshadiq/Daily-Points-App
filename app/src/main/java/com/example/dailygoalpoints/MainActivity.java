package com.example.dailygoalpoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements 
        TasksFragment.OnDataChangeListener, 
        HomeFragment.OnDataChangeListener {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MainPagerAdapter pagerAdapter;
    private HomeFragment homeFragment;
    private TasksFragment tasksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupViewPager();
        
        // Process end-of-day penalties for previous days
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.autoProcessPreviousDays();
        
        // Show penalty summary if there were any recent penalties
        showRecentPenaltySummary(dbHelper);
        
        // Clean up completed "once" tasks
        dbHelper.cleanupOnceTasks();
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
    }

    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Get fragment references
        homeFragment = pagerAdapter.getHomeFragment();
        tasksFragment = pagerAdapter.getTasksFragment();
        
        // Set up bidirectional communication
        tasksFragment.setDataChangeListener(this);
        homeFragment.setDataChangeListener(this);
        homeFragment.setNavigationListener(() -> viewPager.setCurrentItem(1));

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getPageTitle(position));
            
            // Set icons for tabs
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.ic_home);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_tasks);
                    break;
            }
        }).attach();
    }

    @Override
    public void onDataChanged() {
        // When data changes in any fragment, refresh both fragments
        if (homeFragment != null) {
            homeFragment.refreshData();
        }
        if (tasksFragment != null) {
            tasksFragment.refreshData();
        }
    }
    
    private void showRecentPenaltySummary(DatabaseHelper dbHelper) {
        // Check if dialog has already been shown today
        SharedPreferences prefs = getSharedPreferences("daily_dialogs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastShownDate = prefs.getString("penalty_dialog_last_shown", "");
        
        // If dialog was already shown today, don't show it again
        if (today.equals(lastShownDate)) {
            return;
        }
        
        List<DatabaseHelper.PenaltySummary> penalties = dbHelper.getRecentPenalties(3);
        
        if (!penalties.isEmpty()) {
            StringBuilder message = new StringBuilder("Recent penalties applied:\n\n");
            int totalPenalty = 0;
            
            for (DatabaseHelper.PenaltySummary penalty : penalties) {
                message.append("📅 ").append(penalty.getFormattedDate())
                       .append(": -").append(penalty.getPenaltyPoints())
                       .append(" points\n");
                totalPenalty += penalty.getPenaltyPoints();
            }
            
            message.append("\n⚠️ Total penalty: -").append(totalPenalty).append(" points")
                   .append("\n\n💡 Complete tasks daily to avoid penalties!");
            
            new android.app.AlertDialog.Builder(this)
                .setTitle("Task Completion Penalties")
                .setMessage(message.toString())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Got it!", (dialog, which) -> {
                    // Mark dialog as shown for today
                    prefs.edit().putString("penalty_dialog_last_shown", today).apply();
                })
                .setOnDismissListener(dialog -> {
                    // Also mark as shown if user dismisses dialog by tapping outside
                    prefs.edit().putString("penalty_dialog_last_shown", today).apply();
                })
                .show();
        }
    }
}