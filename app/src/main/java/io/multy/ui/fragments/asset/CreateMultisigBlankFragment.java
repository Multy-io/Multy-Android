/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.samwolfand.oneprefs.Prefs;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.CreateMultisigRequest;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.ChooseMembersDialog;
import io.multy.ui.fragments.dialogs.WalletChooserDialogFragment;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anschutz1927@gmail.com on 19.07.18.
 */
public class CreateMultisigBlankFragment extends BaseFragment {

    public static final String TAG = CreateMultisigBlankFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText inputName;
    @BindView(R.id.text_signs_members)
    TextView textSignsMembers;
    @BindView(R.id.text_chain_currency)
    TextView textChain;
    @BindView(R.id.image_currency)
    ImageView imageCurrency;
    @BindView(R.id.text_wallet_name)
    TextView textName;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.text_create)
    View buttonCreate;

    private MutableLiveData<Boolean> isValidData = new MutableLiveData<>();
    private MutableLiveData<Integer> membersCount = new MutableLiveData<>();
    private MutableLiveData<Integer> confirmsCount = new MutableLiveData<>();
    private MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    private Disposable disposable;

    public static CreateMultisigBlankFragment getInstance() {
        return new CreateMultisigBlankFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isValidData.setValue(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_multisig_blank, container, false);
        ButterKnife.bind(this, view);
        initialize();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wallet.getValue() != null && !wallet.getValue().isValid() && getActivity() != null) {
            Wallet wallet = RealmManager.getAssetsDao().getWalletById(getActivity().getIntent()
                    .getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            this.wallet.setValue(wallet);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == ChooseMembersDialog.REQUEST_MEMBERS) {
                setConfirmsAndMembers(data.getIntExtra(ChooseMembersDialog.EXTRA_CONFIRMS, 0),
                        data.getIntExtra(ChooseMembersDialog.EXTRA_MEMBERS, 0));
            } else if (requestCode == WalletChooserDialogFragment.REQUEST_WALLET_ID && getActivity() != null) {
                getActivity().getIntent().putExtra(Constants.EXTRA_WALLET_ID, data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
                Wallet wallet = RealmManager.getAssetsDao().getWalletById(data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
                this.wallet.setValue(wallet);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {
        setConfirmsAndMembersCount(0, 0);
        onWallet(null);
        inputName.postDelayed(() -> inputName.requestFocus(), 300);
        membersCount.observe(this, members -> setConfirmsAndMembersCount(getConfirmsCount(), getMembersCount()));
        textChain.setText("Ethereum ∙ ETH");
        wallet.observe(this, this::onWallet);
        isValidData.observe(this, isValid -> buttonCreate.setBackgroundColor(isValid == null || !isValid ?
                        Color.parseColor("#BEC8D2") : Color.parseColor("#FF459FF9")));
        subscribeInput();
    }

    private void setConfirmsAndMembers(int confirmsCount, int membersCount) {
        this.confirmsCount.setValue(confirmsCount);
        this.membersCount.setValue(membersCount);
    }

    private int getConfirmsCount() {
        return confirmsCount.getValue() == null ? 0 : confirmsCount.getValue();
    }

    private int getMembersCount() {
        return membersCount.getValue() == null ? 0 : membersCount.getValue();
    }

    private void setConfirmsAndMembersCount(int confirms, int members) {
        textSignsMembers.setText(String.format(getString(R.string.count_of), confirms, members));
        validateData();
    }

    private void onWallet(Wallet wallet) {
        if (wallet != null) {
            imageCurrency.setVisibility(View.VISIBLE);
            imageCurrency.setImageResource(wallet.getIconResourceId());
            textName.setVisibility(View.VISIBLE);
            textName.setText(wallet.getWalletName());
            textAddress.setText(wallet.getActiveAddress().getAddress());
            textAddress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        } else {
            imageCurrency.setVisibility(View.GONE);
            textName.setVisibility(View.GONE);
            textAddress.setText(R.string.select_your_wallet);
            textAddress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        }
        validateData();
    }

    private void subscribeInput() {
        disposable = RxTextView.textChanges(inputName).observeOn(AndroidSchedulers.mainThread())
                .filter(name -> {
                    if (name.length() > 0) {
                        return true;
                    }
                    isValidData.setValue(false);
                    return false;
                }).debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString).subscribe(name -> validateData());
    }

    private @Nullable String getInviteCode() {
        StringBuilder inviteCode = new StringBuilder(UUID.randomUUID().toString()).append(Constants.DEVICE_NAME);
        try {
            byte[] result = NativeDataHelper.digestSha3256(inviteCode.toString().getBytes());
            return Base64.encodeToString(result, Base64.DEFAULT);
        } catch (JniException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void validateData() {
        if (inputName.getText().length() > 0 && getMembersCount() > 0 && getConfirmsCount() > 0 && wallet.getValue() != null) {
            isValidData.setValue(true);
        } else {
            isValidData.setValue(false);
        }
    }

    private void handleClick(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
    }

    @OnClick(R.id.name_container)
    void onClickName() {
        inputName.requestFocus();
        showKeyboard(getActivity(), inputName);
    }

    @OnClick(R.id.button_signs_members)
    void onClickMembers(View view) {
        handleClick(view);
        if (getFragmentManager() != null) {
            ChooseMembersDialog membersDialog = (ChooseMembersDialog) getFragmentManager().findFragmentByTag(ChooseMembersDialog.TAG);
            if (membersDialog == null) {
                membersDialog = ChooseMembersDialog.getInstance(getConfirmsCount(), getMembersCount());
            }
            membersDialog.setTargetFragment(this, ChooseMembersDialog.REQUEST_MEMBERS);
            membersDialog.show(getFragmentManager(), ChooseMembersDialog.TAG);
        }
    }

    @OnClick(R.id.button_chain)
    void onClickChain(View view) {
        handleClick(view);
    }

    @OnClick(R.id.button_wallet)
    void onClickWallet(View view) {
        handleClick(view);
        if (getFragmentManager() != null) {
            WalletChooserDialogFragment dialog = WalletChooserDialogFragment.getInstance(NativeDataHelper.Blockchain.ETH.getValue());
            dialog.setTargetFragment(this, WalletChooserDialogFragment.REQUEST_WALLET_ID);
            dialog.show(getFragmentManager(), WalletChooserDialogFragment.TAG);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @OnClick(R.id.text_create)
    void onClickCreate(View view) {
        handleClick(view);
        if (isValidData.getValue() != null && isValidData.getValue()) {
            isValidData.setValue(false);
            showProgressDialog();
            String inviteCode = getInviteCode();
            int networkId = wallet.getValue().getNetworkId();
            final int topIndex = Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX_ETH + networkId, 0);
            CreateMultisigRequest request = new CreateMultisigRequest();
            request.setCurrencyId(NativeDataHelper.Blockchain.ETH.getValue());
            request.setNetworkId(wallet.getValue().getNetworkId());
            request.setAddress(wallet.getValue().getActiveAddress().getAddress());
            request.setAddressIndex(wallet.getValue().getAddresses().size() - 1);
            request.setWalletIndex(topIndex);
            request.setWalletName(inputName.getText().toString());
            request.setMultisig(new CreateMultisigRequest.Multisig(getConfirmsCount(), getMembersCount(), inviteCode));
            MultyApi.INSTANCE.addWallet(view.getContext(), request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        //todo start ms activity
//                        startActivity(new Intent(getContext(), CreateMultiSigActivity.class));
                    } else {
                        Log.e(getClass().getSimpleName(), "creation failed");
                        validateData();
                    }
                    dismissProgressDialog();
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    t.printStackTrace();
                    dismissProgressDialog();
                    validateData();
                }
            });
        }
    }

    @OnClick(R.id.text_cancel)
    void onClickCancel() {
        getActivity().onBackPressed();
    }
}
