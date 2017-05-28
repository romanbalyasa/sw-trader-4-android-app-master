package com.sanwell.sw_4.model.database.objects;

import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.cores.RImageUrl;

import java.io.File;

/*
 * Created by Roman Kyrylenko on 10/02/16.
 */
public class ImageUrl {

    public static final String ITEM_ID_COLUMN = "itemID";

    private String url;
    private String fileUrl;

    public ImageUrl(RImageUrl imageUrl) {
        if (imageUrl != null) {
            url = imageUrl.getUrl();
            String filename = imageUrl.getItemID() + ".jpg";
            fileUrl = SanwellApplication.applicationContext.getFilesDir() + "/"
                    + filename;
        }
    }

    public String getUrl() {
        if (new File(fileUrl).exists()) {
            return fileUrl;
        }
        return url;
    }

}
