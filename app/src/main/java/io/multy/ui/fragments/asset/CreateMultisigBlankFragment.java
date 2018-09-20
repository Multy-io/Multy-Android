/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.samwolfand.oneprefs.Prefs;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.CreateMultisigRequest;
import io.multy.model.requests.Multisig;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.CreateMultiSigActivity;
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
import timber.log.Timber;

/**
 * Created by anschutz1927@gmail.com on 19.07.18.
 */

/**
 * TODO implement pattern with viewmodel. Make it clean
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

    private boolean isDataValid;
    private int membersCount = 0;
    private int confirmationsCount = 0;
    private Wallet wallet;
    private Disposable disposable;
    private int currencyId = NativeDataHelper.Blockchain.ETH.getValue();
    private int networkId = NativeDataHelper.NetworkId.RINKEBY.getValue();
    private String chainName = "Ethereum・ETH Testnet";

    public static CreateMultisigBlankFragment getInstance() {
        return new CreateMultisigBlankFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_multisig_blank, container, false);
        ButterKnife.bind(this, view);
        showInfo();
        setDataValid(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wallet == null || !wallet.isValid() && getActivity() != null) {
            wallet = RealmManager.getAssetsDao().getWalletById(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
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
                confirmationsCount = data.getIntExtra(ChooseMembersDialog.EXTRA_CONFIRMS, 0);
                membersCount = data.getIntExtra(ChooseMembersDialog.EXTRA_MEMBERS, 0);
                updateCounts();
            } else if (requestCode == WalletChooserDialogFragment.REQUEST_WALLET_ID && getActivity() != null) {
                getActivity().getIntent().putExtra(Constants.EXTRA_WALLET_ID, data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
                wallet = RealmManager.getAssetsDao().getWalletById(data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
                showWalletInfo();
            } else if (requestCode == Constants.REQUEST_CODE_SET_CHAIN) {
                networkId = data.getIntExtra(Constants.CHAIN_NET, 0);
                currencyId = data.getIntExtra(Constants.CHAIN_ID, 0);
                chainName = data.getStringExtra(Constants.CHAIN_NAME);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showInfo() {
        updateCounts();
        showWalletInfo();
        inputName.postDelayed(() -> inputName.requestFocus(), 300);
        textChain.setText(chainName);
        subscribeInput();
    }

    private void setDataValid(boolean isValid) {
        isDataValid = isValid;
        buttonCreate.setBackgroundColor(!isValid ?
                getContext().getResources().getColor(R.color.gray_light_dark) : getContext().getResources().getColor(R.color.colorPrimary));
    }

    private void updateCounts() {
        textSignsMembers.setText(String.format(getString(R.string.count_of), confirmationsCount, membersCount));
        validateData();
    }

    private void showWalletInfo() {
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
                    setDataValid(false);
                    return false;
                }).debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString).subscribe(name -> validateData());
    }

    @Nullable
    private String getInviteCode() {
        StringBuilder inviteCode = new StringBuilder(UUID.randomUUID().toString()).append(Constants.DEVICE_NAME);
        try {
            final byte[] codeBytes = NativeDataHelper.digestSha3256(inviteCode.toString().trim().getBytes());
            String result = Base64.encodeToString(codeBytes, Base64.DEFAULT)
                    .replace("/", "").replace("\n", "");
            result = result + result;
            result = result.substring(0, Constants.INVITE_CODE_LENGTH);
            return result.toLowerCase();
        } catch (JniException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void validateData() {
        setDataValid(inputName.getText().length() > 0 && membersCount > 0 && confirmationsCount > 0 && wallet != null);
    }

    private void scheduleButtonDisable(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
    }

    private void loadWalletsAndOpenMultisig(long dateOfCreation, String inviteCode) {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                WalletsResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    RealmManager.getAssetsDao().saveWallets(body.getWallets());
                    wallet = RealmManager.getAssetsDao().getWalletById(dateOfCreation);
                    startActivity(new Intent(getContext(), CreateMultiSigActivity.class)
                            .putExtra(Constants.EXTRA_WALLET_ID, dateOfCreation)
                            .putExtra(Constants.EXTRA_CREATE, true)
                            .putExtra(Constants.EXTRA_RELATED_WALLET_ID, wallet.getId())
                            .putExtra(Constants.EXTRA_INVITE_CODE, inviteCode));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WalletsResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @OnClick(R.id.name_container)
    void onClickName() {
        inputName.requestFocus();
        showKeyboard(getActivity(), inputName);
    }

    @OnClick(R.id.button_signs_members)
    void onClickMembers(View view) {
        scheduleButtonDisable(view);
        if (getFragmentManager() != null) {
            ChooseMembersDialog membersDialog = (ChooseMembersDialog) getFragmentManager().findFragmentByTag(ChooseMembersDialog.TAG);
            if (membersDialog == null) {
                membersDialog = ChooseMembersDialog.getInstance(confirmationsCount, membersCount);
            }
            membersDialog.setTargetFragment(this, ChooseMembersDialog.REQUEST_MEMBERS);
            membersDialog.show(getFragmentManager(), ChooseMembersDialog.TAG);
        }
    }

    @OnClick(R.id.button_chain)
    void onClickChain(View view) {
        scheduleButtonDisable(view);
        if (getView() != null && getView().getParent() != null &&
                getView().getParent() instanceof ViewGroup && getFragmentManager() != null) {
            final int containerId = ((ViewGroup) getView().getParent()).getId();
            ChainChooserFragment fragment = ChainChooserFragment
                    .getInstance(true);
            fragment.setTargetFragment(this, Constants.REQUEST_CODE_SET_CHAIN);
            getFragmentManager().beginTransaction()
                    .replace(containerId, fragment, ChainChooserFragment.TAG)
                    .addToBackStack(ChainChooserFragment.TAG)
                    .commit();
        }
    }

    @OnClick(R.id.button_wallet)
    void onClickWallet(View view) {
        scheduleButtonDisable(view);
        if (getFragmentManager() != null) {
            WalletChooserDialogFragment dialog = WalletChooserDialogFragment
                    .getInstance(currencyId, networkId);
            dialog.exceptMultisig(true);
            dialog.setTargetFragment(this, WalletChooserDialogFragment.REQUEST_WALLET_ID);
            dialog.show(getFragmentManager(), WalletChooserDialogFragment.TAG);
        }
    }

    @OnClick(R.id.text_create)
    void onClickCreate(View view) {
        scheduleButtonDisable(view);
        if (isDataValid) {
            setDataValid(false);
            showProgressDialog();
            final String inviteCode = getInviteCode();
            final int networkId = wallet.getNetworkId();
            final int topIndex = Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX_ETH + networkId, 0);
            //TODO builder.pattern please
            CreateMultisigRequest request = new CreateMultisigRequest();
            request.setCurrencyId(NativeDataHelper.Blockchain.ETH.getValue());
            request.setNetworkId(networkId);
            request.setAddress(wallet.getActiveAddress().getAddress());
            request.setAddressIndex(wallet.getAddresses().size() - 1);
            request.setWalletIndex(wallet.getIndex());
            request.setWalletName(inputName.getText().toString());
            request.setMultisig(new Multisig(confirmationsCount, membersCount, inviteCode));

            MultyApi.INSTANCE.addWallet(view.getContext(), request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Timber.i("create multisig success");
                        getActivity().finish();
                        try {
                            if (response.isSuccessful()) {
                                String body = response.body().string();
                                long dateOfCreation = new JSONObject(body).getLong("time");
                                loadWalletsAndOpenMultisig(dateOfCreation, inviteCode);
                            } else {
                                throw new Exception(getString(R.string.something_went_wrong));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Timber.e("create multisig fail");
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
