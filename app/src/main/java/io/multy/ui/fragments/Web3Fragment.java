/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.samwolfand.oneprefs.Prefs;

import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.MyWebView;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.dialogs.WalletChooserDialogFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.WalletViewModel;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import trust.core.entity.Address;

public class Web3Fragment extends BaseFragment {

    //        https://plasma.bankex.com/
//        "https://dapps.trustwalletapp.com/"
    //https://dragonereum-alpha-test.firebaseapp.com
//        final String url = "https://dapps.trustwalletapp.com/";

    @BindView(R.id.web_view)
    MyWebView webView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.image_coin)
    ImageView imageCoin;
    @BindView(R.id.text_address)
    TextView textAddress;

    private String dappUrl = "https://dragonereum-alpha-test.firebaseapp.com/";
    private WalletViewModel viewModel;

    public static Web3Fragment newInstance() {
        return new Web3Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        dappUrl = requireActivity().getIntent().hasExtra(Constants.EXTRA_URL) ?
                requireActivity().getIntent().getStringExtra(Constants.EXTRA_URL) : Prefs.getString(Constants.PREF_DRAGONS_URL, dappUrl);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_web_dapps, container, false);
        ButterKnife.bind(this, convertView);
        initRedirect();
        refreshLayout.setOnRefreshListener(() -> webView.postDelayed(() -> {
            webView.reload();
            refreshLayout.setRefreshing(false);
        }, 3000));

        return convertView;
    }

    @Override
    public void onDestroy() {
//        if (requireActivity().getIntent().hasExtra(Constants.EXTRA_URL)) {
//            requireActivity().getIntent().removeExtra(Constants.EXTRA_URL);
//            requireActivity().getIntent().removeExtra(Constants.EXTRA_CURRENCY_ID);
//            requireActivity().getIntent().removeExtra(Constants.EXTRA_NETWORK_ID);
//        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            requireActivity().getIntent().putExtra(Constants.EXTRA_WALLET_ID, data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            if (viewModel.getWalletLive().getValue() == null) {
                setWallet(data.getLongExtra(Constants.EXTRA_WALLET_ID, -1));
                initState();
            } else {
                setWallet(data.getLongExtra(Constants.EXTRA_WALLET_ID, -1));
            }
            loadUrl();
//            final WalletAddress walletAddress = selectedWallet.getActiveAddress();
//            if (walletAddress.getAmount() > 5000) {

