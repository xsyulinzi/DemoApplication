package com.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * * y
 * x *  *
 * *
 * 在折叠区,设联动X轴的直角边为x,联动Y轴的直角边为y.整个折叠、镜像和底部虚拟三角行组成一个梯形,计算出X轴焦点A和Y轴焦点B
 * 坐标, 根据{@link Path} moveTo到touch点，再lineTo到A、B两点，形成闭合三角
 *
 * <p>
 * Reference:<a href="http://blog.csdn.net/jjwwmlp456/article/details/52598387"/>
 * Date: 2017/10/11
 *
 * @author Aaron
 */

public class FoldTurnView extends View {
    private float mTouchX;
    private float mTouchY;
    private Path mPath;
    private Path mPathFold;
    private Paint mPaint;
    private Paint mTextPaint;
    private int mWidgetWidth;
    private int mWidgetHeight;
    private int mFoldAreaColor;
    private int mFoldMirroringAreaColor;
    private String mTitleText;
    private String mSizeText;
    private int mFileTitleColor;
    private float mFileTitleSize;
    private int mFileSizeColor;
    private float mFileSizeSize;
    private int mFileTextMarginLeft;
    private int mFileSizeMarginTop;

    public FoldTurnView(Context context) {
        this(context, null);
    }

    public FoldTurnView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldTurnView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttr(attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mPathFold = new Path();
        mPaint = new Paint();
        mTextPaint = new Paint();

        mFileTextMarginLeft = getResources().getDimensionPixelSize(R.dimen.file_margin_left);
        mFileSizeMarginTop = getResources().getDimensionPixelSize(R.dimen.file_size_margin_top);
        mTouchX = getResources().getDimensionPixelSize(R.dimen.file_fold_width);
        mTouchY = getResources().getDimensionPixelSize(R.dimen.file_fold_height);
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.FoldTurnView);
        mFoldAreaColor = t.getColor(R.styleable.FoldTurnView_foldAreaColor, Color.GRAY);
        mFoldMirroringAreaColor = t.getColor(R.styleable.FoldTurnView_foldMirroringAreaColor, Color.WHITE);
        mTitleText = t.getString(R.styleable.FoldTurnView_fileTitle);
        mSizeText = t.getString(R.styleable.FoldTurnView_fileSize);
        mFileTitleColor = t.getColor(R.styleable.FoldTurnView_fileTitleColor, ContextCompat.getColor(getContext(), R.color.file_default_title));
        mFileTitleSize = t.getDimension(R.styleable.FoldTurnView_fileTitleSize, getResources().getDimensionPixelSize(R.dimen.file_title_size));
        mFileSizeColor = t.getColor(R.styleable.FoldTurnView_fileSizeColor, ContextCompat.getColor(getContext(), R.color.file_default_size));
        mFileSizeSize = t.getDimension(R.styleable.FoldTurnView_fileSizeSize, getResources().getDimensionPixelSize(R.dimen.file_size_text_size));
        t.recycle();
    }

    /**
     * 滑动动态改变折叠位置
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = x;
                mTouchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchX = x;
                mTouchY = y;
                break;
            case MotionEvent.ACTION_UP:
                mTouchX = x;
                mTouchY = y;
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidgetWidth = getMeasuredWidth();
        mWidgetHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTitleText(canvas);
        drawSizeText(canvas);

        if (mTouchX < mWidgetWidth / 2) {
            mTouchX = mWidgetWidth / 2;
        }

        if (mTouchY < mWidgetHeight / 2) {
            mTouchY = mWidgetHeight / 2;
        }

        float trapezoidHeight = mWidgetWidth - mTouchX;
        float trapezoidUpLine = mWidgetHeight - mTouchY;
        float c = (float) (Math.pow(trapezoidHeight, 2) + Math.pow(trapezoidUpLine, 2));
        float x = c / (2 * trapezoidHeight);
        float y = c / (2 * trapezoidUpLine);
        float limitHeight = mWidgetHeight / 2;
        if (y > limitHeight) {
            y = limitHeight;
        }
        float limitWidth = mWidgetWidth / 2;
        if (x > limitWidth) {
            x = limitWidth;
        }

        mPath.reset();
        mPath.moveTo(mTouchX, mTouchY);
        mPathFold.reset();
        mPathFold.moveTo(mTouchX, mTouchY);
        //B点 在右部
        mPath.lineTo(mWidgetWidth, mWidgetHeight - y);
        //A点 在底部
        mPath.lineTo(mWidgetWidth - x, mWidgetHeight);

        ////B点 在右部
        mPathFold.lineTo(mWidgetWidth, mWidgetHeight - y);
        mPathFold.lineTo(mWidgetWidth, mWidgetHeight);
        //A点 在底部
        mPathFold.lineTo(mWidgetWidth - x, mWidgetHeight);
        mPath.close();
        mPathFold.close();

        canvas.drawPath(mPath, mPaint);
        canvas.clipPath(mPathFold);

        drawTriangle(canvas);

    }

    private void drawTitleText(Canvas canvas) {
        canvas.save();
        mTextPaint.setColor(mFileTitleColor);
        mTextPaint.setTextSize(mFileTitleSize);
        canvas.drawText(mTitleText, mFileTextMarginLeft, getTop(), mTextPaint);
        canvas.restore();
    }

    private void drawSizeText(Canvas canvas) {
        canvas.save();
        mTextPaint.setColor(mFileSizeColor);
        mTextPaint.setTextSize(mFileSizeSize);
        canvas.drawText(mSizeText, mFileTextMarginLeft, getTop() + mFileSizeMarginTop, mTextPaint);
        canvas.restore();
    }

    private void drawTriangle(Canvas canvas) {
        canvas.save();
        //镜像区域
        canvas.drawColor(mFoldMirroringAreaColor);
        canvas.clipPath(mPath);
        canvas.translate(mTouchX, mTouchY);
        //折叠区域
        canvas.drawColor(mFoldAreaColor);
        canvas.restore();
    }

}
