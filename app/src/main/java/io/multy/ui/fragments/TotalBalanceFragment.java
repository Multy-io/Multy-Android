package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;

public class TotalBalanceFragment extends Fragment {
    @BindView(R.id.text_balance)
    TextView textBalance;
    @BindView(R.id.text_decimals)
    TextView textDecimals;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.item_total_balance, container, false);
        ButterKnife.bind(this, convertView);

        updateBalanceView();

        return convertView;
    }

    public void updateBalanceView() {
        final String balance = getTotalFiatBalance();
        final String decimals = balance.contains(".") ? balance.substring(balance.indexOf(".")) : ".00";
        final String fiat = balance.contains(".") ? balance.substring(0, balance.indexOf(".")) : balance;
        textBalance.setText(fiat);
        textDecimals.setText(decimals);
    }

    private String getTotalFiatBalance() {
        List<Wallet> wallets = RealmManager.get().copyFromRealm(RealmManager.getAssetsDao().getWallets());
        double sum = 0;

        for (Wallet wallet : wallets) {
            if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue() && wallet.getNetworkId() == NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue()
                    || wallet.getCurrencyId() != NativeDataHelper.Blockchain.ETH.getValue() && wallet.getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue()) {
                sum += Double.valueOf(wallet.getFiatBalanceLabelTrimmed().replace(wallet.getFiatString(), ""));
            }
        }

        return NumberFormatter.getFiatInstance().format(sum);
    }
}
