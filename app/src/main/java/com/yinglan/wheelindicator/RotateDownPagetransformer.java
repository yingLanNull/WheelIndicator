package com.yinglan.wheelindicator;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

public class RotateDownPagetransformer implements ViewPager.PageTransformer {

    private static final float MAX_ROTATE = 20F;
    private static float ROTATE = 0F;

    /*
     * 效果分析：
     * 滑动可以分解为：A>B
     * A的position:0.0 >> -1.0
     * B的position:1.0 >> 0.0
     * (non-Javadoc)
     * @see android.support.v4.view.ViewPager.PageTransformer#
     * transformPage(android.view.View, float)
     */

    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            ViewHelper.setAlpha(view, 0);//设置透明度
        } else if (position <= 0) { // A页position:0.0 >> 1.0
            //计算旋转角度
            ROTATE = MAX_ROTATE * position;
            //设置旋转中心
            ViewHelper.setPivotX(view, pageWidth/2);
            ViewHelper.setPivotY(view, view.getMeasuredHeight());
            //设置选择角度
            ViewHelper.setRotation(view, ROTATE);

        } else if (position <= 1) { // B页position:1.0 >> 0.0
            //计算旋转角度
            ROTATE = MAX_ROTATE * position;
            //设置旋转中心
            ViewHelper.setPivotX(view, pageWidth/2);
            ViewHelper.setPivotY(view, view.getMeasuredHeight());
            //设置选择角度
            ViewHelper.setRotation(view, ROTATE);

        } else { // (1,+Infinity]
            ViewHelper.setAlpha(view, 0);
        }
    }
}