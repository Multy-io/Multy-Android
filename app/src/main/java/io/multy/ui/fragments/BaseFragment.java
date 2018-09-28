/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import io.multy.R;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.dialogs.NoConnectionDialogFragment;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.util.ConnectionReceiver;
import io.multy.viewmodels.BaseViewModel;
import timber.log.Timber;

public class BaseFragment extends Fragment implements ConnectionReceiver.ConnectionReceiverListener {

    private BaseViewModel baseViewModel;
    private Dialog progressDialog;
    private NoConnectionDialogFragment noConnectionDialog;
    private ConnectionReceiver receiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (receiver == null) {
            receiver = new ConnectionReceiver();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        subscribeToErrors();
    }

    @Override
    public void onStart() {
        super.onStart();
        setConnectionListener(this);
        if (getActivity() != null && !(getActivity() instanceof MainActivity)) {
            getActivity().registerReceiver(receiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void onStop() {
        if (getActivity() != null && !(getActivity() instanceof MainActivity)) {
            getActivity().unregisterReceiver(receiver);
        }
        super.onStop();
    }

    protected BaseViewModel getBaseViewModel() {
        return baseViewModel;
    }

    protected void setBaseViewModel(BaseViewModel baseViewModel) {
        this.baseViewModel = baseViewModel;
    }

    protected void showMessage(String message) {
        SimpleDialogFragment dialog = SimpleDialogFragment.newInstanceNegative(getString(R.string.error), message, null);
        dialog.setTitleResId(R.string.error);
        dialog.setMessageResId(R.string.error);
        dialog.show(requireFragmentManager(), "");
    }

    protected void subscribeToErrors() {
        if (baseViewModel != null) {
            baseViewModel.errorMessage.observe(this, this::showMessage);

            baseViewModel.isLoading.observe(this, aBoolean -> {
                if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && aBoolean != null) {
                    if (aBoolean) {
                        showProgressDialog();
                    } else {
                        dismissProgressDialog();
                    }
                }
            });

            baseViewModel.criticalMessage.observe(this, s -> {
                //TODO show critical message and maybe exit?
            });
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new Dialog(getActivity());
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setContentView(R.layout.dialog_spinner);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void showKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInputFromInputMethod(activity.getWindow().getDecorView().getWindowToken(), InputMethodManager.SHOW_IMPLICIT);
//        InputMethodManager inputMethodManager = (InputMethodManager)
//                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (inputMethodManager != null) {
//            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        }
    }

    public void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Timber.e("onNetworkConnectionChanged");
        if (!isConnected) {
            if (baseViewModel != null && baseViewModel.isConnectionAvailable.getValue() != null) {
                baseViewModel.isConnectionAvailable.setValue(isConnected);
            }
            showNoConnectionDialog();
        }
    }

    protected void showNoConnectionDialog() {
        if (noConnectionDialog == null) {
            noConnectionDialog = new NoConnectionDialogFragment();
            noConnectionDialog.setCancelable(false);
        }
        if (!noConnectionDialog.isShowing().get()) {
            noConnectionDialog.isShowing().set(true);
            noConnectionDialog.show(getFragmentManager(), NoConnectionDialogFragment.class.getSimpleName());
        }
    }

    public void setConnectionListener(ConnectionReceiver.ConnectionReceiverListener listener) {
        ConnectionReceiver.connectionReceiverListener = listener;
    }

    public void showKeyboard(Activity activity, View view) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(view, 0);
            }
        }
    }
}
