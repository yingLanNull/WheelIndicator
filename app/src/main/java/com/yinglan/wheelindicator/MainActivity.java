package com.yinglan.wheelindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private boolean f;
    private TabLayout tabLayout;
    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {

        final List<Integer> list = new ArrayList<>();
        list.add(Color.parseColor("#55D8FA"));
        list.add(Color.parseColor("#3DCAA0"));
        list.add(Color.parseColor("#EE56B6"));
        list.add(Color.parseColor("#FECD72"));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("█████");

        //设置返回按钮
        //设置收缩展开toolbar字体颜色
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.ctb);

        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#66ffffff"));

        final ViewPager pager = findViewById(R.id.viewpager);
        pager.setPageTransformer(true, new RotateDownPagetransformer());
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(new adapter(this));

        final TabLayout tabLayout1 = findViewById(R.id.tab1);
        tabLayout1.setupWithViewPager(pager);

        tabLayout = findViewById(R.id.tab);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setVisibility(View.INVISIBLE);

        linearLayout = findViewById(R.id.ll);


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                tabLayout.setScrollPosition(position, positionOffset, true);
                Log.e("pooooooo", positionOffset + "");
                if (positionOffset > 0) {
                    tabLayout.setBackgroundColor(getCurrentColor(positionOffset, list.get(position), list.get(position + 1)));
                    tabLayout1.setBackgroundColor(getCurrentColor(positionOffset, list.get(position), list.get(position + 1)));
                    collapsingToolbarLayout.setContentScrimColor(getCurrentColor(positionOffset, list.get(position), list.get(position + 1)));
                    pager.setBackgroundColor(getCurrentColor(positionOffset, list.get(position), list.get(position + 1)));
                } else {
                    tabLayout.setBackgroundColor(list.get(position));
                    tabLayout1.setBackgroundColor(list.get(position));
                    pager.setBackgroundColor(list.get(position));
                    collapsingToolbarLayout.setContentScrimColor(list.get(position));
                }

                if (!f) {
                    pager.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (!f) {
                    start(pager);
                    f = true;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        RoundView roundview = findViewById(R.id.roundview);
        roundview.setViewPager(pager, false);
    }


    /**
     * 根据fraction值来计算当前的颜色。
     */
    private int getCurrentColor(float fraction, int startColor, int endColor) {
        int redCurrent;
        int blueCurrent;
        int greenCurrent;
        int alphaCurrent;

        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int alphaStart = Color.alpha(startColor);

        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);
        int alphaEnd = Color.alpha(endColor);

        int redDifference = redEnd - redStart;
        int blueDifference = blueEnd - blueStart;
        int greenDifference = greenEnd - greenStart;
        int alphaDifference = alphaEnd - alphaStart;

        redCurrent = (int) (redStart + fraction * redDifference);
        blueCurrent = (int) (blueStart + fraction * blueDifference);
        greenCurrent = (int) (greenStart + fraction * greenDifference);
        alphaCurrent = (int) (alphaStart + fraction * alphaDifference);

        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent);
    }

    private void start(final ViewPager pager) {
        final int top = pager.getPaddingTop();
        ValueAnimator aroundAnima = ValueAnimator.ofFloat(0.f, 100.f);
        aroundAnima.setDuration(300);
        aroundAnima.setInterpolator(new

                AccelerateDecelerateInterpolator());
        aroundAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()

        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                pager.setPadding(0, (int) (top * (1 - valueAnimator.getAnimatedFraction())), 0, 0);
                tabLayout.setAlpha(valueAnimator.getAnimatedFraction());
                tabLayout.setVisibility(View.VISIBLE);
            }
        });
        aroundAnima.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tabLayout.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
            }
        });
        aroundAnima.start();
    }
}
