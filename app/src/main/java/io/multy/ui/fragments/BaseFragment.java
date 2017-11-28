/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

    public void showLocalError() {
    }

    public void showPinDialog() {
    }

    public boolean isDeviceRooted() {
        return false;
    }

    public boolean isDeviceEmulator() {
        return false;
    }

}
