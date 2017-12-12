package io.multy.api;


import android.content.Context;

import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;

public interface MultyApiInterface {

    Call<AuthResponse> auth(String userId, String deviceId, String password);

    void getTickets(String firstCurrency, String secondCurrency);

    void getAssetsInfo();

    void getBalance(String address);

    Call<ResponseBody> addWallet(Context context, WalletRealmObject wallet);

    Observable<ExchangePriceResponse> getExchangePrice(String firstCurrency, String secondCurrency);

    void getTransactionInfo(String transactionId);

    void getTransactionSpeed();

    Call<ResponseBody> getSpendableOutputs(int walletIndex);

    Observable<UserAssetsResponse> getUserAssets();

    Observable<UserAssetsResponse> getWalletAddresses(int walletId);

    Call<ResponseBody> sendRawTransaction(String transactionHex);

    Observable<Object> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest);
}
