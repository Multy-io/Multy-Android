/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments;

import android.app.Activity;
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

import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.storage.RealmManager;
import io.multy.ui.MyWebView;
import io.multy.ui.fragments.dialogs.WalletChooserDialogFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;
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

    private Wallet selectedWallet;
    private long walletId;
    private String dappUrl = "https://dragonereum-alpha-test.firebaseapp.com/";
    private int networkId = -1;
    byte[] seed;

    public static Web3Fragment newInstance() {
        Web3Fragment web3Fragment = new Web3Fragment();
        return web3Fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.fragment_web_dapps, container, false);
        ButterKnife.bind(this, convertView);

        if (getActivity().getIntent().hasExtra(Constants.EXTRA_NETWORK_ID) && getActivity().getIntent().hasExtra(Constants.EXTRA_URL)) {
            dappUrl = getActivity().getIntent().getStringExtra(Constants.EXTRA_URL);
            networkId = getActivity().getIntent().getExtras().getInt(Constants.EXTRA_NETWORK_ID);
        }


        seed = RealmManager.getSettingsDao().getSeed().getSeed();
        showChooser();
        refreshLayout.setOnRefreshListener(() -> webView.postDelayed(() -> {
            webView.reload();
            refreshLayout.setRefreshing(false);
        }, 3000));

        return convertView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (selectedWallet == null) {
                initState();
            }

            getActivity().getIntent().putExtra(Constants.EXTRA_WALLET_ID, data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            Wallet wallet = RealmManager.getAssetsDao().getWalletById(data.getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            setWallet(wallet);

//            final WalletAddress walletAddress = selectedWallet.getActiveAddress();
//            if (walletAddress.getAmount() > 5000) {

//            } else {
//                Toast.makeText(getActivity(), "Wrong wallet. Please choose another one", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void showChooser() {
        if (getFragmentManager() != null) {
            WalletChooserDialogFragment dialog;
            if (networkId == -1) {
                dialog = WalletChooserDialogFragment.getInstance(NativeDataHelper.Blockchain.ETH.getValue());
            } else {
                dialog = WalletChooserDialogFragment.getInstance(NativeDataHelper.Blockchain.ETH.getValue(), networkId);
            }

            dialog.exceptMultisig(true);
            dialog.setMainNet(true);
            dialog.setTargetFragment(this, WalletChooserDialogFragment.REQUEST_WALLET_ID);
            dialog.show(getFragmentManager(), WalletChooserDialogFragment.TAG);
        }
    }

    private void fillWalletInfo() {
        if (selectedWallet != null) {
            textAddress.setText(selectedWallet.getActiveAddress().getAddress());
            imageCoin.setImageResource(selectedWallet.getIconResourceId());
        }
    }

    private void setWallet(Wallet wallet) {
        selectedWallet = wallet;
        walletId = wallet.getId();
        fillWalletInfo();
        webView.setWalletAddress(new Address(wallet.getActiveAddress().getAddress()));
        webView.setChainId(wallet.getNetworkId());
        webView.loadUrl("about:blank");
        webView.loadUrl(dappUrl);
    }

    private void initState() {
        fillWalletInfo();
        webView.setRpcUrl("https://rinkeby.infura.io/v3/78ae782ed28e48c0b3f74ca69c4f7ca8");
        webView.setChainId(4);
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

                if (selectedWallet == null || !selectedWallet.isValid()) {
                    selectedWallet = RealmManager.getAssetsDao().getWalletById(walletId);
                }

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
                        " (and " + ethPrice + " in fees) from wallet " + selectedWallet.getWalletName());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", (dialog, which) -> {
                    try {
                        final byte[] tx = NativeDataHelper.makeTransactionEthPayload(seed, selectedWallet.getIndex(), selectedWallet.getActiveAddress().getIndex(),
                                selectedWallet.getCurrencyId(), selectedWallet.getNetworkId(), selectedWallet.getBalance(), finalPriceAmount,
                                transaction.recipient.toString(), String.valueOf(transaction.gasLimit), transaction.gasPrice.toString(), selectedWallet.getEthWallet().getNonce(), transaction.payload.replace("0x", ""));
                        Timber.i("start converting to hex");
                        final String hex = "0x" + SendSummaryFragment.byteArrayToHex(tx);
                        Timber.i("hex converted " + hex);

                        final HdTransactionRequestEntity entity = new HdTransactionRequestEntity(selectedWallet.getCurrencyId(), selectedWallet.getNetworkId(),
                                new HdTransactionRequestEntity.Payload("", selectedWallet.getActiveAddress().getIndex(),
                                        selectedWallet.getIndex(), hex, false));

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

    private void showError(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    private String getPrivateKey() {
        try {
            int walletIndex = selectedWallet.getIndex();
            int addressIndex = selectedWallet.getActiveAddress().getIndex();
            return NativeDataHelper.getMyPrivateKey(seed, walletIndex, addressIndex, selectedWallet.getCurrencyId(), selectedWallet.getNetworkId());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Multy.getContext(), "Error while build private key", Toast.LENGTH_SHORT).show();
            return null;
        }
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
        showChooser();
    }
}
