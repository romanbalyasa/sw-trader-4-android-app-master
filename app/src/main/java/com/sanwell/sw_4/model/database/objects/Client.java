package com.sanwell.sw_4.model.database.objects;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.OrdersDataModel;
import com.sanwell.sw_4.model.database.cores.RClient;
import com.sanwell.sw_4.model.database.cores.RDebt;
import com.sanwell.sw_4.model.database.cores.RInvoice;
import com.sanwell.sw_4.model.database.cores.RItemPlanInfo;
import com.sanwell.sw_4.model.database.cores.ROrder;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class Client implements Comparable {

    private String name;
    private String id;
    private String comments, debtString;
    private String planPercentage;
    private boolean isSectionHeader = false;

    public Client(RClient rClient) {
        if (rClient == null) {
            return;
        }
        name = rClient.getName().toUpperCase();
        id = rClient.getId();
        comments = rClient.getComment();
        debtString = rClient.getDebtString();
        isSectionHeader = false;
    }

    public Client() {
        isSectionHeader = false;
    }

    public boolean hasOverdueInvoiceDebts() {
        List<RInvoice> invoices = getInvoices();
        for (RInvoice invoice : invoices) {
            if (RInvoice.hasOverdueDebt(invoice)) {
                return true;
            }
        }
        return false;
    }

    public String getComments() {
        return comments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o.getClass() == this.getClass()) {
            Client c = (Client) o;
            if (getName().equals("#")) {
                return -1;
            }
            return getName().compareTo(c.getName());
        }
        return 0;
    }

    public boolean isSectionHeader() {
        return isSectionHeader;
    }

    public void setIsSectionHeader(boolean isSectionHeader) {
        this.isSectionHeader = isSectionHeader;
    }

    @NonNull
    public List<Order> getLastFiveOrders() {
        Realm realm = DataModel.getInstance().realm;
        RealmResults<ROrder> orders = realm
                .where(ROrder.class)
                .beginGroup()
                .equalTo(OrdersDataModel.CLIENT_ID_COLUMN, id)
                .endGroup()
                .findAllSorted("openningDate", false);
        ArrayList<Order> ordersArrayList = new ArrayList<>();
        for (int i = 0; i < orders.size() && i < 5; i++) {
            ordersArrayList.add(new Order(orders.get(i)));
        }
        return ordersArrayList;
    }

    @Nullable
    public Order getOrder() {
        Realm realm = DataModel.getInstance().realm;
        ROrder order = realm
                .where(ROrder.class)
                .beginGroup()
                .equalTo(OrdersDataModel.CLIENT_ID_COLUMN, id)
                .equalTo(OrdersDataModel.IS_OPENED_COLUMN, true)
                .endGroup()
                .findFirst();
        if (order != null) {
            return new Order(order);
        }
        return null;
    }

    public String getPlanPercentage() {
        if (planPercentage != null && !planPercentage.isEmpty()) {
            return planPercentage;
        }
        RealmResults<RItemPlanInfo> items = DataModel.getInstance().realm
                .where(RItemPlanInfo.class)
                .equalTo("clientId", id)
                .findAll();
        Double fact = 0.0, plan = 0.0;
        for (int i = 0; i < items.size(); i++) {
            RItemPlanInfo item = items.get(i);
            fact += item.getFact();
            plan += item.getPlanned();
        }
        planPercentage = String.valueOf((int) (plan == 0 ? 0 : 100.0 * fact / plan));
        return planPercentage;
    }

    public String getId() {
        return id;
    }

    @NonNull
    public List<RInvoice> getInvoices() {
        ArrayList<RInvoice> invoices = new ArrayList<>();
        RealmResults<RInvoice> rInvoices = DataModel.getInstance().realm
                .where(RInvoice.class)
                .equalTo("clientID", id)
                .findAllSorted("invoiceDate");
        for (int i = 0; i < rInvoices.size(); i++) {
            invoices.add(rInvoices.get(i));
        }
        return invoices;
    }

    @NonNull
    public List<Debt> getDebts() {
        ArrayList<Debt> debts = new ArrayList<>();
        RealmResults<RDebt> rDebts = DataModel.getInstance().realm
                .where(RDebt.class)
                .equalTo(Debt.CLIENT_ID_COLUMN, id)
                .findAll();
        for (int i = 0; i < rDebts.size(); i++) {
            debts.add(new Debt(rDebts.get(i)));
        }
        return debts;
    }

    public Order addOrder() {
        return new Order(id)
                .setCurrencyID(Helpers.currencyID)
                .commit();
    }


    public String getDebtString() {
        if (debtString == null) {
            String debt = "";
            List<Debt> debts = getDebts();
            for (Debt debt1 : debts) {
                Double value = debt1.getDebt();
                if (value == 0) {
                    continue;
                }
                String iso = debt1.getCurrency();
                if (debt.isEmpty()) {
                    debt = HTMLWrapper.bold(SanwellApplication.applicationContext.getString(R.string.debt)) + " ";
                } else {
                    debt += ", ";
                }
                if (value < 0) {
                    debt += HTMLWrapper.green("" + (-value)) + " " + iso;
                } else {
                    debt += value + " " + iso;
                }
            }
            if (debt.isEmpty()) {
                debt = getComments();
            } else {
                debt += " " + getComments();
            }
            Realm realm = DataModel.getInstance().realm;
            RClient client = realm.where(RClient.class)
                    .equalTo(Debt.CLIENT_ID_COLUMN, id)
                    .findFirst();
            if (client != null) {
                realm.beginTransaction();
                client.setDebtString(debt);
                realm.commitTransaction();
            }
            return debt;
        } else {
            return debtString;
        }
    }
}
