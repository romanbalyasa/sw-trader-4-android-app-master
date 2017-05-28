package com.sanwell.sw_4.model.database.cores;

import com.sanwell.sw_4.model.SanwellApplication;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 16/12/15.
 */

public class RImageUrl extends RealmObject {

    private String url;
    private String itemID;

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getUrl() {
        return url;
    }

    public static String getFileUrlForImage(RImageUrl rImageUrl) {
        if (rImageUrl == null || rImageUrl.getItemID() == null) {
            return null;
        }
        return SanwellApplication.applicationContext.getFilesDir() + "/"
                + rImageUrl.getItemID() + ".jpg";
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
