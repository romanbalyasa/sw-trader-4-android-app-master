package com.sanwell.sw_4.model.database.objects;

/*
 * Created by Roman Kyrylenko on 15/02/16.
 */
public class ItemPriceInfo {

    private String priceCode;
    private String suggestedPriceCode;
    private String price, name, sale, currency;
    public ItemPriceInfo(String price, String currency, String name, String sale, String priceCode, String suggestedPriceCode) {
        this.price = price;
        this.name = name;
        this.sale = sale;
        this.currency = currency;
        this.priceCode = priceCode;
        this.suggestedPriceCode = suggestedPriceCode;
    }

    public String getSuggestedPriceCode() {
        return suggestedPriceCode;
    }

    public String getPriceCode() {
        return priceCode;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getSale() {
        return sale;
    }

}
