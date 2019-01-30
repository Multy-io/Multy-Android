/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.ERC20TokenDAO;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.ExchangeAsset;
import io.multy.model.entities.ExchangePair;
import io.multy.model.entities.Fee;
import io.multy.model.entities.TransactionBTCMeta;
import io.multy.model.entities.TransactionERC20Meta;
import io.multy.model.entities.TransactionETHMeta;
import io.multy.model.entities.TransactionUIEstimation;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.ExchangeCurrenciesListResponse;
import io.multy.model.responses.ExchangePairResponse;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.MessageResponse;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.storage.RealmManager;
import io.multy.ui.fragments.dialogs.CompleteDialogFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.NumberFormatter;
import io.multy.util.SingleLiveEvent;
import io.multy.util.TransactionHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ExchangeViewModel extends BaseViewModel {

    private MutableLiveData<Wallet> payFromWallet = new MutableLiveData<>();
    private MutableLiveData<Wallet> receiveToWallet = new MutableLiveData<>();
    private MutableLiveData<List<String>> assetsList = new MutableLiveData<>();
    private MutableLiveData<Integer> fragmentHolder = new MutableLiveData<>();
    private MutableLiveData<List<ExchangeAsset>> assets  = new MutableLiveData<>();
    private MutableLiveData<ExchangeAsset> assetExchangeTo = new MutableLiveData<>();
    private MutableLiveData<Float> exchangeRate = new MutableLiveData<>();
    private MutableLiveData<Double> minFromAmount = new MutableLiveData<>();
    private MutableLiveData<List<ServerConfigResponse.ERC20TokenSupport>> supportTokens = new MutableLiveData<>();
    public MutableLiveData<FeeRateResponse.Speeds> speeds = new MutableLiveData<>();
    public MutableLiveData<TransactionUIEstimation> transactionFeeEstimation = new MutableLiveData<>();

    public MutableLiveData<ERC20TokenDAO> sendERC20Token = new MutableLiveData<>();

    public MutableLiveData<Boolean> success = new MutableLiveData<>();

//    private MutableLiveData<Boolean> hasRateFromCrypto = new MutableLiveData<>();
//    private MutableLiveData<Boolean> hasRateToCrypto = new MutableLiveData<>();

    //TODO Check if this data is needed


    public MutableLiveData<Estimation> estimation = new MutableLiveData<>();
//    public MutableLiveData<Fee> fee = new MutableLiveData<>();
    public MutableLiveData<String> gasLimit = new MutableLiveData<>();
    private byte[] seed;


    public MutableLiveData<String> thoseAddress = new MutableLiveData<>();
    public static MutableLiveData<Long> transactionPrice = new MutableLiveData<>();
    public SingleLiveEvent<String> transaction = new SingleLiveEvent<>();
    public MutableLiveData<String> contractAddress = new MutableLiveData<>();
    public MutableLiveData<String> tokenBalance = new MutableLiveData<>();
    public MutableLiveData<String> tokenCode = new MutableLiveData<>();
    public MutableLiveData<Integer> decimals = new MutableLiveData<Integer>();
    private double amount;
    private boolean isPayForCommission = true;
    private String donationAmount = "0";
    private boolean isAmountScanned = false;
    private double currenciesRate;
    private long payFromWalletId;

//    private String changeAddress;
//    private String donationAddress;

//    private String signTransactionError;
//    private Handler handler = new Handler();




    public ExchangeViewModel() {
//        currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        fragmentHolder.setValue(0);

        getSupportTokens();
    }

    public void makeExchange(){
        isLoading.setValue(true);
        ExchangePair exchangePair = new ExchangePair(getPayFromAssetName() , assetExchangeTo.getValue().getName(), amount);
        exchangePair.setReceivingToAddress(receiveToWallet.getValue().getActiveAddress().getAddress());
        getPayToAddress(exchangePair);

    }

    private void mapAssets(){
        List<ExchangeAsset> assetsFull = new ArrayList<>();

        List<ServerConfigResponse.ERC20TokenSupport> tokens = supportTokens.getValue();
        List<String> rawAssets = assetsList.getValue();

        String fromWalletChain = getPayFromAssetName();

        for (String asset : rawAssets){

            //We don't have Bitcoin and Ethereum in support Currencies, so we need to add them manually
            if (asset.toLowerCase().equals(Constants.BTC.toLowerCase()) && !fromWalletChain.equals(asset.toLowerCase())){
                ExchangeAsset exchangeAsset = new ExchangeAsset(Constants.BTC, "Bitcoin", 0, String.valueOf(R.drawable.ic_bitcoin));
                assetsFull.add(exchangeAsset);
            }
            if (asset.toLowerCase().equals(Constants.ETH.toLowerCase()) && !fromWalletChain.equals(asset.toLowerCase())){
                ExchangeAsset exchangeAsset = new ExchangeAsset(Constants.ETH, "Ethereum", 60, String.valueOf(R.drawable.ic_ethereum));
                assetsFull.add(exchangeAsset);
            }

            //Now we can make simple iteration beetween supported assets and available exchange assets to fit the in one array
            for(ServerConfigResponse.ERC20TokenSupport token : tokens){
                if (asset.toLowerCase().equals(token.getName().toLowerCase())){
                    if (!token.getSmartContractAddress().isEmpty()){
                        String url = Constants.TOKEN_BASE_LOGO_URL + token.getSmartContractAddress().toLowerCase() + ".png";
                        ExchangeAsset exchangeAsset = new ExchangeAsset(token.getName(), token.getFullName(), 60, url);
                        assetsFull.add(exchangeAsset);
                    }
                }
            }
        }


        assets.setValue(assetsFull);
    }


    private void getSupportTokens(){
        MultyApi.INSTANCE.getServerConfig().enqueue(new Callback<ServerConfigResponse>() {
            @Override
            public void onResponse(Call<ServerConfigResponse> call, Response<ServerConfigResponse> response) {
                //TODO check respone code
                if (response.isSuccessful() && response.body() != null) {
                    supportTokens.setValue(response.body().getSupportTokens());
                    getAssetsList();
                    requestFeeRates(payFromWallet.getValue().getCurrencyId(), payFromWallet.getValue().getNetworkId(), null);

                }
            }

            @Override
            public void onFailure(Call<ServerConfigResponse> call, Throwable t) {

            }
        });
    }

    public void getAssetsList(){
        MultyApi.INSTANCE.getExchangeList().enqueue(new Callback<ExchangeCurrenciesListResponse>() {
            @Override
            public void onResponse(Call<ExchangeCurrenciesListResponse> call, Response<ExchangeCurrenciesListResponse> response) {
                //TODO check respone code
                if (response.isSuccessful() && response.body() != null) {
                    assetsList.setValue(response.body().getAssets());
                    mapAssets();
                }
            }

            @Override
            public void onFailure(Call<ExchangeCurrenciesListResponse> call, Throwable t) {

            }
        });
    }

    public MutableLiveData<Float> getExchangeRate() {return this.exchangeRate;}

    public void getExchangePair(ExchangePair pair){
        MultyApi.INSTANCE.getExchangePair(pair).enqueue(new Callback<ExchangePairResponse>() {
            @Override
            public void onResponse(Call<ExchangePairResponse> call, Response<ExchangePairResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //TODO check respone code
                    Log.d("EXCHANGE VM", "PAIR from:" + pair.getFromAsset() + " to:" + pair.getToAsset() + " amount:" + response.body().getAmount());
                    exchangeRate.setValue(response.body().getAmount());
                    getMinExchangeValue(pair);
                }
            }

            @Override
            public void onFailure(Call<ExchangePairResponse> call, Throwable t) {

            }
        });
    }


    public void getMinExchangeValue(ExchangePair pair){
        MultyApi.INSTANCE.getMinExchangeValue(pair).enqueue(new Callback<ExchangePairResponse>() {
            @Override
            public void onResponse(Call<ExchangePairResponse> call, Response<ExchangePairResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("EXCHANGE VM", "GOTED MIN VALUE:" + response.body().getAmount());
                    minFromAmount.setValue((double) response.body().getAmount());
                }
            }

            @Override
            public void onFailure(Call<ExchangePairResponse> call, Throwable t) {

            }
        });
    }

    public void getPayToAddress(ExchangePair pair){
        MultyApi.INSTANCE.getPayToAddress(pair).enqueue(new Callback<ExchangePairResponse>() {
            @Override
            public void onResponse(Call<ExchangePairResponse> call, Response<ExchangePairResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getTransactionId() == null){
                        //TODO show Pair is not available now
                        success.setValue(false);
                    } else if (response.body().getPayToAddress() == null || response.body().getPayToAddress().length() < 1){
                        //TODO remove this hardcoded string
                        errorMessage.setValue("Amount is too small for exchange");
                        success.setValue(false);
                    } else {
                        //We have currect payToAddress
                        signTransaction(response.body().getPayToAddress());
                        //TODO sign and send transaction to this address;
                        Log.d("EXCHANGE VM", "GOTED PAYING TO ADDRESS:" + response.body().getPayToAddress());
                        Log.d("EXCHANGE VM", "GOTED RECEIVE TO ADDRESS:" + response.body().getReceiveToAddress());
                    }

                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<ExchangePairResponse> call, Throwable t) {
                //TODO show something went wrong message
                isLoading.setValue(false);
            }
        });
    }

    public MutableLiveData<List<ExchangeAsset>>  getAssets(){
        return this.assets;
    }

    public void setFragmentHolder(MutableLiveData<Integer> fragmentIDHolder){
        this.fragmentHolder = fragmentIDHolder;
    }

    public void changeFragment(int id){
        fragmentHolder.setValue(id);
    }

    public double getCurrenciesRate() {
        if (currenciesRate == 0) {
            currenciesRate = RealmManager.getSettingsDao().getCurrenciesRateById(payFromWallet.getValue().getCurrencyId());
        }
        return currenciesRate;
    }

    public void setSelectedAsset(ExchangeAsset asset){
        assetExchangeTo.setValue(asset);
        String from = null;
        if (sendERC20Token.getValue() != null){
            from = sendERC20Token.getValue().getName();
        } else {
            from = payFromWallet.getValue().getCurrencyName();
        }

        String to = asset.getName();
        getExchangePair(new ExchangePair(from, to, 1d));

        //TODO check if walletExchangeTo was selected Before and

        if (receiveToWallet.getValue() == null|| receiveToWallet.getValue().getCurrencyId() != asset.getChainId()){
            //TODO launch select wallet fragment
            fragmentHolder.setValue(2);
        } else {
            fragmentHolder.setValue(0);
        }

    }

    public MutableLiveData<Wallet> getReceiveToWallet(){ return  this.receiveToWallet;}

    public void setReceiveToWallet(Wallet wallet){
        this.receiveToWallet.setValue(wallet);
        fragmentHolder.setValue(0);
    }


    public MutableLiveData<ExchangeAsset> getSelectedAsset(){
        return this.assetExchangeTo;
    }

    public void setPayFromWalletById(long walletId){
        this.payFromWalletId = walletId;
        this.payFromWallet.setValue(RealmManager.getAssetsDao().getWalletById(walletId));
    }

    public void setSendERC20Token(ERC20TokenDAO token){
        this.sendERC20Token.setValue(token);
        this.payFromWalletId = token.getParentWalletID();
        this.payFromWallet.setValue(RealmManager.getAssetsDao().getWalletById(token.getParentWalletID()));
    }

