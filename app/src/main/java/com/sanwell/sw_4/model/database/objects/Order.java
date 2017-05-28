package com.sanwell.sw_4.model.database.objects;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.OrdersDataModel;
import com.sanwell.sw_4.model.database.cores.RBatch;
import com.sanwell.sw_4.model.database.cores.RCurrency;
import com.sanwell.sw_4.model.database.cores.RItem;
import com.sanwell.sw_4.model.database.cores.ROrder;
import com.sanwell.sw_4.model.database.cores.ROrderBatchInfo;
import com.sanwell.sw_4.model.database.cores.ROrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

/*
 * Created by Roman Kyrylenko on 04/12/15.
 */
public class Order {

    private String orderID;
    private String clientID;
    private String openedIn;
    private Date oppeningDate;
    private String currencyID;
    private Currency currency;
    private String comment = null;
    private Boolean isOpen = false;
    private ArrayList<ROrderItem> itemIDs = new ArrayList<>();
    private Integer numberOfItems = 0;

    public Order(ROrder order) {
        if (order != null) {
            openedIn = order.getOpenedIn();
            isOpen = order.isOpen();
            if (order.getItems() != null) {
                itemIDs = new ArrayList<>();
                for (int i = 0; i < order.getItems().size(); i++) {
                    itemIDs.add(order.getItems().get(i));
                }
            }
            orderID = order.getOrderID();
            currencyID = order.getCurrencyID();
            currency = new Currency(currencyID);
            oppeningDate = order.getOpenningDate();
            numberOfItems = order.getNumberOfItems();
            comment = order.getComment();
        }
    }

    public Order(String clientID) {
        this.orderID = Helpers.randomString();
        this.clientID = clientID;
        oppeningDate = new Date();
        openedIn = new SimpleDateFormat(OrdersDataModel.DATE_FORMAT).format(oppeningDate);
        isOpen = true;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public Order setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
        currency = new Currency(currencyID);
        return this;
    }

    public Date getOppeningDate() {
        return oppeningDate;
    }

    public Currency getCurrency() {
        return currency;
    }

    @NonNull
    public String getComment() {
        return comment == null ? "" : comment;
    }

    public Order setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getOpenedIn() {
        return openedIn;
    }

    public Order addItem(Item item) {
        for (ROrderItem rOrderItem : itemIDs) {
            if (rOrderItem.getItemID().equals(item.getItemID())) {
                return this;
            }
        }

        ROrderItem string = new ROrderItem();
        Realm realm = DataModel.getInstance().realm;
        realm.beginTransaction();
        try {
            string.setItemID(item.getItemID());
            itemIDs.add(string);
        } finally {
            realm.commitTransaction();
        }
        numberOfItems++;
        return commit();
    }

    public Boolean isOpen() {
        return isOpen;
    }

    public Boolean isEmpty() {
        return getItems().isEmpty();
    }

    public String[] getSummaryDetailed() {
        ArrayList<Item> items = getItems();
        double summ = 0.0;
        double count = 0;
        int lines = 0;
        int minLower = 0;
        for (Item item : items) {
            List<ROrderBatchInfo> infos = DataModel.getInstance()
                    .realm
                    .where(ROrderBatchInfo.class)
                    .beginGroup()
                    .equalTo("orderID", orderID)
                    .equalTo("itemID", item.getItemID())
                    .endGroup()
                    .findAll();
            lines++;
            boolean checked = false;
            for (ROrderBatchInfo info : infos) {
                double c = info.getItemsCount();
                double p = info.getItemPrice();
                summ += currency.defaultRound(c * p, info.getItemCurrencyID());
                count += c;
                if (!checked && info.getItemsCount() < item.getOrderMinCount()) {
                    minLower++;
                    checked = true;
                }
            }
        }

        String[] result = new String[2];
        result[0] = String.valueOf(lines) + "/" + String.valueOf(minLower) + " " + SanwellApplication.applicationContext.getResources()
                .getQuantityString(R.plurals.position_plurals, (int) count);
        result[1] = String.format(Locale.US, "%.3f", summ);
        return result;
    }

