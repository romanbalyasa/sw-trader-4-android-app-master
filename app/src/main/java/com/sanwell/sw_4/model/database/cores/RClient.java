package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 08/02/16.
 */
public class RClient extends RealmObject {

    public static final String ID_ROW = "id";

    private String id, name, currencyID;
    private String comment;
    private String debtString;

    public String getDebtString() {
        return debtString;
    }

    public void setDebtString(String debtString) {
        this.debtString = debtString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
