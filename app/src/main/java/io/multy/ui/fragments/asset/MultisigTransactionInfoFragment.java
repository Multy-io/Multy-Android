/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.socket.SocketManager;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.TransactionOwner;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.BaseActivity;
import io.multy.ui.adapters.MultisigOwnersAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.WebFragment;
import io.multy.ui.fragments.dialogs.AddressActionsDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.WalletViewModel;

import static io.multy.util.Constants.TX_MEMPOOL_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_OUTCOMING;

/**
 * Created by anschutz1927@gmail.com on 16.01.18.
 */

public class MultisigTransactionInfoFragment extends BaseFragment {

    public static final String TAG = MultisigTransactionInfoFragment.class.getSimpleName();
    private static final String SELECTED_TX = "SELECTED_TX";

    @BindView(R.id.linear_parent)
    LinearLayout parent;
    @BindView(R.id.scroll_view)
    NestedScrollView scrollView;
    @BindView(R.id.toolbar_name)
    TextView toolbarWalletName;
    @BindView(R.id.text_status)
    TextView textStatus;
    @BindView(R.id.image_operation)
    ImageView imageOperation;
    @BindView(R.id.text_value)
    TextView textValue;
    @BindView(R.id.text_coin)
    TextView textCoin;
    @BindView(R.id.text_amount)
    TextView textAmount;
    @BindView(R.id.text_money)
    TextView textMoney;
    @BindView(R.id.text_comment)
    TextView textComment;
    @BindView(R.id.text_addresses_from)
    TextView textAdressesFrom;
    @BindView(R.id.arrow)
    ImageView imageArrow;
    @BindView(R.id.logo)
    ImageView imageCoinLogo;
    @BindView(R.id.text_addresses_to)
    TextView textAddressesTo;
    @BindView(R.id.text_confirmations)
    TextView textConfirmations;
    @BindView(R.id.button_view)
    TextView buttonView;
    @BindView(R.id.text_counter)
    TextView textCounter;
    @BindView(R.id.recycler_owners)
    RecyclerView recyclerOwners;
    @BindView(R.id.progress)
    View progress;
    @BindView(R.id.image_slider_accept)
    View sliderAccept;
    @BindView(R.id.image_slider_decline)
    View sliderDecline;
    @BindView(R.id.button_confirm)
    TextView buttonConfirm;
    @BindView(R.id.group_data_views)
    Group groupDataViews;
    @BindView(R.id.group_fee)
    Group groupFee;
    @BindView(R.id.text_fee)
    TextView textFee;
    @BindView(R.id.group_view)
    Group groupViewButton;
    @BindColor(R.color.green_light)
    int colorGreen;
    @BindColor(R.color.blue_sky)
    int colorBlue;

    private WalletViewModel viewModel;
    private int walletIndex;
    private int currencyId;
    private int networkId;
    private int iconResourceId;
    private int confirmationsNeeded;
    private boolean isConfirming = false;
    private ValueAnimator animator;
    private MultisigOwnersAdapter adapter;
    private TransactionHistory transaction;
    private String walletAddress;
    private SocketManager socketManager;

    public static MultisigTransactionInfoFragment newInstance(String transactionId) {
        MultisigTransactionInfoFragment fragment = new MultisigTransactionInfoFragment();
        Bundle args = new Bundle();
        args.putString(SELECTED_TX, transactionId);
        fragment.setArguments(args);
        return fragment;
    }

    public MultisigTransactionInfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
        adapter = new MultisigOwnersAdapter();
        try {
            socketManager = new SocketManager();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = getLayoutInflater().inflate(R.layout.fragment_multisig_transaction_info, container, false);
        ButterKnife.bind(this, v);
        recyclerOwners.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerOwners.setAdapter(adapter);
        viewModel.wallet.observe(this, this::onWallet);
        recyclerOwners.setNestedScrollingEnabled(false);
        initAnimationSliders();
        return v;
    }

