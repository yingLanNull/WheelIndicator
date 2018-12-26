package com.yinglan.wheelindicator;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class adapter extends PagerAdapter {
    private Context context;

    private List<Integer> list;

    public adapter(Activity activity) {
        context = activity;
        list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        NestedScrollView scrollView = new NestedScrollView(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if (position % 2 == 1)
            for (int i = 0; i < 6; i++) {
                View view = View.inflate(context, R.layout.item, null);
                view.setPadding(20, 20, 20, 20);
                linearLayout.addView(view);
            }
        else
            for (int i = 0; i < 3; i++) {
                View view = View.inflate(context, R.layout.item, null);
                view.setPadding(20, 20, 20, 20);
                linearLayout.addView(view);
            }
        scrollView.addView(linearLayout);
        container.addView(scrollView);
        return scrollView;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "█████";
    }
}
