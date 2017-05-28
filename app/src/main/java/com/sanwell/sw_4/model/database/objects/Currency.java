package com.sanwell.sw_4.model.database.objects;

import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RCurrency;

import java.util.Locale;

/*
 * Created by Roman Kyrylenko on 08/02/16.
 */
public class Currency {

    public static final String ID_COLUMN = "id";

    private String id, name, iso;
    private double rate = 0.0;
    private int roundDigits;

    public Currency(String currencyID) {
        RCurrency rCurrency = DataModel.getInstance().realm
                .where(RCurrency.class)
                .equalTo(ID_COLUMN, currencyID)
                .findFirst();
        if (rCurrency == null) {
            return;
        }
        name = rCurrency.getName();
        iso = rCurrency.getIso();
        rate = rCurrency.getRate();
        roundDigits = rCurrency.getRoundDigits();
    }

    public double specialRound(double value, String currencyID) {
        Currency currency = new Currency(currencyID);
        return round(value * currency.rate, roundDigits) / rate;
    }

    public double defaultRound(double value, String currencyID) {
        Currency currency = new Currency(currencyID);
        return round(value * currency.rate, 3) / rate;
    }

    public double defaultRound(String value, String currencyID) {
        double val = 0;
        try {
            val = Double.parseDouble(value);
        } catch (Exception ignored) {}
        return defaultRound(val, currencyID);
    }

    public String sDefaultRound(String vale, String currencyID) {
        double v = defaultRound(vale, currencyID);
        return String.format(Locale.US, "%.3f", v);
    }

    public String sDefaultRound(double vale, String currencyID) {
        double v = defaultRound(vale, currencyID);
        return String.format(Locale.US, "%.3f", v);
    }

    public String sDefaultRound(double vale) {
        return String.format(Locale.US, "%.2f", vale);
    }

    public Currency(RCurrency currency) {
        if (currency == null) {
            return;
        }
        id = currency.getId();
        name = currency.getName();
        iso = currency.getIso();
        rate = currency.getRate();
        roundDigits = currency.getRoundDigits();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIso() {
        return iso;
    }

    public Double getRate() {
        return rate;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
