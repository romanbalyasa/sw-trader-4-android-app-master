package com.sanwell.sw_4.controller.adapters;

/*
 * Created by Roman Kyrylenko on 09/12/15.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuggestionsAdapter extends BaseAdapter {

    public static final String TAG = SuggestionsAdapter.class.getSimpleName();

    private ArrayList<String> suggestions = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public SuggestionsAdapter(Context context, ActivityKind activityKind) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        suggestions = getSuggestions(context, activityKind);
    }

    public static String highlightSearchRequest(String searchRequest, String title) {
        if (searchRequest == null || searchRequest.isEmpty()) {
            return title;
        }
        String patternString = "(?i)" + Pattern.quote(searchRequest);
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(title);
        while (matcher.find()) {
            title = title.replace(matcher.group(), HTMLWrapper.gray(matcher.group()));
        }
        return title;
    }

    public static void addSuggestion(Context context, ActivityKind activityKind, String suggestion) {
        if (suggestion == null || suggestion.isEmpty()) {
            Log.i(TAG, "Cannot add suggestion: " + suggestion);
            return;
        } else {
            Log.i(TAG, "Add suggestions: " + suggestion);
        }
        SharedPreferences keyValues = context.getSharedPreferences("suggestions", Context.MODE_PRIVATE);
        SharedPreferences.Editor keyValuesEditor = keyValues.edit();

        ArrayList<String> suggestions = new ArrayList<>();
        suggestions.addAll(getSuggestions(context, activityKind));
        suggestions.add(0, suggestion);
        suggestions = new ArrayList<>(new LinkedHashSet<>(suggestions)); // remove duplications
        if (suggestions.size() > 5) {
            suggestions = new ArrayList<>(suggestions.subList(0, 5));
        }
        StringBuilder sb = new StringBuilder();
        for (String s : suggestions) {
            sb.append(s).append(",");
        }
        keyValuesEditor.putString(activityKind.getKey(), sb.toString());
        keyValuesEditor.apply();
    }

    public static ArrayList<String> getSuggestions(Context context, ActivityKind activityKind) {
        SharedPreferences pref = context.getSharedPreferences("suggestions", Context.MODE_PRIVATE);
        String string = pref.getString(activityKind.getKey(), "");
        if (string.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(string.split("\\s*,\\s*")));
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int i) {
        return suggestions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.suggestion_item, viewGroup, false);
        }
        view.findViewById(R.id.suggestions_item_separator).setVisibility(i == 0 ? View.VISIBLE : View.INVISIBLE);
        ((TextView) view.findViewById(R.id.suggestions_item_text_view)).setText(getItem(i));
        return view;
    }

    public enum ActivityKind {
        CLIENTS_LIST, CATALOGUE, ORDER_LIST;

        public String getKey() {
            switch (this) {
                case CLIENTS_LIST:
                    return "CLIENTS_LIST";
                case CATALOGUE:
                    return "GUE";
                case ORDER_LIST:
                    return "ORDER_LIST";
            }
            return "null";
        }
    }
}
