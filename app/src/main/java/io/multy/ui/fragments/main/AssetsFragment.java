/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.events.TransactionUpdateEvent;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.adapters.PortfoliosAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.AssetActionsDialogFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetsViewModel;
import io.multy.viewmodels.WalletViewModel;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetsFragment extends BaseFragment implements MyWalletsAdapter.OnWalletClickListener {

    public static final String TAG = AssetsFragment.class.getSimpleName();

    @BindView(R.id.nested)
    NestedScrollView scrollView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.group_wallets_list)
    Group groupWalletsList;
    @BindView(R.id.group_create_description)
    Group groupCreateDescription;
    @BindView(R.id.group_multy_logo)
    Group groupMultyLogo;
    @BindView(R.id.group_portfolio)
    Group groupPortfolio;
    @BindView(R.id.button_add)
    ImageButton buttonAdd;
    @BindView(R.id.container_create_restore)
    ConstraintLayout containerCreateRestore;
    @BindView(R.id.button_warn)
    View buttonWarn;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.image_dot_portfolio)
    ImageView imageDotPortfolio;
    @BindView(R.id.image_dot_chart)
    ImageView imageDotChart;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout refreshLayout;

    private AssetsViewModel viewModel;
    private MyWalletsAdapter walletsAdapter;
    private boolean isViewsScroll = false;

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
        if (!viewModel.isFirstStart()) {
            Analytics.getInstance(getActivity()).logMainLaunch();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if ((requestCode == Constants.REQUEST_CODE_RESTORE || requestCode == Constants.REQUEST_CODE_CREATE) && resultCode == Activity.RESULT_OK) {
        checkViewsVisibility();
        initialize();
        ((BaseActivity) getActivity()).subscribeToPushNotifications();
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onWalletClick(Wallet wallet) {
        Analytics.getInstance(getActivity()).logMainWalletOpen(viewModel.getChainId());
        Intent intent = new Intent(getActivity(), AssetActivity.class);
        intent.putExtra(Constants.EXTRA_WALLET_ID, wallet.getId());
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdateEvent(TransactionUpdateEvent event) {
        Log.i(TAG, "transaction update event called");
        updateWallets();
    }

    private void initialize() {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            if (RealmManager.getSettingsDao().getUserId() != null) {
                viewModel.rates.observe(this, currenciesRate -> {
                    RealmManager.getSettingsDao().saveCurrenciesRate(currenciesRate, () -> {
                        if (walletsAdapter != null && !isViewsScroll) {
                            walletsAdapter.updateRates(currenciesRate);
                        }
                    });
                });
                viewModel.transactionUpdate.observe(this, transactionUpdateEntity -> {
                    updateWallets();
                });
                viewModel.init(getLifecycle());
            }
        }
        recyclerView.setNestedScrollingEnabled(false);
        viewPager.setAdapter(new PortfoliosAdapter(getChildFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    imageDotPortfolio.setAlpha(1f);
                    imageDotChart.setAlpha(0.3f);
                } else {
                    imageDotPortfolio.setAlpha(0.3f);
                    imageDotChart.setAlpha(1f);
                }
                scrollView.fling(0);
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
        scrollView.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                isViewsScroll = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL ||
                    motionEvent.getAction() == MotionEvent.ACTION_UP) {
                isViewsScroll = false;
            }
            return false;
        });

        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            WalletViewModel.saveDonateAddresses();
            refreshLayout.setOnRefreshListener(() -> {
                updateWallets();
            });
        }
        refreshLayout.setOnChildScrollUpCallback((parent, child) -> recyclerView.getVisibility() != View.VISIBLE);

    }

    private void checkViewsVisibility() {
        if (viewModel.isFirstStart()) {
            groupWalletsList.setVisibility(View.GONE);
            containerCreateRestore.setVisibility(View.VISIBLE);
            groupPortfolio.setVisibility(View.GONE);
            groupMultyLogo.setVisibility(View.VISIBLE);
            buttonWarn.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            initList();
            updateWallets();
            recyclerView.setVisibility(View.VISIBLE);
            buttonWarn.setVisibility(Prefs.getBoolean(Constants.PREF_BACKUP_SEED) ? View.GONE : View.VISIBLE);
            groupWalletsList.setVisibility(View.VISIBLE);
            containerCreateRestore.setVisibility(View.GONE);
            groupPortfolio.setVisibility(View.VISIBLE);
            groupMultyLogo.setVisibility(View.INVISIBLE);
            groupCreateDescription.setVisibility(View.GONE);
        }
    }

    private void updateWallets() {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                if (response.body() != null) {
                    //TODO COMPARE WALLET CURRENCY ID AND TOP INDEX CURRENCY ID
                    response.body().saveBtcTopWalletIndex();
                    response.body().saveEthTopWalletIndex();
                    response.body().saveEosTopWalletIndex();
                    AssetsDao assetsDao = RealmManager.getAssetsDao();
                    if (response.body().getWallets() != null && response.body().getWallets().size() != 0) {
                        assetsDao.deleteAll();
                        assetsDao.saveWallets(response.body().getWallets());
                    }
                    RealmResults<Wallet> realmResults = assetsDao.getWallets();
                    walletsAdapter.setData(realmResults);
                    if (realmResults.size() > 0) {
                        groupCreateDescription.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        groupCreateDescription.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void initList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);

        walletsAdapter = new MyWalletsAdapter(this, viewModel.getWalletsFromDB());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(walletsAdapter);
//        walletsAdapter.setData(viewModel.getWalletsFromDB());
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
//        startActivityForResult(new Intent(getActivity(), CreateAssetActivity.class)
//                /*.addCategory(Constants.EXTRA_RESTORE)*/, Constants.REQUEST_CODE_CREATE);
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            refreshLayout.setRefreshing(true);
            ((MainActivity) getActivity()).createFirstWallets();
        }
    }

    @OnClick(R.id.button_add)
    void onClickAdd(View v) {
        if (getActivity() != null) {
            v.setEnabled(false);
            Analytics.getInstance(getActivity()).logMain(AnalyticsConstants.MAIN_CREATE_WALLET);
            AssetActionsDialogFragment fragment = AssetActionsDialogFragment.getInstance();
            fragment.setListener(() -> v.setEnabled(true));
            fragment.show(getActivity().getSupportFragmentManager(), AssetActionsDialogFragment.TAG);
        }
    }

    @OnClick(R.id.button_create)
    void onClickCreate() {
//        showAddWalletActions();
        Analytics.getInstance(getActivity()).logFirstLaunchCreateWallet();
        onWalletAddClick();
    }

    @OnClick(R.id.button_restore)
    void onClickRestore() {
        Analytics.getInstance(getActivity()).logFirstLaunchRestoreSeed();
        startActivityForResult(new Intent(getActivity(), SeedActivity.class).addCategory(Constants.EXTRA_RESTORE), Constants.REQUEST_CODE_RESTORE);
    }

    @OnClick(R.id.button_warn)
    void onClickWarn() {
        Analytics.getInstance(getActivity()).logMain(AnalyticsConstants.MAIN_BACKUP_SEED);
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

    @OnClick(R.id.logo)
    void onClickLogo() {
        Analytics.getInstance(getActivity()).logMain(AnalyticsConstants.MAIN_LOGO);
    }
}
