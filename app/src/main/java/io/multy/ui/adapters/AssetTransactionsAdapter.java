/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.DonateFeatureEntity;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.asset.TransactionInfoFragment;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.RealmList;
import io.realm.RealmResults;

import static io.multy.ui.fragments.asset.TransactionInfoFragment.MODE_RECEIVE;
import static io.multy.ui.fragments.asset.TransactionInfoFragment.MODE_SEND;
import static io.multy.util.Constants.TX_CONFIRMED_INCOMING;
import static io.multy.util.Constants.TX_IN_BLOCK_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_OUTCOMING;

public class AssetTransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BLOCKED = 102;
    private static final int TYPE_CONFIRMED = 203;
    private static final int TYPE_REJECTED = 204;

    private long walletId;
    private List<TransactionHistory> transactionHistoryList;
    private RealmList<WalletAddress> addresses;
    private RealmResults<DonateFeatureEntity> donations;

    public AssetTransactionsAdapter(List<TransactionHistory> transactionHistoryList, long walletId) {
        this.transactionHistoryList = transactionHistoryList;
        this.walletId = walletId;
        Collections.sort(this.transactionHistoryList, (transactionHistory, t1) -> Long.compare(t1.getMempoolTime(), transactionHistory.getMempoolTime()));
        addresses = RealmManager.getAssetsDao().getWalletById(walletId).getBtcWallet().getAddresses();
        donations = RealmManager.getSettingsDao().getDonationAddresses();
    }

    public AssetTransactionsAdapter() {
        transactionHistoryList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_BLOCKED:
                return new BlockedHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item_blocked, parent, false));
            case TYPE_CONFIRMED:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item, parent, false));
            default:
                return new RejectedHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item_rejected, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_CONFIRMED:
                bindConfirmed((Holder) holder, position);
                break;
            case TYPE_BLOCKED:
                bindBlocked((BlockedHolder) holder, position);
                break;
            case TYPE_REJECTED:
                bindRejected((RejectedHolder) holder, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        TransactionHistory entity = transactionHistoryList.get(position);
        if (entity.getTxStatus() == TX_MEMPOOL_INCOMING || entity.getTxStatus() == TX_MEMPOOL_OUTCOMING) {
            return TYPE_BLOCKED;
        } else if (entity.getTxStatus() < 0) {
            return TYPE_REJECTED;
        } else {
            return TYPE_CONFIRMED;
        }
    }

    private void setItemClickListener(View view, boolean isIncoming, int position) {
        view.setOnClickListener((v) -> {
            Analytics.getInstance(v.getContext()).logWallet(AnalyticsConstants.WALLET_TRANSACTION, 1);
            Bundle transactionInfo = new Bundle();
            int mode = isIncoming ? MODE_RECEIVE : MODE_SEND;
            transactionInfo.putInt(TransactionInfoFragment.SELECTED_POSITION, position);
            transactionInfo.putInt(TransactionInfoFragment.TRANSACTION_INFO_MODE, mode);
            transactionInfo.putLong(TransactionInfoFragment.WALLET_INDEX, walletId);
            ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_full, TransactionInfoFragment.newInstance(transactionInfo))
                    .addToBackStack(TransactionInfoFragment.TAG)
                    .commit();
        });
    }

    private String getFiatAmount(TransactionHistory transactionHistory, double btcValue) {
        if (transactionHistory.getStockExchangeRates() != null && transactionHistory.getStockExchangeRates().size() > 0) {
            double rate = getPreferredExchangeRate(transactionHistory.getStockExchangeRates());
            return CryptoFormatUtils.btcToUsd(btcValue, rate);
        }
        return "";
    }

    private double getPreferredExchangeRate(ArrayList<TransactionHistory.StockExchangeRate> stockExchangeRates) {
        if (stockExchangeRates != null && stockExchangeRates.size() > 0) {
            for (TransactionHistory.StockExchangeRate rate : stockExchangeRates) {
                if (rate.getExchanges().getBtcUsd() > 0) {
                    return rate.getExchanges().getBtcUsd();
                }
            }
        }
        return 0.0;
    }

    private void bindBlocked(BlockedHolder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final boolean isIncoming = transactionHistory.getTxStatus() == TX_MEMPOOL_INCOMING;
        String lockedAmount = "";
        final String lockedFiat;
        final String amount;
        final String amountFiat;
        final String address;

        holder.containerAddresses.removeAllViews();

        if (isIncoming) {
            lockedAmount = CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount());
            lockedFiat = CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount());
            amount = lockedAmount;
            amountFiat = getFiatAmount(transactionHistory, CryptoFormatUtils.satoshiToBtcDouble(transactionHistory.getTxOutAmount()));
            setAddresses(transactionHistory.getInputs(), holder.containerAddresses);

            holder.amountLocked.setText(String.format("%s BTC", lockedAmount));
            holder.fiatLocked.setText(String.format("(%s USD)", lockedFiat));


        } else {
            List<WalletAddress> outputs = transactionHistory.getOutputs();
            WalletAddress userChangeAddress = null;
            WalletAddress addressTo = outputs.get(0);
            List<String> walletAddresses = getWalletAddresses(addresses);

            long outSatoshi = getOutСomingAmount(transactionHistory, walletAddresses);

            for (WalletAddress output : outputs) {
                if (walletAddresses.contains(output.getAddress())) {
                    userChangeAddress = output;
                    break;
                }

                if (!walletAddresses.contains(output.getAddress()) && !isAddressDonation(output.getAddress())) {
                    addressTo = output;
                }
            }

//            if (!lockedAmount.equals("")) {
            holder.containerLocked.setVisibility(View.VISIBLE);
            lockedAmount = CryptoFormatUtils.satoshiToBtc(userChangeAddress.getAmount());
            lockedFiat = CryptoFormatUtils.satoshiToUsd(userChangeAddress.getAmount());
            holder.amountLocked.setText(String.format("%s BTC", lockedAmount));
            holder.fiatLocked.setText(String.format("(%s USD)", lockedFiat));
//            } else {
//                holder.containerLocked.setVisibility(View.GONE);
//            }

            amount = "" + CryptoFormatUtils.satoshiToBtc(outSatoshi);
            amountFiat = getFiatAmount(transactionHistory, CryptoFormatUtils.satoshiToBtcDouble(outSatoshi));
            setAddress(addressTo.getAddress(), holder.containerAddresses);
        }

        holder.amount.setText(String.format("%s BTC", amount));
        holder.fiat.setText(String.format("%s USD", amountFiat));

        setItemClickListener(holder.itemView, isIncoming, position);
    }

    private boolean isAddressDonation(String address) {
        for (DonateFeatureEntity donateFeatureEntity : donations) {
            if (donateFeatureEntity.getDonationAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAddressUser(String address) {
        for (WalletAddress walletAddress : addresses) {
            if (walletAddress.equals(address)) {
                return true;
            }
        }

        return false;
    }

    /**
     * calculates outcoming transaction satoshi amount
     *
     * @param transactionHistory
     * @param walletAddresses
     * @return
     */
    public static long getOutСomingAmount(TransactionHistory transactionHistory, List<String> walletAddresses) {
        long totalAmount = 0;
        long outAmount = 0;

        for (WalletAddress walletAddress : transactionHistory.getInputs()) {
            totalAmount += walletAddress.getAmount();
        }

        for (WalletAddress walletAddress : transactionHistory.getOutputs()) {
            if (walletAddresses.contains(walletAddress.getAddress())) {
                outAmount += walletAddress.getAmount();
            }
        }

        return totalAmount - outAmount;
    }

    private void bindRejected(RejectedHolder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final int txStatus = Math.abs(transactionHistory.getTxStatus());
        boolean isIncoming = txStatus == TX_IN_BLOCK_INCOMING ||
                txStatus == TX_CONFIRMED_INCOMING ||
                txStatus == TX_MEMPOOL_INCOMING;

        holder.imageDirection.setImageResource(isIncoming ? R.drawable.ic_receive_gray : R.drawable.ic_send_gray);
        holder.textRejectedDirection.setText(isIncoming ? R.string.rejected_receive : R.string.rejected_send);
        holder.amount.setText(CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount()));
        holder.fiat.setText(getFiatAmount(transactionHistory, CryptoFormatUtils.satoshiToBtcDouble(transactionHistory.getTxOutAmount())));

        setItemClickListener(holder.itemView, isIncoming, position);
    }

    private void bindConfirmed(Holder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final int txStatus = transactionHistory.getTxStatus();
        boolean isIncoming = txStatus == TX_IN_BLOCK_INCOMING || txStatus == TX_CONFIRMED_INCOMING;

        holder.operationImage.setImageResource(isIncoming ? R.drawable.ic_receive : R.drawable.ic_send);
        holder.date.setText(DateHelper.DATE_FORMAT_HISTORY.format(transactionHistory.getBlockTime() * 1000));
        holder.containerAddresses.removeAllViews();

        if (isIncoming) {
            setAddresses(transactionHistory.getInputs(), holder.containerAddresses);
            holder.amount.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount())));
            String stockFiat = getFiatAmount(transactionHistory, CryptoFormatUtils.satoshiToBtcDouble(transactionHistory.getTxOutAmount()));
            holder.fiat.setText(stockFiat.equals("") ? "" : String.format("%s USD", stockFiat));
        } else {
            List<WalletAddress> outputs = transactionHistory.getOutputs();
            WalletAddress addressTo = outputs.get(0);
            List<String> walletAddresses = getWalletAddresses(addresses);

            long outSatoshi = getOutСomingAmount(transactionHistory, walletAddresses);

            for (WalletAddress output : outputs) {
                if (!walletAddresses.contains(output.getAddress()) && !isAddressDonation(output.getAddress())) {
                    addressTo = output;
                }
            }

            setAddress(addressTo.getAddress(), holder.containerAddresses);
            holder.amount.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(outSatoshi)));
            holder.fiat.setText(String.format("%s USD", getFiatAmount(transactionHistory, CryptoFormatUtils.satoshiToBtcDouble(outSatoshi))));
        }

        setItemClickListener(holder.itemView, isIncoming, position);
    }

    private List<String> getWalletAddresses(RealmList<WalletAddress> addresses) {
        List<String> result = new ArrayList<>();
        for (WalletAddress address : addresses) {
            result.add(address.getAddress());
        }
        return result;
    }

    private void setAddresses(List<WalletAddress> addresses, ViewGroup destination) {
        ArrayList<String> uniqueAddresses = new ArrayList<>();
        for (WalletAddress walletAddress : addresses) {
            if (uniqueAddresses.size() > 0 && uniqueAddresses.contains(walletAddress.getAddress())) {
                continue;
            }
            setAddress(walletAddress.getAddress(), destination);
            uniqueAddresses.add(walletAddress.getAddress());
        }
    }

    private void setAddress(String text, ViewGroup destination) {
        TextView textView = (TextView) LayoutInflater.from(destination.getContext()).inflate(R.layout.item_history_address, destination, false);
        textView.setText(text);
        destination.addView(textView);
    }

    @Override
    public int getItemCount() {
        return transactionHistoryList.size();
    }

    public void setTransactions(List<TransactionHistory> transactions) {
        this.transactionHistoryList = transactions;
        notifyDataSetChanged();
    }

    static class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_date)
        TextView date;

        @BindView(R.id.image_operation)
        ImageView operationImage;

//        @BindView(R.id.text_address)
//        TextView address;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_fiat)
        TextView fiat;

        @BindView(R.id.container_addresses)
        LinearLayout containerAddresses;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class BlockedHolder extends RecyclerView.ViewHolder {

//        @BindView(R.id.text_address)
//        TextView address;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_fiat)
        TextView fiat;

        @BindView(R.id.text_locked_amount)
        TextView amountLocked;

        @BindView(R.id.text_locked_fiat)
        TextView fiatLocked;

        @BindView(R.id.container_addresses)
        LinearLayout containerAddresses;

        @BindView(R.id.container_locked)
        View containerLocked;

        BlockedHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class RejectedHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_rejected_direction)
        ImageView imageDirection;

        @BindView(R.id.text_address)
        TextView address;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_fiat)
        TextView fiat;

        @BindView(R.id.text_rejected)
        TextView textRejectedDirection;

        RejectedHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