    public String getSummary() {
        ArrayList<Item> items = getItems();
        double summ = 0.0, overheadSum = 0.0;
        double overhead = 0, count = 0;
        int lines = 0;
        int minLower = 0;
        for (Item item : items) {
            List<ROrderBatchInfo> infos = DataModel.getInstance()
                    .realm
                    .where(ROrderBatchInfo.class)
                    .beginGroup()
                    .equalTo("orderID", orderID)
                    .equalTo("itemID", item.getItemID())
                    .endGroup()
                    .findAll();
            lines++;
            boolean checked = false;
            for (ROrderBatchInfo info : infos) {
                double c = info.getItemsCount();
                double p = info.getItemPrice();
                double ov = info.getOverheadItemsCount();
                summ += currency.defaultRound(c * p, info.getItemCurrencyID());
                overheadSum += currency.defaultRound(ov * p, info.getItemCurrencyID());
                overhead += ov;
                count += c;
                if (!checked && info.getItemsCount() < item.getOrderMinCount()) {
                    minLower++;
                    checked = true;
                }
            }
        }

        String sCount = String.valueOf((int) count);
        if (count % 1 != 0) {
            sCount = String.format(Locale.US, "%.2f", count);
        }
        String sOverhead = String.valueOf((int) overhead);
        if (overhead % 1 != 0) {
            sOverhead = String.format(Locale.US, "%.2f", overhead);
        }
        String html = String.valueOf(lines) + "/" + String.valueOf(minLower) + " " + SanwellApplication.applicationContext.getResources()
                .getQuantityString(R.plurals.position_plurals, (int) count) + ", " + sCount + " " + SanwellApplication.applicationContext.getResources()
                .getQuantityString(R.plurals.units_plurals, (int) count) + " на сумму <font color=red>";
        html += String.format(Locale.US, "%.3f", summ) + "</font>";
        return html + " (отказ: "
                + sOverhead
                + " поз. на сумму "
                + Helpers.wrapWithRedHtmlFont(String.format(Locale.US, "%.3f", overheadSum))
                + " " + currency.getIso() + ")";
    }

    public String[] getLocalSummary() {
        ArrayList<Item> items = getItems();
        double summ = 0.0, overheadSum = 0.0;
        double overhead = 0, count = 0;
        for (Item item : items) {
            List<ROrderBatchInfo> infos = DataModel.getInstance()
                    .realm
                    .where(ROrderBatchInfo.class)
                    .beginGroup()
                    .equalTo("orderID", orderID)
                    .equalTo("itemID", item.getItemID())
                    .endGroup()
                    .findAll();
            for (ROrderBatchInfo info : infos) {
                double c = info.getItemsCount();
                double p = info.getItemPrice();
                double ov = info.getOverheadItemsCount();
                summ += currency.defaultRound(c * p, info.getItemCurrencyID());
                overheadSum += currency.defaultRound(ov * p, info.getItemCurrencyID());
                overhead += ov;
                count += c;
            }
        }
        String sCount = String.valueOf((int) count);
        if (count % 1 != 0) {
            sCount = String.format(Locale.US, "%.2f", count);
        }
        String sOverhead = String.valueOf((int) overhead);
        if (overhead % 1 != 0) {
            sOverhead = String.format(Locale.US, "%.2f", overhead);
        }
        String first = String.format(Locale.US, "%.3f", summ) + " " + currency.getIso();
        String second = ", " + sCount + " " + SanwellApplication.applicationContext.getResources()
                .getQuantityString(R.plurals.position_plurals, (int) count);
        String third = "Отказ: " + String.format(Locale.US, "%.3f", overheadSum) + " " + currency.getIso()
                + ", " + sOverhead + " поз.";
        String[] output = new String[3];
        output[0] = first;
        output[1] = second;
        output[2] = third;
        return output;
    }

    public String getUploadedSummary() {
        ArrayList<Item> items = getItems();
        double summ = 0.0, count = 0;
        for (Item item : items) {
            List<ROrderBatchInfo> infos = DataModel.getInstance()
                    .realm
                    .where(ROrderBatchInfo.class)
                    .beginGroup()
                    .equalTo("orderID", orderID)
                    .equalTo("itemID", item.getItemID())
                    .endGroup()
                    .findAll();
            for (ROrderBatchInfo info : infos) {
                double c = info.getItemsCount();
                double p = info.getItemPrice();
                summ += currency.defaultRound(c * p, info.getItemCurrencyID());
                count += c;
            }
        }
        String sCount = String.valueOf((int) count);
        if (count % 1 != 0) {
            sCount = String.format(Locale.US, "%.2f", count);
        }
        return String.format(Locale.US, "%.3f", summ) + currency.getIso() + ", " + sCount + " "
                + SanwellApplication.applicationContext.getResources()
                .getQuantityString(R.plurals.position_plurals, (int) count);
    }


    public ArrayList<Item> getItems() {
        ArrayList<Item> items = new ArrayList<>();
        Realm realm = DataModel.getInstance().realm;
        for (ROrderItem string : itemIDs) {
            RItem rItem = realm
                    .where(RItem.class)
                    .equalTo(Item.ITEM_ID_COLUMN, string.getItemID())
                    .findFirst();
            if (rItem != null) {
                items.add(new Item(rItem));
            }
        }
        return items;
    }

