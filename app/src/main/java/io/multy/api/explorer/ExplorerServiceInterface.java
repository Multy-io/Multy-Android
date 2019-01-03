/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.explorer;


import io.multy.model.responses.EthplorerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ExplorerServiceInterface {

    @GET("getAddressInfo/{address}?apiKey=ykt4368BFCHH77")
    Call<EthplorerResponse> fetchTokens(@Path("address") String address);

}
