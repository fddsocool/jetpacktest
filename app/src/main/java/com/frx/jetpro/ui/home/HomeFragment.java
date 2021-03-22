package com.frx.jetpro.ui.home;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;

import com.frx.jetpro.model.Feed;
import com.frx.jetpro.ui.AbsListFragment;
import com.frx.libnavannotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {
    private static final String Tag = "HomeFragment";

    @Override
    protected void afterCreateView() {

    }

    @Override
    protected void afterViewCreated() {

    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }

}