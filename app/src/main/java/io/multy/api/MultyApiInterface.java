package io.multy.api;

public interface MultyApiInterface {

    void auth(String userId, String deviceId, String password);

    void getTickets(String firstCurrency, String secondCurrency);

    void getAssetsInfo();

    void getBalance(String address);

    void addWallet(String wallet);

    void getExchangePrice(String firstCurrency, String secondCurrency);

    void getTransactionInfo(String transactionId);

}
