package com.sanwell.sw_4.model.database.objects;


import android.support.annotation.NonNull;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RBatch;
import com.sanwell.sw_4.model.database.cores.RCurrency;
import com.sanwell.sw_4.model.database.cores.RImageUrl;
import com.sanwell.sw_4.model.database.cores.RItem;
import com.sanwell.sw_4.model.database.cores.RItemPlanInfo;
import com.sanwell.sw_4.model.database.cores.RPriceSuggestion;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class Item {

    public static final String ITEM_ID_COLUMN = "id";
    public static final String BATCH_ITEM_ID_COLUMN = "itemID";

    private String title;
    private String ID;
    private String currencyID;
    private double cost = 0.0;
    private double plan;
    private double fact;
    private boolean hasPicture = false;
    private boolean isFixedCurrency, isPromoted, isNew, isFocused, isPricedUp;
    private String stock, numberOfItemsInPackage;
    private String measurement;
    private Integer pickedImageThumb;
    private String npgId;
    private double m2_price, m_m5_price, m_m4_price, m_m3_price;
    private double m_m2_price;
    private double m_price;
    private double a_price;
    private double a_p2_price;
    private double multFactor;
    private double a_a5_price;
    private double b_price, retail_price;
    private int orderMinCount;


    // BRG цена из заказа - начало
    private double orderPrice = 0.0;
    public boolean setOrderPrice(Double order_Price) {
        this.orderPrice = order_Price;
        return true;
    }
    // BRG - окончание


    public Item(RItemPlanInfo info) {
        if (info == null) {
            return;
        }
        RItem item = DataModel.getInstance().realm.where(RItem.class).equalTo("id", info.getGoodId()).findFirst();
        if (item == null) {
            return;
        }
        readItem(item);
        plan = info.getPlanned();
        fact = info.getFact();
    }

    public Item(RItem item) {
        if (item != null) {
            readItem(item);
        }
    }

    public static Double safeParse(String s) {
        Double v = 0.0;
        try {
            v = Double.parseDouble(s.replace(",", "."));
        } catch (Exception ignored) {
        }
        return v;
    }

    public int getOrderMinCount() {
        return orderMinCount;
    }

    public boolean isFixedCurrency() {
        return isFixedCurrency;
    }

    private void readItem(RItem item) {
        title = item.getTitle();
        ID = item.getId();
        hasPicture = item.isHasPicture();
        currencyID = item.getCurrencyID();
        npgId = item.getNpgId();
        m_m2_price = safeParse(item.getM_m2_price());
        m_m3_price = safeParse(item.getM_m3_price());
        m_m4_price = safeParse(item.getM_m4_price());
        m_m5_price = safeParse(item.getM_m5_price());
        m2_price = safeParse(item.getM2_price());
        m_price = safeParse(item.getM_price());
        a_price = safeParse(item.getA_price());
        a_p2_price = safeParse(item.getA_p2_price());
        a_a5_price = safeParse(item.getA_a5_price());
        b_price = safeParse(item.getB_price());
        retail_price = safeParse(item.getRetail_price());
        stock = item.getStockCount();
        numberOfItemsInPackage = item.getPackaging();
        isPricedUp = item.isPricedUp();
        isFixedCurrency = item.isFixedCurrency();
        isNew = item.isNew();
        isFocused = item.isFocused();
        multFactor = item.getMultFactor();
        isPromoted = item.isPromoted();
        measurement = item.getMeasurement();
        pickedImageThumb = item.getPickedImageThumb();
        orderMinCount = item.getMinOrder();
        try {
            cost = Double.parseDouble(item.getPrice());
        } catch (Exception ignored) {
        }
    }

    public double getMultFactor() {
        return multFactor == 0 ? 1 : multFactor;
    }

    public Integer getPickedImageThumb() {
        return pickedImageThumb;
    }

    public void setPickedImageThumb(Integer pickedImageThumb) {
        this.pickedImageThumb = pickedImageThumb;
        Realm realm = DataModel.getInstance().realm;
        RItem item = realm.where(RItem.class)
                .equalTo(ITEM_ID_COLUMN, ID)
                .findFirst();
        if (item != null) {
            realm.beginTransaction();
            item.setPickedImageThumb(pickedImageThumb);
            realm.commitTransaction();
        }
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getStock() {
        return stock;
    }

    public String getNumberOfItemsInPackage() {
        return numberOfItemsInPackage;
    }

    public boolean hasPicture() {
        return hasPicture;
    }

    public String getCurrency() {
        RCurrency currency = DataModel.getInstance()
                .realm
                .where(RCurrency.class)
                .equalTo(Currency.ID_COLUMN, currencyID)
                .findFirst();
        if (currency != null) {
            return currency.getIso();
        }
        return "";
    }

    public String getCurrencyId() {
        return currencyID;
    }

    public Double getCost() {
        double m_5 = m_m5_price > m2_price ? m_m5_price : 0,
                m_4 = m_m4_price > m2_price ? m_m4_price : 0,
                m_3 = m_m3_price > m2_price ? m_m3_price : 0,
                m_2 = m_m2_price > m2_price ? m_m2_price : 0;
        double[] prices = {m2_price, m_5, m_4, m_3, m_2, m_price, a_price, a_p2_price, a_a5_price, b_price, retail_price};
        String[] codes = {"1", "2", "3", "4", "11", "5", "6", "7", "8", "9", "10"};
        RPriceSuggestion priceSuggestion = DataModel.getInstance().realm
                .where(RPriceSuggestion.class)
                .equalTo("clientId", "0")
                .equalTo("npgId", "0")
                .findFirst();
        if (priceSuggestion != null)
            for (int i = 0; i < codes.length; i++) {
                if (priceSuggestion.getPriceId().equals(codes[i])) {
                    return prices[i];
                }
            }
        return cost;
    }

    public String getSubtitle(Boolean isCommonList) {
        String subtitle = "";
        ArrayList<String> inp = new ArrayList<>();
        if (isCommonList) {
            inp.add(getStatuses());
            if (stock != null) {
                inp.add(stock);
            }
            if (numberOfItemsInPackage != null) {
                inp.add(numberOfItemsInPackage);
            }
        } else {
            inp.add("Продано: " + (int) fact + " (" + (int) (100 * fact / plan) + "%)");
            inp.add("По плану: " + (int) plan);
            if (stock != null && !stock.isEmpty()) {
                inp.add(SanwellApplication.applicationContext.getString(R.string.available_short_string, stock));
            }
        }
        for (String str : inp) {
            if (!str.isEmpty()) {
                if (subtitle.isEmpty()) {
                    subtitle += str;
                } else {
                    subtitle += ", " + str;
                }
            }
        }

        return subtitle;
    }

    public String getItemID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    public String getImageThumbURL() {
        if (pickedImageThumb == null || pickedImageThumb == 0) {
            return getImageURL();
        }
        RealmResults<RImageUrl> url = DataModel.getInstance()
                .realm
                .where(RImageUrl.class)
                .equalTo(ImageUrl.ITEM_ID_COLUMN, ID)
                .findAll();
        String ur = "";
        if (pickedImageThumb < url.size()) {
            RImageUrl rImageUrl = url.get(pickedImageThumb);
            File f = new File(RImageUrl.getFileUrlForImage(rImageUrl));
            if (f.exists()) {
                ur = f.getAbsolutePath();
            } else {
                ur = rImageUrl.getUrl();
            }
        }
        return ur;
    }

    @NonNull
    public String getImageURL() {
        RImageUrl url = DataModel.getInstance()
                .realm
                .where(RImageUrl.class)
                .equalTo(ImageUrl.ITEM_ID_COLUMN, ID)
                .findFirst();
        if (url != null) {
            File file = new File(RImageUrl.getFileUrlForImage(url));
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            return url.getUrl() == null ? "" : url.getUrl();
        }
        return "";
    }

    public ArrayList<ImageUrl> getImageURLS() {
        ArrayList<ImageUrl> urls = new ArrayList<>();
        RealmResults<RImageUrl> realmResults = DataModel.getInstance()
                .realm
                .where(RImageUrl.class)
                .equalTo(ImageUrl.ITEM_ID_COLUMN, ID)
                .findAll();
        for (int i = 0; i < realmResults.size(); i++) {
            urls.add(new ImageUrl(realmResults.get(i)));
        }
        return urls;
    }

//            «M2».
//            «M-5%». Цена «М» - 5%. Расчетный параметр.
//            «M-4%». Цена «М» - 4%. Расчетный параметр.
//            «M-3%». Цена «М» - 3%. Расчетный параметр.
//            «M-2%».
//            «М».
//            «А». Базовая оптовая цена. Хранимый параметр.
//            «Ц». Индикативный показатель дополнительного вознаграждения. Расчетный параметр.
//            «А+2%». Цена «А» + 2%. Расчетный параметр.
//            «А+5%». Цена «А» + 5%. Расчетный параметр.
//            «Б». «Дорогая» оптовая цена. Хранимый параметр.
//            «Р». Розничная цена. Практически не используется. Хранимый параметр.


    public Integer fillPricePointsArray(ArrayList<Integer> points, ArrayList<ItemPriceInfo> priceInfos, Currency orderCurrency, String clientId) {
        if (isFixedCurrency()) {
            orderCurrency = new Currency(getCurrencyId());
        }
        String currency = orderCurrency.getIso();
        if (isFixedCurrency()) {
            currency = new Currency(currencyID).getIso();
        }

        RPriceSuggestion planInfo = DataModel.getInstance().realm
                .where(RPriceSuggestion.class)
                .equalTo("clientId", clientId)
                .equalTo("npgId", npgId)
                .findFirst();

        if (planInfo == null) {
            planInfo = DataModel.getInstance().realm
                    .where(RPriceSuggestion.class)
                    .equalTo("clientId", clientId)
                    .equalTo("npgId", "0")
                    .findFirst();
        }
        if (planInfo == null) {
            planInfo = DataModel.getInstance().realm
                    .where(RPriceSuggestion.class)
                    .equalTo("clientId", "0")
                    .equalTo("npgId", "0")
                    .findFirst();
        }

        String suggestedPriceId = ""; // m2
        if (planInfo != null && planInfo.getPriceId() != null) {
            suggestedPriceId = planInfo.getPriceId();
        }

        double m2 = orderCurrency.specialRound(m2_price, currencyID);
        double m_5 = m_m5_price > m2_price ? m_m5_price : 0,
                m_4 = m_m4_price > m2_price ? m_m4_price : 0,
                m_3 = m_m3_price > m2_price ? m_m3_price : 0,
                m_2 = m_m2_price > m2_price ? m_m2_price : 0;
        m_5 = orderCurrency.specialRound(m_5, currencyID);
        m_4 = orderCurrency.specialRound(m_4, currencyID);
        m_3 = orderCurrency.specialRound(m_3, currencyID);
        m_2 = orderCurrency.specialRound(m_2, currencyID);
        double m = orderCurrency.specialRound(m_price, currencyID);
        double a = orderCurrency.specialRound(a_price, currencyID);
        double a2 = orderCurrency.specialRound(a_p2_price, currencyID);
        double a5 = orderCurrency.specialRound(a_a5_price, currencyID);
        double b = orderCurrency.specialRound(b_price, currencyID);
        double p = orderCurrency.specialRound(retail_price, currencyID);
        double[] prices;
        String[] names;
        String[] sales;
        String[] codes;
        if (suggestedPriceId.equals("1")) {
            prices = new double[]{m2, m_5, m_4, m_3, m_2, m, a, a2, a5, b, p};
            names = new String[]{"M", "M", "M", "M", "M", "M", "A", "А", "А", "Б", "Р"};
            sales = new String[]{"2", "-5%", "-4%", "-3%", "-2%", "", "", "+2%", "+5%", "", ""};
            codes = new String[]{"1", "2", "3", "4", "11", "5", "6", "7", "8", "9", "10"};
        } else {
            prices = new double[]{m_5, m_4, m_3, m_2, m, a, a2, a5, b, p};
            names = new String[]{"M", "M", "M", "M", "M", "A", "А", "А", "Б", "Р"};
            sales = new String[]{"-5%", "-4%", "-3%", "-2%", "", "", "+2%", "+5%", "", ""};
            codes = new String[]{"2", "3", "4", "11", "5", "6", "7", "8", "9", "10"};
        }
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (double price : prices) {
            if (price < min && price != 0) {
                min = price;
            }
            if (price > max && price != 0) {
                max = price;
            }
        }

        int selection = 0;
        int j = 0;
        for (int i = 0; i < prices.length; i++) {
            double price = prices[i];
            if (price == 0) {
                continue;
            }
            String name = names[i];
            String sale = sales[i];
            String code = codes[i];
            if (code.equals(suggestedPriceId) & this.orderPrice == 0.0) {
                selection = j;
            }
            if (price == this.orderPrice)
            {
                selection = j;
            }
            j++;
            priceInfos.add(new ItemPriceInfo(String.format(Locale.US, "%.2f", price), currency, name, sale, code, suggestedPriceId));
            points.add((int) (100 * ((price - min) / (max - min))));
        }
        return selection;
    }

    public ArrayList<RBatch> getBatches() {
        ArrayList<RBatch> batches = new ArrayList<>();
        RealmResults<RBatch> realmResults = DataModel.getInstance().realm
                .where(RBatch.class)
                .equalTo(BATCH_ITEM_ID_COLUMN, ID)
                .findAll();
        for (int i = 0; i < realmResults.size(); i++) {
            batches.add(realmResults.get(i));
        }
        return batches;
    }

    public String getExpandedStatuses() {
        String status = "";
        if (isPromoted) {
            status += "Акция, ";
        }
        if (isPricedUp) {
            status += "Цена повышена, ";
        }
        if (isNew) {
            status += "Новинка, ";
        }
        if (isFocused) {
            status += "Фокус группа";
        }
        if (status.endsWith(", ")) {
            status = status.substring(0, status.length() - 2);
        }
        return status;
    }

    public String getStatuses() {
        String status = "";
        if (isPromoted) {
            status += "А, ";
        }
        if (isPricedUp) {
            status += "Ц, ";
        }
        if (isNew) {
            status += "H, ";
        }
        if (isFocused) {
            status += "Ф";
        }
        if (status.endsWith(", ")) {
            status = status.substring(0, status.length() - 2);
        }
        return status;
    }

    public double getFact() {
        return fact;
    }

    public double getPlan() {
        return plan;
    }
}
