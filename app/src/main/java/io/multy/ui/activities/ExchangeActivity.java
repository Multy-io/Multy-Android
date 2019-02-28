/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.ERC20TokenDAO;
import io.multy.ui.fragments.exchange.ChooserExchangePairFragment;
import io.multy.ui.fragments.exchange.ExchangeFragment;
import io.multy.ui.fragments.send.AmountChooserFragment;
import io.multy.ui.fragments.send.AssetSendFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.ui.fragments.send.TransactionFeeFragment;
import io.multy.ui.fragments.send.WalletChooserFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.ExchangeViewModel;


public class ExchangeActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean isFirstFragmentCreation;
    private ExchangeViewModel viewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        setContentView(R.layout.activity_exchange);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        viewModel = ViewModelProviders.of(this).get(ExchangeViewModel.class);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        startFlow();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
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
        logCancel();


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
        }
    }

    private void startFlow() {

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


        transaction.addToBackStack(fragment.getClass().getName());
//        isFirstFragmentCreation = false;
        transaction.commit();
        hideKeyboard(this);
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel() {
        logCancel();
        finish();
    }
}

