/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.api;


import android.support.annotation.NonNull;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.samwolfand.oneprefs.Prefs;

import io.multy.model.entities.AuthEntity;
import io.multy.model.responses.AuthResponse;
import io.multy.util.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public enum MultyApi implements MultyApiInterface {

    INSTANCE {

        static final String BASE_URL = "http://192.168.0.121:8080/";

        private ApiServiceInterface api = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .addInterceptor(chain -> {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    //TODO check auth date and remove Bearer hardcode
                                    .header("Authorization", "Bearer " + Prefs.getString(Constants.PREF_AUTH, ""))
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        })
                        .build())
                .build().create(ApiServiceInterface.class);

        @Override
        public void auth(String userId, String deviceId, String password) {
            Call<AuthResponse> responseCall = api.auth(new AuthEntity(userId, deviceId, password));
            responseCall.enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    Prefs.putString(Constants.PREF_AUTH, response.body().getToken());
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                }
            });
        }

        @Override
        public void getTickets(String firstCurrency, String secondCurrency) {
            Call<ResponseBody> responseCall = api.getTickets(firstCurrency, secondCurrency);
        }

        @Override
        public void getAssetsInfo() {
            Call<ResponseBody> responseCall = api.getAssetsInfo();
        }

        @Override
        public void getBalance(String address) {
            Call<ResponseBody> responseCall = api.getBalance(address);
        }

        @Override
        public void addWallet(String wallet) {
            Call<ResponseBody> responseCall = api.addWallet(wallet);
        }

        @Override
        public void getExchangePrice(String firstCurrency, String secondCurrency) {
            Call<ResponseBody> responseCall = api.getExchangePrice(firstCurrency, secondCurrency);
        }

        @Override
        public void getTransactionInfo(String transactionId) {
            Call<ResponseBody> responseCall = api.getTransactionInfo(transactionId);
        }
    }
}
