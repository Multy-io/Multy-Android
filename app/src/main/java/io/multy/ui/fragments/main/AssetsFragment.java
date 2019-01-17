/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.SocketManager;
import io.multy.model.entities.BrokenAddresses;
import io.multy.model.entities.wallet.BtcWallet;
import io.multy.model.entities.wallet.DiscoverableWalletInfo;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.model.entities.wallet.MultisigWallet;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletPrivateKey;
import io.multy.model.events.TransactionUpdateEvent;
import io.multy.model.requests.DiscoverWalletRequest;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.activities.CreateMultiSigActivity;
import io.multy.ui.activities.FastReceiveActivity;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.MyWalletsAdapter;
import io.multy.ui.adapters.PortfoliosAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.MultireceiverFragment;
import io.multy.ui.fragments.TotalBalanceFragment;
import io.multy.ui.fragments.dialogs.AssetActionsDialogFragment;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetsViewModel;
import io.multy.viewmodels.WalletViewModel;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

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
    @BindView(R.id.image_dot_multireceiver)
    ImageView imageDotMultireceiver;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout refreshLayout;

    private AssetsViewModel viewModel;
    private MyWalletsAdapter walletsAdapter;
    private SocketManager socketManager;
    private Wallet deepMagicWallet = null;
    private boolean isViewsScroll = false;
    private boolean checkMetamask = false;
    private static int BANNERS_COUNT = 2;
    private PortfoliosAdapter portfoliosAdapter;
    private BannersPagerAdapter bannersAdapter;
    private TotalBalanceFragment totalBalanceFragment;
    private MultireceiverFragment multireceiverFragment;

    public static AssetsFragment newInstance() {
        return new AssetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetsViewModel.class);
        setBaseViewModel(viewModel);
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
        if (socketManager != null) {
            socketManager.disconnect();
            socketManager = null;
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
        if (requestCode == Constants.REQUEST_CODE_METAMUSK && resultCode == RESULT_OK) {
            checkMetamask = true;
        }

//        if ((requestCode == Constants.REQUEST_CODE_RESTORE || requestCode == Constants.REQUEST_CODE_CREATE) && resultCode == Activity.RESULT_OK) {
        checkViewsVisibility();
        initialize();
        ((BaseActivity) getActivity()).subscribeToPushNotifications();
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void discoverWallets() {
        Timber.i("discover wallets called");
        List<DiscoverableWalletInfo> walletsToDiscover = new ArrayList<>(10);
        for (int i = 0; i < 9; i++) {
            try {
                walletsToDiscover.add(new DiscoverableWalletInfo(0, i, NativeDataHelper.makeAccountAddress(RealmManager.getSettingsDao().getSeed().getSeed(),
                        0, i, NativeDataHelper.Blockchain.ETH.getValue(), NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue()), "Account " + i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MultyApi.INSTANCE.discoverWallets(new DiscoverWalletRequest(walletsToDiscover, NativeDataHelper.Blockchain.ETH.getValue(), NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue())).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Timber.i("wallets discover " + response.isSuccessful());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Timber.i("wallets discover " + t.getLocalizedMessage());
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onWalletClick(Wallet wallet) {
        if (wallet.isValid() && !wallet.isSyncing()) {
            if (wallet.isMultisig() && wallet.getMultisigWallet().getDeployStatus() == Constants.DEPLOY_STATUS_PENDING) {
                showMessage(getString(R.string.wait_creation));
            } else {
                Analytics.getInstance(getActivity()).logMainWalletOpen(viewModel.getChainId());
                if (wallet.isMultisig() &&
                        (wallet.getMultisigWallet().getDeployStatus() == MultisigWallet.Status.CREATED ||
                                wallet.getMultisigWallet().getDeployStatus() == MultisigWallet.Status.READY)) {
                    //pre deploy period = waiting for members screen, pre choose
                    Intent intent = new Intent(getActivity(), CreateMultiSigActivity.class);
                    intent.putExtra(Constants.EXTRA_WALLET_ID, wallet.getId());
                    intent.putExtra(Constants.EXTRA_INVITE_CODE, wallet.getMultisigWallet().getInviteCode());
                    intent.putExtra(Constants.EXTRA_CREATE, true);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), AssetActivity.class);
                    intent.putExtra(Constants.EXTRA_WALLET_ID, wallet.getId());
                    startActivity(intent);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdateEvent(TransactionUpdateEvent event) {
        Log.i(TAG, "transaction update event called");
        updateWallets();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialize() {
        portfoliosAdapter = new PortfoliosAdapter(getChildFragmentManager());

        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            if (RealmManager.getSettingsDao().getUserId() != null) {
                viewModel.rates.observe(this, currenciesRate -> {
                    RealmManager.getSettingsDao().saveCurrenciesRate(currenciesRate, () -> {
                        if (walletsAdapter != null && !isViewsScroll) {
                            walletsAdapter.updateRates(currenciesRate);
                        }

                        if (portfoliosAdapter != null) {
                            portfoliosAdapter.notifyDataSetChanged();
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
        viewPager.setAdapter(portfoliosAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    imageDotPortfolio.setAlpha(1f);
                    imageDotMultireceiver.setAlpha(0.3f);
                } else {
                    imageDotPortfolio.setAlpha(0.3f);
                    imageDotMultireceiver.setAlpha(1f);
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
            refreshLayout.setOnRefreshListener(this::updateWallets);
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
            checkMultisigWallets();
            recyclerView.setVisibility(View.VISIBLE);
            buttonWarn.setVisibility(Prefs.getBoolean(Constants.PREF_BACKUP_SEED) ? View.GONE : View.VISIBLE);
            groupWalletsList.setVisibility(View.VISIBLE);
            containerCreateRestore.setVisibility(View.GONE);
            groupPortfolio.setVisibility(View.VISIBLE);
            groupMultyLogo.setVisibility(View.INVISIBLE);
            groupCreateDescription.setVisibility(View.GONE);
        }
    }

    private void checkMultisigWallets() {
        RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getMultisigWallets();
        if (wallets.size() > 0) {
            subscribeSocketsUpdate();
        }
    }

    private void subscribeSocketsUpdate() {
        try {
            if (socketManager == null) {
                socketManager = new SocketManager();
            }
            final String eventReceive = SocketManager.getEventReceive(RealmManager.getSettingsDao().getUserId().getUserId());
            socketManager.listenEvent(eventReceive, args -> updateWallets());
            socketManager.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void updateWallets() {
        Timber.i("update wallets called");
//        viewModel.isLoading.setValue(true);
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                if (response.body() != null) {
                    response.body().saveTopIndexes();
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

                    detectBrokenWallets();
                    createDeepMagicWallet();
                    Timber.i("check meta mask " + checkMetamask);
                    if (checkMetamask) {
                        checkMetamask = false;
                        discoverWallets();
                    }
                }
                refreshLayout.setRefreshing(false);
                viewModel.isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {
                t.printStackTrace();
                viewModel.isLoading.setValue(false);
            }
        });
    }

    private void createDeepMagicWallet() {
        Timber.i("create deep magic called " + (deepMagicWallet == null));
        if (deepMagicWallet != null) {
            final int blockChainId = deepMagicWallet.getCurrencyId();
            final int networkId = deepMagicWallet.getNetworkId();

            final int topIndex = blockChainId == NativeDataHelper.Blockchain.BTC.getValue() ?
                    Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX_BTC + networkId, 0) :
                    Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX_ETH + networkId, 0);

            String creationAddress = null;
            try {
                creationAddress = NativeDataHelper.makeAccountAddress(RealmManager.getSettingsDao().getSeed().getSeed(), topIndex, 0, blockChainId, networkId);
            } catch (JniException e) {
                getActivity().finish();
                e.printStackTrace();
            }

            Wallet walletRealmObject = new Wallet();
            walletRealmObject.setWalletName(deepMagicWallet.getWalletName());

            RealmList<WalletAddress> addresses = new RealmList<>();
            addresses.add(new WalletAddress(0, creationAddress));

            switch (NativeDataHelper.Blockchain.valueOf(blockChainId)) {
                case BTC:
                    walletRealmObject.setBtcWallet(new BtcWallet());
                    walletRealmObject.getBtcWallet().setAddresses(addresses);
                    break;
                case ETH:
                    walletRealmObject.setEthWallet(new EthWallet());
                    walletRealmObject.getEthWallet().setAddresses(addresses);
                    break;
            }

            walletRealmObject.setCurrencyId(blockChainId);
            walletRealmObject.setNetworkId(networkId);
            walletRealmObject.setCreationAddress(creationAddress);
            walletRealmObject.setIndex(topIndex);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MultyApi.INSTANCE.addWallet(getActivity(), walletRealmObject).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                try {
                                    String body = response.body().string();
                                    long dateOfCreation = new JSONObject(body).getLong("time");
                                    walletRealmObject.setDateOfCreation(dateOfCreation);
                                    RealmManager.getAssetsDao().saveWallet(walletRealmObject);
                                    Log.i("wise", "DDOS with id " + dateOfCreation);
                                    Log.i("wise", "wallet pre-id " + walletRealmObject.getId());
                                    ddosTrueResponse(topIndex);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }, 1000);
        }
        Timber.i("create deep wallets ended");
    }

    private void ddosTrueResponse(final int index) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
                    @Override
                    public void onResponse(Call<WalletsResponse> call, Response<WalletsResponse> response) {
                        if (response.isSuccessful()) {
                            for (Wallet wallet : response.body().getWallets()) {
                                if (wallet.getNetworkId() == deepMagicWallet.getNetworkId() &&
                                        wallet.getCurrencyId() == deepMagicWallet.getCurrencyId() &&
                                        wallet.getIndex() == index) {
                                    getActivity().getIntent().putExtra(Constants.EXTRA_ADDRESS, wallet.getActiveAddress().getAddress());
                                    getActivity().getIntent().putExtra(Constants.EXTRA_WALLET_ID, wallet.getId());
                                    ((BaseActivity) getActivity()).dismissProgressDialog();
                                    deepMagicWallet = null;
                                    startMagicReceive();
                                    return;
                                }
                            }
                        }

                        ddosTrueResponse(index);
                    }

                    @Override
                    public void onFailure(Call<WalletsResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }, 4000);
    }

    public void startMagicReceive() {
        Intent intent = new Intent(getActivity(), FastReceiveActivity.class);
        intent.putExtras(getActivity().getIntent());
        startActivity(intent);
        getActivity().finish();
    }

    private void detectBrokenWallets() {
        Timber.i("broken wallets called");
        if (!Prefs.getBoolean(Constants.PREF_DETECT_BROKEN, false) || checkMetamask) {
            Timber.i("broken wallets ended");
            return;
        }

        List<Wallet> walletList = RealmManager.getAssetsDao().getWallets(NativeDataHelper.Blockchain.ETH.getValue(), false);
        List<WalletPrivateKey> keys = new ArrayList<>();
        List<String> forcedAddresses = new ArrayList<>();

        final byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();

        for (Wallet wallet : walletList) {
            final WalletAddress address = wallet.getActiveAddress();
            try {
                final String libraryAddress = NativeDataHelper.makeAccountAddress(seed, wallet.getIndex(), address.getIndex(), wallet.getCurrencyId(), wallet.getNetworkId());
                final boolean isEqual = address.equals(libraryAddress);

                if (!isEqual) {
                    //NOTIFY API THAT ADDRESS CANT BE RESTORED AND DO BRUTE FORCE
                    final String privateKey = NativeDataHelper.bruteForceAddress(seed, wallet.getIndex(), address.getIndex(), wallet.getCurrencyId(), wallet.getNetworkId(), address.getAddress());

                    if (!privateKey.equals("")) {
                        forcedAddresses.add(wallet.getActiveAddress().getAddress());
                        keys.add(new WalletPrivateKey(wallet.getActiveAddress().getAddress(), privateKey, wallet.getCurrencyId(), wallet.getNetworkId()));
                    }
                }

            } catch (JniException e) {
                e.printStackTrace();
            }
        }

        if (forcedAddresses.size() > 0) {
            MultyApi.INSTANCE.makeBroken(new BrokenAddresses(forcedAddresses)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Prefs.putBoolean(Constants.PREF_DETECT_BROKEN, false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
            RealmManager.getAssetsDao().savePrivateKeys(keys);
        } else {
            Prefs.putBoolean(Constants.PREF_DETECT_BROKEN, false);
        }

        Timber.i("broken wallets ended");
    }

    private void initList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);

        walletsAdapter = new MyWalletsAdapter(this, viewModel.getWalletsFromDB());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(walletsAdapter);
//        walletsAdapter.setData(viewModel.getWalletsFromDB());
    }

    public void setDeepMagicWallet(Wallet wallet) {
        deepMagicWallet = wallet;
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
        Analytics.getInstance(getActivity()).logFirstLaunchCreateWallet();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            refreshLayout.setRefreshing(true);
            ((MainActivity) getActivity()).createFirstWallets();
        }
    }

    @OnClick(R.id.button_restore)
    void onClickRestore() {
        Analytics.getInstance(getActivity()).logFirstLaunchRestoreSeed();
        startActivityForResult(new Intent(getActivity(), SeedActivity.class).addCategory(Constants.EXTRA_RESTORE), Constants.REQUEST_CODE_RESTORE);
    }

    @OnClick(R.id.button_metamask)
    void onClickMetamusk() {
        Intent intent = new Intent(getActivity(), SeedActivity.class);
        intent.addCategory(Constants.EXTRA_RESTORE);
        intent.putExtra(Constants.EXTRA_METAMUSK, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_METAMUSK);
    }

    @OnClick(R.id.button_backup)
    void onClickWarn() {
        Analytics.getInstance(getActivity()).logMain(AnalyticsConstants.MAIN_BACKUP_SEED);
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

    @OnClick(R.id.logo)
    void onClickLogo() {
        Analytics.getInstance(getActivity()).logMain(AnalyticsConstants.MAIN_LOGO);
    }

    private class BannersPagerAdapter extends FragmentStatePagerAdapter {

        public BannersPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TotalBalanceFragment();

                case 1:
                    return  new MultireceiverFragment();

                default:
                    return  null;
            }
        }

        @Override
        public int getCount() {
            return BANNERS_COUNT;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment result = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    totalBalanceFragment = (TotalBalanceFragment) result;
                    break;

                case 1:
                    multireceiverFragment = (MultireceiverFragment) result;
                    break;
            }
            return result;
        }
    }
}
