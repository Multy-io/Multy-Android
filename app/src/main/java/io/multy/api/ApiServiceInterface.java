/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api;


import io.multy.model.entities.AuthEntity;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.TransactionRequestEntity;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.requests.CreateMultisigRequest;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.requests.ImportWalletRequest;
import io.multy.model.requests.UpdateWalletNameRequest;
import io.multy.model.responses.AccountsResponse;
import io.multy.model.responses.AddressBalanceResponse;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ChainInfoResponse;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.model.responses.TestWalletResponse;
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
    Call<ResponseBody> addWallet(@Body Wallet wallet);

    @POST("api/v1/wallet")
    Observable<ResponseBody> addWalletReactive(@Body Wallet wallet);

    @POST("api/v1/wallet")
    Call<ResponseBody> addWallet(@Body CreateMultisigRequest request);

    @POST("api/v1/wallet")
    Call<ResponseBody> importWallet(@Body ImportWalletRequest wallet);

    @POST("api/v1/wallet")
    Call<ResponseBody> importMultisigWallet(@Body ImportWalletRequest request);

    @POST("api/v1/gettransactioninfo/{id}")
    Call<ResponseBody> getTransactionInfo(@Path("id") String transactionId);

    /**
     * Request for default fee rates
     */
    @GET("api/v1/transaction/feerate/{currencyId}/{networkId}")
    Call<FeeRateResponse> getFeeRates(@Path("currencyId") int currencyId, @Path("networkId") int networkId);

    /**
     * Request for custom fee rates (current networks: Ethereum)
     */
    @GET("api/v1/transaction/feerate/{currencyId}/{networkId}/{address}")
    Call<FeeRateResponse> getFeeRates(@Path("currencyId") int currencyId, @Path("networkId") int networkId, @Path("address") String address);

    @GET("/api/v1/multisig/estimate/{address}")
    Call<Estimation> getEstimations(@Path("address") String multisigWalletAddress);

    @GET("api/v1/getwalletaddresses/{walletId}")
    Observable<UserAssetsResponse> getWalletAddresses(@Path("walletId") int walletId);

    @POST("/api/v1/transaction/send/{currencyId}")
    Call<ResponseBody> sendRawTransaction(@Body TransactionRequestEntity transactionRequestEntity, @Path("currencyId") int currencyId);

    @POST("/api/v1/transaction/send")
    Call<ResponseBody> sendHdTransaction(@Body HdTransactionRequestEntity transactionRequestEntity);

    @GET("/api/v1/address/balance/{currencyId}/{address}")
    Call<AddressBalanceResponse> getBalanceByAddress(@Path("currencyId") int currencyId, @Path("address") String address);

    @POST("api/v1/address")
    Call<ResponseBody> addWalletAddress(@Body AddWalletAddressRequest addWalletAddressRequest);

    @GET("api/v1/wallet/{walletIndex}/verbose/{currencyId}/{networkId}/{assetType}")
    Call<SingleWalletResponse> getWalletVerboseByIndex(@Path("walletIndex") int walletIndex, @Path("currencyId") int currencyId, @Path("networkId") int networkId, @Path("assetType") int assetType);

    @GET("api/v1/wallet/{inviteCode}/verbose/{currencyId}/{networkId}/{assetType}")
    Call<SingleWalletResponse> getMultisigWalletVerboseByInvite(@Path(value = "inviteCode", encoded = true) String inviteCode, @Path("currencyId") int currencyId, @Path("networkId") int networkId, @Path("assetType") int assetType);

    @POST("api/v1/wallet/name")
    Call<ResponseBody> updateWalletName(@Body UpdateWalletNameRequest updateWalletName);

    @DELETE("api/v1/wallet/{currencyId}/{networkId}/{walletIndex}")
    Call<ResponseBody> removeWallet(@Path("currencyId") int currencyId, @Path("networkId") int networkId, @Path("walletIndex") int walletIndex);

    @GET("api/v1/wallets/verbose")
    Call<WalletsResponse> getWalletsVerbose();

    @GET("api/v1/wallets/transactions/{currencyid}/{networkid}/{walletIndex}/0")
    Call<TransactionHistoryResponse> getTransactionHistory(@Path("currencyid") int currencyId, @Path("networkid") int networkId, @Path("walletIndex") int walletIndex);

    @GET("api/v1/wallets/transactions/{currencyId}/{networkId}/{address}/{assetType}")
    Call<TransactionHistoryResponse> getMultisigTransactionHistory(@Path("currencyId") int currencyId, @Path("networkId") int networkId, @Path("address") String address, @Path("assetType") int assetType);

    @GET("/server/config")
    Call<ServerConfigResponse> getServerConfig();

    @GET("api/v1/wallets/verbose")
    Call<TestWalletResponse> testWalletVerbose();

    @GET("api/v1/account/{currencyid}/{networkid}/key/{public_key}")
    Call<AccountsResponse> getAccounts(@Path("currencyid") int currencyId, @Path("networkid") int networkId, @Path("public_key") String publicKey);

    @GET("/api/v1/chain/{currencyid}/{networkid}/info")
    Call<ChainInfoResponse> getChainInfo(@Path("currencyid") int currencyId, @Path("networkid") int networkId);

    @POST("/api/v1/resync/wallet/{currencyId}/{networkId}/{walletIndex}/{assetType}")
    Call<ResponseBody> resyncWallet(@Path("currencyId") int currencyId, @Path("networkId") int networkId, @Path("walletIndex") int walletIndex, @Path("assetType") int assetType);
}
