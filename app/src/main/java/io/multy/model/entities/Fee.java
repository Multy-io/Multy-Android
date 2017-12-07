/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities;


public class Fee {

    private String time;
    private String cost;
    private double amount;
    private boolean isSelected;

    public Fee(String time, String cost, double amount, boolean isSelected) {
        this.time = time;
        this.cost = cost;
        this.amount = amount;
        this.isSelected = isSelected;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fee fee = (Fee) o;

        if (Double.compare(fee.amount, amount) != 0) return false;
        if (isSelected != fee.isSelected) return false;
        if (time != null ? !time.equals(fee.time) : fee.time != null) return false;
        return cost != null ? cost.equals(fee.cost) : fee.cost == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = time != null ? time.hashCode() : 0;
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Fee{" +
                "time='" + time + '\'' +
                ", cost='" + cost + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
