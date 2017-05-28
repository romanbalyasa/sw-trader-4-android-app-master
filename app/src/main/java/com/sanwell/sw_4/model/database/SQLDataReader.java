package com.sanwell.sw_4.model.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.ServerCommunicator;
import com.sanwell.sw_4.model.database.cores.RBatch;
import com.sanwell.sw_4.model.database.cores.RClient;
import com.sanwell.sw_4.model.database.cores.RCurrency;
import com.sanwell.sw_4.model.database.cores.RDebt;
import com.sanwell.sw_4.model.database.cores.RGroup;
import com.sanwell.sw_4.model.database.cores.RInvoice;
import com.sanwell.sw_4.model.database.cores.RItem;
import com.sanwell.sw_4.model.database.cores.RItemPlanInfo;
import com.sanwell.sw_4.model.database.cores.RPriceSuggestion;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Roman Kyrylenko on 2/9/16.
 */
public class SQLDataReader extends SQLiteAssetHelper {

    private static final String CURRENCY_TABLE_NAME = "Currencies";
    private static final String CLIENTS_TABLE_NAME = "Clients";
    private static final String GOODS_TABLE_NAME = "Goods";
    private static final String GROUPS_TABLE_NAME = "NomTree";
    private static final String PLAN_TABLE_NAME = "PlanLines";
    private static final String PLAN_GROUPS_TABLE_NAME = "PlanGroups";
    private static final String INVOICES_TABLE_NAME = "UnpaidInvoices";
    private static final String INVOICES_DATA_TABLE_NAME = "DebtsByInvoices";
    private static final String DEBTS_TABLE_NAME = "DebtsByCurrencies";
    private static final String BATCHES_TABLE_NAME = "Batches";
    private static final String SUGGESTED_PRICES_TABLE_NAME = "PriceConditions";

    private static final String TAG = "SQLDataModel";

