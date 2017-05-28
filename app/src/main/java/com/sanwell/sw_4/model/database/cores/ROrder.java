package com.sanwell.sw_4.model.database.cores;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 12/02/16.
 */
public class ROrder extends RealmObject {

    public static final String CLIENT_ID_ROW = "clientID";

    private String orderID;
    private String clientID;
    private String openedIn;
    private Date openningDate;
    private String currencyID;
    private String comment;
    private boolean isOpen;
    private int numberOfItems = 0;
    private RealmList<ROrderItem> items = new RealmList<>();

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }

    public Date getOpenningDate() {
        return openningDate;
    }

    public void setOpenningDate(Date openningDate) {
        this.openningDate = openningDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getOpenedIn() {
        return openedIn;
    }

    public void setOpenedIn(String openedIn) {
        this.openedIn = openedIn;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public RealmList<ROrderItem> getItems() {
        return items;
    }

    public void setItems(RealmList<ROrderItem> items) {
        this.items = items;
    }

}
