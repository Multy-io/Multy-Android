/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send.ethereum;

import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.MessageResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class EthSendSummaryFragment extends BaseFragment {

    private static final String TAG = EthSendSummaryFragment.class.getSimpleName();
    public static final String TAG_SEND_SUCCESS = EthSendSummaryFragment.class.getSimpleName();

    @BindView(R.id.text_receiver_balance_original)
    TextView textReceiverBalanceOriginal;
    @BindView(R.id.text_receiver_balance_currency)
    TextView textReceiverBalanceCurrency;
    @BindView(R.id.text_receiver_address)
    TextView textReceiverAddress;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_sender_balance_original)
    TextView textSenderAddress;
    @BindView(R.id.text_sender_balance_currency)
    TextView textSenderBalance;
    @BindView(R.id.text_fee_amount)
    TextView textFeeAmount;
    @BindView(R.id.text_fee_speed_label)
    TextView textFeeSpeedLabel;
    @BindView(R.id.button_next)
    TextView buttonNext;
    @BindView(R.id.input_note)
    View inputNote;
    @BindView(R.id.image_slider)
    ImageView slider;
    @BindView(R.id.image_slider_finish)
    ImageView sliderFinish;
    @BindView(R.id.scrollview)
    ScrollView scrollView;
    @BindView(R.id.image_logo)
    ImageView imageLogo;

    @BindString(R.string.donation_format_pattern)
    String formatPattern;
    @BindString(R.string.donation_format_pattern_bitcoin)
    String formatPatternBitcoin;

    private AssetSendViewModel viewModel;
    private boolean isSending = false;
    private ValueAnimator textLarger;
    private ValueAnimator textSmaller;
    private ValueAnimator textAlpha;

    public static EthSendSummaryFragment newInstance() {
        return new EthSendSummaryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_summary_eth, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        subscribeToErrors();
        setInfo();
        viewModel.setAmountScanned(false);
        inputNote.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Analytics.getInstance(getActivity()).logSendSummary(AnalyticsConstants.SEND_SUMMARY_NOTE, viewModel.getChainId());
            }
        });
        Analytics.getInstance(getActivity()).logSendSummaryLaunch(viewModel.getChainId());
        initAnimations();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        buttonNext.postDelayed(() -> {
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && imm.isActive())
                    imm.hideSoftInputFromWindow(buttonNext.getWindowToken(), 0);
            }
        }, 150);
    }

    @Override
    public void onResume() {
        super.onResume();
        startSlideAnimation();
    }

    @Override
    public void onPause() {
        stopSlideAnimation();
        super.onPause();
    }

    private void initAnimations() {
        final float startSize = buttonNext.getTextSize();
        final float endSize = startSize * 1.1f;
        textLarger = ValueAnimator.ofFloat(startSize, endSize);
        textLarger.setStartDelay(300);
        textLarger.setDuration(100);
        textLarger.addUpdateListener(valueAnimator -> {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            buttonNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
        });
        textSmaller = ValueAnimator.ofFloat(endSize, startSize);
        textSmaller.setStartDelay(400);
        textSmaller.setDuration(100);
        textSmaller.addUpdateListener(valueAnimator -> {
            float animatedValue = (float) valueAnimator.getAnimatedValue();
            buttonNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
        });
        textAlpha = ValueAnimator.ofInt(76, 200);
        textAlpha.setStartDelay(200);
        textAlpha.setDuration(200);
        textAlpha.setRepeatCount(1);
        textAlpha.setRepeatMode(ValueAnimator.REVERSE);
        textAlpha.addUpdateListener(valueAnimator -> {
            int animatedValue = (int) valueAnimator.getAnimatedValue();
            setButtonAlpha(animatedValue);
        });
    }

    private void send() {
        viewModel.isLoading.setValue(true);
        String addressTo = viewModel.getReceiverAddress().getValue();
        Wallet wallet = viewModel.getWallet().isMultisig() ?
                RealmManager.getAssetsDao().getMultisigLinkedWallet(viewModel.getWallet().getMultisigWallet().getOwners()) : viewModel.getWallet();

        try {
            final String txHex = viewModel.transaction.getValue();
            viewModel.isLoading.setValue(true);
            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(wallet.getCurrencyId(), wallet.getNetworkId(),
                    new HdTransactionRequestEntity.Payload("", 0, wallet.getIndex(), txHex.startsWith("0x") ? txHex : "0x" + txHex, false))).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        viewModel.isLoading.postValue(false);
                        long uniqueId = RecentAddress.stringToId(addressTo);
                        if (!RealmManager.getAssetsDao().ifAddressExist(uniqueId)) {
                            RealmManager.getAssetsDao().saveRecentAddress(new RecentAddress(viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId(), addressTo, uniqueId));
                        }
                        CompleteDialogFragment.newInstance(viewModel.getChainId()).show(getActivity().getSupportFragmentManager(), TAG_SEND_SUCCESS);
                    } else {
                        Analytics.getInstance(getActivity()).logError(AnalyticsConstants.ERROR_TRANSACTION_API);
                        try {
                            String errorBody = response.errorBody() == null ? "" : response.errorBody().string();
                            showError(new IllegalStateException(errorBody));
                            if (response.code() == 406 && getContext() != null) {
                                Analytics.getInstance(getContext()).logEvent(TAG, "406", errorBody);
                            }                        } catch (IOException e) {
                            e.printStackTrace();
                            showError(new IllegalStateException(""));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                    showError(t);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError(e);
        }
    }


    private void setInfo() {
        CurrenciesRate currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        textReceiverBalanceOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmountFull()));
        textReceiverBalanceOriginal.append(Constants.SPACE);
        textReceiverBalanceOriginal.append(CurrencyCode.ETH.name());
        textReceiverBalanceCurrency.setText(NumberFormatter.getFiatInstance().format(viewModel.getAmount() * currenciesRate.getEthToUsd()));
        textReceiverBalanceCurrency.append(Constants.SPACE);
        textReceiverBalanceCurrency.append(CurrencyCode.USD.name());