//            } else {
//                Toast.makeText(getActivity(), "Wrong wallet. Please choose another one", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void initRedirect() {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
            checkIntentParams();
        } else {
            showProgressDialog();
            viewModel.createFirstWallets(() -> {
                dismissProgressDialog();
                if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED, false)) {
                    checkIntentParams();
                } else {
                    restartActivity();
                }
            });
        }
    }

    private void checkIntentParams() {
        if (getActivity() != null) {
            if (getActivity().getIntent().hasExtra(Constants.EXTRA_CURRENCY_ID) &&
                    getActivity().getIntent().hasExtra(Constants.EXTRA_NETWORK_ID)) {
                final int currencyId = getActivity().getIntent().getIntExtra(Constants.EXTRA_CURRENCY_ID, -1);
                final int networkId = getActivity().getIntent().getIntExtra(Constants.EXTRA_NETWORK_ID, -1);
                if (currencyId == -1 || networkId == -1) {
                    restartActivity(); //wrong currencyId and/or networkId //todo we can simply load url here
                } else {
                    updateWallets(currencyId, networkId);
                } /*else if (!isWalletsExist(currencyId, networkId)) {
                    createWalletWithParams(currencyId, networkId);
                } else {
                    showChooser(currencyId, networkId);
                }*/
            } else if (dappUrl.equals(Prefs.getString(Constants.PREF_DRAGONS_URL, "no value")) &&
                    Prefs.getInt(Constants.PREF_URL_CURRENCY_ID, -1) != -1 && Prefs.getInt(Constants.PREF_URL_NETWORK_ID, -1) != -1) {
                updateWallets(Prefs.getInt(Constants.PREF_URL_CURRENCY_ID), Prefs.getInt(Constants.PREF_URL_NETWORK_ID));
                /*if (!isWalletsExist(Prefs.getInt(Constants.PREF_URL_CURRENCY_ID), Prefs.getInt(Constants.PREF_URL_NETWORK_ID))) {
                    createWalletWithParams(Prefs.getInt(Constants.PREF_URL_CURRENCY_ID), Prefs.getInt(Constants.PREF_URL_NETWORK_ID));
                } else {
                    showChooser(Prefs.getInt(Constants.PREF_URL_CURRENCY_ID), Prefs.getInt(Constants.PREF_URL_NETWORK_ID));
                }*/
            } else {
                loadUrl(); //todo load url if wallet is not needed
            }
        }
    }

    private void createWalletWithParams(final int currencyId, final int networkId) {
        final String walletName = String.format(getString(R.string.my_first_wallet_name),
                NativeDataHelper.Blockchain.valueOf(currencyId).name());
        Wallet newWallet = viewModel.createWallet(walletName, currencyId, networkId);
        showProgressDialog();
        MultyApi.INSTANCE.addWallet(getActivity(), newWallet).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                updateWallets(() -> {
                    dismissProgressDialog();
                    if (!isWalletsExist(currencyId, networkId)) {
                        restartActivity();
                    } else {
                        showChooser(currencyId, networkId);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                dismissProgressDialog();
                t.printStackTrace();
                Crashlytics.logException(t);
                restartActivity();
            }
        });
    }

    private void updateWallets(Runnable onComplete) {
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                WalletsResponse body = response.body();
                if (body != null && body.getWallets() != null) {
                    RealmManager.getAssetsDao().deleteAll();
                    RealmManager.getAssetsDao().saveWallets(body.getWallets());
                }
                onComplete.run();
            }

            @Override
            public void onFailure(@NonNull Call<WalletsResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                onComplete.run();
            }
        });
    }

    private void updateWallets(int currencyId, int networkId) {
        showProgressDialog();
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                dismissProgressDialog();
                WalletsResponse body = response.body();
                if (body != null && body.getWallets() != null) {
                    RealmManager.getAssetsDao().deleteAll();
                    RealmManager.getAssetsDao().saveWallets(body.getWallets());
                }
                checkAssets(currencyId, networkId);
            }

            @Override
            public void onFailure(@NonNull Call<WalletsResponse> call, @NonNull Throwable t) {
                dismissProgressDialog();
                t.printStackTrace();
                checkAssets(currencyId, networkId);
            }
        });
    }

    private void checkAssets(int currencyId, int networkId) {
        if (!isWalletsExist(currencyId, networkId)) {
            createWalletWithParams(currencyId, networkId);
        } else {
            showChooser(currencyId, networkId);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isWalletsExist(int currencyId, int networkId) {
        RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getWallets(currencyId, networkId, false);
        return wallets.size() != 0;
    }

    private void showChooser(final int currencyId, final int networkId) {
        if (getFragmentManager() != null) {
            WalletChooserDialogFragment dialog;
            if (currencyId != -1 && networkId != -1) {
                dialog = WalletChooserDialogFragment.getInstance(currencyId, networkId);
            } else if (dappUrl.equals(Prefs.getString(Constants.PREF_DRAGONS_URL, "no value")) &&
                    Prefs.getInt(Constants.PREF_URL_CURRENCY_ID, -1) != -1 &&
                    Prefs.getInt(Constants.PREF_URL_NETWORK_ID, -1) != -1) {
                dialog = WalletChooserDialogFragment.getInstance(Prefs.getInt(Constants.PREF_URL_CURRENCY_ID), Prefs.getInt(Constants.PREF_URL_NETWORK_ID));
            } else if (currencyId != -1) {
                dialog = WalletChooserDialogFragment.getInstance(currencyId);
            } else {
                dialog = WalletChooserDialogFragment.getInstance();
            }
            dialog.exceptMultisig(true);
            dialog.setMainNet(true);
            dialog.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                void onDestroy() {
                    dialog.getLifecycle().removeObserver(this);
                    if (viewModel.getWalletLive().getValue() == null && getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showAssetsScreen();
                    }
                }
            });
            dialog.setTargetFragment(this, WalletChooserDialogFragment.REQUEST_WALLET_ID);
            dialog.show(getFragmentManager(), WalletChooserDialogFragment.TAG);
        }
    }

    private void initState() {
        webView.setRpcUrl("https://rinkeby.infura.io/v3/78ae782ed28e48c0b3f74ca69c4f7ca8");
        webView.requestFocus();
        webView.setOnSignMessageListener(message -> {
            Timber.d("onSignMessage:" + message.value);
            String signed = "0x" + NativeDataHelper.ethereumPersonalSign(getPrivateKey(), message.value);
            Timber.i("signMessage=" + signed);
            webView.onSignMessageSuccessful(message, signed);
        });
        webView.setOnSignPersonalMessageListener(message -> {
            Timber.d("onSignPersonalMessage:" + message.value + "\n");
            String signed = "0x" + NativeDataHelper.ethereumPersonalSign(getPrivateKey(), message.value);
            Timber.i("onSignPersonalMessage=" + signed);
            webView.onSignPersonalMessageSuccessful(message, signed);
        });
        webView.setOnSignTransactionListener(transaction -> {
            getActivity().runOnUiThread(() -> {
                Timber.i("payload " + transaction.payload);
                Timber.i("start signing tx");

                String priceAmount = "0";

                try {
                    priceAmount = transaction.value.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final BigInteger feePrice = new BigInteger(String.valueOf(transaction.gasLimit)).multiply(transaction.gasPrice);
                final String ethPrice = CryptoFormatUtils.weiToEthLabel(feePrice.toString());
                final String finalPriceAmount = priceAmount;

                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setMessage("You are going to spend " + CryptoFormatUtils.weiToEthLabel(finalPriceAmount) +
                        " (and " + ethPrice + " in fees) from wallet " + viewModel.getWalletLive().getValue().getWalletName());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", (dialog, which) -> {
                    try {
                        Wallet wallet = viewModel.getWalletLive().getValue();
                        byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
                        final byte[] tx = NativeDataHelper.makeTransactionEthPayload(seed, wallet.getIndex(), wallet.getActiveAddress().getIndex(),
                                wallet.getCurrencyId(), wallet.getNetworkId(), wallet.getBalance(), finalPriceAmount,
                                transaction.recipient.toString(), String.valueOf(transaction.gasLimit), transaction.gasPrice.toString(), wallet.getEthWallet().getNonce(), transaction.payload.replace("0x", ""));
                        Timber.i("start converting to hex");
                        final String hex = "0x" + SendSummaryFragment.byteArrayToHex(tx);
                        Timber.i("hex converted " + hex);

                        final HdTransactionRequestEntity entity = new HdTransactionRequestEntity(wallet.getCurrencyId(), wallet.getNetworkId(),
                                new HdTransactionRequestEntity.Payload("", wallet.getActiveAddress().getIndex(),
                                        wallet.getIndex(), hex, false));

                        Timber.i("hex=%s", hex);

                        webView.onSignTransactionSuccessful(transaction, hex);

                        MultyApi.INSTANCE.sendHdTransaction(entity).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    Timber.i("BUYING SUCCESS");
                                } else {
                                    Timber.i("BUYING FAIL");
                                    webView.onSignError(transaction, response.message());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                t.printStackTrace();
                                Timber.i("BUYING FAIL");
                                webView.onSignError(transaction, t.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError(e.getLocalizedMessage());
                        webView.onSignError(transaction, e.getMessage());
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        webView.onSignError(transaction, "Canceled");
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            });

            Timber.d("onSignTransactionMessage:" + transaction.value + "\n recepient:" + transaction.recipient + "\n payLoad:" + transaction.payload);
        });
    }

    private void setWallet(long walletId) {
        if (walletId == -1) {
            restartActivity();
        }
        Wallet wallet = viewModel.getWallet(walletId);
        fillWalletInfo(wallet);
        webView.setWalletAddress(new Address(wallet.getActiveAddress().getAddress()));
        webView.setRpcUrl(wallet.getNetworkId() == NativeDataHelper.NetworkId.TEST_NET.getValue() ?
                "https://rinkeby.infura.io/v3/78ae782ed28e48c0b3f74ca69c4f7ca8" : "https://mainnet.infura.io/v3/78ae782ed28e48c0b3f74ca69c4f7ca8");
        webView.setChainId(wallet.getNetworkId());
    }

    private void fillWalletInfo(Wallet wallet) {
        textAddress.setText(wallet.getActiveAddress().getAddress());
        imageCoin.setImageResource(wallet.getIconResourceId());
    }

    private void loadUrl() {
        if (getActivity() != null) {
            webView.loadUrl("about:blank");
            webView.loadUrl(dappUrl);
        }
    }

    private void restartActivity() {
        if (getActivity() != null) {
            getActivity().finish();
            startActivity(new Intent(getActivity(), MainActivity.class));
            Toast.makeText(getActivity().getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    private String getPrivateKey() {
        try {
            Wallet wallet = viewModel.getWalletLive().getValue();
            int walletIndex = wallet.getIndex();
            int addressIndex = wallet.getActiveAddress().getIndex();
            byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
            return NativeDataHelper.getMyPrivateKey(seed, walletIndex, addressIndex, wallet.getCurrencyId(), wallet.getNetworkId());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Multy.getContext(), "Error while build private key", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showError(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            getActivity().finish();
        }
    }

    @OnClick(R.id.text_address)
    public void onClickAddress() {
        final int currencyId = getActivity().getIntent().getIntExtra(Constants.EXTRA_CURRENCY_ID, -1);
        final int networkId = getActivity().getIntent().getIntExtra(Constants.EXTRA_NETWORK_ID, -1);
        showChooser(currencyId, networkId);
    }
}
