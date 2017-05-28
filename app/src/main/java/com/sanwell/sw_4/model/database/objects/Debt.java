package com.sanwell.sw_4.model.database.objects;

import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RCurrency;
import com.sanwell.sw_4.model.database.cores.RDebt;

/*
 * Created by Roman Kyrylenko on 11/02/16.
 */
public class Debt {

    public static final String CLIENT_ID_COLUMN = "clientID";
    private String currency;
    private Double debt = 0.0;
    private Double prepayment = 0.0;

    public Debt(RDebt debt) {
        if (debt != null) {
            try {
                this.debt = Double.parseDouble(debt.getDebt());
                this.prepayment = Double.parseDouble(debt.getPrepayment());
            } catch (Exception ignored) {
            }
            RCurrency currency = DataModel.getInstance().realm
                    .where(RCurrency.class)
                    .equalTo(Currency.ID_COLUMN, debt.getCurrencyID())
                    .findFirst();
            if (currency != null) {
                this.currency = currency.getIso();
            }
        }
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getDebt() {
        return debt;
    }

    public void setDebt(Double debt) {
        this.debt = debt;
    }

    public Double getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(Double prepayment) {
        this.prepayment = prepayment;
    }

}
