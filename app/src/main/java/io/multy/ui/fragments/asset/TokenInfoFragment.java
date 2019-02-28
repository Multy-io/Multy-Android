/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.events.TransactionUpdateEvent;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.activities.ExchangeActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.activities.TokenSendActivity;
import io.multy.ui.adapters.AssetTransactionsAdapter;
import io.multy.ui.adapters.EthTransactionsAdapter;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.MultisigSettingsFragment;
import io.multy.ui.fragments.dialogs.AddressActionsDialogFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;

public class TokenInfoFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    public static final String TAG = TokenInfoFragment.class.getSimpleName();
    public static final String ARG_NAME = "ARG_NAME";
    public static final String ARG_BALANCE = "ARG_BALANCE";
    public static final String ARG_BALANCE_FIAT = "ARG_BALANCE_FIAT";
    public static final String ARG_ADDRESS = "ARG_ADDRESS";
    public static final String ARG_DECIMALS = "ARG_DECIMALS";
    public static final String ARG_IMAGE_URL = "ARG_IMAGE_URL";
    public static final String ARG_TOKEN_RATE = "ARG_TOKEN_RATE";

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.button_addresses)
    View buttonAddresses;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.text_balance)
    TextView textBalance;
    @BindView(R.id.text_balance_fiat)
    TextView textBalanceFiat;

    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;

//    @BindView(R.id.image_arrow)
//    ImageView imageArrow;
    @BindView(R.id.text_operations_empty)
    TextView textEmpty;
