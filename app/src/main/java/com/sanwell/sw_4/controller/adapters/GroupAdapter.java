package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sanwell.sw_4.model.database.objects.Group;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.R;

import java.util.ArrayList;

public class GroupAdapter extends ArrayAdapter<Group> {

    private final Context context;
    private final ArrayList<Group> values;
    private ArrayList<Group> filteredValues;
    private String chain;
    private String defaultChain;
    private Integer selectedCategory;

    public GroupAdapter(Context context, ArrayList<Group> values) {
        super(context, R.layout.group_item, values);
        this.context = context;
        this.values = values;
        filteredValues = new ArrayList<>(values);
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    public void setSearchRequest(String searchRequest) {
        if (searchRequest == null || searchRequest.isEmpty()) {
            filteredValues = new ArrayList<>(values);
            for (Group group : values) {
                group.setSearchRequest(searchRequest);
            }
        } else {
            filteredValues = new ArrayList<>();
            for (Group group : values) {
                group.setSearchRequest(searchRequest);
                if (group.getNumberOfFilteredChildren() != 0 || group.hasSubCategories()) {
                    filteredValues.add(group);
                }
            }
        }
    }

    public void setSelectedCategory(Integer selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    @Override
    public int getCount() {
        return filteredValues.size() + 1;
    }

    @Override
    public Group getItem(int position) {
        if (position != 0) {
            return filteredValues.get(position - 1);
        }
        return null;
    }

    public ArrayList<Item> getSelectedGroupItems() {
        if (selectedCategory != null && selectedCategory != -1 && values.size() != 0) {
            return getItem(selectedCategory).getChildren();
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.group_item, parent, false);
        }
        ImageView disclosureIndicator = (ImageView) view.findViewById(R.id.group_item_disclosure);
        TextView textView = (TextView) view.findViewById(R.id.group_item_text_view);
        disclosureIndicator.setVisibility(View.GONE);
        if (position != 0) {
            Group group = getItem(position);
            Boolean hasSubGroups = group.hasSubCategories();
            textView.setBackgroundColor(context.getResources().getColor(R.color.white));
            if (group.getChildren().size() == 0 && !hasSubGroups) {
                textView.setText(context.getString(R.string.empty_group_title, group.getTitle()));
                textView.setTextColor(context.getResources().getColor(R.color.subtitle_black));
            } else {
                String title = group.getTitle();
                if (group.getChildren().size() != 0) {
                    title += " (" + group.getChildren().size() + ")";
                }
                textView.setText(title);
                textView.setTextColor(context.getResources().getColor(R.color.catalog_item_title));
                if (hasSubGroups)
                    disclosureIndicator.setVisibility(View.VISIBLE);
                if (!hasSubGroups && selectedCategory != null && position == selectedCategory) {
                    textView.setBackgroundColor(context.getResources().getColor(R.color.group_item_highlighted));
                }
            }
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = (int) convertDpToPixel(58, context);
            view.setLayoutParams(lp);
            textView.setSingleLine(false);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setMaxLines(2);
        } else { // header
            if (chain != null && !chain.isEmpty()) {
                textView.setText(chain);
                if (chain.contains("/") && chain.length() > 2) { // make last path item bold
                    Integer pos = chain.lastIndexOf("/");
                    String fPart = chain.substring(0, pos + 1), sPart = chain.replace(fPart, "");
                    textView.setText(Html.fromHtml(fPart + " " + HTMLWrapper.bold(sPart)));
                }
            } else {
                textView.setText(defaultChain);
            }
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setTextColor(context.getResources().getColor(R.color.white));
            textView.setBackgroundColor(context.getResources().getColor(R.color.red_title));
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = (int) convertDpToPixel(38, context);
            textView.setEllipsize(TextUtils.TruncateAt.START);
            view.setLayoutParams(lp);
        }
        return view;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public void setDefaultChain(String defaultChain) {
        this.defaultChain = defaultChain;
    }
}