    public SQLDataReader(Context context, String name, String storageDirectory,
                         SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, storageDirectory, factory, version);
    }

    public static File DbFile() {
        return new File(SanwellApplication.applicationContext.getFilesDir() + "/"
                + ServerCommunicator.DATABASE_FILE_NAME);
    }

    public static Boolean isDBFileExist() {
        return DbFile().exists();
    }

    @Nullable
    private Cursor getCursor(String tableName) {
        if (tableName == null) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        String raw = "SELECT * FROM " + tableName;
        Cursor c = null;
        try {
            c = db.rawQuery(raw, null);
            c.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Nullable
    private Cursor getCursor(String tableName, String where) {
        if (tableName == null) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        String raw = "SELECT * FROM " + tableName + " " + where;
        Cursor c = null;
        try {
            c = db.rawQuery(raw, null);
            c.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }


    public ArrayList<RClient> readClients() {
        ArrayList<RClient> clientsList = new ArrayList<>();
        Cursor cursor = getCursor(CLIENTS_TABLE_NAME);
        if (cursor == null || cursor.getColumnCount() < 7) {
            Log.e(TAG, "Cannot read clients");
            return clientsList;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            RClient client = new RClient();
            client.setId(cursor.getString(0));
            client.setName(cursor.getString(1));
            client.setCurrencyID(cursor.getString(2));
            client.setComment(cursor.getString(6));
            clientsList.add(client);
        }
        cursor.close();
        return clientsList;
    }

    public HashMap<String, RItem> readGoods() {
        Cursor c = getCursor(GOODS_TABLE_NAME);
        HashMap<String, RItem> itemHashMap = new HashMap<>();
        if (c == null || c.getColumnCount() < 27) {
            Log.e(TAG, "Cannot read items");
            return itemHashMap;
        }
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            RItem item = new RItem();

            item.setId(c.getString(0));
            item.setIsFixedCurrency(c.getInt(1) == 1);
            item.setMeasurement(c.getString(2));
            item.setHasPicture(c.getInt(3) == 1);
            item.setIsPromoted(c.getInt(4) == 1);
            item.setIsNew(c.getInt(5) == 1);
            item.setIsPricedUp(c.getInt(6) == 1);
            item.setIsFocused(c.getInt(7) == 1);
            item.setPackaging(c.getString(8));
            item.setCurrencyID(c.getString(10));
            item.setStockCount(c.getString(11));
            item.setPrice(c.getString(12));
            item.setM2_price(c.getString(13));
            item.setM_m5_price(c.getString(14));
            item.setM_m4_price(c.getString(15));
            item.setM_m3_price(c.getString(16));
            item.setM_m2_price(c.getString(17));
            item.setM_price(c.getString(18));
            item.setA_price(c.getString(19));
            item.setA_p2_price(c.getString(20));
            item.setA_a5_price(c.getString(21));
            item.setB_price(c.getString(22));
            item.setRetail_price(c.getString(23));
            item.setC_price(c.getString(24));
            item.setNpgId(c.getString(25));
            item.setMinOrder(c.getInt(26));

            itemHashMap.put(item.getId(), item);
        }
        c.close();
        Log.v(TAG, "Found " + itemHashMap.size() + " goods");
        return itemHashMap;
    }

    public ArrayList<RBatch> readBatches() {
        Cursor cursor = getCursor(BATCHES_TABLE_NAME);
        ArrayList<RBatch> array = new ArrayList<>();
        if (cursor == null || cursor.getColumnCount() < 8) {
            Log.e(TAG, "Cannot read currencies");
            return array;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            RBatch object = new RBatch();
            object.setOrigBatchId(cursor.getString(0));
            object.setItemID(cursor.getString(1));
            object.setWarehouse(cursor.getString(2));
            object.setSupplier(cursor.getString(3));
            object.setSupplyDate(cursor.getString(5));
            object.setPurchasePrice(cursor.getString(6));
            object.setCurrencyID(cursor.getString(7));
            object.setStock(cursor.getInt(8));
            object.setBatchID(String.valueOf(object.getOrigBatchId() + System.currentTimeMillis())
                    + object.getSupplyDate() + object.getSupplier());
            array.add(object);
        }
        cursor.close();
        return array;
    }

    public ArrayList<RCurrency> readCurrencies() {
        Cursor cursor = getCursor(CURRENCY_TABLE_NAME);
        ArrayList<RCurrency> array = new ArrayList<>();
        if (cursor == null || cursor.getColumnCount() < 4) {
            Log.e(TAG, "Cannot read currencies");
            return array;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            RCurrency object = new RCurrency();
            object.setId(cursor.getString(0));
            object.setName(cursor.getString(1));
            object.setIso(cursor.getString(2));
            object.setRate(cursor.getDouble(3));
            object.setRoundDigits(cursor.getInt(4));
            array.add(object);
        }
        cursor.close();
        return array;
    }

    public Map<String, RInvoice> readInvoices() {
        Map<String, RInvoice> map = new HashMap<>();
        Cursor cursor = getCursor(INVOICES_TABLE_NAME);
        if (cursor == null || cursor.getColumnCount() < 6) {
            Log.e(TAG, "Cannot read invoices");
            return map;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            RInvoice invoice = new RInvoice();
            invoice.setId(cursor.getString(0));
            invoice.setClientID(cursor.getString(1));
            invoice.setInvoiceDate(cursor.getString(2));
            invoice.setPaymentDate(cursor.getString(3));
            invoice.setOverallDebt(cursor.getString(4));
            invoice.setOverallOverdueDebt(cursor.getString(5));
            map.put(invoice.getId(), invoice);
        }
        cursor.close();
        return map;
    }

    public List<RInvoice> readDebtsByInvoices(Map<String, RInvoice> invoiceHashMap) {
        List<RInvoice> invoices = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("WHERE invoice_id IN (");
        for (RInvoice item : invoiceHashMap.values()) {
            stringBuilder.append(item.getId()).append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length() - 1);
        stringBuilder.append(");");

        Cursor cursor = getCursor(INVOICES_DATA_TABLE_NAME, stringBuilder.toString());
        if (cursor == null || cursor.getColumnCount() < 5) {
            Log.e(TAG, "Cannot read debts by invoices");
            return invoices;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String invoiceID = cursor.getString(1);
            RInvoice invoice = invoiceHashMap.get(invoiceID);
            if (invoice == null) {
                continue;
            }
            RInvoice invoice1 = RInvoice.withInvoice(invoice);
            invoice1.setCurrencyID(cursor.getString(2));
            invoice1.setDebt(cursor.getString(3));
            invoice1.setOverdueDebt(cursor.getString(4));
            invoices.add(invoice1);

        }
        cursor.close();
        return invoices;
    }

    public ArrayList<RDebt> readDebts() {
        ArrayList<RDebt> list = new ArrayList<>();
        Cursor cursor = getCursor(DEBTS_TABLE_NAME);
        if (cursor == null || cursor.getColumnCount() < 5) {
            Log.e(TAG, "Cannot read debts");
            return list;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            RDebt debt = new RDebt();
            debt.setClientID(cursor.getString(1));
            debt.setCurrencyID(cursor.getString(2));
            debt.setDebt(cursor.getString(3));
            debt.setPrepayment(cursor.getString(4));
            list.add(debt);
        }
        cursor.close();
        return list;
    }


    public void readPlanItems(List<String> values, IterateableBlock<List<RItemPlanInfo>> block) {
        Log.v(TAG, "read plan items (" + values.size() + " requested)");
        List<String> requests = new ArrayList<>();
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            String item = values.get(i);
            sBuilder.append(item).append(", ");
            if (i % 100 == 0) {
                requests.add(sBuilder.toString());
                sBuilder = new StringBuilder();
            }
        }
        values.clear();

        List<RItemPlanInfo> planInfos = new ArrayList<>();
        for (String s : requests) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("WHERE good_id IN (");
            stringBuilder.append(s);
            stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length() - 1);
            stringBuilder.append(");");
            Cursor cursor = getCursor(PLAN_TABLE_NAME, stringBuilder.toString());
            if (cursor == null || cursor.getColumnCount() < 8) {
                Log.e(TAG, "Cannot read plan items");
                continue;
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String id = cursor.getString(3);
                RItemPlanInfo item = new RItemPlanInfo();
                item.setId(cursor.getString(0));
                item.setClientId(cursor.getString(1));
                item.setGoodId(id);
                item.setGroupId(cursor.getString(2));
                item.setPlanned(cursor.getDouble(4));
                item.setFact(item.getFact() + cursor.getDouble(6));
                item.setPlanSum(item.getPlanSum() + cursor.getDouble(5));
                item.setFactSum(item.getFactSum() + cursor.getDouble(7));
                planInfos.add(item);
            }
            cursor.close();
            block.onNext(planInfos);
            planInfos.clear();
        }
        Log.v(TAG, "Found bunch of plan items");
    }

    public List<RPriceSuggestion> readSuggestedPrices() {
        List<RPriceSuggestion> prices = new ArrayList<>();
        Cursor cursor = getCursor(SUGGESTED_PRICES_TABLE_NAME);
        if (cursor == null || cursor.getColumnCount() < 4) {
            Log.e(TAG, "Cannot read suggested prices");
            return prices;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            RPriceSuggestion priceSuggestion = new RPriceSuggestion();
            priceSuggestion.setId(cursor.getString(0));
            priceSuggestion.setClientId(cursor.getString(1));
            priceSuggestion.setNpgId(cursor.getString(2));
            priceSuggestion.setPriceId(cursor.getString(3));
            prices.add(priceSuggestion);
        }
        cursor.close();
        return prices;
    }

    public ArrayList<RGroup> readPlanGroups() {
        ArrayList<RGroup> groups = new ArrayList<>();
        Cursor cursor = getCursor(PLAN_GROUPS_TABLE_NAME);
        if (cursor == null || cursor.getColumnCount() < 2) {
            Log.e(TAG, "Cannot read plan groups");
            return groups;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (cursor.getString(1).equals("<не задано>")) {
                continue;
            }
            RGroup object = new RGroup();
            object.setId(cursor.getString(0));
            object.setPlanOrder(cursor.getString(2));
            object.setIsPlanGroup("1");
            object.setName(cursor.getString(1));
            groups.add(object);
        }
        cursor.close();
        return groups;
    }

    public void readGroups(ArrayList<RGroup> groups, Map<String, RItem> itemsMap) {
        Cursor cursor = getCursor(GROUPS_TABLE_NAME);
        if (cursor == null || cursor.getColumnCount() < 7) {
            Log.e(TAG, "Cannot read groups");
            return;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (cursor.getString(5).equals("2")) {
                RGroup object = new RGroup();
                object.setId(cursor.getString(0));
                object.setName(cursor.getString(6));
                object.setParent(cursor.getString(1));
                groups.add(object);
            } else if (cursor.getString(5).equals("1")) {
                String id = cursor.getString(0);
                RItem item = itemsMap.get(id);
                item.setMultFactor(cursor.getDouble(7));
                item.setTitle(cursor.getString(6).toUpperCase());
                item.setParent(cursor.getString(1));
            }
        }
        cursor.close();
    }

    interface IterateableBlock<T> {
        void onNext(T object);
    }

}
