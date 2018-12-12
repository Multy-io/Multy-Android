/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import io.multy.model.entities.Erc20TokenPrice;

public class TokenPriceTypeAdapter extends TypeAdapter<Erc20TokenPrice> {

    private Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, Erc20TokenPrice value) {
        gson.toJson(value, Erc20TokenPrice.class, out);
    }

    @Override
    public Erc20TokenPrice read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.BOOLEAN) {
            jsonReader.nextBoolean();
            return null;
        }

        return gson.fromJson(jsonReader, Erc20TokenPrice.class);
    }
}
