/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.api;


import io.multy.model.entities.AuthEntity;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServiceInterface {

    @POST("auth")
    Call<AuthResponse> auth(@Body AuthEntity authEntity);

    @GET("api/v1/gettickets/{firstCurrency}_{secondCurrency}")
    Call<ResponseBody> getTickets(@Path("firstCurrency") String firstCurrency,
                                  @Path("secondCurrency") String secondCurrency);

    @GET("api/v1/getassetsinfo")
    Call<ResponseBody> getAssetsInfo();

    @GET("api/v1/getaddressbalance/{address}")
    Call<ResponseBody> getBalance(@Path("address") String address);

    @POST("api/v1/addwallet")
    Call<ResponseBody> addWallet(@Body WalletRealmObject wallet);

    @GET("api/v1/getexchangeprice/{firstCurrency}/{secondCurrency}")
    Observable<ExchangePriceResponse> getExchangePrice(@Path("firstCurrency") String firstCurrency,
                                                       @Path("secondCurrency") String secondCurrency);

    @POST("api/v1/gettransactioninfo/{id}")
    Call<ResponseBody> getTransactionInfo(@Path("id") String transactionId);

    @GET("api/v1/gettxspeed")
    Call<ResponseBody> getTransactionSpeed();

    @GET("api/v1/getspendableoutputs/{walletIndex}")
    Call<ResponseBody> getSpendableOutputs(@Path("address") int walletIndex);

    @GET("api/v1/getalluserassets")
    Observable<UserAssetsResponse> getUserAssets();

    @GET("api/v1/getwalletaddresses/{walletId}")
    Observable<UserAssetsResponse> getWalletAddresses(@Path("walletId") int walletId);

}
