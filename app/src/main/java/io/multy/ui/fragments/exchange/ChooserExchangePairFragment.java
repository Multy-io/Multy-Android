/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.exchange;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.ExchangeAsset;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.ExchangePairAdapter;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.send.WalletChooserFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.AssetSendViewModel;
import io.multy.viewmodels.ExchangeViewModel;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooserExchangePairFragment extends BaseFragment implements ExchangePairAdapter.OnAssetClickListener {

    private ExchangeViewModel viewModel;
    private Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    MutableLiveData<List<ExchangeAsset>> assets = new MutableLiveData<>();


    public static ChooserExchangePairFragment newInstance() { return new ChooserExchangePairFragment();}



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chooser_exchange_pair, container, false);
        this.unbinder = ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(ExchangeViewModel.class);
        this.assets = viewModel.getAssets();
        setupAdapter();
//        viewModel.getAssetsList();
        return view;
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        unbinder.unbind();
        super.onDestroyView();
    }

    private void setupAdapter() {

        assets.observe(this, exAssets ->{
            ExchangePairAdapter adapter = new ExchangePairAdapter(this, exAssets);
            recyclerView.setAdapter(adapter);
//            adapter.setData(exAssets);

        });


//        RealmChangeListener<RealmResults<Wallet>> listener = adapter::setData;
//        final LifecycleObserver observer = new LifecycleObserver() {
//            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//            public void onResume() {
//                wallets.addChangeListener(listener);
//            }
//
//            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//            public void onPause() {
//                if (wallets != null && wallets.isValid()) {
//                    wallets.removeAllChangeListeners();
//                }
//            }
//        };
//        getLifecycle().addObserver(observer);
    }

    @Override
    public void onAssetClick(ExchangeAsset asset) {



        viewModel.setSelectedAsset(asset);
//        MultyApi.INSTANCE.getWalletVerbose(wallet.getIndex(), wallet.getCurrencyId(), wallet.getNetworkId(), wallet.isMultisig() ?
//                Constants.ASSET_TYPE_ADDRESS_MULTISIG : Constants.ASSET_TYPE_ADDRESS_MULTY).enqueue(new Callback<SingleWalletResponse>() {
//            @Override
//            public void onResponse(Call<SingleWalletResponse> call, Response<SingleWalletResponse> response) {
//                viewModel.isLoading.setValue(false);
//                if (response.isSuccessful() && response.body().getWallets() != null && response.body().getWallets().size() > 0) {
//                    Wallet wallet = response.body().getWallets().get(0);
//                    RealmManager.getAssetsDao().saveWallet(wallet);
//                    viewModel.setWallet(RealmManager.getAssetsDao().getWalletById(wallet.getId()));
//                    proceed(viewModel.getWallet());
//                } else {
//                    viewModel.setWallet(wallet);
//                    proceed(wallet);
//                }
//
//                viewModel.isLoading.postValue(false);
//            }
//
//            @Override
//            public void onFailure(Call<SingleWalletResponse> call, Throwable t) {
//                viewModel.isLoading.postValue(false);
////                proceed(wallet);
//            }
//        });
    }

}
