/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.api;


import android.content.Context;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.samwolfand.oneprefs.Prefs;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.multy.Multy;
import io.multy.model.DataManager;
import io.multy.model.entities.AuthEntity;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.TransactionRequestEntity;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.responses.AddressBalanceResponse;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.model.responses.FeeRatesResponse;
import io.multy.model.responses.OutputsResponse;
import io.multy.model.responses.RestoreResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletsResponse;
import io.multy.util.Constants;
import io.reactivex.Observable;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public enum MultyApi implements MultyApiInterface {

    INSTANCE {


        static final String BASE_URL = "http://192.168.0.121:7778/";  // local
//        static final String BASE_URL = "http://88.198.47.112:7780/";  // remote


        private ApiServiceInterface api = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
                            public Request authenticate(Route route, okhttp3.Response response) throws IOException {
                                DataManager dataManager = new DataManager(Multy.getContext());
                                final UserId userIdEntity = dataManager.getUserId();
                                final DeviceId deviceIdEntity = dataManager.getDeviceId();
                                final String userId = userIdEntity == null ? "" : userIdEntity.getUserId();
                                final String deviceId = deviceIdEntity == null ? "" : deviceIdEntity.getDeviceId();
                                Call<AuthResponse> responseCall = api.auth(new AuthEntity(userId, deviceId, "somePushToken", 2));
                                AuthResponse body = responseCall.execute().body();
                                Prefs.putString(Constants.PREF_AUTH, body.getToken());

                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + Prefs.getString(Constants.PREF_AUTH, ""))
                                        .build();
                            }
                        })
                        .build())
                .build().create(ApiServiceInterface.class);

        @Override
        public Call<AuthResponse> auth(String userIdString, String deviceIdString, String password) {
            DataManager dataManager = new DataManager(Multy.getContext());
            final String userId = dataManager.getUserId().getUserId();
            final String deviceId = dataManager.getDeviceId().getDeviceId();
            return api.auth(new AuthEntity(userId, deviceId, "somePushToken", 2));
        }

        @Override
        public void getTickets(String firstCurrency, String secondCurrency) {
            Call<ResponseBody> responseCall = api.getTickets(firstCurrency, secondCurrency);
            responseCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("wise", "onResponse Tickets ");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i("wise", "onFailure Tickets ");
                }
            });
        }

        @Override
        public void getAssetsInfo() {
            Call<ResponseBody> responseCall = api.getAssetsInfo();
        }

        @Override
        public Call<AddressBalanceResponse> getBalanceByAddress(int currencyId, String address) {
            return api.getBalanceByAddress(currencyId, address);
        }

        @Override
        public Call<ResponseBody> addWallet(Context context, WalletRealmObject wallet) {
            return api.addWallet(wallet);

        }

        @Override
        public Observable<ExchangePriceResponse> getExchangePrice(String firstCurrency, String secondCurrency) {
            return api.getExchangePrice(firstCurrency, secondCurrency);
//            responseCall.enqueue(new Callback<ExchangePriceResponse>() {
//                @Override
//                public void onResponse(@NonNull Call<ExchangePriceResponse> call, @NonNull Response<ExchangePriceResponse> response) {
//                    Prefs.putDouble(Constants.PREF_EXCHANGE_PRICE, response.body().getUSD());
//                }
//
//                @Override
//                public void onFailure(Call<ExchangePriceResponse> call, Throwable t) {
//                }
//            });
        }

        @Override
        public void getTransactionInfo(String transactionId) {
            Call<ResponseBody> responseCall = api.getTransactionInfo(transactionId);
        }

        @Override
        public Call<FeeRatesResponse> getFeeRates() {
            return api.getFeeRates();
        }

        @Override
        public Call<OutputsResponse> getSpendableOutputs(int net, String address) {
            return api.getSpendableOutputs(net, address);
        }

        @Override
        public Observable<UserAssetsResponse> getUserAssets() {
            return api.getUserAssets();
//            responseBodyCall.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    Log.i("wise", "response ");
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    t.printStackTrace();
//                }
//            });
        }

        @Override
        public Observable<UserAssetsResponse> getWalletAddresses(int walletId) {
            return api.getWalletAddresses(walletId);
        }

        @Override
        public Call<ResponseBody> sendRawTransaction(String transactionHex, int currencyId) {
            return api.sendRawTransaction(new TransactionRequestEntity(transactionHex, false), 1);
        }

        @Override
        public Observable<Object> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest) {
            return api.addWalletAddress(addWalletAddressRequest);
        }

        @Override
        public Call<ResponseBody> getWalletVerbose(int walletIndex) {
            return api.getWalletVerboseByIndex(walletIndex);
        }

        @Override
        public Call<WalletsResponse> getWalletsVerbose() {
            return api.getWalletsVerbose();
        }

        @Override
        public Observable<RestoreResponse> restore() {
            return api.restore();
        }
    }
}
