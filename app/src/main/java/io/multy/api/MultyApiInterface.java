package io.multy.api;


import android.content.Context;

import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.requests.AddWalletAddressRequest;
import io.multy.model.requests.UpdateWalletNameRequest;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.OutputsResponse;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.model.responses.TransactionHistoryResponse;
import io.multy.model.responses.UserAssetsResponse;
import io.multy.model.responses.WalletsResponse;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;

public interface MultyApiInterface {

    Call<AuthResponse> auth(String userId, String deviceId, String password);

    Call<ResponseBody> addWallet(Context context, WalletRealmObject wallet);

    void getTransactionInfo(String transactionId);

    Call<OutputsResponse> getSpendableOutputs(int net, String address);

    Observable<UserAssetsResponse> getWalletAddresses(int walletId);

    Call<ResponseBody> sendRawTransaction(String transactionHex, int currencyId);

    Observable<Object> addWalletAddress(AddWalletAddressRequest addWalletAddressRequest);

    Call<WalletsResponse> getWalletVerbose(int walletIndex);

    Call<WalletsResponse> getWalletsVerbose();

    Call<WalletsResponse> restore();

    Call<ResponseBody> updateWalletName(int id, UpdateWalletNameRequest updateWalletName);

    Call<ResponseBody> removeWallet(int walletIndex);

    Call<TransactionHistoryResponse> getTransactionHistory(int walletIndex);

    Call<ServerConfigResponse> getServerConfig();

}
