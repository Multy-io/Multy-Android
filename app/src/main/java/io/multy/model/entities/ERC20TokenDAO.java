package io.multy.model.entities;

import io.multy.model.entities.wallet.Wallet;

public class ERC20TokenDAO {
    private String balance;
    private String name;
    private int decimals;
    private String contractAddress;
    private Wallet parentWallet;
    private long parentWalletID;
    private String logo;
    private String rate;

    public ERC20TokenDAO(String name, String logo, String contractAddress, int decimals, String balance, long parentWalletID, String rate){
        this.name = name;
        this.logo = logo;
        this.contractAddress = contractAddress;
        this.decimals = decimals;
        this.balance = balance;
        this.parentWalletID = parentWalletID;
        this.rate = rate;
    }

    public String getName() {return this.name;}
    public String getLogo() {return this.logo;}
    public String getContractAddress() {return this.contractAddress;}
    public int getDecimals() {return this.decimals;}
    public String getBalance() {return this.balance;}
    public long getParentWalletID() {return this.parentWalletID;}
    public String getRate() {return this.rate;}


}
