/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.WalletViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAssetFragment extends BaseFragment {

    public static final String TAG = CreateAssetFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText editTextWalletName;
    @BindView(R.id.text_create)
    TextView textViewCreateWallet;
    @BindView(R.id.text_fiat_currency)
    TextView textViewFiatCurrency;
    @BindView(R.id.text_chain_currency)
    TextView textViewChainCurrency;

    private boolean isFirstStart = false;
    private WalletViewModel walletViewModel;

    public static CreateAssetFragment newInstance() {
        return new CreateAssetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        View v = inflater.inflate(R.layout.view_assets_action_add, container, false);
        ButterKnife.bind(this, v);
        initialize();
        subscribeToCurrencyUpdate();
        Analytics.getInstance(getActivity()).logCreateWalletLaunch();

        if (getArguments() != null) {
            isFirstStart = getArguments().getBoolean(CreateAssetActivity.EXTRA_IS_FIRST_START, false);
        }

        editTextWalletName.requestFocus();
        editTextWalletName.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }

            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }, 100);
        return v;
    }

    private void subscribeToCurrencyUpdate() {
        walletViewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(walletViewModel);
        walletViewModel.fiatCurrency.observe(this, s -> {
            //TODO update wallet fiat currency
            textViewFiatCurrency.setText(s);
        });
        walletViewModel.chainCurrency.observe(this, s -> {
            //TODO update wallet chain currency
            textViewChainCurrency.setText(s);
        });
    }

    private void initialize() {
        editTextWalletName.addTextChangedListener(getEditWatcher());
    }

    private TextWatcher getEditWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    if (textViewChainCurrency.getText().length() > 0 && textViewFiatCurrency.getText().length() > 0) {
                        textViewCreateWallet.setEnabled(true);
                        textViewCreateWallet.setBackgroundColor(Color.parseColor("#FF459FF9"));
                    }
                } else {
                    textViewCreateWallet.setEnabled(false);
                    textViewCreateWallet.setBackgroundColor(Color.parseColor("#BEC8D2"));
                }
            }
        };
    }

    private void showWalletInfoActivity(WalletRealmObject walletRealmObject) {
        Intent intent = new Intent(getActivity(), AssetActivity.class);
        if (walletRealmObject != null) {
            intent.putExtra(Constants.EXTRA_WALLET_ID, walletRealmObject.getWalletIndex());
        }

        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.button_chain)
    public void onClickChain() {
        Analytics.getInstance(getActivity()).logCreateWalletChain();
//        ArrayList<String> chains = new ArrayList<>(2);
//        chains.add(Constants.BTC);
//        chains.add(Constants.ETH);
//        ListDialogFragment.newInstance(chains, CurrencyType.CHAIN).show(getFragmentManager(), "");
        if (!isFirstStart && getActivity() != null) {
            ChainChooserFragment fragment = (ChainChooserFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(ChainChooserFragment.TAG);
            if (fragment == null) {
                fragment = ChainChooserFragment.getInstance();
            }
            fragment.setSelectedChain(textViewChainCurrency.getText().toString());
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_main, fragment).addToBackStack(ChainChooserFragment.TAG)
                    .commit();
        }
    }

    @OnClick(R.id.button_fiat)
    public void onClickFiat() {
        Analytics.getInstance(getActivity()).logCreateWalletFiatClick();
//        ArrayList<String> chains = new ArrayList<>(3);
//        chains.add(Constants.USD);
//        chains.add(Constants.EUR);
//        ListDialogFragment.newInstance(chains, CurrencyType.FIAT).show(getFragmentManager(), "");
        if (!isFirstStart && getActivity() != null) {
            CurrencyChooserFragment fragment = (CurrencyChooserFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(CurrencyChooserFragment.TAG);
            if (fragment == null) {
                fragment = CurrencyChooserFragment.getInstance();
            }
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_main, fragment).addToBackStack(ChainChooserFragment.TAG)
                    .commit();
        }
    }

    @OnClick(R.id.text_create)
    public void onClickCreate() {
        Analytics.getInstance(getActivity()).logCreateWallet();
        WalletRealmObject walletRealmObject = walletViewModel.createWallet(editTextWalletName.getText().toString());
        MultyApi.INSTANCE.addWallet(getActivity(), walletRealmObject).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                walletViewModel.isLoading.setValue(false);
                if (response.isSuccessful()) {
                    RealmManager.getAssetsDao().saveWallet(walletRealmObject);
                    showWalletInfoActivity(walletRealmObject);
                } else {
                    walletViewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                walletViewModel.isLoading.setValue(false);
                walletViewModel.errorMessage.setValue(t.getLocalizedMessage());
                t.printStackTrace();
            }
        });
    }

    @OnClick(R.id.text_cancel)
    void onCancelClick() {
        Analytics.getInstance(getActivity()).logCreateWalletClose();
        getActivity().finish();
    }
}
