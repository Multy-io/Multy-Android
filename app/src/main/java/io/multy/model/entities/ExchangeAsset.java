/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;

public class ExchangeAsset {
    private String name;
    private String fullName;
    private int chainId;
    private boolean isSelected;
    private String logo;


    public ExchangeAsset(String name, String fullName, int chainId, String logo){
        this.name = name;
        this.fullName = fullName;
        this.chainId = chainId;
        this.logo = logo;
        this.isSelected = false;
    }
    public void setName(String name){this.name = name;}
    public void setFullName(String fullName){this.fullName = name;}
    public void setChainId(int chainId){this.chainId = chainId;}
    public void setSelected(boolean selection){this.isSelected = selection;}
    public void setLogo(String url){this.logo = url;}

    public String getName(){return this.name;}
    public String getFullName(){return this.fullName;}
    public int getChainId(){return this.chainId;}
    public boolean isSelected(){return this.isSelected;}
    public String getLogo(){return this.logo;}
}
