/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


public class Fee {

//    private String time;
    private String name;
    private long amount;
//    private int blockCount;
    private boolean isSelected = false;

    public Fee(String name, long amount/*, int blockCount, String time*/) {
        this.name = name;
        this.amount = amount;
//        this.blockCount = blockCount;
//        this.time = time;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

//    public String getTime() {
//        return time;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

//    public int getBlockCount() {
//        return blockCount;
//    }

//    public void setBlockCount(int blockCount) {
//        this.blockCount = blockCount;
//    }
}
