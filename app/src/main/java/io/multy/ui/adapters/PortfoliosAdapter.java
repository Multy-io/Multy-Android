package io.multy.ui.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.multy.R;

/**
 * Created by appscrunch on 16.11.17.
 */

public class PortfoliosAdapter extends PagerAdapter {

    private static final String PAGE_TITLE = "Portfolio ";
    private static final int ITEMS_COUNT = 10;

    @Override
    public int getCount() {
        return ITEMS_COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View layout = LayoutInflater.from(container.getContext())
                .inflate(R.layout.view_portfolios_item, container, false);
        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PAGE_TITLE + String.valueOf(position);
    }
}
