package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.WalletsResponse;
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
import io.multy.viewmodels.WalletViewModel;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

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
        viewModel.rates.observe(this, currenciesRate -> updateBalanceViews(currenciesRate));
        viewModel.transactionUpdate.observe(this, transactionUpdateEntity -> {
            refreshWallet();
        });
        swipeRefreshLayout.setOnRefreshListener(() -> refreshWallet());

        WalletRealmObject wallet = viewModel.getWallet(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
        viewModel.getWalletLive().observe(this, this::setupWalletInfo);
        viewModel.getWalletLive().setValue(wallet);
        return view;
    }

    private void refreshWallet() {
        swipeRefreshLayout.setRefreshing(false);
        viewModel.isLoading.setValue(true);
        viewModel.isLoading.call();

        final int walletIndex = viewModel.getWalletLive().getValue().getWalletIndex();
        MultyApi.INSTANCE.getWalletVerbose(walletIndex).enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(Call<WalletsResponse> call, Response<WalletsResponse> response) {
                viewModel.isLoading.setValue(false);
                if (response.isSuccessful() && response.body().getWallets() != null && response.body().getWallets().size() > 0) {
                    AssetsDao assetsDao = RealmManager.getAssetsDao();
                    assetsDao.saveWallet(response.body().getWallets().get(0));
                    viewModel.wallet.setValue(assetsDao.getWalletById(walletIndex));
                }

                requestTransactions();
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {
                viewModel.isLoading.setValue(false);
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.subscribeSocketsUpdate();
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
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

        updateBalanceViews(null);
    }

    private void updateBalanceViews(CurrenciesRate rate) {
        WalletRealmObject wallet = viewModel.getWalletLive().getValue();
        double balance = wallet.getBalance();
        double pending = wallet.getPendingBalance() + balance;

        if (pending == 0 || balance == pending) {
            hideAvailableAmount();
        }

        final double formatBalance = balance / Math.pow(10, 8);
        final double formatPending = pending / Math.pow(10, 8);
        final double exchangePrice = rate == null ? RealmManager.getSettingsDao().getCurrenciesRate().getBtcToUsd() : rate.getBtcToUsd();

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
                transactionsAdapter.setTransactions(transactions);
                recyclerView.setAdapter(new AssetTransactionsAdapter(transactions, viewModel.wallet.getValue().getWalletIndex()));
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
        ((AssetActivity) getActivity()).setFragment(R.id.frame_container, AssetSettingsFragment.newInstance());
    }

    @OnClick(R.id.card_addresses)
    void onClickAddress() {
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
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getAddressToShare());
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.send_via)));
    }

    @OnClick(R.id.text_address)
    void onClickCopy() {
        String address = getAddressToShare();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.close)
    void onClickClose() {
        getActivity().finish();
    }

    @OnClick(R.id.button_warn)
    void onClickWarn() {
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }
}