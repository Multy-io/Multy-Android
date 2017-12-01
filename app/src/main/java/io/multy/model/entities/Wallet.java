/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


public abstract class Wallet {

    private String name;
    private String address;
    private double balance;

    public Wallet(String name, String address, double balance) {
        this.name = name;
        this.address = address;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public abstract String getBalanceWithCode();

    @Override
    public String toString() {
        return "Wallet{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", balance=" + balance +
                '}';
    }
}
