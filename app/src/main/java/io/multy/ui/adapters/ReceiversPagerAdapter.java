/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.multy.R;
import io.multy.model.entities.FastReceiver;
import io.multy.ui.fragments.FastReceiverFragment;

public class ReceiversPagerAdapter extends FragmentStatePagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private List<FastReceiver> data = new ArrayList<>();
    private List<String> addresses = new ArrayList<>();

    public ReceiversPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        FastReceiverFragment fragment = new FastReceiverFragment();
        fragment.setReceiver(data.get(position));
        return fragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public FastReceiver getReceiver(int position) {
        return data.get(position);
    }

    public void addItem(FastReceiver receiver) {
        if (!addresses.contains(receiver.getAddress())) {
            addresses.add(receiver.getAddress());
            data.add(receiver);
        }

        ArrayList<FastReceiver> toDelete = new ArrayList<>();
        for (FastReceiver fastReceiver : data) {
            if (addresses.contains(fastReceiver.getAddress())) {
                addresses.remove(fastReceiver.getAddress());
                toDelete.add(fastReceiver);
            }
        }

        for (FastReceiver deleteReceiver : toDelete) {
            data.remove(deleteReceiver);
        }

        notifyDataSetChanged();
    }

    public void setData(List<FastReceiver> receivers) {
        List<FastReceiver> toDelete = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (!receivers.contains(data.get(i))) {
                toDelete.add(data.get(i));
                registeredFragments.remove(i);
            }
        }


        for (FastReceiver deleteReceiver : toDelete) {
            data.remove(deleteReceiver);
        }

        for (FastReceiver newReceiver : receivers) {
            if (!data.contains(newReceiver)) {
                final int index = data.size() == 0 ? 0 : data.size() - 1;
                data.add(index, newReceiver);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public void showSuccess(int position) {
        FastReceiverFragment fragment = getFragment(position);
        if (fragment != null) {
            fragment.animateSuccess();
        }
    }

    public void setGreenState(int position) {
        FastReceiverFragment fragment = getFragment(position);
        if (fragment != null) {
            fragment.setGreenColorMode();
        }
    }

    public void resetColorState(int position) {
        FastReceiverFragment fragment = getFragment(position);
        if (fragment != null) {
            fragment.setNormalColorMode();
        }
    }

    public FastReceiverFragment getFragment(int position) {
        FastReceiverFragment fragment = (FastReceiverFragment) getRegisteredFragment(position);
        return fragment;
    }

    public void hideElements(int currentPosition) {
        FastReceiverFragment left = getFragmentAtPosition(currentPosition - 1);
        if (left != null) {
            left.hideLeft();
        }

        FastReceiverFragment right = getFragmentAtPosition(currentPosition + 1);
        if (right != null) {
            right.hideRight();
        }
    }

    public void showElements(int currentPosition) {
        FastReceiverFragment left = getFragmentAtPosition(currentPosition - 1);
        if (left != null) {
            left.showLeft();
        }

        FastReceiverFragment right = getFragmentAtPosition(currentPosition + 1);
        if (right != null) {
            right.showRight();
        }
    }

    private FastReceiverFragment getFragmentAtPosition(int position) {
        FastReceiverFragment fragment = (FastReceiverFragment) getRegisteredFragment(position);
        return fragment;
    }
}
