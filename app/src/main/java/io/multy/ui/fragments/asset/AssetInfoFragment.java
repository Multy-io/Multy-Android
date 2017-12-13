package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.adapters.AssetTransactionsAdapter;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.WalletViewModel;

public class AssetInfoFragment extends BaseFragment {

    public static final String TAG = AssetInfoFragment.class.getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_transactions)
    RecyclerView recyclerView;
    @BindView(R.id.constraint_empty)
    ConstraintLayout emptyAsset;
    @BindView(R.id.text_value)
    TextView textBalanceOriginal;
    @BindView(R.id.text_amount)
    TextView textBalanceFiat;
    @BindView(R.id.text_address)
    TextView textAddress;

    private WalletViewModel viewModel;

    private AssetTransactionsAdapter transactionsAdapter;

    public static AssetInfoFragment newInstance() {
        return new AssetInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionsAdapter = new AssetTransactionsAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false).getRoot();
        View view = inflater.inflate(R.layout.fragment_asset_info, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        viewModel.setContext(getActivity());
        WalletRealmObject wallet = viewModel.getWallet(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
        if (wallet != null) {
            setupWalletInfo(wallet);
        } else {
            viewModel.getWalletLive().observe(this, this::setupWalletInfo);
        }

        initialize();
        return view;
    }

    private void initialize() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(transactionsAdapter);
        if (transactionsAdapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyAsset.setVisibility(View.VISIBLE);
            setToolbarScrollFlag(0);
        } else {
            emptyAsset.setVisibility(View.GONE);
            setToolbarScrollFlag(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
        }
    }

    private void setupWalletInfo(WalletRealmObject wallet) {
        textAddress.setText(wallet.getCreationAddress()); // TODO wallet.getAddresses();
        textBalanceOriginal.setText(String.valueOf(wallet.getCurrency()));
        viewModel.getExchangePrice().observe(AssetInfoFragment.this, exchangePrice -> textBalanceFiat.setText(String.valueOf(wallet.getCurrency() + exchangePrice)));
    }

    private void setToolbarScrollFlag(int flag) {
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(flag);
    }

    private void switchNfcPayment() {

    }

    private void subscribeViewModel() {

    }

    @OnClick(R.id.card_addresses)
    void onClickAddress() {
        if (viewModel.getWalletLive().getValue() != null) {
            ((AssetActivity) getActivity()).setFragment(R.id.container_full,
                    AddressesFragment.newInstance(viewModel.getWalletLive().getValue().getWalletIndex()));
        } else {
            WalletRealmObject wallet = viewModel.getWallet(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
            ((AssetActivity) getActivity()).setFragment(R.id.container_full,
                    AddressesFragment.newInstance(wallet.getWalletIndex()));
        }
    }

    @OnClick(R.id.image_copy)
    void onClickShare() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, viewModel.getWalletLive().getValue().getCreationAddress());
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.send_via)));
    }

    @OnClick(R.id.text_address)
    void onClickCopy() {
        String address = viewModel.getWalletLive().getValue().getCreationAddress();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.address_copied, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.close)
    void onClickClose() {
        getActivity().finish();
    }
}