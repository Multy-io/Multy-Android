/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetsViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetsFragment extends BaseFragment {

    public static final String TAG = AssetsFragment.class.getSimpleName();

    @BindView(R.id.recycler_wallets)
    RecyclerView recyclerView;
    @BindView(R.id.group_wallets_list)
    Group groupWalletsList;
    @BindView(R.id.group_create_description)
    Group groupCreateDescription;
    @BindView(R.id.button_add)
    FloatingActionButton buttonAdd;
    @BindView(R.id.container_create_restore)
    ConstraintLayout containerCreateRestore;
    @BindView(R.id.button_warn)
    View buttonWarn;

    private AssetsViewModel viewModel;
    private WalletsAdapter walletsAdapter;

    public static AssetsFragment newInstance() {
        return new AssetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(AssetsViewModel.class);

        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)){
            if (RealmManager.getSettingsDao().getUserId() != null) {
                viewModel.rates.observe(this, new Observer<CurrenciesRate>() {
                    @Override
                    public void onChanged(@Nullable CurrenciesRate currenciesRate) {
                        RealmManager.getSettingsDao().saveCurrenciesRate(currenciesRate);
                        if (walletsAdapter != null) {
                            walletsAdapter.updateRates(currenciesRate);
                        }
                    }
                });
                viewModel.init(getLifecycle());
            }
        }
        return view;
    }

    public static float getMaxValue(float[] array) {
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    public static float getMinValue(float[] array) {
        float minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    private void checkViewsVisibility() {
        if (viewModel.isFirstStart()) {
            groupWalletsList.setVisibility(View.GONE);
            containerCreateRestore.setVisibility(View.VISIBLE);
            buttonWarn.setVisibility(View.GONE);
        } else {
            initList();
            updateWallets();
            buttonWarn.setVisibility(Prefs.getBoolean(Constants.PREF_BACKUP_SEED) ? View.GONE : View.VISIBLE);
            groupWalletsList.setVisibility(View.VISIBLE);
            containerCreateRestore.setVisibility(View.GONE);
            groupCreateDescription.setVisibility(View.GONE);
        }
    }

    private void updateWallets() {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                if (response.body() != null) {
                    Prefs.putInt(Constants.PREF_WALLET_TOP_INDEX, response.body().getTopIndex());
                    if (response.body().getWallets() != null && response.body().getWallets().size() != 0) {
                        DataManager dataManager = DataManager.getInstance();
                        for (WalletRealmObject wallet : response.body().getWallets()) {
                            dataManager.saveWallet(wallet);
                        }
                        walletsAdapter.setData(dataManager.getWallets());
                    } else {
                        groupCreateDescription.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkViewsVisibility();
    }

    @Override
    public void onPause() {
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager().findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }

    private void initList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);

        walletsAdapter = new WalletsAdapter(null);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(walletsAdapter);
        walletsAdapter.setData(viewModel.getWalletsFromDB());
    }

    private void showAddWalletActions() {
        WalletActionsDialog.Callback callback = new WalletActionsDialog.Callback() {
            @Override
            public void onCardAddClick() {
                onWalletAddClick();
            }

            @Override
            public void onCardImportClick() {
            }
        };

        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager().findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog == null) {
            dialog = WalletActionsDialog.newInstance(callback);
        }
        dialog.show(getChildFragmentManager(), WalletActionsDialog.TAG);
    }

    private void onWalletAddClick() {
        startActivity(new Intent(getContext(), CreateAssetActivity.class));
    }

    @OnClick(R.id.button_add)
    void onClickAdd() {
//        showAddWalletActions();
        onWalletAddClick();
    }

    @OnClick(R.id.button_create)
    void onClickCreate() {
//        showAddWalletActions();
        onWalletAddClick();
    }

    @OnClick(R.id.button_restore)
    void onClickRestore() {
        startActivity(new Intent(getActivity(), SeedActivity.class).addCategory(Constants.EXTRA_RESTORE));
    }

    @OnClick(R.id.button_warn)
    void onClickWarn() {
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }
}
