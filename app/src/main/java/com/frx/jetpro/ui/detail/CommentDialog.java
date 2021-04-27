package com.frx.jetpro.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.Observer;

import com.frx.jetpro.databinding.LayoutCommentDialogBinding;
import com.frx.jetpro.model.Comment;
import com.frx.jetpro.ui.login.UserManager;
import com.frx.jetpro.ui.publish.CaptureActivity;
import com.frx.libcommon.global.AppGlobals;
import com.frx.libcommon.utils.FileUtils;
import com.frx.libcommon.utils.PixUtils;
import com.frx.libcommon.view.LoadingDialog;
import com.frx.libcommon.view.ViewHelper;
import com.frx.libnetwork.ApiResponse;
import com.frx.libnetwork.ApiService;
import com.frx.libnetwork.JsonCallback;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("RestrictedApi")
public class CommentDialog extends AppCompatDialogFragment {

    private static final String KEY_ITEM_ID = "key_item_id";

    private LayoutCommentDialogBinding commentDialogBinding;

    private String filePath;
    private int width, height;
    private boolean isVideo;
    private long itemId;

    private commentAddListener commentAddListener;

    private LoadingDialog loadingDialog;

    public void setCommentAddListener(CommentDialog.commentAddListener commentAddListener) {
        this.commentAddListener = commentAddListener;
    }

    public static CommentDialog newInstance(long itemId) {
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 结果回调
     */
    public interface commentAddListener {
        void onAddComment(Comment comment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemId = getArguments().getLong(KEY_ITEM_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        //设置全屏
        commentDialogBinding = LayoutCommentDialogBinding.inflate(inflater, window.findViewById(android.R.id.content), false);

        commentDialogBinding.commentVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureActivity.startActivity(getActivity());
            }
        });

        commentDialogBinding.commentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishComment();
            }
        });

        commentDialogBinding.commentAnnexDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePath = null;
                isVideo = false;
                width = 0;
                height = 0;
                commentDialogBinding.commentAnnexCover.setImageDrawable(null);
                commentDialogBinding.commentAnnexLayout.setVisibility(View.GONE);

                commentDialogBinding.commentVideo.setEnabled(true);
                commentDialogBinding.commentVideo.setAlpha(255);
            }
        });

        window.setWindowAnimations(0);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        //底部弹出
        window.setGravity(Gravity.BOTTOM);
        ViewHelper.setViewOutline(commentDialogBinding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);

        commentDialogBinding.getRoot().post(this::showSoftInputMethod);
        dismissWhenPressBack();

        return commentDialogBinding.getRoot();
    }

    /**
     * 屏蔽返回键
     */
    private void dismissWhenPressBack() {
        commentDialogBinding.inputView.setOnBackKeyEventListener(() -> {
            commentDialogBinding.inputView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 200);
            return true;
        });
    }

    /**
     * 展示软键盘
     */
    private void showSoftInputMethod() {
        commentDialogBinding.inputView.setFocusable(true);
        commentDialogBinding.inputView.setFocusableInTouchMode(true);
        //请求获得焦点
        commentDialogBinding.inputView.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(commentDialogBinding.inputView, 0);
    }

    private void publishComment() {
        if (TextUtils.isEmpty(commentDialogBinding.inputView.getText())) {
            return;
        }

        String commentText = commentDialogBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", commentText)
                .addParam("image_url", null)
                .addParam("video_url", null)
                .addParam("width", 0)
                .addParam("height", 0)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败:" + response.message);
                        dismissLoadingDialog();
                    }
                });
    }

    private void onCommentSuccess(Comment body) {
        showToast("评论发布成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            if (commentAddListener != null) {
                commentAddListener.onAddComment(body);
            }
            dismiss();
        });
    }

    private void showToast(String s) {
        //showToast几个可能会出现在异步线程调用
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(() -> Toast.makeText(AppGlobals.getApplication(), s, Toast.LENGTH_SHORT).show());
        }
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
            loadingDialog.setLoadingText("正在发布...");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            //dismissLoadingDialog 的调用可能会出现在异步线程调用
            if (Looper.myLooper() == Looper.getMainLooper()) {
                ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                });
            } else if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            if (!TextUtils.isEmpty(filePath)) {
                commentDialogBinding.commentAnnexLayout.setVisibility(View.VISIBLE);
                commentDialogBinding.commentAnnexCover.setImageUrl(filePath);
                if (isVideo) {
                    commentDialogBinding.commentAnnexIconVideo.setVisibility(View.VISIBLE);
                }
            }
            commentDialogBinding.commentVideo.setEnabled(false);
            commentDialogBinding.commentVideo.setAlpha(80);
        }
    }
}
