/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import io.multy.R;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.viewmodels.BaseViewModel;

public class BaseFragment extends Fragment {

    private BaseViewModel baseViewModel;
    private ProgressDialog progressDialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscribeToErrors();
    }

    protected BaseViewModel getBaseViewModel() {
        return baseViewModel;
    }

    protected void setBaseViewModel(BaseViewModel baseViewModel) {
        this.baseViewModel = baseViewModel;
    }

    protected void subscribeToErrors() {
        if (baseViewModel != null) {
            baseViewModel.errorMessage.observe(this, s -> {
                SimpleDialogFragment dialog = SimpleDialogFragment.newInstanceNegative(getString(R.string.error), baseViewModel.errorMessage.getValue(),
                        null);
                dialog.setTitleResId(R.string.error);
                dialog.setMessageResId(R.string.error);
                dialog.show(getFragmentManager(), "");
            });

            baseViewModel.isLoading.observe(this, aBoolean -> {
                if (aBoolean != null) {
                    if (aBoolean) {
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(BaseFragment.this.getActivity());
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                        } else {
                            progressDialog.show();
                        }
                    } else {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                }
            });

            baseViewModel.criticalMessage.observe(this, s -> {
                //TODO show critical message and maybe exit?
            });
        }
    }



}
