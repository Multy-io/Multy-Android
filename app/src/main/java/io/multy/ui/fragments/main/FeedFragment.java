/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.samwolfand.oneprefs.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.MainActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.ui.fragments.dialogs.WebDialogFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.RoundedCornersDrawable;
import io.multy.util.analytics.Analytics;
import io.multy.viewmodels.WalletViewModel;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedFragment extends BaseFragment {

    @BindView(R.id.image)
    ImageView imageViewChallenge;

    @BindView(R.id.button_challenge)
    CardView cardViewChallenge;

    private WalletViewModel viewModel;

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, view);
        Analytics.getInstance(getActivity()).logActivityLaunch();
        setRoundedImage();
        if (requireActivity().getIntent().hasExtra(Constants.EXTRA_URL)) {
            initRedirect();
        }
        return view;
    }

    @Override
    public void onDestroy() {
        if (requireActivity().getIntent().hasExtra(Constants.EXTRA_URL)) {
            requireActivity().getIntent().removeExtra(Constants.EXTRA_URL);
            requireActivity().getIntent().removeExtra(Constants.EXTRA_CURRENCY_ID);
            requireActivity().getIntent().removeExtra(Constants.EXTRA_NETWORK_ID);
        }
        super.onDestroy();
    }

    private void setRoundedImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.activity_bgd);
        RoundedCornersDrawable round = new RoundedCornersDrawable(bitmap, getResources().getDimension(R.dimen.card_challenge_radius), 0);
        cardViewChallenge.setPreventCornerOverlap(false);
        imageViewChallenge.setBackground(round);
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
                } else if (!setWallet(currencyId, networkId)) {
                    createWalletWithParams(currencyId, networkId);
                } else {
                    loadUrl(); //todo load url and you can get wallet from viewmodel
                }
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
                    if (!setWallet(currencyId, networkId)) {
                        restartActivity();
                    } else {
                        loadUrl();
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean setWallet(int currencyId, int networkId) {
        RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getWallets(currencyId, networkId, false);
        if (wallets.size() == 0) {
            return false;
        } else {
            viewModel.getWallet(wallets.get(0).getId()); //todo put here sample condition if you need some wallet
        }
        return true;
    }

    private void loadUrl() {
        if (getActivity() != null) {
            final String url = getActivity().getIntent().getStringExtra(Constants.EXTRA_URL);
            //todo load url here (check for state at least started)
            WebDialogFragment.newInstance(url).show(getFragmentManager(), "");
        }
    }

    private void restartActivity() {
        if (getActivity() != null) {
            getActivity().finish();
            startActivity(new Intent(getActivity(), MainActivity.class));
            Toast.makeText(getActivity().getApplicationContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_challenge)
    void onClickChallenge() {
        WebDialogFragment.newInstance("http://multy.io/index.php/feature-challenge/").show(getFragmentManager(), "");
    }

    @OnClick(R.id.card_donation)
    void onClickDonate(View view) {
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
        DonateDialog.getInstance(Constants.DONATE_ADDING_ACTIVITY).show(getChildFragmentManager(), DonateDialog.TAG);
    }
}
