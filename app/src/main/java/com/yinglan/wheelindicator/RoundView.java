package com.yinglan.wheelindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RoundView extends View {

    private static final int DEFAULT_PAINT_WIDTH = 20;
    private static final int DEFAULT_NUMBER_COLOR = 0x8819224F;
    private static final int DEFAULT_LINE_COLOR = 0xFF3DCAA0;
    private static final int DEFAULT_DIVIDER_COLOR = 0xFF19224F;
    private static final int DEFAULT_TOWARD_TOP = 2 << 2;
    private static final int DEFAULT_TOWARD_BOTTOM = 2 << 3;
    private static final int DEFAULT_TOWARD_LEFT = 2 << 4;
    private static final int DEFAULT_TOWARD_RIGHT = 2 << 5;
    private static final int STATE_ROTATING = 2 << 6;
    private static final int STATE_STOP = 2 << 7;

    private Paint mNumberPaint;
    private Paint mModulePaint;
    private Paint mDividerPaint;


    private int mNumberColor;
    private int mLineColor;
    private int mDividerColor;

    // Parameters
    //中心x轴
    private float mCx;
    //中心y轴
    private float mCy;
    //大圆圈半径
    private float mRadius;
    //分割中心的半径
    private float mDivideRadius;
    //分割线画笔宽度
    private float mDivideStrokeWidth;
    //旋转角度
    private float mRotatDegress;
    //最后一次的旋转角度
    private float mLastRotatDegress;
    //按下时x轴值
    private float mTouchX;
    //按下时y轴值
    private float mTouchY;
    //总份数之和
    private float mSumParts;
    //一份的度数
    private float mOnePart;
    //动画转动时的参数
    private float mGradientParameter;
    //viewpager拖动时的参数
    private float mDragParameter;

    //朝向
    private int mToward;
    //前一次选中的集合角标
    private int mLastIndex;
    //选中的集合角标
    private int mIndex;
    //状态
    private int mState;

    private List<HashMap> chartList;
    private List<Float> chartListKey;

    private ViewPager mViewPager;
    /**
     * 当前的条目索引
     */
    private int mCurrentItem = 0;

    //是否转动
    private boolean mIsTurn;
    //是否可以点击
    private boolean isClickable;
    //是否默认选中
    private boolean defaultSelect;

    public RoundView(Context context) {
        this(context, null);
    }

    public RoundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {


        chartListKey = new ArrayList<>();
        chartList = new ArrayList<>();

        HashMap hashMap1 = new HashMap<Float, Integer>();
        hashMap1.put(200f, Color.parseColor("#FECD72"));
        chartList.add(hashMap1);
        HashMap hashMap2 = new HashMap<Float, Integer>();
        hashMap2.put(300f, Color.parseColor("#EE56B6"));
        chartList.add(hashMap2);
        HashMap hashMap3 = new HashMap<Float, Integer>();
        hashMap3.put(400f, Color.parseColor("#3DCAA0"));
        chartList.add(hashMap3);
        HashMap hashMap4 = new HashMap<Float, Integer>();
        hashMap4.put(300f, Color.parseColor("#55D8FA"));
        chartList.add(hashMap4);
//        HashMap hashMap5 = new HashMap<Float, Integer>();
//        hashMap5.put(400f, Color.parseColor("#B34BBD"));
//        chartList.add(hashMap5);

        mSumParts = 0f;
        for (HashMap<Float, Integer> key : chartList) {
            mSumParts += key.keySet().iterator().next();
            chartListKey.add(key.keySet().iterator().next());
        }

        mSumParts = mSumParts == 0 ? 1f : mSumParts;
        mOnePart = 360.0000000000f / mSumParts;
        mToward = DEFAULT_TOWARD_BOTTOM;
        mState = STATE_STOP;

        mModulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //Width
        mDivideStrokeWidth = DEFAULT_PAINT_WIDTH;

        //Color
        mNumberColor = DEFAULT_NUMBER_COLOR;
        mLineColor = DEFAULT_LINE_COLOR;
        mDividerColor = DEFAULT_DIVIDER_COLOR;

        //boolean
        isClickable = true;

        // LinePaint
        mModulePaint.setColor(mLineColor);
        mModulePaint.setAntiAlias(true);
        mModulePaint.setStyle(Paint.Style.FILL);

        // NumberPaint
        mNumberPaint.setColor(mNumberColor);
        mNumberPaint.setAntiAlias(true);
        mNumberPaint.setStyle(Paint.Style.FILL);

        // NumberPaint
        mDividerPaint.setColor(mDividerColor);
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setTextAlign(Paint.Align.CENTER);
        mDividerPaint.setStyle(Paint.Style.FILL);
        mDividerPaint.setStrokeWidth(mDivideStrokeWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Ensure width = height

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        this.mCx = width / 2;
        this.mCy = height / 2;

        this.mRadius = (width > height ? height / 2 : width / 2) - mDivideStrokeWidth;
        this.mDivideRadius = this.mRadius / 4;
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        rotateCanvas(canvas);

        canvas.drawCircle(mCx, mCy, mRadius, mNumberPaint);
        canvas.save();

        drawChart(canvas);
        super.onDraw(canvas);
    }

    private void rotateCanvas(Canvas canvas) {
        canvas.rotate(mRotatDegress, mCx, mCy);
        canvas.save();
    }

    private void drawChart(Canvas canvas) {

        RectF rect = new RectF(mCx - mRadius
                , mCy - mRadius
                , mCx + mRadius
                , mCy + mRadius);

        int parts = chartListKey.size();

        if (parts > 0) {

            float startPart = 0;

            for (int i = 0; i < chartList.size(); i++) {

                HashMap<Float, Integer> map = chartList.get(i);

                float key = map.keySet().iterator().next();

                float part = key * mOnePart;

                int charColor = map.get(key);

                int newColor = charColor;
                int alpha = Color.alpha(charColor);

                if (mIndex == i) {
                    newColor = Color.argb((int) (100 + (alpha - 100) * mGradientParameter), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
                } else {
                    if (mLastIndex == i) {
                        float c = (alpha - 100) * mGradientParameter;
                        newColor = Color.argb((int) (alpha - c), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
                        Log.e("alpha", newColor + "");
                    } else {
                        newColor = Color.argb(100, Color.red(newColor), Color.green(newColor), Color.blue(newColor));
                    }
                }

                mModulePaint.setColor(newColor);

                canvas.rotate(-90, mCx, mCy);

                canvas.drawArc(rect, startPart, part, true, mModulePaint);

                startPart += part;

                canvas.restore();

                canvas.save();
            }

            startPart = 0;

            for (Float key : chartListKey) {

                float part = key * mOnePart;

                canvas.rotate(-90, mCx, mCy);

                startPart += part;

                canvas.drawLine(mCx, mCy, mCx + getX(startPart), mCy + getY(startPart), mDividerPaint);

                canvas.restore();
                canvas.save();


            }

            mDividerPaint.setStyle(Paint.Style.FILL);
            mDivideRadius = mDivideRadius < mRadius ? mDivideRadius : (mDivideRadius - 20);
            canvas.drawCircle(mCx, mCy, mDivideRadius, mDividerPaint);

            mDividerPaint.setStyle(Paint.Style.STROKE);
            //增加一个像素的重叠
            float mRadiusL = mRadius + mDividerPaint.getStrokeWidth() / 2 - 1;
            canvas.drawCircle(mCx, mCy, mRadiusL, mDividerPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                mTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                int index = mInCircleButton(mTouchX, mTouchY);
                if (index >= 0 && mState == STATE_STOP && isClickable) {
                    startClickRotatAnimation(index);
                }
                break;
        }
        return true;
    }

    private int mInCircleButton(float x, float y) {

        buildDrawingCache();
        Bitmap bitmap = getDrawingCache();
        if (null != bitmap) {
            int pixel = bitmap.getPixel((int) x, (int) y);
            if (pixel != Color.TRANSPARENT && pixel != mDividerColor) {
                double mPoint = Math.sqrt(Math.pow((x - mCx), 2) + Math.pow((y - mCy), 2));
                if ((mPoint < mRadius) && (mPoint > (mDivideRadius + mDivideStrokeWidth / 2))) {

                    float rotation = getRotationBetweenLines(x, y);

                    if (rotation >= 0) {

                        float startPart = mRotatDegress;

                        int selectIndex = chartListKey.size() - 1;

                        for (HashMap<Float, Integer> map : chartList) {

                            float key = map.keySet().iterator().next();

                            float part = key * mOnePart;
                            startPart += part;

                            if (mRotatDegress < 0) {
                                if ((rotation - 360) > mRotatDegress) {
                                    if ((rotation - 360) < startPart) {
                                        return selectIndex;
                                    }
                                } else {
                                    if (rotation < startPart) {
                                        return selectIndex;
                                    }
                                }
                            } else {

                                if (rotation < mRotatDegress) {
                                    rotation += 360;
                                }

                                if (rotation < startPart) {
                                    return selectIndex;
                                }
                            }
                            selectIndex--;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private void startClickRotatAnimation(int index) {

        if (null != mViewPager) {
            mIsTurn = true;
            mCurrentItem = index;
            //不能使用平滑滚动，否者颜色改变会乱
            mViewPager.setCurrentItem(index, true);
            return;
        }

        mIndex = getmIndex(index);

        mState = STATE_ROTATING;

        float startPart = 0f;

        float degress = 0f;

        List<HashMap<Float, Integer>> list = new ArrayList(chartList);

        Collections.reverse(list);

        for (int i = 0; i < list.size(); i++) {

            HashMap<Float, Integer> map = list.get(i);

            float key = map.keySet().iterator().next();

            float part = key * mOnePart;

            if (index == i) {
                degress = startPart + part / 2;
                switch (mToward) {
                    case DEFAULT_TOWARD_TOP:
                        degress = degress;
                        break;
                    case DEFAULT_TOWARD_BOTTOM:
                        degress = degress - 180;
                        break;
                    case DEFAULT_TOWARD_LEFT:
                        degress = degress - 90;
                        break;
                    case DEFAULT_TOWARD_RIGHT:
                        degress = degress - 270;
                        break;
                    case DEFAULT_TOWARD_TOP | DEFAULT_TOWARD_LEFT:
                        degress = degress - 45;
                        break;
                    case DEFAULT_TOWARD_TOP | DEFAULT_TOWARD_RIGHT:
                        degress = degress - 315;
                        break;
                    case DEFAULT_TOWARD_BOTTOM | DEFAULT_TOWARD_RIGHT:
                        degress = degress - 135;
                        break;
                    case DEFAULT_TOWARD_BOTTOM | DEFAULT_TOWARD_LEFT:
                        degress = degress + 225;
                        break;
                    default:
                        degress = degress - 180;
                        break;
                }
                degress = degress - mRotatDegress;
                break;
            }
            startPart += part;
        }

        // 选中菜单项转动一周动画驱动
        ValueAnimator aroundAnima = ValueAnimator.ofFloat(1.f, 100.f);
        aroundAnima.setDuration((long) (Math.abs(degress) * 2));
        aroundAnima.setInterpolator(new

                AccelerateDecelerateInterpolator());
        final float rotatDegress = degress;
        aroundAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()

        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mGradientParameter = valueAnimator.getAnimatedFraction();
                mRotatDegress = mLastRotatDegress + rotatDegress * mGradientParameter;
                invalidate();
            }
        });
        aroundAnima.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLastRotatDegress = mRotatDegress;
                mLastIndex = mIndex;
                mState = STATE_STOP;
            }
        });
        aroundAnima.start();
    }

    private void startRotatAnimation(int index) {

        mIndex = getmIndex(index);

        mState = STATE_ROTATING;

        float startPart = 0f;

        float degress = 0f;

        List<HashMap<Float, Integer>> list = new ArrayList(chartList);

        Collections.reverse(list);

        for (int i = 0; i < list.size(); i++) {

            HashMap<Float, Integer> map = list.get(i);

            float key = map.keySet().iterator().next();

            float part = key * mOnePart;

            if (index == i) {
                degress = startPart + part / 2;
                switch (mToward) {
                    case DEFAULT_TOWARD_TOP:
                        degress = degress;
                        break;
                    case DEFAULT_TOWARD_BOTTOM:
                        degress = degress - 180;
                        break;
                    case DEFAULT_TOWARD_LEFT:
                        degress = degress - 90;
                        break;
                    case DEFAULT_TOWARD_RIGHT:
                        degress = degress - 270;
                        break;
                    case DEFAULT_TOWARD_TOP | DEFAULT_TOWARD_LEFT:
                        degress = degress - 45;
                        break;
                    case DEFAULT_TOWARD_TOP | DEFAULT_TOWARD_RIGHT:
                        degress = degress - 315;
                        break;
                    case DEFAULT_TOWARD_BOTTOM | DEFAULT_TOWARD_RIGHT:
                        degress = degress - 135;
                        break;
                    case DEFAULT_TOWARD_BOTTOM | DEFAULT_TOWARD_LEFT:
                        degress = degress + 225;
                        break;
                    default:
                        degress = degress - 180;
                        break;
                }
                degress = degress - mLastRotatDegress;
                break;
            }
            startPart += part;
        }

        mRotatDegress = mLastRotatDegress + degress * mGradientParameter;

        invalidate();
    }

    private float getY(float radian) {
        return (float) (Math.sin(radian * Math.PI / 180) * mRadius);
    }

    private float getX(float radian) {
        return (float) (Math.cos(radian * Math.PI / 180) * mRadius);
    }

    /**
     * 获取两条线的夹角
     *
     * @param xInView
     * @param yInView
     * @return
     */
    public int getRotationBetweenLines(float xInView, float yInView) {
        double rotation = 0;

        double k1 = (double) (mCy - mCy) / (mCx * 2 - mCx);
        double k2 = (double) (yInView - mCy) / (xInView - mCx);
        double tmpDegree = Math.atan((Math.abs(k1 - k2)) / (1 + k1 * k2)) / Math.PI * 180;

        if (xInView > mCx && yInView < mCy) {  //第一象限
            rotation = 90 - tmpDegree;
        } else if (xInView > mCx && yInView > mCy) //第二象限
        {
            rotation = 90 + tmpDegree;
        } else if (xInView < mCx && yInView > mCy) { //第三象限
            rotation = 270 - tmpDegree;
        } else if (xInView < mCx && yInView < mCy) { //第四象限
            rotation = 270 + tmpDegree;
        } else if (xInView == mCx && yInView < mCy) {
            rotation = 0;
        } else if (xInView == mCx && yInView > mCy) {
            rotation = 180;
        }

        return (int) rotation;
    }

    public void setViewPager(ViewPager viewPager, boolean flag) {
        this.defaultSelect = flag;

        this.mViewPager = viewPager;
        if (null != mViewPager) {
            if (null == mViewPager.getAdapter()) {
                throw new NullPointerException("viewpager的adapter为null");
            }
//            if (mViewPager.getAdapter().getCount() != mChildCounts) {
//                throw new IllegalArgumentException("LinearLayout的子View数量必须和ViewPager条目数量一致");
//            }
            //对ViewPager添加监听
            mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        }
    }

    public void setViewPager(ViewPager viewPager) {
        setViewPager(viewPager, true);
    }

    private class MyOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //滑动时的透明度动画
            if (defaultSelect) {
                if (mIsTurn) {
                    if (positionOffset > 0) {
                        if (position < mCurrentItem) {
                            mGradientParameter = positionOffset;
                            startRotatAnimation(mCurrentItem);
                        } else {
                            mGradientParameter = 1 - positionOffset;
                            startRotatAnimation(mCurrentItem);
                        }
                    }
                } else {
                    if (positionOffset > 0) {
                        if (position >= mCurrentItem) {
                            if (position - mCurrentItem > 0) {
                                mCurrentItem = position;
                                mLastRotatDegress = mRotatDegress;
                                mLastIndex = mIndex;
                            }
                            mGradientParameter = positionOffset;
                            startRotatAnimation(position + 1);
                        } else {
                            if (mCurrentItem - position > 1) {
                                mCurrentItem = position + 1;
                                mLastRotatDegress = mRotatDegress;
                                mLastIndex = mIndex;
                            }
                            mGradientParameter = 1 - positionOffset;
                            startRotatAnimation(position);
                        }
                        Log.e("positionOffset", positionOffset + "");
                    }
                }
                if (positionOffset == 0) {
                    mGradientParameter = 1;
                    mLastRotatDegress = mRotatDegress;
                    startRotatAnimation(position);
                    mLastRotatDegress = mRotatDegress;
                    mCurrentItem = position;
                    mState = STATE_STOP;
                    mLastIndex = mIndex;
                    mIsTurn = false;
                }
            } else {
                defaultSelect = true;
            }

            Log.e("position11111", position + "");
            Log.e("position33333", mCurrentItem + "");
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
//            if (Math.abs(mCurrentItem - position) >= 2) {
//                mCurrentItem = mCurrentItem1;
//            }
//            Log.e("position22222", position + "");
        }
    }

    private int getmIndex(int index) {
        ArrayList<Integer> ints = new ArrayList<>();
        for (int i = chartList.size() - 1; i >= 0; i--) {
            ints.add(i);
        }
        return ints.isEmpty() ? 0 : ints.get(index);
    }


    public void setClickableIndex(boolean clickable) {
        isClickable = clickable;
    }

}
