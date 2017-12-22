/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;
import com.samwolfand.oneprefs.Prefs;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.Output;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.WalletsResponse;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.FirstLaunchHelper;
import io.multy.util.JniException;
import io.multy.viewmodels.AssetsViewModel;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetsFragment extends BaseFragment {

    public static final String TAG = AssetsFragment.class.getSimpleName();

    @BindView(R.id.recycler_wallets)
    RecyclerView recyclerWallets;
    @BindView(R.id.group_wallets_list)
    Group groupWalletsList;
    @BindView(R.id.group_create_description)
    Group groupCreateDescription;
    @BindView(R.id.button_add)
    FloatingActionButton buttonAdd;
    @BindView(R.id.container_create_restore)
    ConstraintLayout containerCreateRestore;
    @BindView(R.id.chart)
    SparkView sparkView;

    private AssetsViewModel viewModel;
    private WalletsAdapter walletsAdapter;

    public static AssetsFragment newInstance() {
        return new AssetsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<WalletRealmObject> wallets = new ArrayList<>();
        WalletRealmObject wallet = new DataManager(getActivity()).getWallet();
        if (wallet != null) {
            wallets.add(wallet);
        }
        walletsAdapter = new WalletsAdapter(wallets);
        walletsAdapter = new WalletsAdapter(new ArrayList<>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);
        subscribeViewModel();
        ButterKnife.bind(this, view);
        initialize();
        viewModel = ViewModelProviders.of(getActivity()).get(AssetsViewModel.class);
        viewModel.setContext(getActivity());

        if (new DataManager(getActivity()).getUserId() != null) {
            viewModel.rates.observe(this, currenciesRate -> walletsAdapter.updateRates(currenciesRate));
            viewModel.init(getLifecycle());
            viewModel.graphPoints.observe(this, graphPoints -> {
                float[] values = new float[graphPoints.size()];
                String[] stamps = new String[graphPoints.size()];

                for (int i = 0; i < graphPoints.size(); i++) {
                    values[i] = graphPoints.get(i).getPrice();
                    stamps[i] = graphPoints.get(i).getDate();

                    sparkView.setLineColor(getResources().getColor(R.color.colorPrimaryDark));
                    sparkView.setAdapter(new SparkAdapter() {
                        @Override
                        public int getCount() {
                            return graphPoints.size();
                        }

                        @Override
                        public Object getItem(int index) {
                            return stamps[index];
                        }

                        @Override
                        public float getY(int index) {
                            return values[index];
                        }

                        @Override
                        public boolean hasBaseLine() {
                            return false;
                        }
                    });
                }
            });
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.isFirstStart()) {
            groupWalletsList.setVisibility(View.GONE);
            containerCreateRestore.setVisibility(View.VISIBLE);
        } else {
            groupWalletsList.setVisibility(View.VISIBLE);
            containerCreateRestore.setVisibility(View.GONE);
            groupCreateDescription.setVisibility(View.GONE);
        }

        walletsAdapter.setData(viewModel.getWalletsFromDB());
        if (Prefs.contains(Constants.PREF_IS_FIRST_START)) {
            updateWallets();
        }
    }

    private void updateWallets() {
        DataManager dataManager = new DataManager(getActivity());
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                if (response.body() != null && response.body().getWallets() != null) {
                    for (WalletRealmObject wallet : response.body().getWallets()) {
                        dataManager.updateWallet(wallet.getWalletIndex(), wallet.getAddresses(), wallet.calculateBalance(), wallet.calculatePendingBalance());
                    }
                    walletsAdapter.setData(viewModel.getWalletsFromDB());

                    RealmResults<WalletRealmObject> wallets = dataManager.getWallets();
                    Log.i(TAG, "wallets " + wallets.size());
                    WalletRealmObject walletRealmObject = wallets.get(0);
                    if (walletRealmObject.getAddresses() == null) {
                        Log.i(TAG, "addresses EMPTY");
                    } else {
                        Log.i(TAG, "addresses " + walletRealmObject.getAddresses().size());
                        for (WalletAddress addr : walletRealmObject.getAddresses()) {
                            Log.i(TAG, "addr " + addr);

                            if (addr.getOutputs() != null) {
                                Log.i(TAG, "outs " + addr.getOutputs().size());
                                for (Output output : addr.getOutputs()) {
                                    Log.v(TAG, output.toString());
                                }
                            } else {
                                Log.i(TAG, "outs == null");
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onPause() {
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager()
                .findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }

    private void initialize() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerWallets.setLayoutManager(layoutManager);
        recyclerWallets.setNestedScrollingEnabled(false);
        setAdapter();
    }

    private void subscribeViewModel() {
        viewModel = ViewModelProviders.of(this).get(AssetsViewModel.class);
    }

    private void setAdapter() {
        recyclerWallets.setAdapter(walletsAdapter);
    }

    private void showAddWalletActions() {
        WalletActionsDialog.Callback callback = new WalletActionsDialog.Callback() {
            @Override
            public void onCardAddClick() {
                onWalletAddClick();
            }

            @Override
            public void onCardImportClick() {
                onWalletImportClick();
            }
        };
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager().findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog == null) {
            dialog = WalletActionsDialog.newInstance(callback);
        }
        dialog.show(getChildFragmentManager(), WalletActionsDialog.TAG);
    }

    private void onWalletImportClick() {

    }

    private void onWalletAddClick() {
        startActivity(new Intent(getContext(), CreateAssetActivity.class));
    }

    @OnClick(R.id.button_add)
    void onPlusClick() {
        showAddWalletActions();
    }

    @OnClick(R.id.button_create)
    void onCLickCreateWallet() {
        groupCreateDescription.setVisibility(View.VISIBLE);
        groupWalletsList.setVisibility(View.VISIBLE);
        containerCreateRestore.setVisibility(View.GONE);
        Prefs.putBoolean(Constants.PREF_IS_FIRST_START, false);

        try {
            FirstLaunchHelper.setCredentials(null, getActivity());
        } catch (JniException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_restore)
    void onCLickRestoreSeed() {
        groupCreateDescription.setVisibility(View.GONE);
        groupWalletsList.setVisibility(View.VISIBLE);
        startActivity(new Intent(getActivity(), SeedActivity.class).addCategory(Constants.EXTRA_RESTORE));
        Prefs.putBoolean(Constants.PREF_IS_FIRST_START, false);
    }
}
