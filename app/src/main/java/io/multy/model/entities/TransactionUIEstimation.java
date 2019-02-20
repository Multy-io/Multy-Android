package io.multy.model.entities;

public class TransactionUIEstimation {
    private String fromCryptoValue;
    private String toCtyptoValue;
    private String fromFiatValue;
    private String toFiatValue;

    public String getFromCryptoValue() {return this.fromCryptoValue;}
    public String getToCtyptoValue() {return this.toCtyptoValue;}
    public String getFromFiatValue() {return this.fromFiatValue;}

    public void setFromCryptoValue(String value) {this.fromCryptoValue = value;}
    public void setToCtyptoValue(String value) {this.toCtyptoValue = value;}
    public void setFromFiatValue(String value) {this.fromFiatValue = value;}
}
