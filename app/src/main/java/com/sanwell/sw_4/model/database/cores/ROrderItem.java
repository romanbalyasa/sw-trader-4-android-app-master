package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 12/02/16.
 */
public class ROrderItem extends RealmObject {

    private String itemID;
    private double itemsCount;

    public double getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(double itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String content) {
        this.itemID = content;
    }
}
