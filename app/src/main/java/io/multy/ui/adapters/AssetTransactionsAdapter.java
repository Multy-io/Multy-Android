package io.multy.ui.adapters;

import android.os.Bundle;
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
import io.multy.model.entities.TransactionHistory;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.asset.TransactionInfoFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DateHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.RealmList;

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

    private int walletIndex;
    private List<TransactionHistory> transactionHistoryList;

    public AssetTransactionsAdapter(List<TransactionHistory> transactionHistoryList, int walletIndex) {
        this.transactionHistoryList = transactionHistoryList;
        this.walletIndex = walletIndex;
        Collections.sort(this.transactionHistoryList, (transactionHistory, t1) -> Long.compare(t1.getMempoolTime(), transactionHistory.getMempoolTime()));
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

    private void setItemClickListener(View view, boolean isIncoming, int position) {
        view.setOnClickListener((v) -> {
            Analytics.getInstance(v.getContext()).logWallet(AnalyticsConstants.WALLET_TRANSACTION, 1);
            Bundle transactionInfo = new Bundle();
            int mode = isIncoming ? MODE_RECEIVE : MODE_SEND;
            transactionInfo.putInt(TransactionInfoFragment.SELECTED_POSITION, position);
            transactionInfo.putInt(TransactionInfoFragment.TRANSACTION_INFO_MODE, mode);
            transactionInfo.putInt(TransactionInfoFragment.WALLET_INDEX, walletIndex);
            ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_full, TransactionInfoFragment.newInstance(transactionInfo))
                    .addToBackStack(TransactionInfoFragment.TAG)
                    .commit();
        });
    }

    private String getStockFiatAmount(TransactionHistory transactionHistory) {
        if (transactionHistory.getStockExchangeRates() != null && transactionHistory.getStockExchangeRates().size() > 0) {
            return String.valueOf(CryptoFormatUtils.satoshiToUsd(transactionHistory.getTxOutAmount(), transactionHistory.getStockExchangeRates().get(0).getExchanges().getBtcUsd()));
        }
        return "";
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
            amountFiat = getStockFiatAmount(transactionHistory);
            setAddresses(transactionHistory.getInputs(), holder.containerAddresses);

            holder.amountLocked.setText(String.format("%s BTC", lockedAmount));
            holder.fiatLocked.setText(String.format("(%s USD)", lockedFiat));
        } else {
            //TODO REMOVE DRY AND OPTIMIZE
            RealmList<WalletAddress> addresses = RealmManager.getAssetsDao().getWalletById(walletIndex).getAddresses();
//            List<WalletAddress> inputs = transactionHistory.getInputs();

            List<WalletAddress> outputs = transactionHistory.getOutputs();
//            user change address must be last, so reversing
//            Collections.reverse(outputs);
            WalletAddress userChangeAddress = null;
            String addressTo = "";

            for (WalletAddress output : outputs) {

                if (!output.getAddress().equals(Constants.DONTAION_ADDRESS)) {
                    for (WalletAddress walletAddress : addresses) {
                        if (output.getAddress().equals(walletAddress.getAddress())) {
                            userChangeAddress = output;
                        } else {
                            addressTo = output.getAddress();
                        }
                    }
                }
            }

            if (!lockedAmount.equals("")) {
                holder.containerLocked.setVisibility(View.VISIBLE);
                lockedAmount = CryptoFormatUtils.satoshiToBtc(userChangeAddress.getAmount());
                lockedFiat = CryptoFormatUtils.satoshiToUsd(userChangeAddress.getAmount());
                holder.amountLocked.setText(String.format("%s BTC", lockedAmount));
                holder.fiatLocked.setText(String.format("(%s USD)", lockedFiat));
            } else {
                holder.containerLocked.setVisibility(View.GONE);
            }

            amount = CryptoFormatUtils.satoshiToBtc(transactionHistory.getTxOutAmount());
            amountFiat = getStockFiatAmount(transactionHistory);
            setAddress(addressTo, holder.containerAddresses);
        }

        holder.amount.setText(String.format("%s BTC", amount));
        holder.fiat.setText(String.format("%s USD", amountFiat));

        setItemClickListener(holder.itemView, isIncoming, position);
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
        holder.fiat.setText(getStockFiatAmount(transactionHistory));

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
            String stockFiat = getStockFiatAmount(transactionHistory);
            holder.fiat.setText(stockFiat.equals("") ? "" : String.format("%s USD", stockFiat));
        } else {
            WalletAddress addressTo = null;
            List<WalletAddress> outputs = transactionHistory.getOutputs();
            //user change address must be last, so reversing
            Collections.reverse(outputs);

//            for (WalletAddress output : outputs) {
//                if (!output.getAddress().equals(Constants.DONTAION_ADDRESS)) {
//                    for (WalletAddress walletAddress : RealmManager.getAssetsDao().getWalletById(walletIndex).getAddresses()) {
//                        if (!output.getAddress().equals(walletAddress.getAddress())) {
//                            addressTo = output;
//                        }
//                    }
//                }
//            }

            for (WalletAddress input : transactionHistory.getInputs()) {
                for (WalletAddress walletAddress : RealmManager.getAssetsDao().getWalletById(walletIndex).getAddresses()) {
                    if (input.getAddress().equals(walletAddress.getAddress())) {
                        addressTo = input;
                    }
                }
            }

            if (addressTo != null) {
                setAddress(addressTo.getAddress(), holder.containerAddresses);
                holder.amount.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(addressTo.getAmount())));
                String stockFiat = getStockFiatAmount(transactionHistory);
                holder.fiat.setText(stockFiat.equals("") ? "" : String.format("%s USD", stockFiat));
            }
        }

        setItemClickListener(holder.itemView, isIncoming, position);
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
