package com.sanwell.sw_4.controller.adapters;

/*
 * Created by Roman Kyrylenko on 04/12/15.
*/

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.database.objects.Currency;

import java.util.ArrayList;

public class CurrencySpinnerAdapter extends ArrayAdapter<String> {

    private ArrayList<Currency> objects;
    private Context context;
    private String subTitle;
    private Integer selectedItem;

    public CurrencySpinnerAdapter(Context context, int textViewResourceId, ArrayList<Currency> objects) {
        super(context, textViewResourceId);
        this.objects = objects;
        this.context = context;
        selectedItem = 0;
    }

    public int getPos(String iso) {
        iso = iso.toLowerCase();
        for (int i = 0; i < objects.size(); i++) {
            Currency currency = objects.get(i);
            String cIso = currency.getIso().toLowerCase();
            if (cIso.equals(iso)) {
                return i;
            }
        }
        return 3;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, false);
    }

    public
    @Nullable
    String getItemID(int position) {
        if (position >= 0 && position < objects.size()) {
            return objects.get(position).getId();
        }
        return null;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    public void setSelected(Integer position) {
        selectedItem = position;
        if (selectedItem < 0) {
            selectedItem = 0;
        }
        if (selectedItem < objects.size() && selectedItem >= 0) {
            setSubTitle(objects.get(selectedItem).getIso());
        } else {
            setSubTitle("");
        }
    }

    public View getCustomView(int position, View convertView, Boolean isExpanded) {

        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.currency_spinner, null);
            holder = new ViewHolder();
            holder.subTitleTextView = (TextView) convertView.findViewById(R.id.currency_spinner_subtitle_text_view);
            holder.singleTitleTextView = (TextView) convertView.findViewById(R.id.currency_spinner_single_text_view);
            holder.subTitleLayout = convertView.findViewById(R.id.currency_spinner_subtitle_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setSelected(isExpanded && position == selectedItem);
        holder.setTitle(objects.get(position).getIso());
        if (subTitle == null) { // first init
            setSelected(selectedItem);
        }
        holder.setSubTitle(subTitle);
        Boolean showSubTitle = isExpanded;
        if (subTitle == null) {
            showSubTitle = false;
        }
        holder.setShowSubtitle(showSubTitle);
        return convertView;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    class ViewHolder {

        TextView subTitleTextView;
        TextView singleTitleTextView;
        View subTitleLayout;

        public void setSelected(Boolean isSelected) {
            if (singleTitleTextView != null) {
                if (isSelected) {
                    singleTitleTextView.setTextColor(context.getResources().getColor(R.color.red_title));
                } else {
                    singleTitleTextView.setTextColor(context.getResources().getColor(R.color.inactive_subtitle));
                }
            }
        }

        public void setTitle(String title) {
            if (singleTitleTextView != null && title != null)
                singleTitleTextView.setText(title);
        }

        public void setSubTitle(String subTitle) {
            if (subTitleTextView != null && subTitle != null)
                subTitleTextView.setText(subTitle);
        }

        public void setShowSubtitle(Boolean isExpanded) {
            if (isExpanded) {
                if (subTitleLayout != null)
                    subTitleLayout.setVisibility(View.GONE);
                if (singleTitleTextView != null)
                    singleTitleTextView.setVisibility(View.VISIBLE);
            } else {
                if (subTitleLayout != null)
                    subTitleLayout.setVisibility(View.VISIBLE);
                if (singleTitleTextView != null)
                    singleTitleTextView.setVisibility(View.GONE);
            }
        }
    }


}
