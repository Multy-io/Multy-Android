/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.RecentAddressesAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetSendViewModel;

public class AssetSendFragment extends BaseFragment {

    public static AssetSendFragment newInstance(){
        return new AssetSendFragment();
    }

    @BindView(R.id.input_address)
    EditText inputAddress;
    @BindView(R.id.button_next)
    TextView buttonNext;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_send, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        if (!TextUtils.isEmpty(viewModel.getReceiverAddress().getValue())) {
            inputAddress.setText(viewModel.getReceiverAddress().getValue()); // to set address from scanning qr or wallet
        }
        viewModel.getReceiverAddress().observe(getActivity(), s -> inputAddress.setText(s));
        setupInputAddress();
        initRecentAddresses();
        return view;
    }

    private void initRecentAddresses() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new RecentAddressesAdapter(RealmManager.getAssetsDao().getRecentAddresses(), address -> inputAddress.setText(address)));
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    @OnClick(R.id.button_address)
    void onClickAddressBook(){
        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_scan_wireless)
    void onClickWirelessScan(){
        Toast.makeText(getActivity(), R.string.not_implemented, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button_scan_qr)
    void onClickScanQr(){
        ((AssetSendActivity) getActivity()).showScanScreen();
    }

    @OnClick(R.id.button_next)
    void onClickNext(){
        viewModel.setReceiverAddress(inputAddress.getText().toString());
        viewModel.thoseAddress.setValue(inputAddress.getText().toString());
        ((AssetSendActivity) getActivity()).setFragment(R.string.send_from, R.id.container, WalletChooserFragment.newInstance());
        if (getActivity().getIntent().hasCategory(Constants.EXTRA_SENDER_ADDRESS)) {
            RealmManager.getAssetsDao().getWalletById(getActivity().getIntent().getIntExtra(Constants.EXTRA_WALLET_ID, 0));
            ((AssetSendActivity) getActivity()).setFragment(R.string.transaction_fee, R.id.container, TransactionFeeFragment.newInstance());
        }
    }

    private void setupInputAddress(){
        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)){
                    buttonNext.setBackgroundResource(R.color.disabled);
                    buttonNext.setEnabled(false);
                } else {
                    buttonNext.setBackgroundResource(R.drawable.btn_gradient_blue);
                    buttonNext.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

}
