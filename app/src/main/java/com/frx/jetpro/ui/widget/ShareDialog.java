package com.frx.jetpro.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frx.jetpro.R;
import com.frx.jetpro.view.PPImageView;
import com.frx.libcommon.utils.PixUtils;
import com.frx.libcommon.view.CornerFrameLayout;
import com.frx.libcommon.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {

    private CornerFrameLayout mLayout;
    private String mShareContent;
    private List<ResolveInfo> mShareItems = new ArrayList<>();
    private ShareAdapter mShareAdapter;
    private View.OnClickListener listener;

    public ShareDialog(@NonNull Context context) {
        super(context);
    }

    public void setShareContent(String shareContent) {
        this.mShareContent = shareContent;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //背景
        mLayout = new CornerFrameLayout(getContext());
        mLayout.setBackgroundColor(Color.WHITE);
        mLayout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP);

        //网格
        RecyclerView gridView = new RecyclerView(getContext());
        gridView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        mShareAdapter = new ShareAdapter(getContext());
        gridView.setAdapter(mShareAdapter);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = PixUtils.dp2px(20);
        params.bottomMargin = params.topMargin = margin / 2;
        params.rightMargin = params.leftMargin = margin;
        params.gravity = Gravity.CENTER;
        mLayout.addView(gridView, params);

        //设置控件
        setContentView(mLayout);

        //底部弹出
        getWindow().setGravity(Gravity.BOTTOM);

        //设置底色，防止出现间距
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                              ViewGroup.LayoutParams.WRAP_CONTENT);

        queryShareItems();
    }

    private void queryShareItems() {
        //查询系统安装了什么可以分享的软件
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        List<ResolveInfo> resolveInfos = getContext().getPackageManager().queryIntentActivities(
                intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName, "com.tencent.mm") || TextUtils.equals(packageName,
                                                                                    "com.tencent.mobileqq")) {
                mShareItems.add(resolveInfo);
            }
        }
        mShareAdapter.notifyDataSetChanged();
    }

    private class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ShareViewHolder> {

        private final PackageManager mPackageManager;
        private Context mContext;

        public ShareAdapter(Context context) {
            mContext = context;

            mPackageManager = mContext.getPackageManager();
        }

        @NonNull
        @Override
        public ShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_item,
                                                                     parent, false);
            return new ShareViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull ShareViewHolder holder, int position) {
            ResolveInfo resolveInfo = mShareItems.get(position);
            Drawable drawable = resolveInfo.loadIcon(mPackageManager);
            holder.imageView.setImageDrawable(drawable);
            holder.shareText.setText(resolveInfo.loadLabel(mPackageManager));
            holder.imageView.setOnClickListener(v -> {
                Toast.makeText(mContext, "分享成功:" + mShareContent, Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onClick(v);
                }
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return mShareItems == null
                   ? 0
                   : mShareItems.size();
        }

        private class ShareViewHolder extends RecyclerView.ViewHolder {

            public TextView shareText;
            public PPImageView imageView;

            public ShareViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.share_icon);
                shareText = itemView.findViewById(R.id.share_text);
            }
        }
    }

}
