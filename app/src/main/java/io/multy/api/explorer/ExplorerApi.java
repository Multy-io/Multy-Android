/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.explorer;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import io.multy.model.entities.Erc20TokenPrice;
import io.multy.model.responses.EthplorerResponse;
import io.multy.util.TokenPriceTypeAdapter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public enum ExplorerApi implements ExplorerApiInterface {

    INSTANCE {
        private ExplorerServiceInterface api = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .baseUrl("http://api.ethplorer.io/")
                .client(new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build())
                .build().create(ExplorerServiceInterface.class);

        @Override
        public Call<EthplorerResponse> fetchTokens(String address) {
            return api.fetchTokens(address);
        }

        private Gson getGson() {
            return new GsonBuilder().registerTypeAdapter(Erc20TokenPrice.class, new TokenPriceTypeAdapter()).create();
        }
    }
}
