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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.multy.Multy;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.Estimation;
import io.multy.model.entities.ExchangeAsset;
import io.multy.model.entities.ExchangePair;
import io.multy.model.entities.Fee;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.responses.ExchangeCurrenciesListResponse;
import io.multy.model.responses.ExchangePairResponse;
import io.multy.model.responses.FeeRateResponse;
import io.multy.model.responses.ServerConfigResponse;
import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.multy.util.SingleLiveEvent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
//    private MutableLiveData<Boolean> hasRateFromCrypto = new MutableLiveData<>();
//    private MutableLiveData<Boolean> hasRateToCrypto = new MutableLiveData<>();

    //TODO Check if this data is needed

    public MutableLiveData<FeeRateResponse.Speeds> speeds = new MutableLiveData<>();
    public MutableLiveData<Estimation> estimation = new MutableLiveData<>();
    public MutableLiveData<Fee> fee = new MutableLiveData<>();
    public MutableLiveData<String> gasLimit = new MutableLiveData<>();

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

    private String changeAddress;
    private String donationAddress;
    private byte[] seed;
    private String signTransactionError;
    private Handler handler = new Handler();




    public ExchangeViewModel() {
//        currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();
        fragmentHolder.setValue(0);
        getSupportTokens();
    }

    private void mapAssets(){
        List<ExchangeAsset> assetsFull = new ArrayList<>();

        List<ServerConfigResponse.ERC20TokenSupport> tokens = supportTokens.getValue();
        List<String> rawAssets = assetsList.getValue();


        String fromWalletChain = payFromWallet.getValue().getCurrencyName().toLowerCase();


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

//        for(ServerConfigResponse.ERC20TokenSupport token : tokens){
//            for (String asset : rawAssets){
//                if (asset.toLowerCase().equals(token.getName().toLowerCase())){
//                    if (!token.getSmartContractAddress().isEmpty()){
//
//                        String url = "https://raw.githubusercontent.com/Multy-io/tokens/master/images/"+ token.getSmartContractAddress().toLowerCase() + ".png";
//
//                        ExchangeAsset exchangeAsset = new ExchangeAsset(token.getName(), token.getFullName(), 60, url);
//                        assetsFull.add(exchangeAsset);
//                    }
//
//                }
//            }
//        }
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
                    Log.d("EXCHANGE VM", "GOTED PAYING TO ADDRESS:" + response.body().getPayToAddress());
                    Log.d("EXCHANGE VM", "GOTED RECEIVE TO ADDRESS:" + response.body().getReceiveToAddress());
                }
            }

            @Override
            public void onFailure(Call<ExchangePairResponse> call, Throwable t) {

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

        String from = payFromWallet.getValue().getCurrencyName();
        String to = asset.getName();
        getExchangePair(new ExchangePair(from, to, 1f));

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

    public void setPayFromWallet(Wallet wallet) {
        payFromWalletId = wallet.getId();
        this.payFromWallet.setValue(wallet);
    }

//    public Wallet getWallet() {
//        if (wallet.getValue() == null || !wallet.getValue().isValid()) {
//            setWallet(RealmManager.getAssetsDao().getWalletById(walletId));
//        }
//        return this.wallet.getValue();
//    }

    public void setFee(Fee fee) {
        this.fee.setValue(fee);
    }

    public Fee getFee() {
        return fee.getValue();
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public BigDecimal getAmountFull() {
        return new BigDecimal(amount);
    }

    public MutableLiveData<Wallet> getPayFromWallet(){
        return this.payFromWallet;
    }

//    public MutableLiveData<String> getReceiverAddress() {
//        return receiverAddress;
//    }
//
//    public void setReceiverAddress(String receiverAddress) {
//        this.receiverAddress.setValue(receiverAddress);
//    }

    public String getDonationAmount() {
        return donationAmount;
    }

    public String getDonationSatoshi() {
        if (getDonationAmount() != null) {
            return String.valueOf((long) (Double.valueOf(getDonationAmount()) * Math.pow(10, 8)));
        } else {
            return "0";
        }
    }

    public void setDonationAmount(String donationAmount) {
        this.donationAmount = donationAmount;
    }

    public boolean isPayForCommission() {
        return isPayForCommission;
    }

    public void setPayForCommission(boolean payForCommission) {
        isPayForCommission = payForCommission;
    }

    public boolean isAmountScanned() {
        return isAmountScanned;
    }

    public void setAmountScanned(boolean amountScanned) {
        isAmountScanned = amountScanned;
    }

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

//    public void scheduleUpdateTransactionPrice(long amount) {
//        final int walletIndex = getWallet().getIndex();
//        final long feePerByte = getFee().getAmount();
//
//        if (handler != null) {
//            handler.removeCallbacksAndMessages(null);
//        }
//
//        if (seed == null) {
//            seed = RealmManager.getSettingsDao().getSeed().getSeed();
//        }
//
//        if (changeAddress == null) {
//            try {
//                changeAddress = NativeDataHelper.makeAccountAddress(seed, walletIndex, getWallet().getBtcWallet().getAddresses().size(),
//                        getWallet().getCurrencyId(), getWallet().getNetworkId());
//            } catch (JniException e) {
//                e.printStackTrace();
//                errorMessage.setValue("Error creating change address " + e.getMessage());
//            }
//        }
//
//        if (getWallet().getNetworkId() == NativeDataHelper.NetworkId.MAIN_NET.getValue()) {
//            donationAddress = RealmManager.getSettingsDao().getDonationAddress(Constants.DONATE_WITH_TRANSACTION);
//        } else {
//            donationAddress = Constants.DONATION_ADDRESS_TESTNET;
//        }
//
//        if (donationAddress == null) {
//            donationAddress = "";
//        }
//
//        handler.postDelayed(() -> {
//            try {
////                Log.i("wise", getWallet().getId() + " " + getWallet().getNetworkId() + " " + amount + " " + getFee().getAmount() + " " + getDonationSatoshi() + " " + isPayForCommission);
//                //important notice - native makeTransaction() method will update UI automatically with correct transaction price
//                byte[] transactionHex = NativeDataHelper.makeTransaction(getWallet().getId(), getWallet().getNetworkId(),
//                        seed, walletIndex, String.valueOf(amount),
//                        String.valueOf(getFee().getAmount()), getDonationSatoshi(),
//                        getReceiverAddress().getValue(), changeAddress, donationAddress, isPayForCommission);
//            } catch (JniException e) {
//                e.printStackTrace();
//                signTransactionError = e.getMessage();
//            }
//        }, 300);
//    }


    //TODO This code we can use for make Exchange!!!!
//    public void signTransaction() {
//        try {
////            Log.i("wise", getWallet().getId() + " " + getWallet().getNetworkId() + " " + amount + " " + getFee().getAmount() + " " + getDonationSatoshi() + " " + isPayForCommission);
//            byte[] transactionHex = NativeDataHelper.makeTransaction(getWallet().getId(), getWallet().getNetworkId(),
//                    seed, getWallet().getIndex(), String.valueOf(CryptoFormatUtils.btcToSatoshi(String.valueOf(String.valueOf(amount)))),
//                    String.valueOf(getFee().getAmount()), getDonationSatoshi(),
//                    getReceiverAddress().getValue(), changeAddress, donationAddress, isPayForCommission);
//            transaction.setValue(byteArrayToHex(transactionHex));
//        } catch (JniException e) {
//            errorMessage.setValue(Multy.getContext().getString(R.string.invalid_entered_sum));
//            e.printStackTrace();
//        }
//    }
//
//    public void signTransactionEth() {
//        try {
//            String signAmount;
//            if (getWallet().isMultisig()) {
//                signAmount = CryptoFormatUtils.ethToWei(String.valueOf(isPayForCommission ?
//                        amount : (amount - EthWallet.getTransactionMultisigPrice(getFee().getAmount(), Long.parseLong(estimation.getValue().getSubmitTransaction())))));
//            } else {
//                signAmount = CryptoFormatUtils.ethToWei(String.valueOf(isPayForCommission ?
//                        amount : (amount - EthWallet.getTransactionPrice(getFee().getAmount()))));
//            }
////            Log.i("wise", getWallet().getId() + " " + getWallet().getNetworkId() + " " + amount + " " + getFee().getAmount() + " " + getDonationSatoshi() + " " + isPayForCommission);
//            byte[] tx;
//            TransactionResponse txResult = null;
//            if (wallet.getValue().isMultisig()) {
//                Wallet linkedWallet = RealmManager.getAssetsDao().getMultisigLinkedWallet(wallet.getValue().getMultisigWallet().getOwners());
//                double price = EthWallet.getTransactionMultisigPrice(getFee().getAmount(), Long.parseLong(estimation.getValue().getSubmitTransaction()));
//                if (linkedWallet.getAvailableBalanceNumeric().compareTo(new BigDecimal(CryptoFormatUtils.ethToWei(price))) < 0) {
//                    errorMessage.setValue(Multy.getContext().getString(R.string.not_enough_linked_balance));
//                    return;
//                }
//                if (linkedWallet.shouldUseExternalKey()) {
//                    WalletPrivateKey privateKey = RealmManager.getAssetsDao()
//                            .getPrivateKey(linkedWallet.getActiveAddress().getAddress(), linkedWallet.getCurrencyId(), linkedWallet.getNetworkId());
//                    tx = NativeDataHelper.makeTransactionMultisigETHFromKey(
//                            privateKey.getPrivateKey(),
//                            linkedWallet.getCurrencyId(),
//                            linkedWallet.getNetworkId(),
//                            linkedWallet.getActiveAddress().getAmountString(),
//                            getWallet().getActiveAddress().getAddress(),
//                            signAmount,
//                            receiverAddress.getValue(),
//                            //TODO please test this solution!
////                            estimation.getValue().getSubmitTransaction(),
//                            String.valueOf(fee.getValue().getGasLimit()),
//                            String.valueOf(fee.getValue().getAmount()),
//                            linkedWallet.getEthWallet().getNonce());
//                } else {
//                    tx = NativeDataHelper.makeTransactionMultisigETH(
//                            RealmManager.getSettingsDao().getSeed().getSeed(),
//                            linkedWallet.getIndex(),
//                            0,
//                            linkedWallet.getCurrencyId(),
//                            linkedWallet.getNetworkId(),
//                            linkedWallet.getActiveAddress().getAmountString(),
//                            getWallet().getActiveAddress().getAddress(),
//                            signAmount,
//                            getReceiverAddress().getValue(),
//                            //TODO please test this solution!
////                            estimation.getValue().getSubmitTransaction(),
//                            String.valueOf(fee.getValue().getGasLimit()),
//                            String.valueOf(fee.getValue().getAmount()),
//                            linkedWallet.getEthWallet().getNonce());
//                }
//            } else if (wallet.getValue().shouldUseExternalKey()) {
//                WalletPrivateKey keyObject = RealmManager.getAssetsDao()
//                        .getPrivateKey(getWallet().getActiveAddress().getAddress(), getWallet().getCurrencyId(), getWallet().getNetworkId());
//                tx = NativeDataHelper.makeTransactionETHFromKey(
//                        keyObject.getPrivateKey(),
//                        getWallet().getCurrencyId(),
//                        getWallet().getNetworkId(),
//                        getWallet().getActiveAddress().getAmountString(),
//                        signAmount, getReceiverAddress().getValue(),
//                        //TODO please test this solution
////                        gasLimit.getValue(),
//                        String.valueOf(fee.getValue().getGasLimit()),
//                        String.valueOf(fee.getValue().getAmount()),
//                        getWallet().getEthWallet().getNonce());
//            } else {
//                //now see some true magic. lost two days for that sh8t
//                final int walletIndex = Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? getWallet().getActiveAddress().getIndex() : getWallet().getIndex();
//                final int addressIndex = Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? getWallet().getIndex() : getWallet().getActiveAddress().getIndex();
//
//                final String key = NativeDataHelper.getMyPrivateKey(RealmManager.getSettingsDao().getSeed().getSeed(), walletIndex, addressIndex,
//                        NativeDataHelper.Blockchain.ETH.getValue(), Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue() : wallet.getValue().getNetworkId());
//
////                final String key2 = NativeDataHelper.getMyPrivateKey(RealmManager.getSettingsDao().getSeed().getSeed(), getWallet().getIndex(),
////                        getWallet().getNetworkId(), NativeDataHelper.Blockchain.ETH.getValue(), wallet.getValue().getNetworkId());
//
//                TransactionBuilder builder =
//                        new TransactionBuilder(
//                                NativeDataHelper.Blockchain.ETH.getName(),
//                                getWallet().getNetworkId(),
//                                new Account(Account.ACCOUNT_TYPE_DEFAULT, key),
//                                new Builder(Builder.TYPE_BASIC,
//                                        new Payload(getWallet().getActiveAddress().getAmountString(),
//                                                getReceiverAddress().getValue(), signAmount)), new Transaction(getWallet().getEthWallet().getNonce(),
//                                new io.multy.model.core.Fee(
//                                        //TODO please test this solution
////                                        String.valueOf(fee.getValue().getAmount()), gasLimit.getValue()))
//                                        String.valueOf(fee.getValue().getAmount()), String.valueOf(fee.getValue().getGasLimit())))
//                        );
//
//                String json = new Gson().toJson(builder);
//                tx = null;
//                txResult = new Gson().fromJson(NativeDataHelper.makeTransactionJSONAPI(json), TransactionResponse.class);
//
//
////                tx = NativeDataHelper.makeTransactionETH(
////                        RealmManager.getSettingsDao().getSeed().getSeed(),
////                        getWallet().getIndex(),
////                        0,
////                        wallet.getValue().getCurrencyId(),
////                        Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue() : wallet.getValue().getNetworkId(),
////                        getWallet().getActiveAddress().getAmountString(),
////                        signAmount,
////                        getReceiverAddress().getValue(),
////                        gasLimit.getValue(),
////                        String.valueOf(fee.getValue().getAmount()),
////                        getWallet().getEthWallet().getNonce());
//            }
//            transaction.setValue(tx == null ? txResult.getTransactionHex() : byteArrayToHex(tx));
//        } catch (JniException e) {
//            errorMessage.setValue(Multy.getContext().getString(R.string.invalid_entered_sum));
//            e.printStackTrace();
//        }
//    }





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
        return haveFiatRate(payFromWallet.getValue().getCurrencyId());
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
        boolean out = false;
        switch (payFromWallet.getValue().getCurrencyName()){
            case Constants.BTC:
                c
                return payFromWallet.getValue().getBtcDoubleValue() >= amount ? true : false;

            case Constants.ETH:
                return payFromWallet.getValue().getEthAvailableValue().doubleValue() >= amount ? true : false;

            default:
                //TODO update this logic for tokens
//                return payFromWallet.getValue().getTokenAmount >= amount ? true : false;
                return true;

        }
    }
}
