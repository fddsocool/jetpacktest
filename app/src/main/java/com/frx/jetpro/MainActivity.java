package com.frx.jetpro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.frx.jetpro.utils.NavGraphBuilder;
import com.frx.jetpro.view.AppBottomBar;
import com.frx.libcommon.utils.StatusBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //由于 启动时设置了 R.style.launcher 的windowBackground属性
        //势必要在进入主页后,把窗口背景清理掉
        setTheme(R.style.AppTheme);
        //启用沉浸式布局，白底黑字
        StatusBar.fitSystemBar(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        AppBottomBar navView = findViewById(R.id.nav_view);
//
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);
//
//        NavGraphBuilder.build(navController, this, fragment.getId());
//
//        navView.setOnNavigationItemSelectedListener(menuItem -> {
//            navController.navigate(menuItem.getItemId());
//            return TextUtils.isEmpty(menuItem.getTitle());
//        });

        navView = findViewById(R.id.nav_view);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
        NavGraphBuilder.build(navController, this, fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navController.navigate(item.getItemId());
        return TextUtils.isEmpty(item.getTitle());
    }
}