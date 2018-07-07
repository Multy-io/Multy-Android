/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 06.07.18.
 */
public class AddressChainAdapter extends ArrayAdapter {

    private TypedArray chainImageIds;
    private String[] chainAbbrevs;
    private String[] chainNames;
    private int[] chainIds;
    private int[] chainNets;

    public AddressChainAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_chain, parent, false);
        }
        ImageView imageChain = convertView.findViewById(R.id.image_chain);
        TextView textShort = convertView.findViewById(R.id.text_chain_short);
        TextView textName = convertView.findViewById(R.id.text_chain_name);

        imageChain.setImageDrawable(chainImageIds.getDrawable(position));
        textShort.setText(chainAbbrevs[position]);
        textName.setText(chainNames[position]);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_chain, parent, false);
        }
        ImageView imageChain = convertView.findViewById(R.id.image_chain);
        TextView textShort = convertView.findViewById(R.id.text_chain_short);
        TextView textName = convertView.findViewById(R.id.text_chain_name);

        imageChain.setImageDrawable(chainImageIds.getDrawable(position));
        textShort.setText(chainAbbrevs[position]);
        textName.setText(chainNames[position]);
        return convertView;
    }

    @Override
    public int getCount() {
        return chainNames == null ? 0 : chainNames.length;
    }

    public void setData(TypedArray chainImageIds, String[] chainAbbrevs, String[] chainNames, int[] chainIds, int[] chainNets) {
        this.chainImageIds = chainImageIds;
        this.chainAbbrevs = chainAbbrevs;
        this.chainNames = chainNames;
        this.chainIds = chainIds;
        this.chainNets = chainNets;
    }

    public int getChainId(int position) {
        return chainIds[position];
    }

    public int getChainNet(int position) {
        return chainNets[position];
    }

    public int getImgId(int position) {
        return chainImageIds.getResourceId(position, 0);
    }
}
