package com.sanwell.sw_4.model;

/*
 * Created by Roman Kyrylenko on 17/12/15.
 */
public class HTMLWrapper {

    public static String color(String input, String color) {
        return "<font color=\"" + color + "\">" + input + "</font>";
    }

    public static String red(String input) {
        return color(input, "#ea4543");
    }

    public static String green(String input) {
        return color(input, "#03ad7f");
    }

    public static String gray(String input) {
        return color(input, "#A6A6A6");
    }

    public static String bold(String string) {
        return "<b>" + string + "</b>";
    }

    public static String space(Integer count) {
        String spaces = "";
        for (int i = 0; i < count; i++) {
            spaces += "&nbsp;";
        }
        return spaces;
    }

}
