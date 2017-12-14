package io.multy.api;


import android.content.Context;

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

public interface MultyApiInterface {

    Call<AuthResponse> auth(String userId, String deviceId, String password);

    void getTickets(String firstCurrency, String secondCurrency);

    void getAssetsInfo();

    Call<AddressBalanceResponse>  getBalanceByAddress(int currencyId, String address);

    Call<ResponseBody> addWallet(Context context, WalletRealmObject wallet);

    Observable<ExchangePriceResponse> getExchangePrice(String firstCurrency, String secondCurrency);

    void getTransactionInfo(String transactionId);

    void getTransactionSpeed();

    Call<OutputsResponse> getSpendableOutputs(int net, String address);

    Observable<UserAssetsResponse> getUserAssets();

    Observable<UserAssetsResponse> getWalletAddresses(int walletId);

    Call<ResponseBody> sendRawTransaction(String transactionHex, int currencyId);

    Observable<Object> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest);

    Call<ResponseBody> getWalletVerbose(int walletIndex);

    Call<WalletsResponse> getWalletsVerbose();
}
