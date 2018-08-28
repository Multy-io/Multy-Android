/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.ui.adapters.AccountsAdapter;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class FoundWalletsActivity extends BaseActivity {

    @BindView(R.id.button_continue)
    TextView textViewNext;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private ArrayList<String> accounts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_wallets);
        ButterKnife.bind(this);

        accounts = getIntent().getStringArrayListExtra(Constants.EXTRA_ACCOUNTS);
        textViewNext.setText(accounts != null && accounts.size() > 1 ? R.string.create_wallets : R.string.create_wallet);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AccountsAdapter(accounts));
    }

    @SuppressLint("CheckResult")
    @OnClick(R.id.button_continue)
    public void onCLickCreate() {

        ArrayList<Wallet> wallets = new ArrayList<>(accounts.size());
        final int networkId = NativeDataHelper.NetworkId.TEST_NET.getValue();
        final int currencyId = NativeDataHelper.Blockchain.EOS.getValue();
        final int topEosIndex = Prefs.getInt(Constants.PREF_WALLET_TOP_INDEX_EOS + networkId, 0);

        Wallet wallet;
        for (int i = 0; i < accounts.size(); i++) {
            wallet = new Wallet();
            wallet.setCurrencyId(currencyId);
            wallet.setNetworkId(networkId);
            wallet.setWalletName(accounts.get(i));
            wallet.setCreationAddress(accounts.get(i));
            wallet.setIndex(topEosIndex + i);
            wallets.add(wallet);
        }

        Observable.fromIterable(wallets)
                .flatMap((Function<Wallet, ObservableSource<ResponseBody>>) MultyApi.INSTANCE::addWalletReactive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toList()
                .subscribeWith(new SingleObserver<List<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<ResponseBody> responseBodies) {
                        FoundWalletsActivity.this.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(FoundWalletsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