    public Order commit() {
        Realm realm = DataModel.getInstance().realm;
        realm.beginTransaction();
        try {
            ROrder order = realm
                    .where(ROrder.class)
                    .equalTo(OrdersDataModel.ORDER_ID_COLUMN, orderID)
                    .findFirst();
            if (order == null) {
                order = new ROrder();
                fillOrder(order);
                realm.copyToRealm(order);
            } else {
                fillOrder(order);
            }
        } finally {
            realm.commitTransaction();
        }
        return this;
    }

    private ROrder fillOrder(ROrder order) {
        if (order != null) {
            order.setIsOpen(isOpen);
            if (orderID != null)
                order.setOrderID(orderID);
            if (clientID != null)
                order.setClientID(clientID);
            if (itemIDs != null) {
                order.getItems().clear();
                order.getItems().addAll(itemIDs);
            }
            order.setNumberOfItems(numberOfItems);
            if (openedIn != null)
                order.setOpenedIn(openedIn);
            if (comment != null)
                order.setComment(comment);
            if (oppeningDate != null)
                order.setOpenningDate(oppeningDate);
            if (currencyID != null)
                order.setCurrencyID(currencyID);
        }
        return order;
    }

    @Nullable
    public ROrderBatchInfo getBatchInfo(Item item) {
        return DataModel.getInstance().realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("orderID", orderID)
                .equalTo("itemID", item.getItemID())
                .endGroup()
                .findFirst();
    }

    public List<ROrderBatchInfo> getBatchInfos(Item item) {
        List<ROrderBatchInfo> batches = new ArrayList<>();
        Realm realm = DataModel.getInstance().realm;
        List<ROrderBatchInfo> info = realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("orderID", orderID)
                .equalTo("itemID", item.getItemID())
                .endGroup()
                .findAll();
        if (info != null) {
            batches.addAll(info);
        }
        return batches;
    }

    public List<ROrderBatchInfo> getBatchInfos() {
        List<ROrderBatchInfo> batches = new ArrayList<>();
        Realm realm = DataModel.getInstance().realm;
        ArrayList<Item> items = getItems();
        for (Item item : items) {
            List<ROrderBatchInfo> info = realm
                    .where(ROrderBatchInfo.class)
                    .beginGroup()
                    .equalTo("orderID", orderID)
                    .equalTo("itemID", item.getItemID())
                    .endGroup()
                    .findAll();
            if (info != null) {
                batches.addAll(info);
            }
        }
        return batches;
    }

    public double getOrderedItemsInBatch(RBatch batch) {
        ROrderBatchInfo info = DataModel.getInstance()
                .realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("orderID", orderID)
                .equalTo("batchID", batch.getBatchID())
                .equalTo("itemID", batch.getItemID())
                .endGroup()
                .findFirst();
        if (info != null) {
            return info.getItemsCount();
        }
        return 0;
    }

    public double getOverheadOrderedItemsInBatch(RBatch batch) {
        ROrderBatchInfo info = DataModel.getInstance()
                .realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("batchID", batch.getBatchID())
                .equalTo("orderID", orderID)
                .equalTo("itemID", batch.getItemID())
                .endGroup()
                .findFirst();
        if (info != null) {
            return info.getOverheadItemsCount();
        }
        return 0;
    }

    @Nullable
    public ROrderBatchInfo getBatchInfo(String itemID, String batchID) {
        return DataModel.getInstance().realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("orderID", orderID)
                .equalTo("batchID", batchID)
                .equalTo("itemID", itemID)
                .endGroup()
                .findFirst();
    }


    public Order removeItem(Item item) {
        int index = -1;
        for (int i = 0; i < itemIDs.size(); i++) {
            ROrderItem item1 = itemIDs.get(i);
            if (item1.getItemID().equals(item.getItemID())) {
                index = i;
            }
        }
        Realm realm = DataModel.getInstance().realm;
        RealmResults<ROrderBatchInfo> batchInfos = realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("orderID", orderID)
                .equalTo("itemID", item.getItemID())
                .endGroup()
                .findAll();
        realm.beginTransaction();
        while (batchInfos.size() != 0) {
            batchInfos.removeLast();
        }
        realm.commitTransaction();
        if (index >= 0) {
            itemIDs.remove(index);
        }

        return commit();
    }