    @Override
    public void onResume() {
        if (socketManager != null) {
            String eventReceive = SocketManager.getEventReceive(RealmManager.getSettingsDao().getUserId().getUserId());
            socketManager.listenEvent(eventReceive, this::onReceiveEvent);
            socketManager.connect();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }

    private void onReceiveEvent(Object[] objects) {
        viewModel.getMultisigTransactionsHistory(currencyId, networkId, walletAddress);
    }

    private void onWallet(Wallet wallet) {
        if (wallet != null) {
            walletIndex = wallet.getIndex();
            currencyId = wallet.getCurrencyId();
            networkId = wallet.getNetworkId();
            iconResourceId = wallet.getIconResourceId();
            walletAddress = wallet.getActiveAddress().getAddress();
            toolbarWalletName.setText(wallet.getWalletName());
            confirmationsNeeded = wallet.getMultisigWallet().getConfirmations();
            textCoin.setText(NativeDataHelper.Blockchain.valueOf(wallet.getCurrencyId()).name());
            textMoney.setText(CurrencyCode.USD.name());
            imageCoinLogo.setImageResource(iconResourceId);
            viewModel.transactions.observe(this, this::onHistory);
        }
    }

    private void onHistory(ArrayList<TransactionHistory> transactionHistories) {
        if (transactionHistories != null && getArguments() != null) {
            transaction = findTransaction(transactionHistories, getArguments().getString(SELECTED_TX));
            if (transaction != null) {
                setData(transaction);
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    private TransactionHistory findTransaction(ArrayList<TransactionHistory> histories, String transactionId) {
        for (TransactionHistory history : histories) {
            if (history.getTxHash().equals(transactionId)) {
                return history;
            }
        }
        return null;
    }

    private void setData(TransactionHistory transactionHistory) {
        this.transaction = transactionHistory;
        checkOwnerViewStatus(transactionHistory.getMultisigInfo().getOwners());
        boolean isIncoming = transactionHistory.getTxStatus() == Constants.TX_MEMPOOL_INCOMING;
        textValue.setText(isIncoming ? "+ " : "- ");
        textAmount.setText(isIncoming ? "+ " : "- ");
        if (!isIncoming) {
            groupFee.setVisibility(View.VISIBLE);
            final double feeAmount =CryptoFormatUtils.weiToEth(String.valueOf(transactionHistory.getGasLimit() * transactionHistory.getGasPrice()));
            final double feeFiat = feeAmount * getPreferredExchangeRate(transactionHistory.getStockExchangeRates());
            String fee = CryptoFormatUtils.FORMAT_ETH.format(feeAmount) + " ETH / " + CryptoFormatUtils.FORMAT_USD.format(feeFiat) + " USD";
            textFee.setText(fee);
        }
        textStatus.setText(transactionHistory.getMultisigInfo().isConfirmed() ?
                getTransactionDate(transactionHistory) : getString(R.string.waiting_confirmations));
        setVisibilityConfirmButtons(View.GONE);
        if (transactionHistory.getMultisigInfo().isConfirmed()) {
            setVisibilityConfirmButtons(View.GONE);
            parent.setBackgroundColor(isIncoming ? colorGreen : colorBlue);
            imageOperation.setImageResource(isIncoming ? R.drawable.ic_receive_big_new : R.drawable.ic_send_big);
            textConfirmations.setText(String.format(Locale.ENGLISH, "%d %s", transactionHistory.getConfirmations(),
                    getString(transactionHistory.getConfirmations() > 1 ?  R.string.confirmations : R.string.confirmation)));
        } else {
            textConfirmations.setText("");
            buttonView.setVisibility(View.GONE);
            imageOperation.setImageResource(isIncoming ? R.drawable.ic_receive_big_icon_waiting : R.drawable.ic_send_big_icon_waiting);
            if (!isOwnerHasStatus(transactionHistory.getMultisigInfo().getOwners(),
                    Constants.MULTISIG_OWNER_STATUS_CONFIRMED, Constants.MULTISIG_OWNER_STATUS_DECLINED)) {
                setVisibilityConfirmButtons(View.VISIBLE);
                enableAnimationSliders();
            }
        }
        if (!transactionHistory.isInternal()) {
            groupViewButton.setVisibility(View.VISIBLE);
        }
        textValue.append(getCurrencyAmount(transactionHistory));
        textAmount.append(getFiatAmount(transactionHistory, getPreferredExchangeRate(transactionHistory.getStockExchangeRates())));
        textAdressesFrom.setText(transactionHistory.getFrom());
        textAddressesTo.setText(transactionHistory.getTo());
        textCounter.setText(String.format(Locale.ENGLISH,
                "%d %s %d", getConfirmCount(transactionHistory.getMultisigInfo().getOwners()), getString(R.string.of), confirmationsNeeded));
        adapter.setOwners(transactionHistory.getMultisigInfo().getOwners());
    }

    private boolean isOwnerHasStatus(ArrayList<TransactionOwner> owners, int... statuses) {
        if (viewModel.getWalletLive().getValue() != null && viewModel.getWalletLive().getValue().getMultisigWallet() != null) {
            Wallet linkedWallet = RealmManager.getAssetsDao().getMultisigLinkedWallet(viewModel.getWalletLive().getValue().getMultisigWallet().getOwners());
            for (TransactionOwner owner : owners) {
                if (owner.getAddress().equals(linkedWallet.getActiveAddress().getAddress())) {
                    for (int status : statuses) {
                        if (owner.getConfirmationStatus() == status) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private void checkOwnerViewStatus(ArrayList<TransactionOwner> owners) {
        if (!isOwnerHasStatus(owners, Constants.MULTISIG_OWNER_STATUS_SEEN,
                Constants.MULTISIG_OWNER_STATUS_DECLINED, Constants.MULTISIG_OWNER_STATUS_CONFIRMED)) {
            viewModel.sendViewTransaction(currencyId, networkId, walletIndex, transaction.getTxHash(), getLifecycle());
        }
    }

    private String getTransactionDate(TransactionHistory transaction) {
        return DateHelper.DATE_FORMAT_TRANSACTION_INFO
                .format((transaction.getTxStatus() == TX_MEMPOOL_INCOMING || transaction.getTxStatus() == TX_MEMPOOL_OUTCOMING ?
                        transaction.getMempoolTime() : transaction.getBlockTime()) * 1000);
    }

    private int getConfirmCount(ArrayList<TransactionOwner> owners) {
        int result = 0;
        for (TransactionOwner owner : owners) {
            if (owner.getConfirmationStatus() == Constants.MULTISIG_OWNER_STATUS_CONFIRMED) {
                result++;
            }
        }
        return result;
    }

    private String getCurrencyAmount(TransactionHistory transactionHistory) {
        return CryptoFormatUtils.FORMAT_ETH.format(CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount()));
    }

    private String getFiatAmount(TransactionHistory transactionHistory, double exchangeRate) {
        return CryptoFormatUtils.ethToUsd(CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount()), exchangeRate);
    }

    private double getPreferredExchangeRate(ArrayList<TransactionHistory.StockExchangeRate> stockExchangeRates) {
        if (stockExchangeRates != null && stockExchangeRates.size() > 0) {
            for (TransactionHistory.StockExchangeRate rate : stockExchangeRates) {
                if (rate.getExchanges().getEthUsd() > 0) {
                    return rate.getExchanges().getEthUsd();
                }
            }
        }
        return 0.0;
    }

    private void setVisibilityConfirmButtons(int visibility) {
        sliderAccept.setVisibility(visibility);
        sliderDecline.setVisibility(visibility);
        buttonConfirm.setVisibility(visibility);
    }

    private void initAnimationSliders() {
        animator = ValueAnimator.ofFloat(-0.8f, 0.8f);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float absValue = Math.abs((float) animation.getAnimatedValue());
            sliderAccept.setAlpha(0.2f + absValue);
            sliderDecline.setAlpha(1f - absValue);
            if (absValue > 0.4f && absValue < 0.415f) {
                buttonConfirm.setText(R.string.slide_to_confirm);
            } else if (absValue > 0.385f && absValue < 0.4f) {
                buttonConfirm.setText(R.string.slide_to_decline);
            }
        });
    }

    private void enableAnimationSliders() {
        if (animator != null && !animator.isRunning()) {
            animator.setStartDelay(2000);
            animator.start();
        }
    }

    private void disableAnimationSliders() {
        if (animator != null) {
            animator.cancel();
        }
        sliderAccept.setAlpha(0.2f);
        sliderDecline.setAlpha(0.2f);
    }

    private void hideConfirmButtonAnimation() {
        int buttonHeight = buttonConfirm.getHeight();
        ValueAnimator hideAnimator = ValueAnimator.ofInt(0, buttonHeight);
        hideAnimator.setInterpolator(new DecelerateInterpolator());
        hideAnimator.setDuration(1000);
        hideAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            buttonConfirm.setTranslationY(value);
            sliderAccept.setTranslationY(value);
            sliderDecline.setTranslationY(value);
        });
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibilityConfirmButtons(View.GONE);
            }
        });
        hideAnimator.start();
    }

