/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.exchange;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.multy.R;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.activities.MagicSendActivity;
import io.multy.ui.activities.TokenSendActivity;
import io.multy.ui.adapters.RecentAddressesAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.AddressActionsDialogFragment;
import io.multy.ui.fragments.main.contacts.ContactsFragment;
import io.multy.ui.fragments.send.AmountChooserFragment;
import io.multy.ui.fragments.send.TransactionFeeFragment;
import io.multy.ui.fragments.send.ethereum.EthTransactionFeeFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;
import io.multy.viewmodels.ExchangeViewModel;
import io.realm.RealmResults;

public class ExchangeFragment extends BaseFragment {

    public static final int REQUEST_CONTACT = 1101;

    @BindView(R.id.tv_exchange_summery_from_wallet)
    TextView tvPayFromWallet;
    @BindView(R.id.ib_exchange_from_icon)
    ImageButton ibPayFromWalletLogo;
    @BindView(R.id.til_exchange_from_crypto)
    TextInputLayout inputFromCryptoLayout;
    @BindView(R.id.tie_exchange_from_crypto)
    TextInputEditText inputFromCrypto;
    @BindView(R.id.tv_exchange_summery_crypto_amount)
    TextView tvSummeryCrypto;
    @BindView(R.id.iv_summery_from_logo)
    ImageView ivSummeryFromLogo;
    @BindView(R.id.tv_exchange_summery_fiat_amount)
    TextView tvSummeryFromFiat;
    @BindView(R.id.button_max)
    TextView buttonMax;
    @BindView(R.id.tv_exchange_rate)
    TextView tvExchangeRate;
    @BindView(R.id.tv_exchange_min_rate)
    TextView tvExchangeMin;
    @BindView(R.id.tv_exchange_select_asset)
    TextView tvExchangeTap;
    @BindView(R.id.til_exchange_to_crtypo)
    TextInputLayout tilExchangeToCryptoLayout;
    @BindView(R.id.tie_exchange_to_crypto)
    TextInputEditText inputToCrypto;
    @BindView(R.id.til_exchange_to_fiat)
    TextInputLayout tilExchangeToFiatLayout;
    @BindView(R.id.tv_exchange_summery_receive_wallet)
    TextView tvSummeryToWallet;
    @BindView(R.id.tv_exchange_summery_receive_crypto_amount)
    TextView tvSummeryReceiveCrypto;
    @BindView(R.id.tv_exchange_summery_receive_fiat_amount)
    TextView tvSummeryReceiveFiat;
    @BindView(R.id.iv_summery_to_logo)
    ImageView ivSummeryToLogo;
    @BindView(R.id.tv_exchange_summery_receive)
    TextView tvSummeryToTitle;

    @BindView(R.id.ib_receive)
    ImageButton ibReceiveToLogo;

    @OnClick(R.id.ib_receive)
    void onClickReceiveLogo(){
        Log.d("EXCHANGE FR", "LOGO TAPPED");
        viewModel.changeFragment(2);
    }

//    @BindView(R.id.input_address)
//    EditText inputAddress;
//    @BindView(R.id.button_next)
//    TextView buttonNext;
//    @BindView(R.id.recycler_view)
//    RecyclerView recyclerView;

    private ExchangeViewModel viewModel;
    private Unbinder unbinder;
    private RecentAddressesAdapter adapter;

    public static ExchangeFragment newInstance() {
        return new ExchangeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        this.unbinder = ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(requireActivity()).get(ExchangeViewModel.class);
        setBaseViewModel(viewModel);

        setupPayFromWallet();

//        viewModel.getReceiverAddress().observe(this, s -> {
//            inputAddress.setText(s);
//            inputAddress.setSelection(inputAddress.length());
//        });
//        setupInputAddress();
//        initRecycler();
//        initRecentAddresses();
//        logLaunch();
        return view;
    }

