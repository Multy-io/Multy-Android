///*
// * Copyright 2018 Idealnaya rabota LLC
// * Licensed under Multy.io license.
// * See LICENSE for details
// */
//
//package io.multy.ui.adapters;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.squareup.picasso.Picasso;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import io.multy.R;
//import io.multy.model.entities.Erc20Balance;
//import io.multy.util.CryptoFormatUtils;
//import io.multy.util.NumberFormatter;
//
//public class Erc20TokensAdapter extends RecyclerView.Adapter<Erc20TokensAdapter.ViewHolder> {
//
//    private List<Erc20Balance> data;
//
//    public Erc20TokensAdapter(List<Erc20Balance> data) {
//        this.data = data;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_token, parent, false));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        final Erc20Balance erc20Balance = data.get(position);
//
//        if (erc20Balance != null && erc20Balance.getToken() != null) {
//            Picasso.get()
//                    .load("https://raw.githubusercontent.com/TrustWallet/tokens/master/images/" + erc20Balance.getAddress() + ".png")
//                    .into(holder.image);
//            if (erc20Balance.getToken().getTokenInfo() != null) {
//                final int decimals = erc20Balance.getToken().getTokenInfo().getDecimals();
//                final String price = getTokenPrice(erc20Balance.getBalance(), erc20Balance.getToken().getTokenInfo().getPrice().getRate(), decimals);
//                holder.textBalance.setText(String.format("%s %s", toHumanReadable(erc20Balance.getBalance(), decimals), erc20Balance.getToken().getTicker()));
//                holder.textPrice.setText(price);
//            }
//            holder.textName.setText(erc20Balance.getToken().getName());
//        }
//    }
//
//    private String getTokenPrice(String balance, String price, int decimals) {
//        BigDecimal result = new BigDecimal(balance);
//        final BigDecimal divisor = new BigDecimal(Math.pow(10, decimals));
//        result = result.divide(divisor);
//        result = result.multiply(new BigDecimal(price));
//        return NumberFormatter.getFiatInstance().format(result) + "$";
//    }
//
//    private String toHumanReadable(String balance, int decimals) {
//        BigDecimal balanceNumeric = new BigDecimal(balance);
//        final BigDecimal divisor = new BigDecimal(Math.pow(10, decimals));
//        balanceNumeric = balanceNumeric.divide(divisor);
//
//        return NumberFormatter.getFiatInstance().format(balanceNumeric);
//    }
//
//    @Override
//    public int getItemCount() {
//        return data.size();
//    }
//
//    class ViewHolder extends RecyclerView.ViewHolder {
//
//        @BindView(R.id.image)
//        ImageView image;
//        @BindView(R.id.text_name)
//        TextView textName;
//        @BindView(R.id.text_balance)
//        TextView textBalance;
//        @BindView(R.id.text_price)
//        TextView textPrice;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//    }
//}
