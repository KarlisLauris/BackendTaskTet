package org.example.domain;

public class Conversion {
    private String date;
    private String currency;
    private String conversionRate;

    public Conversion(String date, String currency, String conversionRate) {
        this.currency = currency;
        this.conversionRate = conversionRate;
        this.date = date;
    }
    public Conversion(String currency, String conversionRate) {
        this.currency = currency;
        this.conversionRate = conversionRate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(String conversionRate) {
        this.conversionRate = conversionRate;
    }


    public String getDate() {
        return date;
    }



    @Override
    public String toString() {
        return "Conversion{" +
                "currency='" + currency + '\'' +
                ", conversionRate='" + conversionRate + '\'' +
                '}';
    }
}
