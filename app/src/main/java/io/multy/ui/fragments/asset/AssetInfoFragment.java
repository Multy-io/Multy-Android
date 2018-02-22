package io.multy.ui.fragments.asset;

import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.events.TransactionUpdateEvent;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.AssetTransactionsAdapter;
import io.multy.ui.adapters.EmptyTransactionsAdapter;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Intent.ACTION_SEND;

public class AssetInfoFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    public static final String TAG = AssetInfoFragment.class.getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_value)
    TextView textBalanceOriginal;
    @BindView(R.id.text_amount)
    TextView textBalanceFiat;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.group_empty_state)
    View groupEmptyState;
    @BindView(R.id.button_warn)
    FloatingActionButton buttonWarn;
    @BindView(R.id.container_available)
    View containerAvailableBalance;
    @BindView(R.id.text_available_value)
    TextView textAvailableValue;
    @BindView(R.id.text_available_fiat)
    TextView textAvailableFiat;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.container_info)
    View containerInfo;

    private WalletViewModel viewModel;
    private final static DecimalFormat format = new DecimalFormat("#.##");
    private AssetTransactionsAdapter transactionsAdapter;
    private SharingBroadcastReceiver receiver;

    public static AssetInfoFragment newInstance() {
        return new AssetInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionsAdapter = new AssetTransactionsAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_info, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
        receiver = new SharingBroadcastReceiver();
        viewModel.rates.observe(this, currenciesRate -> updateBalanceViews());
        viewModel.transactionUpdate.observe(this, transactionUpdateEntity -> {
            new Handler().postDelayed(() -> refreshWallet(), 300);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_PULL, viewModel.getChainId());
            refreshWallet();
        });

        WalletRealmObject wallet = viewModel.getWallet(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
        viewModel.getWalletLive().observe(this, this::setupWalletInfo);
        viewModel.getWalletLive().setValue(wallet);
        Analytics.getInstance(getActivity()).logWalletLaunch(AnalyticsConstants.WALLET_SCREEN, viewModel.getChainId());
        return view;
    }

    private void refreshWallet() {
        swipeRefreshLayout.setRefreshing(false);
        viewModel.isLoading.postValue(true);

        final int walletIndex = viewModel.getWalletLive().getValue().getWalletIndex();
        final int currencyId = viewModel.getWalletLive().getValue().getCurrency();
        MultyApi.INSTANCE.getWalletVerbose(currencyId, walletIndex).enqueue(new Callback<SingleWalletResponse>() {
            @Override
            public void onResponse(Call<SingleWalletResponse> call, Response<SingleWalletResponse> response) {
                viewModel.isLoading.postValue(false);
                if (response.isSuccessful() && response.body().getWallets() != null && response.body().getWallets().size() > 0) {
                    AssetsDao assetsDao = RealmManager.getAssetsDao();
                    assetsDao.saveWallet(response.body().getWallets().get(0));
                    viewModel.wallet.postValue(assetsDao.getWalletById(walletIndex));
                }

                updateBalanceViews();
                requestTransactions();
            }

            @Override
            public void onFailure(Call<SingleWalletResponse> call, Throwable t) {
                viewModel.isLoading.setValue(false);
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
                getActivity().registerReceiver(receiver, new IntentFilter());
        }
        viewModel.subscribeSocketsUpdate();
        appBarLayout.addOnOffsetChangedListener(this);
        checkWarnVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        viewModel.unsubscribeSocketsUpdate();
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//        final int total = appBarLayout.getTotalScrollRange();
//        final int offset = Math.abs(i);
//        final int percentage = offset == 0 ? 0 : offset / total;
//        containerInfo.setBackgroundColor((Integer) argbEvaluator.evaluate(colorOffset, colorTransparent, colorPrimaryDark));
        swipeRefreshLayout.setEnabled(i == 0);
//        containerInfo.setAlpha(1 - percentage);
    }

    private void initialize() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(transactionsAdapter);
        if (transactionsAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setEnabled(false);
            recyclerView.setAdapter(new EmptyTransactionsAdapter());
            groupEmptyState.setVisibility(View.VISIBLE);
            setToolbarScrollFlag(0);
        } else {
            swipeRefreshLayout.setEnabled(true);
            groupEmptyState.setVisibility(View.GONE);
            setToolbarScrollFlag(3);
        }

        checkWarnVisibility();
    }

    private void checkWarnVisibility() {
        if (Prefs.getBoolean(Constants.PREF_BACKUP_SEED)) {
            buttonWarn.setVisibility(View.GONE);
            buttonWarn.getLayoutParams().height = 0;
        }
    }

    private void setTransactionsState() {
        if (transactionsAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setEnabled(false);
            recyclerView.setAdapter(new EmptyTransactionsAdapter());
            groupEmptyState.setVisibility(View.VISIBLE);
            setToolbarScrollFlag(0);
        } else {
            swipeRefreshLayout.setEnabled(true);
            groupEmptyState.setVisibility(View.GONE);
            setToolbarScrollFlag(3);
        }
    }

    private void setupWalletInfo(WalletRealmObject wallet) {
        initialize();
        requestTransactions();

        Timber.i("wallet %s", wallet.toString());
        if (wallet.getAddresses() != null && !wallet.getAddresses().isEmpty()) {
            for (WalletAddress address : wallet.getAddresses()) {
                Timber.i("address %s", address.getAddress());
            }
        } else {
            Timber.i("addresses empty ");
        }
        textWalletName.setText(wallet.getName());
        if (wallet.getAddresses() != null && !wallet.getAddresses().isEmpty()) {
            textAddress.setText(wallet.getAddresses().get(wallet.getAddresses().size() - 1).getAddress());
        } else {
            textAddress.setText(wallet.getCreationAddress());
        }

        updateBalanceViews();
    }

    private void updateBalanceViews() {
        WalletRealmObject wallet = viewModel.getWalletLive().getValue();
        double balance = wallet.getBalance();
        double pending = wallet.getPendingBalance() + balance;

        Log.i(TAG, "balance = " + wallet.getBalance());
        Log.i(TAG, "pending = " + wallet.getPendingBalance());

        if (pending == 0 || balance == pending) {
            hideAvailableAmount();
        } else {
            showAvailableAmount();
        }

        final double formatBalance = balance / Math.pow(10, 8);
        final double formatPending = pending / Math.pow(10, 8);

        final CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        final double exchangePrice = currenciesRate != null ? currenciesRate.getBtcToUsd() : 0;

        textAvailableFiat.setText(balance == 0 ? "0.0$" : format.format(exchangePrice * formatBalance) + "$");
        textBalanceFiat.setText(pending == 0 ? "0.0$" : format.format(exchangePrice * formatPending) + "$");

        textBalanceOriginal.setText(pending != 0 ? CryptoFormatUtils.satoshiToBtc(pending) : String.valueOf(pending));
        textAvailableValue.setText(balance != 0 ? CryptoFormatUtils.satoshiToBtc(balance) : String.valueOf(balance));
    }

    private void showAvailableAmount() {
        containerAvailableBalance.setVisibility(View.VISIBLE);
    }

    private void hideAvailableAmount() {
        containerAvailableBalance.setVisibility(View.GONE);
    }

    private void requestTransactions() {
        viewModel.getTransactionsHistory().observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                try {
                    transactionsAdapter.setTransactions(transactions);
                    recyclerView.setAdapter(new AssetTransactionsAdapter(transactions, viewModel.wallet.getValue().getWalletIndex()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setTransactionsState();
        });
    }

    private void setToolbarScrollFlag(int flag) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(flag);
    }

    private String getAddressToShare() {
        RealmList<WalletAddress> addresses = viewModel.wallet.getValue().getAddresses();
        return addresses.get(addresses.size() - 1).getAddress();
    }

    @OnClick(R.id.options)
    void onClickOptions() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SETTINGS, viewModel.getChainId());
        ((AssetActivity) getActivity()).setFragment(R.id.container_full, AssetSettingsFragment.newInstance());
    }

    @OnClick(R.id.card_addresses)
    void onClickAddress() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESSES, viewModel.getChainId());
        if (viewModel.getWalletLive().getValue() != null) {
            ((AssetActivity) getActivity()).setFragment(R.id.container_full,
                    AddressesFragment.newInstance(viewModel.getWalletLive().getValue().getWalletIndex()));
        } else {
            WalletRealmObject wallet = viewModel.getWallet(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
            ((AssetActivity) getActivity()).setFragment(R.id.container_full,
                    AddressesFragment.newInstance(wallet.getWalletIndex()));
        }
    }

    @OnClick(R.id.button_share)
    void onClickShare() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SHARE, viewModel.getChainId());
        Intent sharingIntent = new Intent(ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getAddressToShare());
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Intent intentReceiver = new Intent(getActivity(), SharingBroadcastReceiver.class);
            intentReceiver.putExtra(getString(R.string.chain_id), viewModel.getChainId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intentReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.send_via), pendingIntent.getIntentSender()));
        } else {
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.send_via)));
        }
    }

    @OnClick(R.id.text_address)
    void onClickCopy() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESS, viewModel.getChainId());
        String address = getAddressToShare();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.close)
    void onClickClose() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
        getActivity().finish();
    }

    @OnClick(R.id.button_warn)
    void onClickWarn() {
        Analytics.getInstance(getActivity()).logWalletBackup(AnalyticsConstants.WALLET_BACKUP_SEED);
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

    @OnClick(R.id.text_value)
    void onClickBalance() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE, viewModel.getChainId());
    }

    @OnClick(R.id.text_coin)
    void onClickBalanceCurrency() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE, viewModel.getChainId());
    }

    @OnClick(R.id.text_amount)
    void onClickBalanceFiat() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE_FIAT, viewModel.getChainId());
    }

    @OnClick(R.id.text_money)
    void onClickBalanceFiatCurrency() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE_FIAT, viewModel.getChainId());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdateEvent(TransactionUpdateEvent event) {
        Log.i(TAG, "transaction update event called");
        refreshWallet();
    }

    public static class SharingBroadcastReceiver extends BroadcastReceiver {

        public SharingBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT) != null) {
                String component = intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT).toString();
                String packageName = component.substring(component.indexOf("{") + 1, component.indexOf("/"));
                Analytics.getInstance(context).logWalletSharing(intent.getIntExtra(context.getString(R.string.chain_id), 1), packageName);
            }
        }
    }

}