/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Native;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.BuildConfig;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.storage.SettingsDao;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.adapters.AddressesAdapter;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressesFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_title)
    TextView textViewTitle;
    @BindView(R.id.btn_add)
    View buttonAddAddress;

    private WalletViewModel viewModel;

    public static AddressesFragment newInstance(long walletId) {
        AddressesFragment addressesFragment = new AddressesFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(Constants.EXTRA_WALLET_ID, walletId);
        addressesFragment.setArguments(arguments);
        return addressesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_addresses, container, false);
        ButterKnife.bind(this, convertView);

        if (getActivity() instanceof AssetRequestActivity) {
            toolbar.setVisibility(View.GONE);
        }
        Analytics.getInstance(getActivity()).logWalletAddressesLaunch(1);
        buttonAddAddress.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
//        buttonAddAddress.setVisibility(View.GONE);
        return convertView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments().getLong(Constants.EXTRA_WALLET_ID) != -1) {
            Wallet wallet = RealmManager.getAssetsDao().getWalletById(getArguments().getLong(Constants.EXTRA_WALLET_ID, -1));
            textViewTitle.setText(wallet.getWalletName());
            recyclerView.setAdapter(new AddressesAdapter(wallet.getBtcWallet().getAddresses()));
        } else {
            Toast.makeText(getActivity(), R.string.addresses_empty, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_back)
    public void onClickCancel() {
        getActivity().onBackPressed();
    }

    private void refreshWallet() {
        if (!viewModel.getWalletLive().getValue().isValid()) {
            Wallet wallet = viewModel.getWallet(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            viewModel.getWalletLive().setValue(wallet);
        }

        final int walletIndex = viewModel.getWalletLive().getValue().getIndex();
        final int currencyId = viewModel.getWalletLive().getValue().getCurrencyId();
        final int networkId = viewModel.getWalletLive().getValue().getNetworkId();
        final long walletId = viewModel.getWalletLive().getValue().getId();

        viewModel.isLoading.setValue(true);
        MultyApi.INSTANCE.getWalletVerbose(walletIndex, currencyId, networkId).enqueue(new Callback<SingleWalletResponse>() {
            @Override
            public void onResponse(Call<SingleWalletResponse> call, Response<SingleWalletResponse> response) {
                viewModel.isLoading.postValue(false);
                if (response.isSuccessful() && response.body().getWallets() != null && response.body().getWallets().size() > 0) {
                    AssetsDao assetsDao = RealmManager.getAssetsDao();
                    assetsDao.saveWallet(response.body().getWallets().get(0));
                    viewModel.wallet.postValue(assetsDao.getWalletById(walletId));
                    recyclerView.setAdapter(new AddressesAdapter(response.body().getWallets().get(0).getBtcWallet().getAddresses()));
                }
            }

            @Override
            public void onFailure(Call<SingleWalletResponse> call, Throwable t) {
                viewModel.isLoading.setValue(false);
                t.printStackTrace();
            }
        });
    }

    @OnClick(R.id.btn_add)
    public void onClickAdd() {
        try {
            Wallet wallet = RealmManager.getAssetsDao().getWalletById(getArguments().getLong(Constants.EXTRA_WALLET_ID, -1));
            final int nextAddressIndex = wallet.getBtcWallet().getAddresses().size();
            final String address = NativeDataHelper.makeAccountAddress(RealmManager.getSettingsDao().getSeed().getSeed(), wallet.getIndex(), nextAddressIndex, wallet.getCurrencyId(), wallet.getNetworkId());

            MultyApi.INSTANCE.addWalletAddress(new AddWalletAddressRequest(wallet.getIndex(), address, nextAddressIndex)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Address has been added", Toast.LENGTH_SHORT).show();
                        refreshWallet();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (JniException e) {
            e.printStackTrace();
        }
    }
}
