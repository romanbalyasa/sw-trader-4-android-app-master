package com.sanwell.sw_4.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sanwell.sw_4.model.database.OrdersDataModel;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.Order;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/*
 * Created by Roman Kyrylenko on 16/12/15.
 */
public class Helpers {

    public static final String DEFAULT_EMAIL = "office@sanwell.com.ua"; // office@sanwell.com.ua | scream@sanwell.com.ua
    public static final String DEVICE_USER_ID = "DEVICE_USER_ID";
    public static final String DEVICE_USER_EMAIL_KEY = "DEVICE_USER_EMAIL_KEY";
    public static final String DEVICE_USER_NAME_KEY = "DEVICE_USER_NAME_KEY";
    public static final String SHARED_PREFERENCES_COMMON = "kSHARED_PREFERENCES_COMMON";

    public static Item currentItem;
    public static String orderId = "-1";
    public static Order order() {
        return OrdersDataModel.getInstance().getOrder(orderId);
    }
    public static Client currentClient = null;
    public static String currencyID;

    public static String wrapWithRedHtmlFont(String input) {
        return "<font color=red>" + input + "</font>";
    }

    public static String stringFromDouble(double value) {
        if (value % 1 == 0) {
            return String.valueOf((int)value);
        } else {
            return String.format("%.2f", value);
        }
    }

    public static SharedPreferences getCommonSharedPref(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_COMMON, Context.MODE_PRIVATE);
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void writeToFile(String data, File file) {
        if (data == null || file == null) {
            return;
        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, false);
            stream.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        char tempChar;
        for (int i = 0; i < 32; i++) {
            tempChar = (char) (generator.nextInt(25) + 97);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static void edit_sp(String key, String newValue) {
        Context context = SanwellApplication.applicationContext;
        if (newValue == null) {
            newValue = "";
        }
        if (key == null || context == null) {
            return;
        }
        SharedPreferences.Editor editor = getCommonSharedPref(context).edit();
        editor.putString(key, newValue);
        editor.apply();
    }

    public static String read_sp(String key, String defaultValue) {
        Context context = SanwellApplication.applicationContext;
        if (defaultValue == null) {
            defaultValue = "";
        }
        if (key == null || context == null) {
            return "";
        }
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getString(key, defaultValue);
    }

}
