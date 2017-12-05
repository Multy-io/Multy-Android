package io.multy.api;


import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ExchangePriceResponse;
import io.reactivex.Observable;
import retrofit2.Call;

public interface MultyApiInterface {

    Call<AuthResponse> auth(String userId, String deviceId, String password);

    void getTickets(String firstCurrency, String secondCurrency);

    void getAssetsInfo();

    void getBalance(String address);

    void addWallet(String wallet);

    Observable<ExchangePriceResponse> getExchangePrice(String firstCurrency, String secondCurrency);

    void getTransactionInfo(String transactionId);

}
