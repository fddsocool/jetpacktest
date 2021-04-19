package com.frx.jetpro.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.frx.jetpro.model.Feed;
import com.frx.libcommon.utils.StatusBar;

public class FeedDetailActivity extends AppCompatActivity {

    private static final String KEY_FEED = "key_feed";
    public static final String KEY_CATEGORY = "key_category";

    public static void startFeedDetailActivity(Context context, Feed item, String category) {
        Intent intent = new Intent(context, FeedDetailActivity.class);
        intent.putExtra(KEY_FEED, item);
        intent.putExtra(KEY_CATEGORY, category);
        context.startActivity(intent);
    }

    private ViewHandler viewHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //启用沉浸式布局，白底黑字
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        Feed feed = (Feed) getIntent().getSerializableExtra(KEY_FEED);
        if (feed == null) {
            finish();
            return;
        }
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }

        viewHandler.bindInitData(feed);
    }
}