package com.frx.jetpro.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frx.jetpro.R;
import com.frx.libcommon.utils.PixUtils;

import java.lang.ref.WeakReference;

public class RecordView extends View {

    //进度条更新频率，毫秒
    private static final int PROGRESS_INTERVAL = 100;

    private final Context context;

    private final int radius;
    private final int progressWidth;
    private final int progressColor;
    private final int fillColor;
    private final int maxDuration;

    private int progressMaxValue;
    private int progressValue;

    //是否正在录制
    private boolean isRecording;

    private Paint fillPaint;
    private final Paint progressPaint;
    private long startRecordTime;

    private final RecordViewHandler handler;

    private OnRecordListener onRecordListener;

    public interface OnRecordListener {
        void onClick();

        void onLongClick();

        void onFinish();
    }

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        TypedArray typedArray = this.context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes);
        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, PixUtils.dp2px(3));
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        maxDuration = typedArray.getInteger(R.styleable.RecordView_duration, 10);
        typedArray.recycle();

        setMaxDuration(maxDuration);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);

        handler = new RecordViewHandler(Looper.getMainLooper(), this);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //ACTION_DOWN时，开始录制，并且发送进度条进度
                //ACTION_UP，结束录制
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isRecording = true;
                    startRecordTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(0);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    long upTime = System.currentTimeMillis();
                    //判断是拍照还是录制视频
                    if (upTime - startRecordTime > ViewConfiguration.getLongPressTimeout()) {
                        //录制视频
                        finishRecord();
                    }
                    handler.removeCallbacksAndMessages(null);
                    isRecording = false;
                    startRecordTime = 0;
                    progressValue = 0;
                    postInvalidate();
                }
                return false;
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRecordListener != null) {
                    onRecordListener.onClick();
                }
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onRecordListener != null) {
                    onRecordListener.onLongClick();
                }
                return true;
            }
        });
    }

    /**
     * 设置进度条最大值
     *
     * @param maxDuration int
     */
    private void setMaxDuration(int maxDuration) {
        this.progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        if (isRecording) {
            //绘制圆形按钮
            canvas.drawCircle(width / 2f, height / 2f, width / 2f, fillPaint);
            //绘制进度条
            int left = progressWidth / 2;
            int top = progressWidth / 2;
            int right = width - progressWidth / 2;
            int bottom = height - progressWidth / 2;
            float sweepAngle = (progressValue * 1.0f / progressMaxValue) * 360;
            // -90 是正上方
            canvas.drawArc(left, top, right, bottom, -90, sweepAngle, false, progressPaint);
        } else {
            //绘制未录像时的圆形按钮
            canvas.drawCircle(width / 2f, height / 2f, radius, fillPaint);
        }
    }

    public void finishRecord() {
        if (onRecordListener != null) {
            onRecordListener.onFinish();
        }
    }

    public int getProgressValue() {
        return progressValue;
    }

    public void setProgressValue(int progressValue) {
        this.progressValue = progressValue;
    }

    public int getProgressMaxValue() {
        return progressMaxValue;
    }

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    static class RecordViewHandler extends Handler {

        WeakReference<RecordView> recordViewWeakReference;

        public RecordViewHandler(@NonNull Looper looper, RecordView view) {
            super(looper);
            recordViewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            RecordView recordView = recordViewWeakReference.get();
            if (recordView == null) {
                return;
            }

            if (msg.what == 0) {
                int progressValue = recordView.getProgressValue() + 1;
                recordView.setProgressValue(progressValue);
                recordView.postInvalidate();
                if (progressValue <= recordView.getProgressMaxValue()) {
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                } else {
                    recordView.finishRecord();
                }
            }


        }
    }
}
