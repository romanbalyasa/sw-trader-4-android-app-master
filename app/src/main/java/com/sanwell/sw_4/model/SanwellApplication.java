package com.sanwell.sw_4.model;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.sanwell.sw_4.controller.activities.CatalogueActivity;
import com.sanwell.sw_4.controller.activities.OrderListActivity;
import com.sanwell.sw_4.model.database.DataModel;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/*
 * Created by Roman Kyrylenko on 08/02/16.
 */
public class SanwellApplication extends Application {

    public static Context applicationContext = null;
    public static CatalogueActivity catalogueActivity;
    public static OrderListActivity orderListActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Fabric.with(this, new Crashlytics());
        RealmConfiguration rConfig = new RealmConfiguration.Builder(applicationContext)
                .name("default.realm")
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(rConfig);
        DataModel.getInstance();
    }

}