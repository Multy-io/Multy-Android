package io.multy.ui.fragments.asset;

import android.arch.lifecycle.Observer;
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
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
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
import timber.log.Timber;

public class AssetInfoFragment extends BaseFragment {

    public static final String TAG = AssetInfoFragment.class.getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_transactions)
    RecyclerView recyclerView;
    @BindView(R.id.text_value)
    TextView textBalanceOriginal;
    @BindView(R.id.text_amount)
    TextView textBalanceFiat;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
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

        WalletRealmObject wallet = viewModel.getWallet(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
        if (wallet != null) {
            setupWalletInfo(wallet);
        } else {
            viewModel.getWalletLive().observe(this, this::setupWalletInfo);
        }

        initialize();
        requestTransactions();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.subscribeSocketsUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.unsubscribeSocketsUpdate();
    }

    private void initialize() {
        recyclerView.setAdapter(transactionsAdapter);

        if (Prefs.getBoolean(Constants.PREF_BACKUP_SEED)) {
            buttonWarn.setVisibility(View.GONE);
            buttonWarn.getLayoutParams().height = 0;
        }
    }

    private void setTransactionsState() {
        if (transactionsAdapter.getItemCount() == 0) {
            refreshLayout.setEnabled(false);
            recyclerView.setAdapter(new EmptyTransactionsAdapter());
            groupEmptyState.setVisibility(View.VISIBLE);
//            setToolbarScrollFlag(0);
        } else {
            refreshLayout.setEnabled(true);
            groupEmptyState.setVisibility(View.GONE);
            setToolbarScrollFlag(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
        }
    }

    private void setupWalletInfo(WalletRealmObject wallet) {
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

        final double formatBalance = balance / Math.pow(10, 8);
        final double formatPending = pending / Math.pow(10, 8);
        final double exchangePrice = rate == null ? RealmManager.getSettingsDao().getCurrenciesRate().getBtcToUsd() : rate.getBtcToUsd();

        textAvailableValue.setText(balance == 0 ? "0.0$" : format.format(exchangePrice * formatBalance) + "$");
        textBalanceFiat.setText(pending == 0 ? "0.0$" : format.format(exchangePrice * formatPending) + "$");

        textAvailableValue.setText(pending != 0 ? CryptoFormatUtils.satoshiToBtc(pending) : String.valueOf(pending));
        textBalanceOriginal.setText(balance != 0 ? CryptoFormatUtils.satoshiToBtc(balance) : String.valueOf(balance));

        //TODO calculating available and original balance depending on history
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
            }
            setTransactionsState();
        });
    }

    private void setToolbarScrollFlag(int flag) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(flag);
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
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, viewModel.getWalletLive().getValue().getCreationAddress());
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.send_via)));
    }

    @OnClick(R.id.text_address)
    void onClickCopy() {
        String address = viewModel.getWalletLive().getValue().getCreationAddress();
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