    private void readyViewsToSlide(View firstSlide, View secondSlide, int confirmText) {
        disableAnimationSliders();
        firstSlide.setAlpha(1f);
        secondSlide.setAlpha(0.2f);
        buttonConfirm.setText(confirmText);
    }

    private void onSliderMove(View view, float rawX, boolean isNegativeVector, Runnable onSliderFinish) {
        scrollView.requestDisallowInterceptTouchEvent(true);
        float x = isNegativeVector ? (buttonConfirm.getWidth() - (rawX + (view.getWidth() / 2))) : rawX - (view.getWidth() / 2);
        if (x > 0) {
            view.setTranslationX(isNegativeVector ? -x : x);
            float limit = isNegativeVector ? view.getX() : (buttonConfirm.getWidth() - view.getX() - view.getWidth());
            if (limit < 0 && !isConfirming) {
                isConfirming = true;
                view.clearFocus();
                hideConfirmButtonAnimation();
                onSliderFinish.run();
            }
        }
    }

    private void cancelSlide(View view) {
        view.performClick();
        enableAnimationSliders();
        view.setTranslationX(0f);
    }

    private boolean presender(int confirmations, ArrayList<TransactionOwner> owners) {
        int confirmedCount = getConfirmCount(owners);
        return confirmations - 1 <= confirmedCount;
    }

