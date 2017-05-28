package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 11/02/16.
 */
public class RDebt extends RealmObject {

    private String clientID;
    private String currencyID;
    private String debt;
    private String prepayment;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }

    public String getDebt() {
        return debt;
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public String getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(String prepayment) {
        this.prepayment = prepayment;
    }
}