    @Override
    public void onResume() {
        //TODO check this shitcode
//        if (!viewModel.getWallet().isValid()) {
//            viewModel.setWallet(RealmManager.getAssetsDao().getWalletById(getActivity().getIntent()
//                    .getLongExtra(Constants.EXTRA_WALLET_ID, -1)));
//        }
//        if (adapter != null && !adapter.isValidData()) {
//            initRecentAddresses();
//        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK && data != null) {
//            setArguments(data.getExtras());
//            setAddressAndNext(data.getStringExtra(Constants.EXTRA_ADDRESS));
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
    }

//    @Override
//    public void onClickRecentAddress(String address) {
//        setAddressAndNext(address);
//    }
//
//    @Override
//    public boolean onLongClickRecentAddress(String address, int currencyId, int networkId, int resImgId) {
//        AddressActionsDialogFragment.getInstance(address, currencyId, networkId, resImgId, true, () -> {
//            recyclerView.postDelayed(() -> {
//                viewModel.setWallet(RealmManager.getAssetsDao().getWalletById(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, -1)));
//                initRecentAddresses();
//            }, 2000);
//        })
//                .show(getChildFragmentManager(), AddressActionsDialogFragment.TAG);
//        return true;
//    }
//
//    private void initRecycler() {
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setHasFixedSize(true);
//        adapter = new RecentAddressesAdapter(null, this);
//        recyclerView.setAdapter(adapter);
//    }
//
//    private void initRecentAddresses() {
//        RealmResults<RecentAddress> recentAddresses;
//        if (getActivity() != null && getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)
//                && viewModel.getWallet() != null) {
//            recentAddresses = RealmManager.getAssetsDao()
//                    .getRecentAddresses(viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId());
//        } else {
//            recentAddresses = RealmManager.getAssetsDao().getRecentAddresses();
//        }
//        adapter.setAddresses(recentAddresses);
//    }
//
//    private void setAddressAndNext(String address) {
//        inputAddress.setText(address);
//        inputAddress.setSelection(inputAddress.length());
//        inputAddress.postDelayed(this::onClickNext, 300);
//    }
//
//    private void setupInputAddress() {
//        inputAddress.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                boolean isValidAddress = getActivity() != null && getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)
//                        && viewModel.getWallet() != null && viewModel.getWallet().isValid() ?
//                        checkAddressForValidation(charSequence.toString(),
//                                viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId()) :
//                        checkAddressForValidation(charSequence.toString());
//                if (TextUtils.isEmpty(charSequence) || !isValidAddress) {
//                    buttonNext.setBackgroundResource(R.color.disabled);
//                    buttonNext.setEnabled(false);
//                } else {
//                    buttonNext.setBackgroundResource(R.drawable.btn_gradient_blue);
//                    buttonNext.setEnabled(true);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//        if (getArguments() != null) {
//            inputAddress.post(() -> {
//                inputAddress.setText(getArguments().getString(Constants.EXTRA_ADDRESS));
//                getArguments().remove(Constants.EXTRA_ADDRESS);
//            });
//        }
//    }


    private void setupPayFromWallet(){
        viewModel.getPayFromWallet().observe(this, wallet -> {
            if (wallet != null){
                tvPayFromWallet.setText(wallet.getWalletName());
                ibPayFromWalletLogo.setImageResource(wallet.getIconResourceId());
                inputFromCryptoLayout.setHint(wallet.getCurrencyName());
                tvSummeryCrypto.setText(wallet.getCurrencyName());
                ivSummeryFromLogo.setImageResource(wallet.getIconResourceId());
                tvSummeryFromFiat.setText("$ 0.0");
                buttonMax.setVisibility(View.INVISIBLE);
                tvExchangeRate.setVisibility(View.INVISIBLE);
                tvExchangeMin.setVisibility(View.INVISIBLE);

                //TODO add additional check for that
                tvExchangeTap.setVisibility(View.VISIBLE);
                tilExchangeToCryptoLayout.setVisibility(View.INVISIBLE);
                tilExchangeToFiatLayout.setVisibility(View.INVISIBLE);
                tvSummeryToWallet.setVisibility(View.INVISIBLE);
                tvSummeryReceiveCrypto.setVisibility(View.INVISIBLE);
                tvSummeryReceiveFiat.setVisibility(View.INVISIBLE);
                ivSummeryToLogo.setVisibility(View.INVISIBLE);
                tvSummeryToTitle.setVisibility(View.INVISIBLE);
            }
//            Log.d("EXCHANGE FRAGMENT", "GOT WALLET NAME:"+wallet.getCurrencyId() + " CURRENCIE ID:"+ wallet.getCurrencyName());
        });
    }

