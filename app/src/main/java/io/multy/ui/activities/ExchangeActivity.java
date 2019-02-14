/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import java.net.URISyntaxException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.socket.SocketManager;
import io.multy.model.entities.ERC20TokenDAO;
import io.multy.model.entities.ExchangeAsset;
import io.multy.model.entities.ExchangePair;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.exchange.ChooserExchangePairFragment;
import io.multy.ui.fragments.exchange.ExchangeFragment;
import io.multy.ui.fragments.exchange.ExchangeWalletChooserFragment;
import io.multy.ui.fragments.send.AmountChooserFragment;
import io.multy.ui.fragments.send.AssetSendFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.ui.fragments.send.TransactionFeeFragment;
import io.multy.ui.fragments.send.WalletChooserFragment;
import io.multy.ui.fragments.send.ethereum.EthAmountChooserFragment;
import io.multy.ui.fragments.send.ethereum.EthSendSummaryFragment;
import io.multy.ui.fragments.send.ethereum.EthTransactionFeeFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.ExchangeViewModel;
import timber.log.Timber;


public class ExchangeActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isFirstFragmentCreation;
    private ExchangeViewModel viewModel;

//    private MutableLiveData<Integer> fragmentIDHolder = new MutableLiveData<>();
//    private int currentFragmentId = 0;

    //TODO check if SocketManager is needed here
    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        setContentView(R.layout.activity_exchange);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        viewModel = ViewModelProviders.of(this).get(ExchangeViewModel.class);

//        viewModel.setFragmentHolder(fragmentIDHolder);





//        fragmentIDHolder.observe(this, id ->{
//            switch (id){
//                case 0:
//                    setFragment(R.string.exchanging, R.id.container, ExchangeFragment.newInstance());
//                    currentFragmentId = 0;
//                    break;
//                case 1:
//                    setFragment(R.string.exchanging, R.id.container, ChooserExchangePairFragment.newInstance());
//                    currentFragmentId = 1;
//                    break;
//                case 2:
//                    ExchangeAsset asset = viewModel.getSelectedAsset().getValue();
//                    if (asset != null){
//                        int chainId = 0;
//                        int networdId = 0;
//                        if (asset.getChainId() != chainId){
//                            chainId = 60;
//                            networdId = 1;
//                        }
//                        setFragment(R.string.select_walet, R.id.container, ExchangeWalletChooserFragment.newInstance(chainId,networdId));
//                        currentFragmentId = 2;
//                    }
//            }
//        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        startFlow();


    }

    @Override
    protected void onResume() {
        super.onResume();


//        try {
//            if (socketManager == null) {
//                socketManager = new SocketManager();
//            }
//            socketManager.listenTransactionUpdates(() -> {//todo remove it when it will become deprecated
//                viewModel.updateWallets();
//            });
//            socketManager.listenEvent(SocketManager.getEventReceive(
//                    RealmManager.getSettingsDao().getUserId().getUserId()), args -> viewModel.updateWallets());
//            socketManager.connect();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onPause() {
//        if (socketManager != null && socketManager.isConnected()) {
//            socketManager.disconnect();
//        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //TODO WTF is that. fragment backstack, nah?
        logCancel();
//        switch (currentFragmentId){
//            case 0:
//                super.onBackPressed();
//                finish();
//                break;
//            case 1:
//                setFragment(R.string.exchanging, R.id.container, ExchangeFragment.newInstance());
//                currentFragmentId = 0;
//                break;
//            case 2:
//                setFragment(R.string.exchanging, R.id.container, ChooserExchangePairFragment.newInstance());
//                currentFragmentId = 1;
//                break;
//        }
//
//
//        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
//            List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
//            for (Fragment backStackFragment : backStackFragments) {
//                //TODO update this
//                if (backStackFragment.getClass().getSimpleName().equals(ExchangeFragment.TAG_SEND_SUCCESS)) {
//                    super.onBackPressed();
//                } else if (backStackFragment instanceof AmountChooserFragment || backStackFragment instanceof EthAmountChooserFragment) {
//                    toolbar.setTitle(R.string.transaction_fee);
//                } else if (backStackFragment instanceof TransactionFeeFragment || backStackFragment instanceof EthTransactionFeeFragment) {
//                    toolbar.setTitle(R.string.send_from);
//                } else if (backStackFragment instanceof WalletChooserFragment) {
//                    toolbar.setTitle(R.string.send_to);
//                } else if (backStackFragment instanceof AssetSendFragment || backStackFragment instanceof EthSendSummaryFragment) {
//                    toolbar.setTitle(R.string.send_to);
//                }
//            }
//            getSupportFragmentManager().popBackStack();
//        } else {
//            super.onBackPressed();
//            finish();
//        }

        int fragmentsInBackStack = getSupportFragmentManager().getBackStackEntryCount();
        if (fragmentsInBackStack > 1){

            int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            if (tag.equals(ExchangeFragment.class.getName())){
                finish();
            } else {
                if (tag.equals(WalletChooserFragment.class.getName()) || tag.equals(ChooserExchangePairFragment.class.getName())){
                    getSupportActionBar().setTitle(R.string.exchanging);
                }
                getSupportFragmentManager().popBackStack();
            }
        } else{
            finish();
//            super.onBackPressed();
        }
    }

    private void startFlow() {
        //TODO update this flow!
        ExchangeViewModel viewModel = ViewModelProviders.of(this).get(ExchangeViewModel.class);


        if (getIntent().hasExtra(Constants.EXTRA_TOKEN_CODE)){

            ERC20TokenDAO token = new ERC20TokenDAO(
                    getIntent().getExtras().getString(Constants.EXTRA_TOKEN_CODE),
                    getIntent().getExtras().getString(Constants.EXTRA_TOKEN_IMAGE_URL),
                    getIntent().getExtras().getString(Constants.EXTRA_CONTRACT_ADDRESS),
                    getIntent().getExtras().getInt(Constants.EXTRA_TOKEN_DECIMALS),
                    getIntent().getExtras().getString(Constants.EXTRA_TOKEN_BALANCE),
                    getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID),
                    getIntent().getExtras().getString(Constants.EXTRA_TOKEN_RATE)
            );

            viewModel.setSendERC20Token(token);

        } else if (getIntent().hasExtra(Constants.EXTRA_WALLET_ID)){
            viewModel.setPayFromWalletById(getIntent().getExtras().getLong(Constants.EXTRA_WALLET_ID));
        }

        setFragment(R.string.exchanging, R.id.container, ExchangeFragment.newInstance());


    }