    public void addItemsFromBatch(RBatch batch, String itemsCount, String price, String currencyID,
                                  String priceCode, String suggestedPriceCode) {
        if (batch == null) {
            Log.e("Order", "Cannot save items: batch is nil");
            return;
        }
        Realm realm = DataModel.getInstance().realm;
        ROrderBatchInfo info = getBatchInfo(batch.getItemID(), batch.getBatchID());
        boolean shouldBeCopied = false;
        if (info == null) {
            info = new ROrderBatchInfo();
            shouldBeCopied = true;
        }
        realm.beginTransaction();
        info.setOrigBatchId(batch.getOrigBatchId());
        info.setBatchID(batch.getBatchID());
        info.setOrderID(orderID);
        info.setPriceCode(priceCode);
        info.setSuggestedPriceCode(suggestedPriceCode);
        info.setItemID(batch.getItemID());
        info.setItemCurrencyID(currencyID);
        double dPrice = Item.safeParse(price);
        info.setItemPrice(dPrice);
        try {
            int items = Integer.parseInt(itemsCount);
            int itemsOverhead = items - batch.getStock();
            info.setItemsCount(items);
            info.setOverheadItemsCount(itemsOverhead > 0 ? itemsOverhead : 0);
        } catch (Exception e) {
            info.setItemsCount(0);
            info.setOverheadItemsCount(0);
        }

        // setup one price for all:
        RealmResults<ROrderBatchInfo> realmResults = realm
                .where(ROrderBatchInfo.class)
                .equalTo("orderID", orderID)
                .equalTo("itemID", batch.getItemID())
                .findAll();
        for (int i = 0; i < realmResults.size(); i++) {
            ROrderBatchInfo rOrderBatchInfo = realmResults.get(i);
            rOrderBatchInfo.setSuggestedPriceCode(suggestedPriceCode);
            rOrderBatchInfo.setPriceCode(priceCode);
            rOrderBatchInfo.setItemCurrencyID(currencyID);
            rOrderBatchInfo.setItemPrice(dPrice);
        }
        try {
            if (shouldBeCopied)
                realm.copyToRealm(info);
            for (ROrderItem item : itemIDs) {
                if (item.getItemID().equals(info.getItemID())) {
                    item.setItemsCount(info.getItemsCount());
                }
            }
        } finally {
            realm.commitTransaction();
        }
        commit();
    }

    public String getCurrencyISO() {
        RCurrency currency = DataModel.getInstance().realm
                .where(RCurrency.class)
                .equalTo(Currency.ID_COLUMN, currencyID)
                .findFirst();
        if (currency != null) {
            return currency.getIso();
        }
        return "";
    }

    public double getOverheadOrderedOfItem(Item item) {
        if (item == null) {
            return 0;
        }
        RealmResults<ROrderBatchInfo> info = DataModel.getInstance()
                .realm
                .where(ROrderBatchInfo.class)
                .beginGroup()
                .equalTo("orderID", orderID)
                .equalTo("itemID", item.getItemID())
                .endGroup()
                .findAll();
        double sum = 0.0;
        for (int i = 0; i < info.size(); i++) {
            sum += info.get(i).getOverheadItemsCount();
        }
        return sum;
    }

    // returns picked price for item
    public Double getItemPrice(String itemId, Currency currency, String itemCurrencyId) {
        ROrderBatchInfo info = DataModel.getInstance()
                .realm
                .where(ROrderBatchInfo.class)
                .equalTo("orderID", orderID)
                .equalTo("itemID", itemId)
                .findFirst();
        if (info == null) {
            return null;
        }
        return info.getItemPrice();//currency.defaultRound(info.getItemPrice(), itemCurrencyId);
    }

    public double getItemsCount(Item item) {
        if (item == null) {
            return 0;
        }
        RealmResults<ROrderBatchInfo> info = DataModel.getInstance()
                .realm
                .where(ROrderBatchInfo.class)
                .equalTo("orderID", orderID)
                .equalTo("itemID", item.getItemID())
                .findAll();
        double sum = 0.0;
        for (int i = 0; i < info.size(); i++) {
            sum += info.get(i).getItemsCount();
        }
        return sum;
    }

    public Object formattedOpenDate() {
        return new SimpleDateFormat("dd MMM").format(oppeningDate)
                + ". "
                + new SimpleDateFormat("yyyy").format(oppeningDate)
                + "г.";
    }

    public String getOrderID() {
        return orderID;
    }

    public boolean isItemInOrder(Item item) {
        if (item == null) return false;
        for (ROrderItem item1 : itemIDs) {
            if (item1.getItemID().equals(item.getItemID())) {
                return true;
            }
        }
        return false;
    }
}
