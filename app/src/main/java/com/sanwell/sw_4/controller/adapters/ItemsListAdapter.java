package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.activities.OrderListActivity;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.cores.ROrderBatchInfo;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.Order;
import com.sanwell.sw_4.model.interfaces.ItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemsListAdapter extends BaseAdapter {

    final private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Item> sortedList;
    private ArrayList<Item> objectsList;
    private Boolean showImages;
    private ItemClickListener cartOnClickListener;
    private String searchRequest;
    private Boolean isInOrderList = false;
    private OrderListActivity.Mode mode = OrderListActivity.Mode.ORDER;
    private boolean isFullListShowed = false;

    public ItemsListAdapter(Context context, ArrayList<Item> products, ItemClickListener cartOnClickListener) {
        objectsList = products;
        sortedList = new ArrayList<>(products);
        showImages = false;
        for (Item item : products) {
            if (item.hasPicture()) {
                showImages = true;
                break;
            }
        }
        this.cartOnClickListener = cartOnClickListener;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setObjectsList(ArrayList<Item> objectsList) {
        this.objectsList = objectsList;
        sortedList = new ArrayList<>(objectsList);
        setSearchRequest(searchRequest);
    }

    public void setMode(OrderListActivity.Mode mode) {
        if (this.mode != mode && isInOrderList) {
            this.mode = mode;
            Order order = Helpers.order();
            if (order == null) {
                return;
            }
            sortedList = new ArrayList<>();
            String target = searchRequest == null ? "" : searchRequest.toLowerCase();
            if (mode == OrderListActivity.Mode.OVERHEAD) {
                for (Item item : objectsList) {
                    if (target.isEmpty()) {
                        if (!item.getTitle().toLowerCase().contains(target)) {
                            continue;
                        }
                    }
                    ROrderBatchInfo info = order.getBatchInfo(item);
                    if (info != null && info.getOverheadItemsCount() > 0) {
                        sortedList.add(item);
                    }
                }
            } else {
                sortedList.addAll(objectsList);
            }
            notifyDataSetChanged();
        }
    }

    public String getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(String request) {
        searchRequest = request;
        if (isInOrderList && searchRequest != null && !searchRequest.isEmpty()) {
            sortedList = new ArrayList<>();
            String target = searchRequest.toLowerCase();
            for (Item item : objectsList) {
                if (item.getTitle().toLowerCase().contains(target)) {
                    sortedList.add(item);
                }
            }
        } else {
            sortedList = new ArrayList<>(objectsList);
        }
        notifyDataSetChanged();
    }

    public void setIsFullListShowed(boolean isFullListShowed) {
        this.isFullListShowed = isFullListShowed;
    }

    public void setIsInOrderList(Boolean isInOrderList) {
        this.isInOrderList = isInOrderList;
    }

    @Override
    public int getCount() {
        return sortedList.size();
    }

    @Override
    public Item getItem(int i) {
        return sortedList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.items_list_item, parent, false);
            holder = new ViewHolder(convertView, isInOrderList);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = sortedList.get(position);
        try {
            if (holder.imageView != null) {
                holder.imageView.setVisibility(showImages ? View.VISIBLE : View.GONE);
                if (holder.imageView.getVisibility() == View.VISIBLE) {
                    // noinspection deprecation
                    holder.imageView.setImageDrawable(context.getResources()
                            .getDrawable(R.drawable.no_photo_icon));
                    if (item.hasPicture()) {
                        String url = item.getImageURL();
                        if (!url.isEmpty()) {
                            Ion.with(context)
                                    .load(url)
                                    .withBitmap()
                                    .placeholder(R.drawable.no_photo_icon)
                                    .intoImageView(holder.imageView);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String title = item.getTitle();
        if (searchRequest != null && !searchRequest.isEmpty()) {
            title = SuggestionsAdapter.highlightSearchRequest(searchRequest, title);
            if (holder.imageView != null)
                holder.imageView.setVisibility(View.VISIBLE);
        }
        if (holder.titleTextView != null)
            holder.titleTextView.setText(Html.fromHtml(title.toUpperCase()));
        if (holder.subtitleTextView != null) {
            String subTitle = item.getSubtitle(isFullListShowed);
            if (subTitle.isEmpty())
                holder.subtitleTextView.setVisibility(View.GONE);
            else
                holder.subtitleTextView.setText(subTitle);
        }
        Order order = Helpers.order();
        if (order == null) {
            order = Helpers.currentClient.getOrder();
        }
        if (order == null) {
            return convertView;
        }
        Currency orderCurrency = order.getCurrency();
        if (holder.currencyTextView != null)
            holder.currencyTextView.setText(orderCurrency.getIso());
        boolean isItemInOrder = order.isItemInOrder(item);
        double count = order.getItemsCount(item);
        if (isInOrderList || isItemInOrder) {
            List<ROrderBatchInfo> batchInfos = order.getBatchInfos(item);
            Double cost = 0.0;
            for (ROrderBatchInfo batchInfo : batchInfos) {
                cost += orderCurrency.defaultRound(batchInfo.getItemPrice() * batchInfo.getItemsCount(),
                        batchInfo.getItemCurrencyID());
            }

            // BRG 2017/04/10 вставка цены (получается, что средней по позиции) - начало
            if (holder.costTextView != null) {
                Double priceOrd = cost / count;
                String priceOrdtext = " (цена: " + Helpers.stringFromDouble(priceOrd)+")";
                holder.costTextView.setText(String.format(Locale.getDefault(), "%.2f", cost) + priceOrdtext);
            }
            // BRG 2017/04/10 вставка цены (получается, что средней по позиции) - конец

            if (holder.countTextView != null) {
                String text = "x" + Helpers.stringFromDouble(count);
                double overHeadCount = order.getOverheadOrderedOfItem(item);
                if (overHeadCount != 0) {
                    text += " (+" + Helpers.stringFromDouble(overHeadCount) + ")";
                }
                holder.countTextView.setText(text);
                holder.countTextView.setVisibility(View.VISIBLE);
            }
            if (holder.plusButton != null) {
                holder.plusButton.setVisibility(View.INVISIBLE);
                holder.plusButton.setTag(position);
            }
            if (holder.cartButton != null) {
                holder.cartButton.setVisibility(View.VISIBLE);
                holder.cartButton.setVisibility(order.isOpen() ? View.VISIBLE : View.GONE);
                holder.cartButton.setTag(position);
            }
        } else {
            if (holder.costTextView != null)
                holder.costTextView.setText(orderCurrency.sDefaultRound(item.getCost(),
                        item.getCurrencyId()));
            if (holder.plusButton != null) {
                holder.plusButton.setVisibility(View.VISIBLE);
                holder.plusButton.setTag(position);
            }
            if (holder.cartButton != null) {
                holder.cartButton.setVisibility(View.GONE);
                holder.cartButton.setTag(position);
            }
            if (holder.countTextView != null) {
                holder.countTextView.setVisibility(View.GONE);
            }
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartOnClickListener != null && view != null) {
                    int position = (Integer) view.getTag();
                    cartOnClickListener.onClick(getItem(position));
                }
            }
        };
        if (holder.cartButton != null)
            holder.cartButton.setOnClickListener(listener);
        if (holder.plusButton != null)
            holder.plusButton.setOnClickListener(listener);
        if (holder.topDivider != null)
            holder.topDivider.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    static class ViewHolder {

        ImageView imageView;
        TextView titleTextView, subtitleTextView;
        TextView costTextView, currencyTextView, countTextView;
        ImageButton cartButton, plusButton;
        View topDivider;

        ViewHolder(View convertView, boolean isInOrderList) {
            imageView = (ImageView) convertView.findViewById(R.id.items_list_item_image_view);
            titleTextView = (TextView) convertView.findViewById(R.id.items_list_item_title);
            subtitleTextView = (TextView) convertView.findViewById(R.id.items_list_item_subtitle);
            cartButton = (ImageButton) convertView.findViewById(isInOrderList ?
                    R.id.items_list_item_delete_button : R.id.items_list_item_basket_button);
            topDivider = convertView.findViewById(R.id.items_list_item_divider_top);
            countTextView = (TextView) convertView.findViewById(R.id.items_list_item_count_text_view);
            costTextView = (TextView) convertView.findViewById(R.id.items_list_item_cost_text_view);
            currencyTextView = (TextView) convertView.findViewById(R.id.items_list_item_currency_text_view);
            plusButton = (ImageButton) convertView.findViewById(R.id.items_list_item_plus_button);
        }

    }

}
