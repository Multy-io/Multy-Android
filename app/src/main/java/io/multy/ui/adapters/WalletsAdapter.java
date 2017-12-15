package io.multy.ui.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.Multy;
import io.multy.R;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetActivity;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NativeDataHelper;

/**
 * Created by appscrunch on 16.11.17.
 */

public class WalletsAdapter extends RecyclerView.Adapter<WalletsAdapter.Holder> {

    private List<WalletRealmObject> data;

    public WalletsAdapter(ArrayList<WalletRealmObject> data) {
        this.data = data;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_asset_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        NativeDataHelper.Currency chain = NativeDataHelper.Currency.values()[data.get(position).getCurrency()];
        holder.name.setText(data.get(position).getName());
        double balance = data.get(position).getBalance();
        double pending = data.get(position).getPendingBalance();

        if (balance != 0) {
            try {
                double formatBalance = balance / Math.pow(10, 8);
                final double exchangePrice = new DataManager(Multy.getContext()).getExchangePriceDB();
                String fiatBalance = new DecimalFormat("#.##").format(exchangePrice * formatBalance) + "USD";
                holder.equals.setText(fiatBalance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        holder.amount.setText(balance != 0 ? CryptoFormatUtils.satoshiToBtc(balance) : String.valueOf(balance));
        holder.pendingAmount.setText(pending != 0 ? CryptoFormatUtils.satoshiToBtc(pending) : String.valueOf(pending));
        holder.currency.setText(String.valueOf(NativeDataHelper.Currency.values()[data.get(position).getCurrency()]));
        holder.imageChain.setImageResource(chain == NativeDataHelper.Currency.BTC ? R.drawable.ic_btc_huge : R.drawable.ic_eth_medium_icon);

//        String fiatBalance = String.valueOf(Double.valueOf(holder.amount.getText().toString()) * exchangePrice).concat(Constants.SPACE).concat(CurrencyCode.USD.name());

//        holder.equals.setText(fiatBalance);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), AssetActivity.class);
            intent.putExtra(Constants.EXTRA_WALLET_ID, data.get(position).getWalletIndex());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<WalletRealmObject> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setAmount(int position, double amount) {
        data.get(position).setBalance(amount);
        notifyItemChanged(position);
    }

    public List<WalletRealmObject> getData() {
        return data;
    }

    public WalletRealmObject getItem(int position) {
        return data.get(position);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView name;
        @BindView(R.id.text_amount)
        TextView amount;
        @BindView(R.id.text_equals)
        TextView equals;
        @BindView(R.id.text_currency)
        TextView currency;
        @BindView(R.id.image_chain)
        ImageView imageChain;
        @BindView(R.id.text_pending_value)
        TextView pendingAmount;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
