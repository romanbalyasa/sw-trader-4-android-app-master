package com.sanwell.sw_4.model.database.cores;

import io.realm.RealmObject;

/*
 * Created by Roman Kyrylenko on 11/02/16.
 */
public class RInvoice extends RealmObject {

    private String id;
    private String currencyID;
    private String debt;
    private String overdueDebt;
    private String clientID;
    private String invoiceDate;
    private String paymentDate;
    private String overallDebt;
    private String overallOverdueDebt;

    public static RInvoice withInvoice(RInvoice invoice) {
        RInvoice rInvoice = new RInvoice();
        rInvoice.setId(invoice.getId());
        rInvoice.setCurrencyID(invoice.getCurrencyID());
        rInvoice.setDebt(invoice.getDebt());
        rInvoice.setClientID(invoice.getClientID());
        rInvoice.setOverdueDebt(invoice.getOverdueDebt());
        rInvoice.setInvoiceDate(invoice.getInvoiceDate());
        rInvoice.setPaymentDate(invoice.getPaymentDate());
        rInvoice.setOverallDebt(invoice.getOverallDebt());
        rInvoice.setOverallOverdueDebt(invoice.getOverallOverdueDebt());
        return rInvoice;
    }

    public static boolean hasOverdueDebt(RInvoice invoice) {
        if (invoice == null) {
            return false;
        }
        if (invoice.getOverallOverdueDebt() != null) {
            try {
                Double val = Double.parseDouble(invoice.getOverallOverdueDebt());
                return val != 0;
            } catch (Exception ignored) {}
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOverdueDebt() {
        return overdueDebt;
    }

    public void setOverdueDebt(String overdueDebt) {
        this.overdueDebt = overdueDebt;
    }

    public String getDebt() {
        return debt;
    }

    public void setDebt(String debt) {
        this.debt = debt;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getOverallDebt() {
        return overallDebt;
    }

    public void setOverallDebt(String overallDebt) {
        this.overallDebt = overallDebt;
    }

    public String getOverallOverdueDebt() {
        return overallOverdueDebt;
    }

    public void setOverallOverdueDebt(String overallOverdueDebt) {
        this.overallOverdueDebt = overallOverdueDebt;
    }
}
