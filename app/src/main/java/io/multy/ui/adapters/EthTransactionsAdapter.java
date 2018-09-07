/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.TransactionOwner;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.asset.EthTransactionInfoFragment;
import io.multy.ui.fragments.asset.MultisigTransactionInfoFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;

import static io.multy.ui.fragments.asset.EthTransactionInfoFragment.MODE_RECEIVE;
import static io.multy.ui.fragments.asset.EthTransactionInfoFragment.MODE_SEND;
import static io.multy.util.Constants.MULTISIG_OWNER_STATUS_CONFIRMED;
import static io.multy.util.Constants.MULTISIG_OWNER_STATUS_DECLINED;
import static io.multy.util.Constants.MULTISIG_OWNER_STATUS_SEEN;
import static io.multy.util.Constants.TX_CONFIRMED_INCOMING;
import static io.multy.util.Constants.TX_IN_BLOCK_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_OUTCOMING;
import static io.multy.util.Constants.TX_REJECTED;

public class EthTransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BLOCKED = 102;
    private static final int TYPE_CONFIRMED = 203;
    private static final int TYPE_REJECTED = 204;
    private static final int TYPE_MULTISIG = 303;

    private long walletId;
    private List<TransactionHistory> transactionHistoryList;

    public EthTransactionsAdapter() {
        this.transactionHistoryList = new ArrayList<>();
    }

    public EthTransactionsAdapter(List<TransactionHistory> transactionHistoryList, long walletId) {
        Collections.sort(transactionHistoryList, (transactionHistory, t1) -> Long.compare(t1.getMempoolTime(), transactionHistory.getMempoolTime()));
        this.transactionHistoryList = transactionHistoryList;
        this.walletId = walletId;
        Collections.sort(this.transactionHistoryList, (history1, history2) -> {
            long compareTime1 = history1.getBlockTime() < 1 ? history1.getMempoolTime() : history1.getBlockTime();
            long compareTime2 = history2.getBlockTime() < 1 ? history2.getMempoolTime() : history2.getBlockTime();
            return Long.compare(compareTime2, compareTime1);
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_BLOCKED:
                return new BlockedHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item_blocked, parent, false));
            case TYPE_MULTISIG:
                return new MultisigHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_transaction_item_blocked_ms, parent, false));
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
            case TYPE_MULTISIG:
                bindMultisig((MultisigHolder) holder, position);
                break;
            case TYPE_REJECTED:
                bindRejected((RejectedHolder) holder, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        TransactionHistory entity = transactionHistoryList.get(position);
        if (entity.getMultisigInfo() != null) {
            if (entity.getTxStatus() == TX_MEMPOOL_INCOMING || entity.getTxStatus() == TX_MEMPOOL_OUTCOMING) {
                return TYPE_BLOCKED;
            }
            return TYPE_MULTISIG;
        } else if (entity.getTxStatus() == TX_MEMPOOL_INCOMING || entity.getTxStatus() == TX_MEMPOOL_OUTCOMING) {
            return TYPE_BLOCKED;
        } else if (entity.getTxStatus() == TX_REJECTED) {
            return TYPE_REJECTED;
        } else {
            return TYPE_CONFIRMED;
        }
    }

    private void setItemClickListener(View view, boolean isIncoming, boolean isMultisig, int position) {
        view.setOnClickListener((v) -> {
            Analytics.getInstance(v.getContext()).logWallet(AnalyticsConstants.WALLET_TRANSACTION, 1);
            Fragment fragment;
            if (isMultisig) {
                fragment = MultisigTransactionInfoFragment.newInstance(position);
            } else {
                Bundle transactionInfo = new Bundle();
                int mode = isIncoming ? MODE_RECEIVE : MODE_SEND;
                transactionInfo.putInt(EthTransactionInfoFragment.SELECTED_POSITION, position);
                transactionInfo.putInt(EthTransactionInfoFragment.TRANSACTION_INFO_MODE, mode);
                transactionInfo.putLong(EthTransactionInfoFragment.WALLET_INDEX, walletId);
                fragment = EthTransactionInfoFragment.newInstance(transactionInfo);
            }
            ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_full, fragment)
                    .addToBackStack(EthTransactionInfoFragment.TAG)
                    .commit();
        });
    }

    private String getFiatAmount(TransactionHistory transactionHistory, double btcValue) {
        if (transactionHistory.getStockExchangeRates() != null && transactionHistory.getStockExchangeRates().size() > 0) {
            double rate = getPreferredExchangeRate(transactionHistory.getStockExchangeRates());
            return CryptoFormatUtils.ethToUsd(btcValue, rate);
        }
        return "";
    }

    private double getPreferredExchangeRate(ArrayList<TransactionHistory.StockExchangeRate> stockExchangeRates) {
        if (stockExchangeRates != null && stockExchangeRates.size() > 0) {
            for (TransactionHistory.StockExchangeRate rate : stockExchangeRates) {
                if (rate.getExchanges().getEthUsd() > 0) {
                    return rate.getExchanges().getEthUsd();
                }
            }
        }
        return 0.0;
    }

    private int calculateStatus(ArrayList<TransactionOwner> owners, int status) {
        int counter = 0;
        for (TransactionOwner owner : owners) {
            if (owner.getConfirmationStatus() == status) {
                counter++;
            }
        }
        return counter;
    }

    private boolean isVotedByMe(ArrayList<TransactionOwner> owners) {
        for (TransactionOwner owner : owners) {
            if (RealmManager.getAssetsDao().getWalletAddress(owner.getAddress()).size() > 0) {
                return owner.getConfirmationStatus() == Constants.MULTISIG_OWNER_STATUS_CONFIRMED ||
                        owner.getConfirmationStatus() == Constants.MULTISIG_OWNER_STATUS_DECLINED;
            }
        }
        return false;
    }

    private void bindBlocked(BlockedHolder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final boolean isIncoming = transactionHistory.getTxStatus() == TX_MEMPOOL_INCOMING;
        final String amount = CryptoFormatUtils.FORMAT_ETH.format(CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount())); //TODO improve
        final String amountFiat = getFiatAmount(transactionHistory, CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount()));
        final String address = isIncoming ? transactionHistory.getFrom() : transactionHistory.getTo();

        holder.containerAddresses.removeAllViews();

        setAddress(address, holder.containerAddresses);
        holder.amount.setText(String.format("%s ETH", amount));
        holder.fiat.setText(String.format("%s USD", amountFiat));

        Wallet wallet = RealmManager.getAssetsDao().getWalletById(walletId);

        if (wallet != null && wallet.isValid()) {
            holder.amountLocked.setText(CryptoFormatUtils.weiToEthLabel(wallet.getBalance()));
            holder.fiatLocked.setText(String.format("%s%s",
                    CryptoFormatUtils.weiToUsd(new BigInteger(wallet.getBalance())), wallet.getFiatString()));
        }
        if (transactionHistory.getMultisigInfo() != null) {
            holder.groupMultisig.setVisibility(View.VISIBLE);
            int confirms = calculateStatus(transactionHistory.getMultisigInfo().getOwners(), Constants.MULTISIG_OWNER_STATUS_CONFIRMED);
            holder.textConfirmations.setText(String.format(holder.itemView.getContext().getString(R.string.confirmations_of),
                    confirms, transactionHistory.getMultisigInfo().getOwners().size()));
            holder.textConfirmCount.setText(String.valueOf(confirms));
        } else {
            holder.groupMultisig.setVisibility(View.GONE);
        }
        setItemClickListener(holder.itemView, isIncoming, transactionHistory.getMultisigInfo() != null, position);
    }

    private void bindMultisig(MultisigHolder holder, int position) {
        TransactionHistory entity = transactionHistoryList.get(position);
        final boolean isIncoming = entity.getTxStatus() == TX_MEMPOOL_INCOMING;
        final String amount = CryptoFormatUtils.FORMAT_ETH.format(CryptoFormatUtils.weiToEth(entity.getTxOutAmount()));
        final String amountFiat = getFiatAmount(entity, CryptoFormatUtils.weiToEth(entity.getTxOutAmount()));
        int operationImage = entity.getMultisigInfo().isConfirmed() ?
                isIncoming ? R.drawable.ic_receive : R.drawable.ic_send :
                entity.getMultisigInfo().isInvocationStatus() ?
                        R.drawable.ic_arrow_declined : R.drawable.ic_arrow_waiting;
        holder.imageOperation.setImageResource(operationImage);
        holder.textAddress.setText(isIncoming ? entity.getFrom() : entity.getTo());
        holder.textAmount.setText(String.format("%s ETH", amount));
        holder.textFiat.setText(String.format("%s USD", amountFiat));
        ArrayList<TransactionOwner> owners = entity.getMultisigInfo().getOwners();
        String dateText;
        if (entity.getMultisigInfo().isInvocationStatus()) {
            dateText = DateHelper.DATE_FORMAT_HISTORY.format(entity.getBlockTime() * 1000);
            holder.textDate.setText(dateText);
        } else if (isVotedByMe(owners)) {
            dateText = holder.textDate.getContext().getString(R.string.waiting_your_confirmation);
            holder.textDate.setText(dateText);
            holder.textDate.setTextColor(ContextCompat.getColor(holder.textDate.getContext(), R.color.red_warn));
        } else {
            dateText = holder.textDate.getContext().getString(R.string.waiting_other_confirmations);
            holder.textDate.setText(dateText);
            holder.textDate.setTextColor(ContextCompat.getColor(holder.textDate.getContext(), R.color.black_light));
        }
        int confirms = calculateStatus(owners, MULTISIG_OWNER_STATUS_CONFIRMED);
        holder.textConfirmations.setText(String.format(holder.itemView.getContext().getString(R.string.confirmations_of), confirms, owners.size()));
        int counter = calculateStatus(owners, MULTISIG_OWNER_STATUS_CONFIRMED);
        holder.textConfirmCount.setVisibility(counter > 0 ? View.VISIBLE : View.GONE);
        holder.textConfirmCount.setText(String.valueOf(counter));
        counter = calculateStatus(owners, MULTISIG_OWNER_STATUS_DECLINED);
        holder.textRejectCount.setVisibility(counter > 0 ? View.VISIBLE : View.GONE);
        holder.textRejectCount.setText(String.valueOf(counter));
        counter = calculateStatus(owners, MULTISIG_OWNER_STATUS_SEEN);
        holder.textViewCount.setText(String.valueOf(counter));
        setItemClickListener(holder.itemView, isIncoming, true, position);
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
        holder.amount.setText(CryptoFormatUtils.FORMAT_ETH.format(CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount())));
        holder.fiat.setText(getFiatAmount(transactionHistory, new BigInteger(transactionHistory.getTxOutAmount()).doubleValue()));

        setItemClickListener(holder.itemView, false, isIncoming, position);
    }

    private void bindConfirmed(Holder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final int txStatus = transactionHistory.getTxStatus();
        boolean isIncoming = txStatus == TX_IN_BLOCK_INCOMING || txStatus == TX_CONFIRMED_INCOMING;

        holder.operationImage.setImageResource(isIncoming ? R.drawable.ic_receive : R.drawable.ic_send);
        holder.date.setText(DateHelper.DATE_FORMAT_HISTORY.format(transactionHistory.getBlockTime() * 1000));
        holder.containerAddresses.removeAllViews();


        final String amount = CryptoFormatUtils.FORMAT_ETH.format(CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount())); //TODO improve
        final String amountFiat = getFiatAmount(transactionHistory, CryptoFormatUtils.weiToEth(transactionHistory.getTxOutAmount()));
        final String address;

        holder.containerAddresses.removeAllViews();

        if (isIncoming) {
            address = transactionHistory.getFrom();
        } else {
            address = transactionHistory.getTo();
        }

        setAddress(address, holder.containerAddresses);
        holder.amount.setText(String.format("%s ETH", amount));
        holder.fiat.setText(String.format("%s USD", amountFiat));
        setItemClickListener(holder.itemView, isIncoming, false, position);
    }

    private void setAddress(String text, ViewGroup destination) {
        TextView textView = (TextView) LayoutInflater.from(destination.getContext()).inflate(R.layout.item_history_address, destination, false);
        String name = RealmManager.getSettingsDao().getContactNameOrNull(text);
        textView.setText(name == null ? text : name);
//        if (name != null) {
//            TextView textName = (TextView) LayoutInflater.from(destination.getContext()).inflate(R.layout.item_history_address, destination, false);
//            textName.setText(name);
//            destination.addView(textName);
//        }
//        textView.setText(text);
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

        @BindView(R.id.text_confirmations)
        TextView textConfirmations;

        @BindView(R.id.text_confirm_count)
        TextView textConfirmCount;

        @BindView(R.id.group_multisig)
        Group groupMultisig;

        BlockedHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class MultisigHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_operation)
        ImageView imageOperation;
        @BindView(R.id.text_address)
        TextView textAddress;
        @BindView(R.id.text_amount)
        TextView textAmount;
        @BindView(R.id.text_date)
        TextView textDate;
        @BindView(R.id.text_fiat)
        TextView textFiat;
        @BindView(R.id.text_comment)
        TextView textComment;
        @BindView(R.id.text_confirmations)
        TextView textConfirmations;
        @BindView(R.id.text_confirm_count)
        TextView textConfirmCount;
        @BindView(R.id.text_reject_count)
        TextView textRejectCount;
        @BindView(R.id.text_view_count)
        TextView textViewCount;

        MultisigHolder(View itemView) {
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
