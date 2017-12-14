/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.api;


import io.multy.model.entities.AuthEntity;
import io.multy.model.entities.TransactionRequestEntity;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.responses.AddressBalanceResponse;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.model.responses.OutputsResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletsResponse;
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

    @POST("api/v1/wallet")
    Call<ResponseBody> addWallet(@Body WalletRealmObject wallet);

    @GET("api/v1/getexchangeprice/{firstCurrency}/{secondCurrency}")
    Observable<ExchangePriceResponse> getExchangePrice(@Path("firstCurrency") String firstCurrency,
                                                       @Path("secondCurrency") String secondCurrency);

    @POST("api/v1/gettransactioninfo/{id}")
    Call<ResponseBody> getTransactionInfo(@Path("id") String transactionId);

    @GET("api/v1/gettxspeed")
    Call<ResponseBody> getTransactionSpeed();

    @GET("api/v1/outputs/spendable/{net}/{address}")
    Call<OutputsResponse> getSpendableOutputs(@Path("net") int net, @Path("address") String address);
  
    @GET("api/v1/outputs/spendable/{walletIndex}")
    Call<ResponseBody> getSpendableOutputs(@Path("walletIndex") int walletIndex);

    @GET("api/v1/getalluserassets")
    Observable<UserAssetsResponse> getUserAssets();

    @GET("api/v1/getwalletaddresses/{walletId}")
    Observable<UserAssetsResponse> getWalletAddresses(@Path("walletId") int walletId);

    @POST("/api/v1/transaction/requestRates/{currencyId}")
    Call<ResponseBody> sendRawTransaction(@Body TransactionRequestEntity transactionRequestEntity, @Path("currencyId") int currencyId);

    @GET("/api/v1/address/balance/{currencyId}/{address}")
    Call<AddressBalanceResponse> getBalanceByAddress(@Path("currencyId") int currencyId, @Path("address") String address);

    @POST("api/v1/address")
    Observable<Object> addWalletAddress(@Body AddWalletAddressRequest addWalletAddressRequest);

    @GET("api/v1/wallets/{walletIndex}/verbose")
    Call<ResponseBody> getWalletVerboseByIndex(@Path ("walletIndex") int walletIndex);

    @GET("api/v1/wallets/verbose")
    Call<WalletsResponse> getWalletsVerbose();
}
