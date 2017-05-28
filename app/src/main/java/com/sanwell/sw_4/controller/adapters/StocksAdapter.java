package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RBatch;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.Order;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Created by Roman Kyrylenko on 05/12/15.
 */
public class StocksAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<RBatch> objects;
    private Order order;
    private Currency orderCurrency;
    private int selectedItem;

    public StocksAdapter(Context context, Item item, Order order) {
        this.order = order;
        objects = new ArrayList<>(item.getBatches());
        orderCurrency = order.getCurrency();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItem = -1;
    }

    public
    @Nullable
    RBatch getSelectedBatch() {
        if (objects != null && selectedItem >= 0 && selectedItem < objects.size()) {
            return objects.get(selectedItem);
        }
        return null;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stock_item, viewGroup, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder == null) {
            return convertView;
        }
        RBatch batch = objects.get(i);

        holder.stockItemID.setText(batch.getWarehouse());
        holder.provider.setText(batch.getSupplier());
        holder.itemDate.setText(batch.getSupplyDate());
        if (orderCurrency != null) {
            holder.minCount.setText(orderCurrency.sDefaultRound(batch.getPurchasePrice(), batch.getCurrencyID()) + " "
                    + orderCurrency.getIso());
        } else {
            holder.minCount.setText(batch.getPurchasePrice());
        }
        holder.left.setText("" + batch.getStock());
        String orderedItems = Helpers.stringFromDouble(order.getOrderedItemsInBatch(batch));
        String overheadOrderedItems = Helpers.stringFromDouble(order.getOverheadOrderedItemsInBatch(batch));
        if (!overheadOrderedItems.equals("0")) {
            holder.ordered.setText(orderedItems
                    + " (+" + overheadOrderedItems + ")");
        } else {
            holder.ordered.setText(orderedItems);
        }

        if (i == 0) { // show/hide top divider
            holder.topDivider.setVisibility(View.VISIBLE);
        } else {
            holder.topDivider.setVisibility(View.GONE);
        }
        if (i == getCount() - 1) { // show/hide bottom divider
            holder.botDivider.setVisibility(View.VISIBLE);
        } else {
            holder.botDivider.setVisibility(View.GONE);
        }
        if (i == selectedItem) {
            holder.selector.setVisibility(View.VISIBLE);
        } else {
            holder.selector.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void setSelectedItem(Integer selectedItem) {
        this.selectedItem = selectedItem == null ? -1 : selectedItem;
        notifyDataSetChanged();
    }

    final class ViewHolder {
        TextView stockItemID, itemDate, provider, minCount, left, ordered;
        View topDivider, botDivider, selector;

        ViewHolder(View view) {
            stockItemID = ((TextView) view.findViewById(R.id.stock_item_id));
            topDivider = view.findViewById(R.id.stock_item_divider_top);
            botDivider = view.findViewById(R.id.stock_item_divider_bottom);
            selector = view.findViewById(R.id.stock_item_selector);
            itemDate = (TextView) view.findViewById(R.id.stock_item_date);
            provider = (TextView) view.findViewById(R.id.stock_item_provider);
            minCount = (TextView) view.findViewById(R.id.stock_item_min_count);
            left = (TextView) view.findViewById(R.id.stock_item_left);
            ordered = (TextView) view.findViewById(R.id.stock_item_ordered);
        }
    }
}
