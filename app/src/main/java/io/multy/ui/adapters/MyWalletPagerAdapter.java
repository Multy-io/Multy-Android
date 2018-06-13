/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.multy.model.entities.wallet.Wallet;
import io.multy.ui.fragments.FastWalletFragment;

public class MyWalletPagerAdapter extends FragmentStatePagerAdapter {

    private View.OnClickListener listener;
    private List<Wallet> data;
    SparseArray<FastWalletFragment> registeredFragments = new SparseArray<>();

    public MyWalletPagerAdapter(FragmentManager fm, View.OnClickListener listener, List<Wallet> data) {
        super(fm);
        this.listener = listener;
        this.data = data;
    }

    @Override
    public Fragment getItem(int position) {
        FastWalletFragment fastWalletFragment = new FastWalletFragment();
        fastWalletFragment.setWallet(data.get(position));
        fastWalletFragment.setListener(listener);
        return fastWalletFragment;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FastWalletFragment fragment = (FastWalletFragment) super.instantiateItem(container, position);
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

    public void hideElements(int currentPosition) {
        FastWalletFragment left = getFragmentAtPosition(currentPosition - 1);
        if (left != null) {
            left.hideLeft();
        }

        FastWalletFragment right = getFragmentAtPosition(currentPosition + 1);
        if (right != null) {
            right.hideRight();
        }
    }

    public void showElements(int currentPosition) {
        FastWalletFragment left = getFragmentAtPosition(currentPosition - 1);
        if (left != null) {
            left.showLeft();
        }

        FastWalletFragment right = getFragmentAtPosition(currentPosition + 1);
        if (right != null) {
            right.showRight();
        }
    }

    private FastWalletFragment getFragmentAtPosition(int position) {
        FastWalletFragment fragment = (FastWalletFragment) getRegisteredFragment(position);
        return fragment;
    }
}
