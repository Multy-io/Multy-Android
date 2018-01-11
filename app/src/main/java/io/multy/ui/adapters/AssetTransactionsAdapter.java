package io.multy.ui.adapters;

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
import io.multy.R;
import io.multy.model.entities.TransactionHistory;

public class AssetTransactionsAdapter extends RecyclerView.Adapter<AssetTransactionsAdapter.Holder> {

    private List<TransactionHistory> data;

    public AssetTransactionsAdapter(List<TransactionHistory> data) {
        this.data = data;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        DecimalFormat fiatFormat = new DecimalFormat("#.##");
        TransactionHistory transactionHistory = data.get(position);
        ArrayList<TransactionHistory.StockExchangeRate> stockRates = transactionHistory.getStockExchangeRates();
        String amountBtc = transactionHistory.getTxOutAmount();
        double amountFiat = stockRates != null && stockRates.size() > 0 ? stockRates.get(0).getBtcUsd() : 16.000;
        String amountFiatString = fiatFormat.format(amountFiat) + " USD";
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_date)
        TextView date;

        @BindView(R.id.image_operation)
        ImageView operationImage;

        @BindView(R.id.text_name)
        TextView name;

        @BindView(R.id.text_description)
        TextView description;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_time)
        TextView time;

        @BindView(R.id.text_comment)
        TextView comment;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