//
//    private void getAddressIds(final String address, int[] addressIdsHolder) {
//        for (NativeDataHelper.Blockchain blockchain : NativeDataHelper.Blockchain.values()) {
//            for (NativeDataHelper.NetworkId networkId : NativeDataHelper.NetworkId.values()) {
//                if (isValidAddress(address, blockchain.getValue(), networkId.getValue())) {
//                    addressIdsHolder[0] = blockchain.getValue();
//                    addressIdsHolder[1] = networkId.getValue();
//                    return;
//                }
//            }
//        }
//    }

//    private boolean isValidAddress(String address, int blockchain, int network) {
//        try {
//            NativeDataHelper.isValidAddress(address, blockchain, network);
//            return true;
//        } catch (Throwable t) {
//            t.printStackTrace();
//            return false;
//        }
//    }

    private void logCancel() {
        List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
        for (Fragment backStackFragment : backStackFragments) {
            if (backStackFragment instanceof SendSummaryFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendSummary(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof AmountChooserFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendChooseAmount(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof TransactionFeeFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logTransactionFee(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof WalletChooserFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendFrom(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            } else if (backStackFragment instanceof AssetSendFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logSendTo(AnalyticsConstants.BUTTON_CLOSE);
            }
        }
    }

    public void setFragment(@StringRes int title, @IdRes int container, Fragment fragment) {
        getSupportActionBar().setTitle(title);



        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment);


//        if (isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
//        }
//
        isFirstFragmentCreation = false;

//        if (addToBackStack){
//            transaction.addToBackStack(fragment.getClass().getSimpleName());
//        }

        transaction.commit();

        hideKeyboard(this);
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel() {
        logCancel();
        finish();
    }

//    private void showAlert(@StringRes int resId, String msg){
//        new AlertDialog.Builder(this)
//                .setTitle(resId)
//                .setMessage(msg)
//                .setCancelable(false)
//                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
//                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
//                .show();
//    }

//    public void showScanScreen() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_REQUEST_CODE);
//        } else {
//            startActivityForResult(new Intent(this, ScanActivity.class), Constants.CAMERA_REQUEST_CODE);
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == Constants.CAMERA_REQUEST_CODE && grantResults.length > 0) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Analytics.getInstance(this).logSendTo(AnalyticsConstants.PERMISSION_GRANTED);
//                showScanScreen();
//            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                Analytics.getInstance(this).logSendTo(AnalyticsConstants.PERMISSION_DENIED);
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            if (data.hasExtra(Constants.EXTRA_QR_CONTENTS)) {
//                AssetSendViewModel viewModel = ViewModelProviders.of(this).get(AssetSendViewModel.class);
//                viewModel.setReceiverAddress(data.getStringExtra(Constants.EXTRA_QR_CONTENTS));
//                viewModel.thoseAddress.setValue(data.getStringExtra(Constants.EXTRA_QR_CONTENTS));
//                if (data.hasExtra(Constants.EXTRA_AMOUNT) && data.getExtras() != null) {
//                    viewModel.setAmountScanned(true);
//                    String amount = data.getExtras().getString(Constants.EXTRA_AMOUNT, "0");
//                    if (!TextUtils.isEmpty(amount) && amount.contains(",")) {
//                        amount = amount.replace(",", ".");
//                    }
//                    viewModel.setAmount(Double.valueOf(amount));
//                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}