//    public Wallet getWallet() {
//        if (wallet.getValue() == null || !wallet.getValue().isValid()) {
//            setWallet(RealmManager.getAssetsDao().getWalletById(walletId));
//        }
//        return this.wallet.getValue();
//    }

//    public void setFee(Fee fee) {
//        this.fee.setValue(fee);
//    }
//
//    public Fee getFee() {
//        return fee.getValue();
//    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public BigDecimal getAmountFull() {
        return new BigDecimal(amount);
    }

    public MutableLiveData<ERC20TokenDAO> getSendERC20Token(){
        return this.sendERC20Token;
    }

    public MutableLiveData<Wallet> getPayFromWallet(){
        return this.payFromWallet;
    }

    public MutableLiveData<Boolean> getSuccess() {return this.success;}

    public void requestFeeRates(int currencyId, int networkId, @Nullable String recipientAddress) {
        isLoading.postValue(true);
        Callback<FeeRateResponse> callback = new Callback<FeeRateResponse>() {
            @Override
            public void onResponse(@NonNull Call<FeeRateResponse> call, @NonNull Response<FeeRateResponse> response) {
                isLoading.postValue(false);
                FeeRateResponse body = response.body();
                if (response.isSuccessful() && body != null) {
                    speeds.postValue(body.getSpeeds());
                    gasLimit.setValue(TextUtils.isEmpty(body.getCustomGasLimit()) ?
                            Constants.GAS_LIMIT_DEFAULT : body.getCustomGasLimit());
                } else {
                    errorMessage.postValue(Multy.getContext().getString(R.string.error_loading_rates));
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeeRateResponse> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue(t.getMessage());
                t.printStackTrace();
            }
        };
        if (recipientAddress == null) {
            MultyApi.INSTANCE.getFeeRates(currencyId, networkId).enqueue(callback);
        } else {
            MultyApi.INSTANCE.getFeeRates(currencyId, networkId, recipientAddress).enqueue(callback);
        }
    }

    public void requestMultisigEstimates(String multisigWalletAddress) {
        isLoading.setValue(true);
        MultyApi.INSTANCE.getEstimations(multisigWalletAddress).enqueue(new Callback<Estimation>() {
            @Override
            public void onResponse(@NonNull Call<Estimation> call, @NonNull Response<Estimation> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    estimation.setValue(response.body());
                } else {
                    errorMessage.setValue(Multy.getContext().getString(R.string.error_loading_rates));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Estimation> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getLocalizedMessage());
            }

        });
    }

    public MutableLiveData<TransactionUIEstimation> getEstimateTransaction(){
        return this.transactionFeeEstimation;
    }

    public void estimateTransaction(String amount){
        TransactionUIEstimation totalSummery = new TransactionUIEstimation();
        Object meta = null;
        if (payFromWallet.getValue().getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()){
            //We are sending BTC so need to make BTC estimation

            //For estimation we don't need some exact payToAddress
//            meta = new TransactionBTCMeta(payFromWallet.getValue(), String.valueOf(speeds.getValue().getVeryFast()), payFromWallet.getValue().getAddresses().get(0).getAddress(), payFromWallet.getValue().getAddresses().get(0).getAddress());
//
//            ((TransactionBTCMeta) meta).setAmount(amount);
//            ((TransactionBTCMeta) meta).setDonationAmount("0");
//            ((TransactionBTCMeta) meta).setDonationAddress(payFromWallet.getValue().getAddresses().get(0).getAddress());
//            ((TransactionBTCMeta) meta).setPayingForComission(true);

            meta = buildBTCTransactionMeta(amount, getPayFromWallet().getValue().getActiveAddress().getAddress());

            String txFeeCost = TransactionHelper.getInstance().estimateTransactionFee(meta);
            if (txFeeCost != null){
                Double fromCryptoAmountD = Double.parseDouble(amount);
                Double fromCryptoFeeValue = Double.parseDouble(CryptoFormatUtils.satoshiToBtc(Long.parseLong(txFeeCost)));
                Double fromTotalCrypto = fromCryptoAmountD + fromCryptoFeeValue;
                Double fiatFromCrypto = fromTotalCrypto * getCurrenciesRate();

                totalSummery.setFromCryptoValue(NumberFormatter.getInstance().format(fromTotalCrypto));
                totalSummery.setFromFiatValue(NumberFormatter.getFiatInstance().format(fiatFromCrypto));

                transactionFeeEstimation.setValue(totalSummery);
            } else {
                transactionFeeEstimation.setValue(null);
            }



        } else if (payFromWallet.getValue().getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue()){
            //We are sending ETH or ERC20 so need to make ETH estimation
            long gasPrice = speeds.getValue().getVeryFast();
            long gasLimitOwn = Long.parseLong(gasLimit.getValue());
            Double txETHCost = CryptoFormatUtils.weiToEth(String.valueOf(gasPrice * gasLimitOwn));
            if (getSendERC20Token().getValue() != null){
                //TODO this is bulshit
                totalSummery.setFromCryptoValue(amount);
                transactionFeeEstimation.setValue(totalSummery);
            } else {
                Double fromCryptoAmountD = Double.parseDouble(amount);
                Double fromTotalCrypto = fromCryptoAmountD + txETHCost;
                Double fiatFromCrypto = fromTotalCrypto * getCurrenciesRate();

                totalSummery.setFromCryptoValue(NumberFormatter.getInstance().format(fromTotalCrypto));
                totalSummery.setFromFiatValue(NumberFormatter.getFiatInstance().format(fiatFromCrypto));

                transactionFeeEstimation.setValue(totalSummery);
            }

        }
    }


    private void signTransaction(String payToAddress){
        Object meta = null;

        if (getPayFromWallet().getValue().getCurrencyId() == NativeDataHelper.Blockchain.ETH.getValue()){
            //TODO let's make ETH META
            if (sendERC20Token.getValue() != null){
               meta = buildERC20TransactionMeta(String.valueOf(amount), payToAddress);
            } else {
                meta = buildETHTransactionMeta(String.valueOf(amount), payToAddress);
            }
        } else if (getPayFromWallet().getValue().getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()){
            //Here we are building metaTransaction
            meta = buildBTCTransactionMeta(String.valueOf(amount), payToAddress);
        } else {
            //Something went absolutelly wrong. We should crash
        }

        String transaction = TransactionHelper.getInstance().makeTransaction(meta);

        Log.d("EXCHANGE VM", "We got signed transaction:"+transaction);
        send(meta, transaction);
    }

    private TransactionBTCMeta buildBTCTransactionMeta(String amount, String payToAddress){

        final int walletIndex = payFromWallet.getValue().getIndex();
        if (seed == null) {
            seed = RealmManager.getSettingsDao().getSeed().getSeed();
        }

        String changeAddress = null;
        try {
            changeAddress = NativeDataHelper.makeAccountAddress(seed, walletIndex, payFromWallet.getValue().getBtcWallet().getAddresses().size(),
                    payFromWallet.getValue().getCurrencyId(), payFromWallet.getValue().getNetworkId());
        } catch (JniException e) {
            e.printStackTrace();
            errorMessage.setValue("Error creating change address " + e.getMessage());
        }

        TransactionBTCMeta meta = new TransactionBTCMeta(
                payFromWallet.getValue(),                                       //From Wallet
                String.valueOf(speeds.getValue().getVeryFast()),                //Fee rate
                payToAddress,                                                   //To Address
                changeAddress                                                   //Change Address
        );

        meta.setAmount(amount);
        meta.setDonationAmount("0");
        meta.setDonationAddress(payFromWallet.getValue().getAddresses().get(0).getAddress()); //this is just fake data ..Core ignore this field
        meta.setPayingForComission(true);                                      //Should check if sending all amount of the wallet or not

        return meta;
    }

    private TransactionETHMeta buildETHTransactionMeta(String amount, String payToAddress){
        TransactionETHMeta meta = new TransactionETHMeta(
                getPayFromWallet().getValue(),                  //Wallet to pay from
                String.valueOf(speeds.getValue().getVeryFast()),
                gasLimit.getValue(),
                payToAddress
        );
        meta.setAmount(amount);
        meta.setPayingForComission(true);

        return meta;
    }

    private TransactionERC20Meta buildERC20TransactionMeta(String amount, String payToAddress){
        TransactionERC20Meta meta = new TransactionERC20Meta(
                payFromWallet.getValue(),
                String.valueOf(speeds.getValue().getVeryFast()),
                gasLimit.getValue(),
                payToAddress,
                sendERC20Token.getValue()
        );
        meta.setAmount(amount);
        meta.setPayingForComission(true);
        return meta;
    }


    private void send(Object rawMeta, String transaction) {
        isLoading.setValue(true);
        Object meta = null;
        if (rawMeta instanceof TransactionBTCMeta){
            sendBTC((TransactionBTCMeta) rawMeta, transaction);
        } else if (rawMeta instanceof  TransactionETHMeta){
            sendETH((TransactionETHMeta) rawMeta, transaction);
        } else if (rawMeta instanceof TransactionERC20Meta){
            sendERC20((TransactionERC20Meta) rawMeta, transaction);
        }
    }

    private void sendERC20(TransactionERC20Meta meta, String transaction){
        //TODO make all the staff here!
        isLoading.setValue(true);

        try {
            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(
                    meta.getWallet().getCurrencyId(),
                    meta.getWallet().getNetworkId(),
                    new HdTransactionRequestEntity.Payload(
                            "",
                            0,
                            meta.getWallet().getIndex(),
                            transaction.startsWith("0x") ? transaction : "0x" + transaction,
                            false)
                    )
            ).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        success.postValue(true);
                    } else {
//                        Analytics.getInstance(getActivity()).logError(AnalyticsConstants.ERROR_TRANSACTION_API);
                        try {
                            String errorBody = response.errorBody() == null ? "" : response.errorBody().string();
                            errorMessage.setValue(errorBody);
                            success.postValue(false);
//                            if (response.code() == 406 && getContext() != null) {
//                                Analytics.getInstance(getContext()).logEvent(TAG, "406", errorBody);
//                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            errorMessage.setValue("Something went wrong :(");
                            success.postValue(false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                    //{"code":200,"message":"d537ac25da78cf85a34eba6396bedf8f9bb7db5e13662773c350497a1c0e5094"}
                    //{"code":200,"message":{"message":"0x78f5d5a0931166045bd878c81cce267291f410c57c61afa5c10bb0f1d8b67880"}}
                    t.printStackTrace();
                    errorMessage.postValue(t.getMessage());
                    success.postValue(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.postValue(e.getMessage());
            success.postValue(false);
        }

    }


    private void sendETH(TransactionETHMeta meta, String transaction){
        //TODO make all staff here

        isLoading.setValue(true);
//        String addressTo = viewModel.getReceiverAddress().getValue();
//        Wallet wallet = viewModel.getWallet().isMultisig() ?
//                RealmManager.getAssetsDao().getMultisigLinkedWallet(viewModel.getWallet().getMultisigWallet().getOwners()) : viewModel.getWallet();

        try {
//            final String txHex = viewModel.transaction.getValue();
            isLoading.setValue(true);
            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(
                    meta.getWallet().getCurrencyId(),                 //Currency ID
                    meta.getWallet().getNetworkId(),                  //Network ID
                    new HdTransactionRequestEntity.Payload(
                            "",
                            0,
                            meta.getWallet().getIndex(),
                            transaction.startsWith("0x") ? transaction : "0x" + transaction,
                            false)
                    )
            ).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        success.postValue(true);
                    } else {
//                        Analytics.getInstance(getActivity()).logError(AnalyticsConstants.ERROR_TRANSACTION_API);
                        try {
                            String errorBody = response.errorBody() == null ? "" : response.errorBody().string();
                            errorMessage.setValue(errorBody);
                            success.postValue(false);
//                            if (response.code() == 406 && getContext() != null) {
//                                Analytics.getInstance(getContext()).logEvent(TAG, "406", errorBody);
//                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            errorMessage.setValue("Something went wrong :(");
                            success.postValue(false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                    //{"code":200,"message":"d537ac25da78cf85a34eba6396bedf8f9bb7db5e13662773c350497a1c0e5094"}
                    //{"code":200,"message":{"message":"0x78f5d5a0931166045bd878c81cce267291f410c57c61afa5c10bb0f1d8b67880"}}
                    t.printStackTrace();
                    errorMessage.postValue(t.getMessage());
                    success.postValue(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.postValue(e.getMessage());
            success.postValue(false);
        }
    }

    private void sendBTC(TransactionBTCMeta meta, String transaction){
        try {
            isLoading.setValue(true);
            //THIS is valid for BTC
            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(
                            meta.getWallet().getCurrencyId(),                                               //Currency ID
                            meta.getWallet().getNetworkId(),                                                //Network ID
                            new HdTransactionRequestEntity.Payload(
                                    meta.getChangeAddress(),
                                    meta.getWallet().getAddresses().size(),                                 //Address Index
                                    meta.getWallet().getIndex(),                                            //Wallet Index
                                    transaction)
                    )
            ).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        success.postValue(true);
                    } else {
//                        Analytics.getInstance(getActivity()).logError(AnalyticsConstants.ERROR_TRANSACTION_API);
                        try {
                            String errorBody = response.errorBody() == null ? "" : response.errorBody().string();
                            errorMessage.postValue(errorBody);
                            success.postValue(false);
//                            if (response.code() == 406 && getContext() != null) {
//                                Analytics.getInstance(getContext()).logEvent(TAG, "406", errorBody);
//                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            errorMessage.postValue("Something went wrong :(");
                            success.postValue(false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                    //{"code":200,"message":"d537ac25da78cf85a34eba6396bedf8f9bb7db5e13662773c350497a1c0e5094"}
                    t.printStackTrace();
                    errorMessage.postValue(t.getMessage());
                    success.postValue(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.postValue(e.getMessage());
            success.postValue(false);
        }
    }



    /**
     * Do not touch this
     * this method will be called from JNI automatically while makeTransaction is processing
     *
     * @param amount transactionPrice.
     */
    public static void setTransactionPrice(String amount) {
        long changeAmountSatoshi = Long.valueOf(amount);
        transactionPrice.setValue(changeAmountSatoshi);
    }

    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public int getChainId() {
        return 1;
    }

    public boolean haveFromCryptoRate(){
        if (sendERC20Token.getValue()!= null){
            return false;
        } else {
            return haveFiatRate(payFromWallet.getValue().getCurrencyId());
        }

    }

    public boolean haveToCryptoRate(){
        return haveFiatRate(receiveToWallet.getValue().getCurrencyId());
    }

    private boolean haveFiatRate(int blockchainID){
        double rate = RealmManager.getSettingsDao().getCurrenciesRateById(blockchainID);

        return rate == 0 ? false : true;
    }

    public MutableLiveData<Double> getMinAmount(){
        return minFromAmount;
    }

    public boolean canSpendAmount(double amount){
        switch (payFromWallet.getValue().getCurrencyName()){
            case Constants.BTC:
                return payFromWallet.getValue().getBtcDoubleValue() >= amount ? true : false;

            case Constants.ETH:
                if (sendERC20Token.getValue()!=null){
                    return Double.parseDouble(sendERC20Token.getValue().getBalance()) >= amount ? true : false;
                } else {
                    return payFromWallet.getValue().getEthAvailableValue().doubleValue() >= amount ? true : false;
                }


            default:
                //TODO update this logic for tokens
//                return payFromWallet.getValue().getTokenAmount >= amount ? true : false;
                return true;

        }
    }


    private String getPayFromAssetName(){
        return sendERC20Token.getValue() !=  null ? sendERC20Token.getValue().getName().toLowerCase() : payFromWallet.getValue().getCurrencyName().toLowerCase();
    }


}
