/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.responses.EthplorerResponse;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;
import io.multy.util.RoundedImageTransformation;

public class PlorerTokensAdapter extends RecyclerView.Adapter<PlorerTokensAdapter.ViewHolder> {

    private List<EthplorerResponse.PlorerToken> tokens = new ArrayList<>();
    private String ethBalance;
    private String ethFiatBalance;

    public PlorerTokensAdapter() {
    }

    public void setData(List<EthplorerResponse.PlorerToken> tokens) {
        this.tokens = tokens;
        notifyDataSetChanged();
    }

    public void setEthereum(String ethBalance, String ethFiatBalance) {
        this.tokens.add(0, null);
        this.ethBalance = ethBalance;
        this.ethFiatBalance = ethFiatBalance;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_token, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final EthplorerResponse.PlorerToken token = tokens.get(position);

        if (token != null) {
            final EthplorerResponse.PlorerTokenInfo tokenInfo = token.getTokenInfo();
            Picasso.get()
                    .load("https://raw.githubusercontent.com/TrustWallet/tokens/master/images/" + token.getTokenInfo().getContractAddress() + ".png")
                    .error(R.drawable.chain_eth)
                    .transform(new RoundedImageTransformation())
                    .into(holder.image);

            holder.textBalance.setText(String.format("%s%s", getTokenBalance(token.getBalance(), tokenInfo.getDecimals()), " " + tokenInfo.getSymbol()));
            holder.textPrice.setText(token.getTokenInfo().getPrice() == null ? "" : getTokenPrice(token.getBalance(), tokenInfo.getPrice().getRate(), tokenInfo.getDecimals()));
            holder.textPrice.setVisibility(token.getTokenInfo().getPrice() == null ? View.GONE : View.VISIBLE);
            holder.textName.setText(token.getTokenInfo().getName());
        } else {
            holder.textBalance.setText(ethBalance);
            holder.textPrice.setText(ethFiatBalance);
            holder.textName.setText(NativeDataHelper.Blockchain.ETH.getName());
            Picasso.get()
                    .load(R.drawable.chain_eth)
                    .error(R.drawable.chain_eth)
                    .transform(new RoundedImageTransformation())
                    .into(holder.image);
        }
    }

    private String getTokenPrice(BigDecimal balance, String price, int decimals) {
        final BigDecimal divisor = new BigDecimal(Math.pow(10, decimals));
        balance = balance.divide(divisor);
        balance = balance.multiply(new BigDecimal(price));
        return NumberFormatter.getFiatInstance().format(balance) + "$";
    }

    private String getTokenBalance(BigDecimal balance, int decimals) {
        final BigDecimal divisor = new BigDecimal(Math.pow(10, decimals));
        balance = balance.divide(divisor);
        return NumberFormatter.getFiatInstance().format(balance);
    }

//    private String getTokenPrice(String balance, String price) {
//        return NumberFormatter.getFiatInstance().format(new BigDecimal(balance).multiply(new BigDecimal(price))) + "$";
//    }

    @Override
    public int getItemCount() {
        return tokens.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.text_name)
        TextView textName;
        @BindView(R.id.text_balance)
        TextView textBalance;
        @BindView(R.id.text_price)
        TextView textPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
