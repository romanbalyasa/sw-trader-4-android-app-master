package com.sanwell.sw_4.model.database;

import android.content.Context;

import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.KMLCollector;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.cores.RClient;
import com.sanwell.sw_4.model.database.cores.RItem;
import com.sanwell.sw_4.model.database.cores.ROrder;
import com.sanwell.sw_4.model.database.cores.ROrderBatchInfo;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Order;
import com.sanwell.sw_4.model.interfaces.CompletionHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


/*
 * Created by Roman Kyrylenko on 02/04/16.
 */
public class OrdersDataModel {

    public static final String CLIENT_ID_COLUMN = "clientID";
    public static final String ORDER_ID_COLUMN = "orderID";
    public static final String IS_OPENED_COLUMN = "isOpen";
    public static final String DATE_FORMAT = "dd.MM.yyyy_HH-mm-ss";

    public static OrdersDataModel getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public Order getOrder(String orderId) {
        Realm realm = DataModel.getInstance().realm;
        ROrder order = realm
                .where(ROrder.class)
                .equalTo(OrdersDataModel.ORDER_ID_COLUMN, orderId)
                .findFirst();
        if (order != null) {
            return new Order(order);
        }
        return null;
    }

    public double getOrderPrice(ROrder order) {
        Currency currency = new Currency(order.getCurrencyID());
        double sum = 0.0;
        RealmResults<ROrderBatchInfo> infos = DataModel.getInstance()
                .realm
                .where(ROrderBatchInfo.class)
                .equalTo("orderID", order.getOrderID())
                .findAll();
        for (int i = 0; i < infos.size(); i++) {
            ROrderBatchInfo info = infos.get(i);
            double c = info.getItemsCount();
            double p = info.getItemPrice();
            sum += currency.defaultRound(c * p, info.getItemCurrencyID());
        }
        return sum;
    }

    public void delete(String id) {
        Realm realm = DataModel.getInstance().realm;
        RealmResults<ROrder> order = realm
                .where(ROrder.class)
                .equalTo(OrdersDataModel.ORDER_ID_COLUMN, id)
                .findAll();
        if (order != null) {
            realm.beginTransaction();
            try {
                while (order.size() != 0) {
                    order.get(0).removeFromRealm();
                }
            } finally {
                realm.commitTransaction();
            }
        }
    }

    public void uploadOrders(CompletionHandler completionHandler) {
        Context context = SanwellApplication.applicationContext;
        Realm realm = DataModel.getInstance().realm;
        RealmResults<ROrder> orders = realm
                .where(ROrder.class)
                .equalTo(IS_OPENED_COLUMN, true)
                .findAll();
        List<File> files = new ArrayList<>();
        realm.beginTransaction();
        String tabletID = "PN" + Helpers.read_sp(Helpers.DEVICE_USER_ID, "xxx");
        for (int i = 0; i < orders.size(); i++) {
            ROrder order = orders.get(i);
            RClient client = realm
                    .where(RClient.class)
                    .equalTo("id", order.getClientID())
                    .findFirst();
            if (client == null) {
                continue;
            }
            Currency currency = new Currency(order.getCurrencyID());
            ArrayList<String> lines = new ArrayList<>();
            lines.add(client.getId() + "\t" + client.getName() + "\tUnicode\tЮникод");
            lines.add("1\t№1\t" + Helpers.randomString());
            lines.add(currency.getIso() + "\t" + getOrderPrice(order) + "\tТорговля 3.15.04.24");
            lines.add("0%\t" + order.getComment());
            RealmResults<ROrderBatchInfo> infos = realm
                    .where(ROrderBatchInfo.class)
                    .equalTo("orderID", order.getOrderID())
                    .findAll();
            for (int j = 0; j < infos.size(); j++) {
                ROrderBatchInfo batchInfo = infos.get(j);
                RItem item = realm
                        .where(RItem.class)
                        .equalTo("id", batchInfo.getItemID())
                        .findFirst();
                if (item == null) {
                    continue;
                }
                String tab = "\t";
                Currency batchCurrency = new Currency(batchInfo.getItemCurrencyID());
                lines.add(batchInfo.getOrigBatchId() + tab
                        + batchInfo.getItemID() + tab
                        + batchInfo.getItemsCount() + tab
                        + String.valueOf(batchInfo.getItemPrice()) + tab
                        + batchCurrency.getIso() + tab
                        + String.valueOf(round(batchInfo.getItemsCount() * batchInfo.getItemPrice(), 5)) + tab
                        + item.getTitle() + tab
                        + "0" + tab
                        + batchCurrency.sDefaultRound(Double.parseDouble(item.getA_price()), item.getCurrencyID()) + tab
                        + batchCurrency.sDefaultRound(Double.parseDouble(item.getB_price()), item.getCurrencyID()) + tab
                        + batchCurrency.sDefaultRound(Double.parseDouble(item.getM_price()), item.getCurrencyID()) + tab
                        + "0" + tab
                        + "#BtPriceName=#BtPreBtCli=" + batchInfo.getPriceCode() + "|" + batchInfo.getSuggestedPriceCode()
                        + "#BtPriceC=" + item.getC_price()
                );
                if (batchInfo.getOverheadItemsCount() != 0) {
                    lines.add("0" + tab
                            + batchInfo.getItemID() + tab
                            + batchInfo.getOverheadItemsCount() + tab
                            + "0.0" + tab
                            + batchCurrency.getIso() + tab
                            + "0.0" + tab
                            + item.getTitle() + tab
                            + "0" + tab
                            + batchCurrency.sDefaultRound(Double.parseDouble(item.getA_price()), item.getCurrencyID()) + tab
                            + batchCurrency.sDefaultRound(Double.parseDouble(item.getB_price()), item.getCurrencyID()) + tab
                            + batchCurrency.sDefaultRound(Double.parseDouble(item.getM_price()), item.getCurrencyID()) + tab
                            + "0" + tab
                            + "#BtPriceName=#BtPreBtCli=" + batchInfo.getPriceCode() + "|" + batchInfo.getSuggestedPriceCode()
                            + "#BtPriceC=" + item.getC_price()
                    );
                }
            }
            StringBuilder content = new StringBuilder();
            for (String line : lines) {
                content.append(line).append("\n");
            }
            File file = new File(context.getExternalFilesDir(null)
                    + "/"
                    + tabletID
                    + "ZAKAZ___"
                    + client.getName()
                    + order.getOpenedIn()
                    + ".txt");
            Helpers.writeToFile(content.toString(), file);
            files.add(file);
        }
        while (orders.size() != 0) {
            orders.first().setIsOpen(false);
        }
        realm.commitTransaction();
        try {
//            File file = KMLCollector.form(context);
//            files.add(file);
            KMLCollector.sendEmail(context, files);
            if (completionHandler != null) {
                completionHandler.onCompletion(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (completionHandler != null) {
                completionHandler.onCompletion(false);
            }
        }
    }

    public boolean hasOpenedOrders() {
        return DataModel.getInstance().realm
                .where(ROrder.class)
                .equalTo(IS_OPENED_COLUMN, true)
                .findFirst() != null;
    }

    public boolean hasOrders() {
        return DataModel.getInstance().realm
                .where(ROrder.class)
                .findFirst() != null;
    }

    public static class SingletonHolder {
        public static final OrdersDataModel HOLDER_INSTANCE = new OrdersDataModel();
    }


}
