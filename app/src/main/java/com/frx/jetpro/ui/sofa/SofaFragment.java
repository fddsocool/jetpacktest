package com.frx.jetpro.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.frx.jetpro.databinding.FragmentSofaBinding;
import com.frx.jetpro.model.SofaTab;
import com.frx.jetpro.ui.home.HomeFragment;
import com.frx.jetpro.utils.AppConfig;
import com.frx.libnavannotation.FragmentDestination;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@FragmentDestination(pageUrl = "main/tabs/sofa")
public class SofaFragment extends Fragment {

    private FragmentSofaBinding sofaBinding;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private SofaTab tabConfig;
    private ArrayList<SofaTab.Tabs> tabsArrayList;

    private FragmentStateAdapter viewPagerAdapter;

    //用于保存FragmentStateAdapter中创建的Fragment
    private Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private TabLayoutMediator mediator;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sofaBinding = FragmentSofaBinding.inflate(inflater, container, false);
        return sofaBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager2 = sofaBinding.viewPager;
        tabLayout = sofaBinding.tabLayout;
        //获取tabs的配置
        tabConfig = getTabConfig();
        tabsArrayList = new ArrayList<>();
        for (SofaTab.Tabs tab : tabConfig.tabs) {
            if (tab.enable) {
                tabsArrayList.add(tab);
            }
        }

        //创建ViewPager的适配器
        viewPagerAdapter = new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {

            @Override
            public int getItemCount() {
                return tabsArrayList.size();
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = fragmentMap.get(position);
                if (fragment == null) {
                    fragment = getTabFragment(position);
                    fragmentMap.put(position, fragment);
                }
                return fragment;
            }
        };

        //限制页面预加载
        viewPager2.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        //关联适配器
        viewPager2.setAdapter(viewPagerAdapter);
        //设置tab属性
        tabLayout.setTabGravity(tabConfig.tabGravity);
        //创建ViewPager和tabLayout的联动器，ViewPager2需要用TabLayoutMediator，ViewPager需要用TabLayout.setUpWithViewPager()
        mediator = new TabLayoutMediator(tabLayout, viewPager2, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                //tab标签的配置
                tab.setCustomView(makeTabView(position));
            }
        });
        mediator.attach();
        //viewPager的监听器
        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int tabCount = tabLayout.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(position);
                    if (tab != null) {
                        TextView customView = (TextView) tab.getCustomView();
                        if (customView != null) {
                            //设置切换字体
                            if (tab.getPosition() == position) {
                                customView.setTextSize(tabConfig.activeSize);
                                customView.setTypeface(Typeface.DEFAULT_BOLD);
                            } else {
                                customView.setTextSize(tabConfig.normalSize);
                                customView.setTypeface(Typeface.DEFAULT);
                            }
                        }
                    }
                }
            }
        };
        //注册监听器
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback);
        //切换到默认选择项，需要等待初始化完成之后才有效
        viewPager2.post(new Runnable() {
            @Override
            public void run() {
                viewPager2.setCurrentItem(tabConfig.select, false);
            }
        });
    }

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        //设置文本属性
        //选中状态
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};
        //颜色
        int[] colors = new int[]{Color.parseColor(tabConfig.activeColor),
                Color.parseColor(tabConfig.normalColor)};

        ColorStateList colorStateList = new ColorStateList(states, colors);
        tabView.setTextColor(colorStateList);
        tabView.setText(tabsArrayList.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    private Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabsArrayList.get(position).tag);
    }

    private SofaTab getTabConfig() {
        return AppConfig.getSofaTabConfig();
    }

    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback);
        onPageChangeCallback = null;
        super.onDestroy();
    }
}