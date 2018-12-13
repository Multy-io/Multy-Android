/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.explorer;


import io.multy.model.responses.EthplorerResponse;
import retrofit2.Call;

public interface ExplorerApiInterface {

    Call<EthplorerResponse> fetchTokens(String address);
}
