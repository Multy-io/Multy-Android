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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.samwolfand.oneprefs.Prefs;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetsViewModel;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetsFragment extends BaseFragment {

    public static final String TAG = AssetsFragment.class.getSimpleName();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.group_wallets_list)
    Group groupWalletsList;
    @BindView(R.id.group_create_description)
    Group groupCreateDescription;
    @BindView(R.id.button_add)
    ImageButton buttonAdd;
    @BindView(R.id.container_create_restore)
    ConstraintLayout containerCreateRestore;
    @BindView(R.id.button_warn)
    View buttonWarn;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;


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
        initialize();
        return view;
    }

    private void initialize() {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            if (RealmManager.getSettingsDao().getUserId() != null) {
                viewModel.rates.observe(this, currenciesRate -> {
                    RealmManager.getSettingsDao().saveCurrenciesRate(currenciesRate);
                    if (walletsAdapter != null) {
                        RealmManager.getSettingsDao().saveCurrenciesRate(currenciesRate);
                        walletsAdapter.notifyDataSetChanged();
                    }
                });
                viewModel.transactionUpdate.observe(this, transactionUpdateEntity -> {
                    updateWallets();
                });
                viewModel.init(getLifecycle());
            }
        }
        appBarLayout.post(() -> disableAppBarScrolling());
    }

    private void disableAppBarScrolling() {
        if (!ViewCompat.isLaidOut(appBarLayout)) {
            return;
        }
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }

    private void checkViewsVisibility() {
        if (viewModel.isFirstStart()) {
            groupWalletsList.setVisibility(View.GONE);
            containerCreateRestore.setVisibility(View.VISIBLE);
            buttonWarn.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            initList();
            updateWallets();
            recyclerView.setVisibility(View.VISIBLE);
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
                    AssetsDao assetsDao = RealmManager.getAssetsDao();
                    if (response.body().getWallets() != null && response.body().getWallets().size() != 0) {
                        assetsDao.saveWallets(response.body().getWallets());
                        walletsAdapter.setData(assetsDao.getWallets());
                    } else {
                        RealmResults realmResults = assetsDao.getWallets();
                        if (realmResults != null && realmResults.size() > 0) {
                            walletsAdapter.setData(realmResults);
                            groupCreateDescription.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            walletsAdapter.setData(new ArrayList<>());
                            groupCreateDescription.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
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
        startActivityForResult(new Intent(getActivity(), CreateAssetActivity.class).addCategory(Constants.EXTRA_RESTORE), Constants.REQUEST_CODE_CREATE);
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
        startActivityForResult(new Intent(getActivity(), SeedActivity.class).addCategory(Constants.EXTRA_RESTORE), Constants.REQUEST_CODE_RESTORE);
    }

    @OnClick(R.id.button_warn)
    void onClickWarn() {
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if ((requestCode == Constants.REQUEST_CODE_RESTORE || requestCode == Constants.REQUEST_CODE_CREATE) && resultCode == Activity.RESULT_OK) {
            checkViewsVisibility();
            initialize();
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
