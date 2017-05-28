package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 08/02/16.
 */
public class RItem extends RealmObject {

    public static final String ID_ROW = "id";

    private String id, parent, title;
    private String measurement, packaging, currencyID, price;
    private String stockCount;
    private int pickedImageThumb;
    private String m2_price, m_m5_price, m_m4_price, m_m3_price;
    private String m_m2_price;
    private String m_price;
    private String a_price;
    private String a_p2_price;
    private double multFactor;
    private String a_a5_price;
    private String npgId;
    private String b_price, retail_price, c_price;
    private boolean isFixedCurrency, isPromoted, isNew, isFocused, isPricedUp;
    private boolean hasPicture = false;
    private int minOrder;

    public int getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(int minOrder) {
        this.minOrder = minOrder;
    }

    public String getNpgId() {
        return npgId;
    }

    public void setNpgId(String npgId) {
        this.npgId = npgId;
    }

    public double getMultFactor() {
        return multFactor;
    }

    public void setMultFactor(double multFactor) {
        this.multFactor = multFactor;
    }

    public int getPickedImageThumb() {
        return pickedImageThumb;
    }

    public void setPickedImageThumb(int pickedImageThumb) {
        this.pickedImageThumb = pickedImageThumb;
    }

    public boolean isFixedCurrency() {
        return isFixedCurrency;
    }

    public void setIsFixedCurrency(boolean isFixedCurrency) {
        this.isFixedCurrency = isFixedCurrency;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setIsPromoted(boolean isPromoted) {
        this.isPromoted = isPromoted;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setIsFocused(boolean isFocused) {
        this.isFocused = isFocused;
    }

    public boolean isPricedUp() {
        return isPricedUp;
    }

    public void setIsPricedUp(boolean isPricedUp) {
        this.isPricedUp = isPricedUp;
    }

    public boolean isHasPicture() {
        return hasPicture;
    }

    public void setHasPicture(boolean hasPicture) {
        this.hasPicture = hasPicture;
    }

    public String getC_price() {
        return c_price;
    }

    public void setC_price(String c_price) {
        this.c_price = c_price;
    }

    public String getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(String retail_price) {
        this.retail_price = retail_price;
    }

    public String getB_price() {
        return b_price;
    }

    public void setB_price(String b_price) {
        this.b_price = b_price;
    }

    public String getA_a5_price() {
        return a_a5_price;
    }

    public void setA_a5_price(String a_a5_price) {
        this.a_a5_price = a_a5_price;
    }

    public String getA_p2_price() {
        return a_p2_price;
    }

    public void setA_p2_price(String a_p2_price) {
        this.a_p2_price = a_p2_price;
    }

    public String getA_price() {
        return a_price;
    }

    public void setA_price(String a_price) {
        this.a_price = a_price;
    }

    public String getM_price() {
        return m_price;
    }

    public void setM_price(String m_price) {
        this.m_price = m_price;
    }

    public String getM_m2_price() {
        return m_m2_price;
    }

    public void setM_m2_price(String m_m2_price) {
        this.m_m2_price = m_m2_price;
    }

    public String getM_m3_price() {
        return m_m3_price;
    }

    public void setM_m3_price(String m_m3_price) {
        this.m_m3_price = m_m3_price;
    }

    public String getM_m4_price() {
        return m_m4_price;
    }

    public void setM_m4_price(String m_m4_price) {
        this.m_m4_price = m_m4_price;
    }

    public String getM_m5_price() {
        return m_m5_price;
    }

    public void setM_m5_price(String m_m5_price) {
        this.m_m5_price = m_m5_price;
    }

    public String getM2_price() {
        return m2_price;
    }

    public void setM2_price(String m2_price) {
        this.m2_price = m2_price;
    }

    public String getStockCount() {
        return stockCount;
    }

    public void setStockCount(String stockCount) {
        this.stockCount = stockCount;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
