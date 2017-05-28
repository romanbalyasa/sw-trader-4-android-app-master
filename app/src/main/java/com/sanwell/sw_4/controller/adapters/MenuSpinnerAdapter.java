package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sanwell.sw_4.R;

public class MenuSpinnerAdapter extends ArrayAdapter<String> {

    private String[] objects, expanded = null;
    private Context context;
    private String subTitle;
    private String title;
    private Integer selectedItem;

    public MenuSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        this.context = context;
        selectedItem = 0;
    }

    public MenuSpinnerAdapter(Context context, int textViewResourceId, String[] objects, String[] expanded) {
        super(context, textViewResourceId, objects);
        this.expanded = expanded;
        this.objects = objects;
        this.context = context;
        selectedItem = 0;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, false);
    }

    public void setSelected(Integer position) {
        selectedItem = position;
    }

    public View getCustomView(int position, View convertView, Boolean isExpanded) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.title_spinner, null);
            holder = new ViewHolder();
            holder.context = this.context;
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title_spinner_title_textview);
            holder.subTitleTextView = (TextView) convertView.findViewById(R.id.title_spinner_subtitle_textview);
            holder.singleTitleTextView = (TextView) convertView.findViewById(R.id.title_spinner_single_textview);
            holder.subTitleLayout = convertView.findViewById(R.id.title_spinner_subtitle_layout);
            holder.subTitleArrow = convertView.findViewById(R.id.title_spinner_icon);
            holder.arrow = convertView.findViewById(R.id.title_spinner_single_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setExpanded(isExpanded);
        holder.setTitle(isExpanded ? objects[position] : (title == null ? objects[position] : title));
        if (isExpanded && expanded != null) {
            holder.setTitle(expanded[position]);
        }
        holder.setSubTitle(subTitle);
        holder.setSelected(isExpanded && position == selectedItem);
        Boolean showSubTitle = !isExpanded;
        if (subTitle == null || subTitle.isEmpty())
            showSubTitle = false;
        holder.setShowSubtitle(showSubTitle);
        return convertView;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    class ViewHolder {

        private View arrow;
        private View subTitleLayout;
        private View subTitleArrow;
        private TextView titleTextView;
        private TextView subTitleTextView;
        private TextView singleTitleTextView;
        private Boolean isExpanded;
        private Context context;

        public void setExpanded(Boolean isExpanded) {
            this.isExpanded = isExpanded;
            TypedValue tv = new TypedValue();
            Resources r = context.getResources();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            int actionBarHeight = r.getDimensionPixelSize(tv.resourceId);
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, r.getDisplayMetrics());
            singleTitleTextView.getLayoutParams().height = isExpanded ? (int) px : actionBarHeight;
            singleTitleTextView.requestLayout();

        }

        public void setSelected(Boolean isSelected) {
            if (singleTitleTextView != null) {
                if (isSelected) {
                    singleTitleTextView.setTextColor(context.getResources().getColor(R.color.red_title));
                } else {
                    singleTitleTextView.setTextColor(context.getResources().getColor(isExpanded ? R.color.inactive_subtitle : R.color.black));
                }
            }
        }

        public void setTitle(String title) {
            if (titleTextView != null && title != null)
                titleTextView.setText(title);
            if (singleTitleTextView != null && title != null)
                singleTitleTextView.setText(title);
        }

        public void setSubTitle(String subTitle) {
            if (subTitleTextView != null && subTitle != null)
                subTitleTextView.setText(subTitle);
        }

        public void setShowSubtitle(Boolean showSubtitle) {
            if (arrow != null) {
                arrow.setVisibility((isExpanded || showSubtitle) ? View.GONE : View.VISIBLE);
                arrow.requestLayout();
            }
            if (subTitleLayout != null) {
                subTitleLayout.setVisibility((!isExpanded && showSubtitle) ? View.VISIBLE : View.GONE);
                subTitleArrow.setVisibility(subTitleLayout.getVisibility());
                subTitleLayout.requestLayout();
            }
            if (singleTitleTextView != null) {
                singleTitleTextView.setVisibility(showSubtitle ? View.GONE : View.VISIBLE);
                singleTitleTextView.requestLayout();
            }
        }
    }


}
