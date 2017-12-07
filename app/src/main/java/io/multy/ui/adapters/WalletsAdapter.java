package io.multy.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetActivity;
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
        holder.name.setText(data.get(position).getName());
        holder.amount.setText(String.valueOf(data.get(position).getBalance()));
        holder.currency.setText(String.valueOf(NativeDataHelper.Currency.values()[data.get(position).getChain()]));
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            context.startActivity(new Intent(context, AssetActivity.class));
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

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView name;
        @BindView(R.id.text_amount)
        TextView amount;
        @BindView(R.id.text_equals)
        TextView equals;
        @BindView(R.id.text_currency)
        TextView currency;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
