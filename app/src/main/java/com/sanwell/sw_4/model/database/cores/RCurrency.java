package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 08/02/16.
 */
public class RCurrency extends RealmObject {

    private String id, name, iso;
    private double rate;
    private int roundDigits;

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getRoundDigits() {
        return roundDigits;
    }

    public void setRoundDigits(int roundDigits) {
        this.roundDigits = roundDigits;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
