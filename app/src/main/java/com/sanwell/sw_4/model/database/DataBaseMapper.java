package com.sanwell.sw_4.model.database;

import android.content.Context;

import com.sanwell.sw_4.model.ServerCommunicator;
import com.sanwell.sw_4.model.database.cores.RBatch;
import com.sanwell.sw_4.model.database.cores.RClient;
import com.sanwell.sw_4.model.database.cores.RCurrency;
import com.sanwell.sw_4.model.database.cores.RDebt;
import com.sanwell.sw_4.model.database.cores.RGroup;
import com.sanwell.sw_4.model.database.cores.RImageUrl;
import com.sanwell.sw_4.model.database.cores.RInvoice;
import com.sanwell.sw_4.model.database.cores.RItem;
import com.sanwell.sw_4.model.database.cores.RItemPlanInfo;
import com.sanwell.sw_4.model.database.cores.RPriceSuggestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 08/02/16.
 */
public class DataBaseMapper {

    private Context context;
    private Realm realm;

    public DataBaseMapper(Context context, Realm realm) {
        this.realm = realm;
        this.context = context;
    }

    public void readURLS(String json) {
        ArrayList<RImageUrl> urls = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                RImageUrl imageUrl = new RImageUrl();
                imageUrl.setUrl(object.getString("link"));
                String id = object.getString("title");
                if (id.contains("_")) {
                    id = id.substring(0, id.indexOf('_'));
                } else if (id.contains(".")) {
                    id = id.substring(0, id.indexOf('.'));
                }
                imageUrl.setItemID(id);
                urls.add(imageUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            realm.beginTransaction();
            realm.copyToRealm(urls);
        } finally {
            realm.commitTransaction();
        }
    }

    public Realm map() {
        realm.close();
        Realm.deleteRealm(realm.getConfiguration());
        realm = DataModel.createRealm();
        SQLDataReader dataModel = new SQLDataReader(context,
                ServerCommunicator.DATABASE_FILE_NAME,
                context.getFilesDir().getAbsolutePath(), null, 1);
        try {

            realm.beginTransaction();
            ArrayList<RClient> clients = dataModel.readClients();
            safeCopyToRealm(clients);
            realm.commitTransaction();
            clients.clear();

            realm.beginTransaction();
            ArrayList<RCurrency> currencies = dataModel.readCurrencies();
            safeCopyToRealm(currencies);
            realm.commitTransaction();
            currencies.clear();

            ArrayList<RGroup> groups = new ArrayList<>();
            HashMap<String, RItem> itemHashMap = dataModel.readGoods();
            List<String> itemsIds = new ArrayList<>(itemHashMap.keySet());

            realm.beginTransaction();
            dataModel.readGroups(groups, itemHashMap);
            safeCopyToRealm(groups);
            safeCopyToRealm(itemHashMap.values());
            realm.commitTransaction();
            groups.clear();
            itemHashMap.clear();

            realm.beginTransaction();
            ArrayList<RGroup> planGroups = dataModel.readPlanGroups();
            safeCopyToRealm(planGroups);
            realm.commitTransaction();
            planGroups.clear();

            ArrayList<RDebt> debts = dataModel.readDebts();
            realm.beginTransaction();
            safeCopyToRealm(debts);
            realm.commitTransaction();
            debts.clear();

            Map<String, RInvoice> invoices = dataModel.readInvoices();
            List<RInvoice> invoicesList = dataModel.readDebtsByInvoices(invoices);
            realm.beginTransaction();
            safeCopyToRealm(invoicesList);
            realm.commitTransaction();
            invoices.clear();
            invoicesList.clear();

            realm.beginTransaction();
            ArrayList<RBatch> batches = dataModel.readBatches();
            safeCopyToRealm(batches);
            realm.commitTransaction();
            batches.clear();

            dataModel.readPlanItems(itemsIds,
                    new SQLDataReader.IterateableBlock<List<RItemPlanInfo>>() {
                        @Override
                        public void onNext(List<RItemPlanInfo> object) {
                            realm.beginTransaction();
                            safeCopyToRealm(object);
                            realm.commitTransaction();
                        }
                    });

            List<RPriceSuggestion> suggestedPrices = dataModel.readSuggestedPrices();
            realm.beginTransaction();
            safeCopyToRealm(suggestedPrices);
            realm.commitTransaction();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (realm != null)
                    realm.commitTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return realm;
    }

    private void safeCopyToRealm(Collection<? extends RealmObject> objects) {
//        for (RealmObject object : objects) {
        try {
            realm.copyToRealm(objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

}
