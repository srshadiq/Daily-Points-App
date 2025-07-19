package com.example.dailygoalpoints;

import android.os.Bundle;
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
}