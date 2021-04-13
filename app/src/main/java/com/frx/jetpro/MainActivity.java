package com.frx.jetpro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.frx.jetpro.model.Destination;
import com.frx.jetpro.ui.login.UserManager;
import com.frx.jetpro.utils.AppConfig;
import com.frx.jetpro.utils.NavGraphBuilder;
import com.frx.jetpro.view.AppBottomBar;
import com.frx.libcommon.utils.StatusBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //由于启动时设置了R.style.launcher的windowBackground属性
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

    /**
     * 选择底部导航菜单中的项目时调用
     *
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //登录拦截，查找destination.json文件，判断needLogin字段
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        for (Map.Entry<String, Destination> stringDestinationEntry : destConfig.entrySet()) {
            Destination destination = stringDestinationEntry.getValue();
            if (destination == null) {
                return false;
            }

            if (!UserManager.get().isLogin() && destination.isNeedLogin() && destination.getId() == item.getItemId()) {
                //用户未登录 && 页面需要登录 && 页面id相等
                //去登陆
                UserManager.get().login(this).observe(this, user -> {
                    if (user != null) {
                        //登录成功后跳转页面
                        navView.setSelectedItemId(item.getItemId());
                    }
                });
                return false;
            }
        }

        navController.navigate(item.getItemId());
        return !TextUtils.isEmpty(item.getTitle());
    }
}