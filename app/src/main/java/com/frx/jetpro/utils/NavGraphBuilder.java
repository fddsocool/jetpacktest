package com.frx.jetpro.utils;

import android.content.ComponentName;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.frx.jetpro.model.Destination;
import com.frx.jetpro.navigator.FixFragmentNavigator;
import com.frx.libcommon.global.AppGlobals;

import java.util.HashMap;

public class NavGraphBuilder {
    public static void build(NavController controller, FragmentActivity activity, int containerId) {

        NavigatorProvider provider = controller.getNavigatorProvider();
        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        //FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        //fragment的导航此处使用我们定制的FixFragmentNavigator，底部Tab切换时 使用hide()/show(),而不是replace()
        FixFragmentNavigator fragmentNavigator = new FixFragmentNavigator(activity,
                activity.getSupportFragmentManager(), containerId);
        provider.addNavigator(fragmentNavigator);
        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);

        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();

        for (Destination value : destConfig.values()) {
            if (value.isFragment()) {
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setId(value.getId());
                destination.setClassName(value.getClazName());
                destination.addDeepLink(value.getPageUrl());
                navGraph.addDestination(destination);
            } else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(value.getId());
                destination.setComponentName(
                        new ComponentName(AppGlobals.getApplication().getPackageName(),
                                value.getClazName()));
                destination.addDeepLink(value.getPageUrl());
                navGraph.addDestination(destination);
            }
            if (value.isAsStarter()) {
                navGraph.setStartDestination(value.getId());
            }
        }
        controller.setGraph(navGraph);
    }
}
