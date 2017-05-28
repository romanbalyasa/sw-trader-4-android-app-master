package com.sanwell.sw_4.model.database;

import android.support.annotation.Nullable;

import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.cores.RClient;
import com.sanwell.sw_4.model.database.cores.RCurrency;
import com.sanwell.sw_4.model.database.cores.RGroup;
import com.sanwell.sw_4.model.database.cores.RItem;
import com.sanwell.sw_4.model.database.cores.RItemPlanInfo;
import com.sanwell.sw_4.model.database.cores.ROrder;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Group;
import com.sanwell.sw_4.model.database.objects.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

public class DataModel {

    public Realm realm;

    private DataModel() {
        realm = createRealm();
    }

    public static DataModel getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public static Realm createRealm() {
        Realm realm;
        RealmConfiguration configuration = new RealmConfiguration.Builder(SanwellApplication.applicationContext)
                .name("clients.realm")
                .schemaVersion(2)
                .build();
        try {
            realm = Realm.getInstance(configuration);
        } catch (RealmMigrationNeededException e) {
            e.printStackTrace();
            Realm.migrateRealm(new RealmConfiguration.Builder(SanwellApplication.applicationContext)
                    .name("clients.realm")
                    .schemaVersion(2)
                    .build(), new RealmMigration() {
                @Override
                public long execute(Realm realm, long version) {
                    if (version == 1) {
                        Table table = realm.getTable(RItem.class);
                        table.addColumn(ColumnType.INTEGER, "minOrder");
                        table.removeColumn(table.getColumnIndex("suggestedPriceId"));
                        version++;
                    }
                    return version;
                }
            });
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public ArrayList<Client> getClients() {
        ArrayList<Client> clientsList = new ArrayList<>();
        RealmResults<RClient> realmResults = realm
                .where(RClient.class)
                .findAll();
        if (realmResults != null) {
            int size = realmResults.size();
            for (int i = 0; i < size; i++) {
                clientsList.add(new Client(realmResults.get(i)));
            }
        }
        return clientsList;
    }

    @Nullable
    public RClient getCoreClient(String clientId) {
        return realm
                .where(RClient.class)
                .equalTo(RClient.ID_ROW, clientId)
                .findFirst();
    }

    public List<ROrder> getCoreOrders(String clientId) {
        return realm
                .where(ROrder.class)
                .equalTo(ROrder.CLIENT_ID_ROW, clientId)
                .findAll();
    }

    public List<ROrder> getCoreOrders() {
        return realm
                .where(ROrder.class)
                .findAll();
    }

    public ArrayList<Item> getItems(String parentID, String clientId, boolean isInPlan) {
        if (isInPlan) {
            return getPlannedItems(parentID, clientId);
        } else {
            return getRegularItems(parentID);
        }
    }

    public ArrayList<Item> getPlannedItems(String parentID, String clientId) {
        RealmResults<RItemPlanInfo> planInfos = realm.where(RItemPlanInfo.class)
                .beginGroup()
                .equalTo("clientId", clientId)
                .equalTo("groupId", parentID)
                .endGroup()
                .findAll();
        ArrayList<Item> arrayList = new ArrayList<>();
        if (planInfos != null) {
            int size = planInfos.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(new Item(planInfos.get(i)));
            }
        }
        Collections.sort(arrayList, new Comparator<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        return arrayList;
    }

    public ArrayList<Item> getRegularItems(String parentID) {
        RealmResults<RItem> items = realm.where(RItem.class)
                .beginGroup()
                .equalTo("parent", parentID)
                .endGroup()
                .findAllSorted("title");
        ArrayList<Item> arrayList = new ArrayList<>();
        if (items != null) {
            int size = items.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(new Item(items.get(i)));
            }
        }
        return arrayList;
    }

    public LinkedHashMap<String, Currency> getCurrencies() {
        LinkedHashMap<String, Currency> currencyHashMap = new LinkedHashMap<>();
        RealmResults<RCurrency> realmResults = realm
                .where(RCurrency.class)
                .findAll();
        if (realmResults != null) {
            int size = realmResults.size();
            for (int i = 0; i < size; i++) {
                RCurrency currency = realmResults.get(i);
                currencyHashMap.put(currency.getIso(), new Currency(currency));
            }
        }
        LinkedHashMap<String, Currency> linkedHashMap = new LinkedHashMap<>();
        String[] order = new String[]{"грн", "UAH", "USD", "EUR", "руб"};
        for (String curr : order) {
            Currency c = currencyHashMap.get(curr);
            if (c == null) {
                c = currencyHashMap.get(curr.toLowerCase());
            }
            if (c == null) {
                c = currencyHashMap.get(curr.toUpperCase());
            }
            if (c != null) {
                linkedHashMap.put(c.getIso(), c);
            }
        }
        return linkedHashMap;
    }

    public String getFormattedCurrency() {
        LinkedHashMap<String, Currency> currencies = getCurrencies();
        String htmlCurrency = "";
        for (Currency currency : currencies.values()) {
            if (currency.getRate() == 1.0) {
                continue;
            }
            if (!htmlCurrency.isEmpty()) {
                htmlCurrency += HTMLWrapper.space(2);
            }
            htmlCurrency += "1 " + currency.getIso().toUpperCase() + " = "
                    + HTMLWrapper.red("" + currency.getRate());
        }
        return htmlCurrency;
    }

    public ArrayList<Group> getCommonGroups(String parentID) {
        ArrayList<Group> groupArrayList = new ArrayList<>();
        RealmResults<RGroup> realmResults = realm.where(RGroup.class)
                .equalTo("parent", parentID)
                .findAllSorted("name");
        if (realmResults != null) {
            int size = realmResults.size();
            for (int i = 0; i < size; i++) {
                Group group = new Group(realmResults.get(i));
                if (group.getChildren().size() != 0 || group.hasSubCategories())
                    groupArrayList.add(group);
            }
        }
        return groupArrayList;
    }

    public ArrayList<Group> getPlanGroups() {
        ArrayList<Group> groupArrayList = new ArrayList<>();
        RealmResults<RGroup> realmResults = realm.where(RGroup.class)
                .equalTo("isPlanGroup", "1")
                .findAllSorted("planOrder");
        if (realmResults != null) {
            int size = realmResults.size();
            for (int i = 0; i < size; i++) {
                Group group = new Group(realmResults.get(i), true);
                if (group.getChildren().size() != 0 || group.hasSubCategories()) {
                    groupArrayList.add(group);
                }
            }
        }
        return groupArrayList;
    }

    public ArrayList<Group> getCatalogue(String parentID, Boolean isCommon) {
        if (parentID == null) {
            return new ArrayList<>();
        }
        if (isCommon) {
            return getCommonGroups(parentID);
        } else {
            return getPlanGroups();
        }
    }

    @Nullable
    public RItem getCoreItem(String itemID) {
        return realm.where(RItem.class)
                .equalTo(RItem.ID_ROW, itemID)
                .findFirst();
    }

    public static class SingletonHolder {
        public static final DataModel HOLDER_INSTANCE = new DataModel();
    }

}