//        textReceiverAddress.setText(viewModel.getReceiverAddress().getValue());
        textReceiverAddress.setText(viewModel.thoseAddress.getValue());
        textWalletName.setText(viewModel.getWallet().getWalletName());
        textSenderAddress.setText(viewModel.getWallet().getActiveAddress().getAddress());
        long balance = viewModel.getWallet().getBalanceNumeric().longValue();
        String balanceLabel = (balance != 0 ? CryptoFormatUtils.weiToEthLabel(viewModel.getWallet().getBalance()) :
                String.valueOf(balance)).concat(" / ").concat(viewModel.getWallet().getFiatBalanceLabel());
        textSenderBalance.setText(balanceLabel);
        textFeeSpeedLabel.setText(viewModel.getFee().getName());

        final double feeETh = EthWallet.getTransactionPrice(viewModel.getFee().getAmount());
        textFeeAmount.setText(String.format("%s ETH / %s USD", CryptoFormatUtils.FORMAT_ETH.format(feeETh), CryptoFormatUtils.ethToUsd(feeETh)));
        imageLogo.setImageResource(viewModel.getWallet().getIconResourceId());
    }

    private void showError(Throwable throwable) {
        viewModel.isLoading.postValue(false);
        if (throwable.getMessage().contains("Transaction is trying to spend more than available") ||
                throwable.getMessage().contains("insufficient funds")) {
            viewModel.errorMessage.setValue(getString(R.string.not_enough_balance));
        } else {
            if (throwable.getMessage().contains("nonce too low")) {
                viewModel.updateWallets();
            }
            viewModel.errorMessage.setValue(getString(R.string.error_sending_tx));
            throwable.printStackTrace();
        }
        slider.postDelayed(() -> {
            isSending = false;
            sliderFinish.setVisibility(View.VISIBLE);
            returnSliderOnStart();
        }, 1000);
    }

    private void startSlideAnimation() {
        slider.animate().cancel();
        if (slider.getDrawable() != null && slider.getDrawable() instanceof Animatable &&
                sliderFinish.getDrawable() != null && sliderFinish.getDrawable() instanceof Animatable) {
            slider.animate().setStartDelay(2000).withEndAction(() ->
                    slider.animate().setStartDelay(3000).withEndAction(() ->
                            slideAnimation((Animatable) slider.getDrawable(), (Animatable) sliderFinish.getDrawable())).start())
                    .start();
        }
    }

    private void slideAnimation(Animatable animatableSlider, Animatable animatableFinish) {
        animatableSlider.start();
        animatableFinish.start();
        textLarger.start();
        textAlpha.start();
        textSmaller.start();
        slider.animate().withEndAction(() -> slideAnimation(animatableSlider, animatableFinish)).start();
    }

    private void goOutAnimation() {
        slider.animate().cancel();
        slider.animate().setStartDelay(0)
                .translationX(buttonNext.getX() + buttonNext.getWidth())
                .setDuration(200)
                .withStartAction(() -> sliderFinish.setVisibility(View.GONE))
                .withEndAction(this::send).start();
    }

    private void stopSlideAnimation() {
        slider.animate().cancel();
    }

    private void moveSliderToNextPoint(float rawX) {
        rawX -= slider.getWidth() / 2;
        if (rawX > 0) {
            slider.setTranslationX(rawX);
        }
        float sliderPosition = slider.getX() + slider.getWidth();
        float endPosition = buttonNext.getX() + buttonNext.getWidth();
        if (sliderPosition > endPosition) {
            isSending = true;
            slider.clearFocus();
            goOutAnimation();
        }
    }

    private void returnSliderOnStart() {
        slider.setTranslationX(0f);
        setButtonAlpha(76);
        startSlideAnimation();
    }

    private void setButtonAlpha(int alpha) {
        if (getContext() != null) {
            int textColor = ColorUtils.setAlphaComponent(ContextCompat.getColor(getContext(), R.color.white), alpha);
            buttonNext.setTextColor(textColor);
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @OnTouch(R.id.image_slider)
    boolean OnSliderTouch(View v, MotionEvent ev) {
        if (isSending) {
            v.clearFocus();
            return false;
        }
        scrollView.requestDisallowInterceptTouchEvent(true);
        stopSlideAnimation();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                moveSliderToNextPoint(ev.getRawX());
                return true;
            case MotionEvent.ACTION_UP:
                returnSliderOnStart();
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return false;
        }
    }
}
