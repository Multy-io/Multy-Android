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
import io.multy.model.requests.UpdateWalletNameRequest;
import io.multy.model.responses.AddressBalanceResponse;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.FeeRatesResponse;
import io.multy.model.responses.OutputsResponse;
import io.multy.model.responses.RestoreResponse;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.model.responses.TransactionHistoryResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletsResponse;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServiceInterface {

    @POST("auth")
    Call<AuthResponse> auth(@Body AuthEntity authEntity);

    @POST("api/v1/wallet")
    Call<ResponseBody> addWallet(@Body WalletRealmObject wallet);

    @POST("api/v1/gettransactioninfo/{id}")
    Call<ResponseBody> getTransactionInfo(@Path("id") String transactionId);

    @GET("api/v1/transaction/feerate")
    Call<FeeRatesResponse> getFeeRates();

    @GET("api/v1/outputs/spendable/{net}/{address}")
    Call<OutputsResponse> getSpendableOutputs(@Path("net") int net, @Path("address") String address);
  
    @GET("api/v1/getwalletaddresses/{walletId}")
    Observable<UserAssetsResponse> getWalletAddresses(@Path("walletId") int walletId);

    @POST("/api/v1/transaction/send/{currencyId}")
    Call<ResponseBody> sendRawTransaction(@Body TransactionRequestEntity transactionRequestEntity, @Path("currencyId") int currencyId);

    @GET("/api/v1/address/balance/{currencyId}/{address}")
    Call<AddressBalanceResponse> getBalanceByAddress(@Path("currencyId") int currencyId, @Path("address") String address);

    @POST("api/v1/address")
    Observable<Object> addWalletAddress(@Body AddWalletAddressRequest addWalletAddressRequest);

    @GET("api/v1/wallet/{walletIndex}/verbose/{currencyId}")
    Call<WalletsResponse> getWalletVerboseByIndex(@Path ("currencyId") int currencyId, @Path ("walletIndex") int walletIndex);

    @POST("api/v1/wallet/name/{currencyId}/{id}")
    Call<ResponseBody> updateWalletName(@Path("currencyId") int currencyId, @Path("id") int id, @Body UpdateWalletNameRequest updateWalletName);

    @DELETE("api/v1/wallet/{walletIndex}")
    Call<ResponseBody> removeWallet(@Path ("walletIndex") int walletIndex);

    @GET("api/v1/wallets/verbose")
    Call<WalletsResponse> getWalletsVerbose();

    @GET("api/v1/wallets/restore")
    Observable<RestoreResponse> restore();

    @GET("api/v1/wallets/transactions/{currencyid}/{walletIndex}")
    Call<TransactionHistoryResponse> getTransactionHistory(@Path ("currencyid") int currencyId, @Path ("walletIndex") int walletIndex);

    @GET("/server/config")
    Call<ServerConfigResponse> getServerConfig();
}