    private boolean checkAddressForValidation(String address) {
        for (NativeDataHelper.Blockchain blockchain : NativeDataHelper.Blockchain.values()) {
            for (NativeDataHelper.NetworkId networkId : NativeDataHelper.NetworkId.values()) {
                if (checkAddressForValidation(address, blockchain.getValue(), networkId.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAddressForValidation(String address, int blockchainId, int networkId) {
        try {
            NativeDataHelper.isValidAddress(address, blockchainId, networkId);
            return true;
        } catch (Throwable ignore) {
        }
        return false;
    }

    private void logLaunch() {
        if (getActivity() != null && !getActivity().getIntent().hasExtra(Constants.EXTRA_ADDRESS)) {
            Analytics.getInstance(getActivity()).logSendToLaunch();
        }
    }

//    @OnClick(R.id.button_address)
//    void onClickAddressBook() {
//        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_ADDRESS_BOOK);
////        if (getActivity() != null) {
////            DonateDialog.getInstance(Constants.DONATE_ADDING_CONTACTS)
////                    .show(getActivity().getSupportFragmentManager(), DonateDialog.TAG);
////        }
//        ContactsFragment fragment = ContactsFragment.newInstance();
//        fragment.setTargetFragment(this, REQUEST_CONTACT);
//        if (getActivity() != null && getView() != null) {
//            getActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container_full, fragment, ContactsFragment.TAG)
//                    .addToBackStack(ContactsFragment.TAG)
//                    .commit();
//        }
//    }
//
//    @OnClick(R.id.button_scan_wireless)
//    void onClickWirelessScan() {
//        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_WIRELESS);
//        startActivity(new Intent(getActivity(), MagicSendActivity.class));
//    }
//
//    @OnClick(R.id.button_scan_qr)
//    void onClickScanQr() {
//        Analytics.getInstance(getActivity()).logSendTo(AnalyticsConstants.SEND_TO_QR);
//        ((AssetSendActivity) getActivity()).showScanScreen();
//    }
//
//    @OnClick(R.id.button_next)
//    void onClickNext() {
//        viewModel.setReceiverAddress(inputAddress.getText().toString());
//        viewModel.thoseAddress.setValue(inputAddress.getText().toString());
////        ((AssetSendActivity) getActivity()).setFragment(R.string.send_from, R.id.container, WalletChooserFragment.newInstance(blockchainId, networkId));
//
//        if (getActivity() instanceof TokenSendActivity) {
//            ((TokenSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, EthTransactionFeeFragment.newInstance());
//            return;
//        }
//
//        if (getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)) {
//            RealmManager.getAssetsDao().getWalletById(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
//            if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
//                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, TransactionFeeFragment.newInstance());
//            } else if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue()) {
//                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, EthTransactionFeeFragment.newInstance());
//            } else if (viewModel.getWallet().getCurrencyId() == NativeDataHelper.Blockchain.EOS.getValue()) {
//                ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, AmountChooserFragment.newInstance());
//            }
//        }
//    }
//
//    @OnClick(R.id.container)
//    void onClickContainer() {
//        if (getActivity() != null) {
//            hideKeyboard(getActivity());
//        }
//    }
}
