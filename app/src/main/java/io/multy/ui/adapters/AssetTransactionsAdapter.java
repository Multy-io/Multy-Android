package io.multy.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.realm.RealmList;

import static io.multy.util.Constants.TX_CONFIRMED_INCOMING;
import static io.multy.util.Constants.TX_IN_BLOCK_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_INCOMING;
import static io.multy.util.Constants.TX_MEMPOOL_OUTCOMING;

public class AssetTransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BLOCKED = 102;
    private static final int TYPE_CONFIRMED = 203;
    private static final int TYPE_REJECTED = 204;

    private int walletIndex;
    private List<TransactionHistory> transactionHistoryList;

    public AssetTransactionsAdapter(List<TransactionHistory> transactionHistoryList, int walletIndex) {
        this.transactionHistoryList = transactionHistoryList;
        this.walletIndex = walletIndex;
        Collections.sort(this.transactionHistoryList, (transactionHistory, t1) -> Long.compare(transactionHistory.getBlockTime(), t1.getBlockTime()));
    }

    public AssetTransactionsAdapter() {
        transactionHistoryList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
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

    private void bindBlocked(BlockedHolder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final boolean isIncoming = transactionHistory.getTxStatus() == TX_MEMPOOL_INCOMING;
        final String lockedAmount;
        final String lockedFiat;
        final String amount;
        final String amountFiat;
        final String address;

        if (isIncoming) {
            lockedAmount = CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount());
            lockedFiat = CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount());
            amount = lockedAmount;
            amountFiat = String.valueOf(CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount(), transactionHistory.getBtcToUsd()));
            address = transactionHistory.getAddress();
        } else {
            RealmList<WalletAddress> addresses = RealmManager.getAssetsDao().getWalletById(walletIndex).getAddresses();
//            List<WalletAddress> inputs = transactionHistory.getInputs();

            List<WalletAddress> outputs = transactionHistory.getOutputs();
            //user change address must be last, so reversing
            Collections.reverse(outputs);

            WalletAddress userChangeAddress = null;

            for (WalletAddress output : outputs) {
                if (userChangeAddress != null) {
                    break;
                }

                for (WalletAddress walletAddress : addresses) {
                    if (output.getAddress().equals(walletAddress.getAddress())) {
                        userChangeAddress = output;
                        break;
                    }
                }
            }

            lockedAmount = CryptoFormatUtils.satoshiToBtc(userChangeAddress.getAmount());
            lockedFiat = CryptoFormatUtils.satoshiToUsd(userChangeAddress.getAmount());
            amount = CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount());
            amountFiat = String.valueOf(CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount(), transactionHistory.getBtcToUsd()));
            address = userChangeAddress.getAddress();
        }

        holder.address.setText(address);
        holder.amount.setText(String.format("%s BTC", amount));
        holder.amountLocked.setText(String.format("%s BTC", lockedAmount));
        holder.fiat.setText(String.format("%s USD", amountFiat));
        holder.fiatLocked.setText(String.format("(%s USD)", lockedFiat));
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
        holder.fiat.setText(CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount(), transactionHistory.getBtcToUsd()));
    }

    private void bindConfirmed(Holder holder, int position) {
        TransactionHistory transactionHistory = transactionHistoryList.get(position);
        final int txStatus = transactionHistory.getTxStatus();
        boolean isIncoming = txStatus == TX_IN_BLOCK_INCOMING || txStatus == TX_CONFIRMED_INCOMING;

        holder.operationImage.setImageResource(isIncoming ? R.drawable.ic_receive : R.drawable.ic_send);
        holder.address.setText(transactionHistory.getAddress());
        holder.date.setText(DateHelper.DATE_FORMAT_HISTORY.format(transactionHistory.getBlockTime() * 1000));

        holder.amount.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount())));
        holder.fiat.setText(String.format("%s USD", CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount(), transactionHistory.getBtcToUsd())));
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

        @BindView(R.id.text_address)
        TextView address;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_fiat)
        TextView fiat;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class BlockedHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_address)
        TextView address;

        @BindView(R.id.text_amount)
        TextView amount;

        @BindView(R.id.text_fiat)
        TextView fiat;

        @BindView(R.id.text_locked_amount)
        TextView amountLocked;

        @BindView(R.id.text_locked_fiat)
        TextView fiatLocked;

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