    private void onAcceptTransaction() {
        if (presender(viewModel.getWalletLive().getValue().getMultisigWallet().getConfirmations(),
                transaction.getMultisigInfo().getOwners())) {
            if (new BigInteger(transaction.getTxOutAmount())
                    .compareTo(new BigInteger(viewModel.getWalletLive().getValue().getAvailableBalance())) > 0) {
                viewModel.errorMessage.setValue(Multy.getContext().getString(R.string.no_balance));
                return;
            }
        }
        viewModel.requestEstimation(walletAddress, estimationConfirm -> {
            viewModel.requestFeeRates(currencyId, networkId, mediumGasPrice -> {
                viewModel.sendConfirmTransaction(walletAddress, transaction.getMultisigInfo().getRequestId(),
                        estimationConfirm, mediumGasPrice, isSuccess -> {
                            if (isSuccess) {
                                viewModel.getMultisigTransactionsHistory(currencyId, networkId, walletAddress);
                            } else {
                                setVisibilityConfirmButtons(View.VISIBLE);
                                viewModel.errorMessage.setValue(getString(R.string.something_went_wrong));
                            }
                        }, this::onThrowable);
            }, this::onThrowable);
        }, this::onThrowable);
    }

    private void onDeclineTransaction() {
        viewModel.sendDeclineTransaction(currencyId, networkId, walletIndex, transaction.getTxHash(), getLifecycle());
    }

    private void onThrowable(Throwable throwable) {
        viewModel.errorMessage.setValue(throwable.getLocalizedMessage());
        throwable.printStackTrace();
    }

    @OnTouch(R.id.image_slider_accept)
    boolean onTouchAccept(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                readyViewsToSlide(sliderAccept, sliderDecline, R.string.slide_to_confirm);
                return true;
            case MotionEvent.ACTION_MOVE:
                onSliderMove(view, event.getRawX(), false, this::onAcceptTransaction);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelSlide(view);
                return true;
        }
        return false;
    }

    @OnTouch(R.id.image_slider_decline)
    boolean onTouchDecline(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                readyViewsToSlide(sliderDecline, sliderAccept, R.string.slide_to_decline);
                return true;
            case MotionEvent.ACTION_MOVE:
                onSliderMove(view, event.getRawX(), true, this::onDeclineTransaction);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelSlide(view);
                return true;
        }
        return false;
    }

    @OnClick(R.id.button_view)
    void onClickView(View view) {
        try {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 500);
            String url = (networkId == NativeDataHelper.NetworkId.RINKEBY.getValue() ?
                    Constants.ETHERSCAN_RINKEBY_INFO_PATH : Constants.ETHERSCAN_MAIN_INFO_PATH) + transaction.getTxHash();
            ((BaseActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_full, WebFragment.newInstance(url))
                    .addToBackStack(TransactionInfoFragment.TAG)
                    .commit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnClick({R.id.text_addresses_from, R.id.text_addresses_to})
    void onClickAddress(View view) {
        if (view instanceof TextView) {
            AddressActionsDialogFragment.getInstance(((TextView) view).getText().toString(),
                    currencyId, networkId, iconResourceId, true, () ->
//                            todo notify observers
                            viewModel.transactions.setValue(viewModel.transactions.getValue()))
                    .show(getChildFragmentManager(), AddressActionsDialogFragment.TAG);
        }
    }

    @OnClick(R.id.back)
    void onClickBack() {
        getActivity().onBackPressed();
    }
}
