package io.multy.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.activities.AssetActivity;

/**
 * Created by appscrunch on 16.11.17.
 */

public class WalletsAdapter extends RecyclerView.Adapter<WalletsAdapter.Holder> {

    public WalletsAdapter() {
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_asset_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            context.startActivity(new Intent(context, AssetActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView name;
        @BindView(R.id.text_amount)
        TextView amount;
        @BindView(R.id.text_equals)
        TextView equals;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
