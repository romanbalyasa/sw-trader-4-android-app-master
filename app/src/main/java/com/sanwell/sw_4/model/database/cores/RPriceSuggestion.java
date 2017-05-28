package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 10/06/16.
 */
public class RPriceSuggestion extends RealmObject {

    private String id;
    private String clientId;
    private String npgId;
    private String priceId;

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public String getNpgId() {
        return npgId;
    }

    public void setNpgId(String npgId) {
        this.npgId = npgId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