//    @BindView(R.id.text_operation_create)
//    TextView textCreate;
    @BindView(R.id.button_warn)
    View buttonWarn;
    @BindView(R.id.container_dummy)
    View containerDummy;

    private EthTransactionsAdapter ethTransactionsAdapter = new EthTransactionsAdapter();
    private WalletViewModel viewModel;
    private SharingBroadcastReceiver receiver = new SharingBroadcastReceiver();

    public static TokenInfoFragment newInstance(String name, String address, String balance, String balanceFiat, int decimals, String imageUrl, String tokenRate) {
        TokenInfoFragment tokenInfoFragment = new TokenInfoFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_NAME, name);
        arguments.putString(ARG_ADDRESS, address);
        arguments.putString(ARG_BALANCE, balance);
        arguments.putString(ARG_BALANCE_FIAT, balanceFiat);
        arguments.putInt(ARG_DECIMALS, decimals);
        arguments.putString(ARG_IMAGE_URL, imageUrl);
        arguments.putString(ARG_TOKEN_RATE, tokenRate);
        tokenInfoFragment.setArguments(arguments);
        return tokenInfoFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_token_info_eth, container, false);
        ButterKnife.bind(this, convertView);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
        showInfo();
        setButtonWarnVisibility();


        Analytics.getInstance(getActivity()).logWalletLaunch(AnalyticsConstants.WALLET_SCREEN, viewModel.getChainId());
        return convertView;
    }

    private void showInfo() {
        Bundle args = getArguments();
        final String name = args.getString(ARG_NAME, "");
        final String address = args.getString(ARG_ADDRESS, "");
        final String balance = args.getString(ARG_BALANCE, "");
        final String balanceFiat = args.getString(ARG_BALANCE_FIAT, "");

        textBalance.setText(balance);
        textBalanceFiat.setText(balanceFiat);
        textAddress.setText(address);
        textWalletName.setText(name);

    }


    public void setWalletName(String name) {
        textWalletName.setText(name);
    }

    private void requestTransactions(final int currencyId, final int networkId, final int walletIndex) {
        if (viewModel.getWalletLive().getValue().isMultisig() || walletIndex < 0) {
            viewModel.getMultisigTransactionsHistory(viewModel.getWalletLive().getValue().getCurrencyId(),
                    viewModel.getWalletLive().getValue().getNetworkId(),
                    viewModel.getWalletLive().getValue().getActiveAddress().getAddress(),
                    walletIndex < 0 ? Constants.ASSET_TYPE_ADDRESS_IMPORTED : Constants.ASSET_TYPE_ADDRESS_MULTISIG)
                    .observe(this, transactions -> onTransactions(transactions, currencyId));
        } else {
            viewModel.getTransactionsHistory(currencyId, networkId, walletIndex)
                    .observe(this, transactions -> onTransactions(transactions, currencyId));
        }
    }

    private void onTransactions(ArrayList<TransactionHistory> transactions, int currencyId) {
        if (transactions != null && !transactions.isEmpty()) {
            try {
                final long walletId = viewModel.wallet.getValue().isValid() ?
                        viewModel.wallet.getValue().getId() :
                        viewModel.getWallet(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0)).getId();

                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                if (currencyId == NativeDataHelper.Blockchain.BTC.getValue()) {
                    recyclerView.setAdapter(new AssetTransactionsAdapter(transactions, walletId));
                } else if (currencyId == NativeDataHelper.Blockchain.EOS.getValue()) {
//                        recyclerView.setAdapter(new EosTransactionsAdapter(transactions, walletId));
                } else {
                    ethTransactionsAdapter.setTransactions(transactions, walletId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setTransactionsState();
    }

    private void setTransactionsState() {
        if (recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) {
            swipeRefreshLayout.setEnabled(false);
            //TODO enable this when tokens transaction hisory will be implemeneted on backend
            setNotificationsVisibility(View.GONE);
            setToolbarScrollFlag(0);
        } else {
            swipeRefreshLayout.setEnabled(false);
            setNotificationsVisibility(View.GONE);
            setToolbarScrollFlag(3);
        }
    }

    private void setNotificationsVisibility(int visibility) {
//        imageArrow.setVisibility(visibility);
        textEmpty.setVisibility(visibility);
//        textCreate.setVisibility(visibility);
        containerDummy.setVisibility(visibility);
    }

    private void setToolbarScrollFlag(int flag) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(flag);
    }

    private void showAddressAction() {
        Wallet wallet = viewModel.getWalletLive().getValue();
        if (wallet != null && wallet.isValid() && !TextUtils.isEmpty(wallet.getActiveAddress().getAddress())) {
            AddressActionsDialogFragment.getInstance(wallet.getActiveAddress().getAddress(), wallet.getCurrencyId(),
                    wallet.getNetworkId(), wallet.getIconResourceId(), false, () -> {
                        if (recyclerView.getAdapter() != null) {
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    }).show(getChildFragmentManager(), AddressActionsDialogFragment.TAG);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdateEvent(TransactionUpdateEvent event) {
//        refreshWallet();
    }

    @Override
    public void onStart() {
        super.onStart();
        Wallet wallet = viewModel.getWalletLive().getValue();
        requestTransactions(wallet.getCurrencyId(), wallet.getNetworkId(), wallet.getIndex());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().registerReceiver(receiver, new IntentFilter());
        }

        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        viewModel.subscribeSocketsUpdate();
        appBarLayout.addOnOffsetChangedListener(this);
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

    @OnClick(R.id.button_send)
    void onClickSend() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SEND, viewModel.getChainId());
        Wallet wallet = viewModel.getWalletLive().getValue();
        if (wallet != null && wallet.getAvailableBalanceNumeric().compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(getActivity(), R.string.no_balance, Toast.LENGTH_SHORT).show();
            return;
        }

        if (wallet.isMultisig()) {
            Wallet linked = RealmManager.getAssetsDao().getMultisigLinkedWallet(wallet.getMultisigWallet().getOwners());
            if (linked == null || linked.getAvailableBalanceNumeric()
                    .compareTo(new BigDecimal(CryptoFormatUtils.ethToWei(String.valueOf("0.0001")))) <= 0) {
                viewModel.errorMessage.setValue(getString(R.string.not_enough_linked_balance));
                return;
            }
        }
        if (wallet.isPending()) {
            return;
        }

        if (wallet.getAvailableBalanceNumeric().compareTo(new BigDecimal(CryptoFormatUtils.ethToWei(String.valueOf("0.0001")))) <= 0){
            //This check is needed to verify that parent wallet have enough ETH to send tokens
            viewModel.errorMessage.setValue(getString(R.string.not_enough_linked_balance));
            return;
        }

        //TODO need to make this code less hardoced
        final String[] balance = textBalance.getText().toString().split(" ");

        startActivity(new Intent(getActivity(), TokenSendActivity.class)
                .addCategory(Constants.EXTRA_SENDER_ADDRESS)
                .putExtra(Constants.EXTRA_TOKEN_BALANCE, balance[0])
                .putExtra(Constants.EXTRA_TOKEN_CODE, balance[1])
                .putExtra(Constants.EXTRA_TOKEN_DECIMALS, getArguments().getInt(ARG_DECIMALS))
                .putExtra(Constants.EXTRA_TOKEN_IMAGE_URL, getArguments().getString(ARG_IMAGE_URL))
                .putExtra(Constants.EXTRA_TOKEN_RATE, getArguments().getString(ARG_TOKEN_RATE))
                .putExtra(Constants.EXTRA_CONTRACT_ADDRESS, textAddress.getText().toString())
                .putExtra(Constants.EXTRA_WALLET_ID, getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0)));
    }

    @OnClick(R.id.button_receive)
    void onClickReceive() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_RECEIVE, viewModel.getChainId());
        startActivity(new Intent(getActivity(), AssetRequestActivity.class)
                .putExtra(Constants.EXTRA_WALLET_ID, getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0)));
    }

    @OnClick(R.id.button_exchange)
    void onClickExchange() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_EXCHANGE, viewModel.getChainId());

        //new implementation

        //TODO need to test this checks
        Wallet wallet = viewModel.getWalletLive().getValue();
        if (wallet != null && wallet.getAvailableBalanceNumeric().compareTo(BigDecimal.ZERO) <= 0) {
            viewModel.errorMessage.setValue(getString(R.string.not_enough_balance));
            return;
        }
        if (wallet.isMultisig()) {
            Wallet linked = RealmManager.getAssetsDao().getMultisigLinkedWallet(wallet.getMultisigWallet().getOwners());
            if (linked == null || linked.getAvailableBalanceNumeric()
                    .compareTo(new BigDecimal(CryptoFormatUtils.ethToWei(String.valueOf("0.0001")))) <= 0) {
                viewModel.errorMessage.setValue(getString(R.string.not_enough_linked_balance));
                return;
            }
        }
        if (wallet.isPending()) {
            return;
        }

        //TODO need to make this code less hardoced
        final String[] balance = textBalance.getText().toString().split(" ");
        startActivity(new Intent(getActivity(), ExchangeActivity.class)
                .addCategory(Constants.EXTRA_SENDER_ADDRESS)
                .putExtra(Constants.EXTRA_TOKEN_BALANCE, balance[0])
                .putExtra(Constants.EXTRA_TOKEN_CODE, balance[1])
                .putExtra(Constants.EXTRA_TOKEN_DECIMALS, getArguments().getInt(ARG_DECIMALS))
                .putExtra(Constants.EXTRA_TOKEN_IMAGE_URL, getArguments().getString(ARG_IMAGE_URL))
                .putExtra(Constants.EXTRA_TOKEN_RATE, getArguments().getString(ARG_TOKEN_RATE))
                .putExtra(Constants.EXTRA_CONTRACT_ADDRESS, textAddress.getText().toString())
                .putExtra(Constants.EXTRA_WALLET_ID, getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0)));

    }

    @OnClick(R.id.button_addresses)
    public void onClickAddresses() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESSES, viewModel.getChainId());
        if (viewModel.getWalletLive().getValue() != null) {
            ((AssetActivity) getActivity()).setFragment(R.id.container_full, AddressesFragment.newInstance(viewModel.getWalletLive().getValue().getId()));
        } else {
            Wallet wallet = viewModel.getWallet(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            ((AssetActivity) getActivity()).setFragment(R.id.container_full, AddressesFragment.newInstance(wallet.getId()));
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //TODO uncoment this when transaction history will be done
//        swipeRefreshLayout.setEnabled(verticalOffset == 0);
    }

    @OnClick(R.id.options)
    public void onClickOptions() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SETTINGS, viewModel.getChainId());
        Fragment fragment = null;
        Wallet wallet = viewModel.getWalletLive().getValue();
        if (wallet.isMultisig()) {
            Wallet linkedWallet = RealmManager.getAssetsDao().getMultisigLinkedWallet(wallet.getMultisigWallet().getOwners());
            fragment = MultisigSettingsFragment.newInstance(wallet, linkedWallet);
        } else {
            fragment = AssetSettingsFragment.newInstance();
        }
        ((AssetActivity) getActivity()).setFragment(R.id.container_full, fragment);
    }

    @OnClick(R.id.button_share)
    public void onClickShare() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SHARE, viewModel.getChainId());
        showAddressAction();
    }

    @OnClick(R.id.button_copy)
    public void onClickCopy() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESS, viewModel.getChainId());
        showAddressAction();
    }

    @OnClick(R.id.close)
    public void onClickClose() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
        getActivity().finish();
    }

    @OnClick(R.id.button_warn)
    public void onClickWarn() {
        Analytics.getInstance(getActivity()).logWalletBackup(AnalyticsConstants.WALLET_BACKUP_SEED);
        startActivity(new Intent(getActivity(), SeedActivity.class));
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

    private void setButtonWarnVisibility() {
        if (Prefs.getBoolean(Constants.PREF_BACKUP_SEED)) {
            buttonWarn.setVisibility(View.GONE);
            buttonWarn.getLayoutParams().height = 0;

        }
    }
}
