package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 19/02/16.
 */
public class ROrderBatchInfo extends RealmObject {

    private String orderID;
    private double itemsCount;
    private double overheadItemsCount;
    private double itemPrice;
    private String itemID;
    private String itemCurrencyID;
    private String batchID;
    private String origBatchId;
    private String priceCode;
    private String suggestedPriceCode;

    public String getSuggestedPriceCode() {
        return suggestedPriceCode;
    }

    public void setSuggestedPriceCode(String suggestedPriceCode) {
        this.suggestedPriceCode = suggestedPriceCode;
    }

    public String getOrigBatchId() {
        return origBatchId;
    }

    public void setOrigBatchId(String origBatchId) {
        this.origBatchId = origBatchId;
    }

    public String getItemCurrencyID() {
        return itemCurrencyID;
    }

    public void setItemCurrencyID(String itemCurrencyID) {
        this.itemCurrencyID = itemCurrencyID;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public double getOverheadItemsCount() {
        return overheadItemsCount;
    }

    public void setOverheadItemsCount(double overheadItemsCount) {
        this.overheadItemsCount = overheadItemsCount;
    }

    public double getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(double itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getPriceCode() {
        return priceCode;
    }

    public void setPriceCode(String priceCode) {
        this.priceCode = priceCode;
    }
}
