/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.samwolfand.oneprefs.Prefs;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.multy.Multy;
import io.multy.model.entities.AuthEntity;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.TransactionRequestEntity;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.requests.CreateMultisigRequest;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.requests.ImportWalletRequest;
import io.multy.model.requests.UpdateWalletNameRequest;
import io.multy.model.responses.AccountsResponse;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ChainInfoResponse;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.model.responses.TestWalletResponse;
import io.multy.model.responses.TransactionHistoryResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletsResponse;
import io.multy.storage.SettingsDao;
import io.multy.util.Constants;
import io.reactivex.Observable;
import io.realm.Realm;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public enum MultyApi implements MultyApiInterface {

    INSTANCE {
        private ApiServiceInterface api = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.BASE_URL)
                .client(new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .hostnameVerifier((hostname, session) -> true)
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + Prefs.getString(Constants.PREF_AUTH, ""))
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        })
                        .authenticator(new Authenticator() {

                            @Nullable
                            @Override
                            public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
                                Realm realm = Realm.getInstance(Multy.getRealmConfiguration());
                                SettingsDao dao = new SettingsDao(realm);

                                final UserId userIdEntity = dao.getUserId();
                                final String userId = userIdEntity == null ? "" : userIdEntity.getUserId();
                                final String pushToken = FirebaseInstanceId.getInstance().getToken() == null ? "noPushToken" : FirebaseInstanceId.getInstance().getToken();

                                Call<AuthResponse> responseCall = api.auth(new AuthEntity(userId, Constants.DEVICE_NAME, pushToken, 2));
                                AuthResponse body = responseCall.execute().body();

                                if (body != null) {
                                    Prefs.putString(Constants.PREF_AUTH, body.getToken());
                                }
                                realm.close();
                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + Prefs.getString(Constants.PREF_AUTH, ""))
                                        .build();
                            }
                        })
                        .build())
                .build().create(ApiServiceInterface.class);

        @Override
        public Call<AuthResponse> auth(String userId) {
            final String pushToken = FirebaseInstanceId.getInstance().getToken() == null ? "noPushToken" : FirebaseInstanceId.getInstance().getToken();
            return api.auth(new AuthEntity(userId, Constants.DEVICE_NAME, pushToken, 2));
        }

        @Override
        public Call<ResponseBody> addWallet(Context context, Wallet wallet) {
            return api.addWallet(wallet);
        }

        @Override
        public Observable<ResponseBody> addWalletReactive(Wallet wallet) {
            return api.addWalletReactive(wallet);
        }

        @Override
        public Call<ResponseBody> addWallet(Context context, CreateMultisigRequest request) {
            return api.addWallet(request);
        }

        @Override
        public Call<ResponseBody> importWallet(ImportWalletRequest request) {
            return api.importWallet(request);
        }

        @Override
        public Call<ResponseBody> importMultisigWallet(ImportWalletRequest request) {
            return api.importMultisigWallet(request);
        }

        @Override
        public void getTransactionInfo(String transactionId) {
            Call<ResponseBody> responseCall = api.getTransactionInfo(transactionId);
        }

        @Override
        public Observable<UserAssetsResponse> getWalletAddresses(int walletId) {
            return api.getWalletAddresses(walletId);
        }

        @Override
        public Call<ResponseBody> sendRawTransaction(String transactionHex, int currencyId) {
            return api.sendRawTransaction(new TransactionRequestEntity(transactionHex, false), currencyId);
        }

        @Override
        public Call<ResponseBody> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest) {
            return api.addWalletAddress(addWalletAddressRequest);
        }

        @Override
        public Call<SingleWalletResponse> getWalletVerbose(int walletIndex, int currencyId, int networkId, int assetType) {
            return api.getWalletVerboseByIndex(walletIndex, currencyId, networkId, assetType);
        }

        @Override
        public Call<SingleWalletResponse> getMultisigWalletVerbose(String inviteCode, int currencyId, int networkId, int assetType) {
            return api.getMultisigWalletVerboseByInvite(inviteCode, currencyId, networkId, assetType);
        }

        @Override
        public Call<WalletsResponse> getWalletsVerbose() {
            return api.getWalletsVerbose();
        }

        @Override
        public Call<ResponseBody> updateWalletName(UpdateWalletNameRequest updateWalletName) {
            return api.updateWalletName(updateWalletName);
        }

        @Override
        public Call<ResponseBody> removeWallet(int currencyId, int networkId, int walletIndex) {
            return api.removeWallet(currencyId, networkId, walletIndex);
        }

        public Call<TransactionHistoryResponse> getTransactionHistory(int currencyId, int networkId, int walletIndex) {
            return api.getTransactionHistory(currencyId, networkId, walletIndex);
        }

        public Call<TransactionHistoryResponse> getMultisigTransactionHistory(int currencyId, int networkId, String address, int assetType) {
            return api.getMultisigTransactionHistory(currencyId, networkId, address, assetType);
        }

        @Override
        public Call<ServerConfigResponse> getServerConfig() {
            return api.getServerConfig();

        }

        /**
         * Request for default fee rates
         */
        @Override
        public Call<FeeRateResponse> getFeeRates(int currencyId, int networkId) {
            return api.getFeeRates(currencyId, networkId);
        }

        /**
         * Request for custom fee rates (current networks: Ethereum)
         */
        @Override
        public Call<FeeRateResponse> getFeeRates(int currencyId, int networkId, String address) {
            return api.getFeeRates(currencyId, networkId, address);
        }

        @Override
        public Call<Estimation> getEstimations(String multisigWalletAddress) {
            return api.getEstimations(multisigWalletAddress);
        }

        @Override
        public Call<ResponseBody> sendHdTransaction(HdTransactionRequestEntity transactionRequestEntity) {
            return api.sendHdTransaction(transactionRequestEntity);
        }

        @Override
        public Call<TestWalletResponse> testWalletVerbose() {
            return api.testWalletVerbose();
        }

        @Override
        public Call<AccountsResponse> getAccounts(int currencyId, int networkId, String publicKey) {
            return api.getAccounts(currencyId, networkId, publicKey);
        }

        @Override
        public Call<ChainInfoResponse> getChainInfo(int currencyId, int networkId) {
            return api.getChainInfo(currencyId, networkId);
        }

        @Override
        public Call<ResponseBody> resyncWallet(int currencyId, int networkId, int walletIndex, int assetType) {
            return api.resyncWallet(currencyId, networkId, walletIndex, assetType);
        }
    }
}
