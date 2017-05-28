package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RClient;
import com.sanwell.sw_4.model.database.cores.ROrder;
import com.sanwell.sw_4.model.database.cores.ROrderBatchInfo;
import com.sanwell.sw_4.model.database.cores.ROrderItem;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * Created by Roman Kyrylenko on 23/08/16.
 */
public class OrdersListRVAdapter extends RecyclerView.Adapter<OrdersListRVAdapter.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private String selectedClient = null;
    private List<Container> objects;
    private List<RClient> clients;

    public OrdersListRVAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        formObjectsArray();
    }

    private void formObjectsArray() {
        objects = new ArrayList<>();
        objects.add(new Container(null, Type.HEADER));

        // clientId : clientOrders
        Map<String, List<ROrder>> orderMap = new HashMap<>();
        List<ROrder> orders;
        if (selectedClient != null) {
            orders = new ArrayList<>();
            orders.addAll(DataModel.getInstance().getCoreOrders(selectedClient));
        } else {
            orders = DataModel.getInstance().getCoreOrders();
        }
        for (ROrder order : orders) {
            List<ROrder> rOrders = orderMap.get(order.getClientID());
            if (rOrders == null) {
                rOrders = new ArrayList<>();
            }
            rOrders.add(order);
            orderMap.put(order.getClientID(), rOrders);
        }
        Map<RClient, List<ROrder>> clientOrdersMap = new HashMap<>();
        for (String clientId : orderMap.keySet()) {
            RClient client = DataModel.getInstance().getCoreClient(clientId);
            if (client == null) continue;
            clientOrdersMap.put(client, orderMap.get(clientId));
        }

        List<RClient> clients = new ArrayList<>(clientOrdersMap.keySet());
        Collections.sort(clients, new Comparator<RClient>() {
            @Override
            public int compare(RClient lhs, RClient rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        if (this.clients == null) {
            this.clients = new ArrayList<>(clients);
        }

        for (RClient client : clients) {
            if (client.getId() == null) continue;
            List<ROrder> ords = clientOrdersMap.get(client);
            Collections.sort(ords, new Comparator<ROrder>() {
                @Override
                public int compare(ROrder lhs, ROrder rhs) {
                    return lhs.getOpenningDate().compareTo(rhs.getOpenningDate());
                }
            });
            for (ROrder order : ords) {
                Order ord = new Order(order);
                objects.add(new Container(client, Type.CLIENT, ord));
                List<ROrderItem> orderItems = new ArrayList<>(order.getItems());
                List<Item> itemList = new ArrayList<>();
                for (ROrderItem item : orderItems) {
                    itemList.add(new Item(DataModel.getInstance()
                            .getCoreItem(item.getItemID())));
                }
                Collections.sort(itemList, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    }
                });
                for (Item item : itemList) {
                    objects.add(new Container(item, Type.ITEM, ord));
                }
                if (order.getComment() != null && !order.getComment().isEmpty()) {
                    objects.add(new Container(order.getComment(), Type.DESCRIPTION));
                }
            }
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Type type = Type.values()[viewType];
        switch (type) {
            case HEADER: {
                return new HeaderVH(mLayoutInflater.inflate(R.layout.item_orders_list_header, parent, false));
            }
            case DESCRIPTION: {
                return new DescriptionVH(mLayoutInflater.inflate(R.layout.item_orders_list_description, parent, false));
            }
            case CLIENT: {
                return new ClientVH(mLayoutInflater.inflate(R.layout.item_orders_list_client, parent, false));
            }
            case ITEM: {
                return new ItemVH(mLayoutInflater.inflate(R.layout.item_orders_list_item, parent, false));
            }

        }
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setup(objects.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        Container container = objects.get(position);
        return container.type.ordinal();
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    enum Type {
        HEADER, CLIENT, ITEM, DESCRIPTION
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void setup(Container container) {
        }

    }

    class HeaderVH extends ViewHolder {

        private Spinner mSpinner;
        private int selection = 0;

        private AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {

            private void handle(String sClient) {
                if ((sClient == null && selectedClient == null)
                        || (sClient != null
                        && selectedClient != null
                        && sClient.equals(selectedClient))) return;
                selectedClient = sClient;
                formObjectsArray();
                notifyDataSetChanged();
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sClient;
                if (position == 0) {
                    sClient = null;
                } else {
                    sClient = clients.get(position - 1).getId();
                }
                selection = position;
                handle(sClient);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        public HeaderVH(View itemView) {
            super(itemView);
            mSpinner = (Spinner) itemView.findViewById(R.id.client_spinner);
        }

        @Override
        public void setup(Container container) {
            super.setup(container);
            String[] clientsArray = new String[clients.size() + 1];
            clientsArray[0] = itemView.getContext().getResources().getString(R.string.all_clients);
            for (int i = 0; i < clients.size(); i++) {
                clientsArray[i + 1] = clients.get(i).getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_dropdown_item, clientsArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setSelection(selection);
            mSpinner.setOnItemSelectedListener(listener);

        }
    }

    class ClientVH extends ViewHolder {

        private TextView mDateTV, mTitleTV, mCostTV, mCurrencyTV, mStatusTV, mLinesTV;

        public ClientVH(View itemView) {
            super(itemView);
            mDateTV = (TextView) itemView.findViewById(R.id.date_tv);
            mTitleTV = (TextView) itemView.findViewById(R.id.title_tv);
            mCostTV = (TextView) itemView.findViewById(R.id.cost_tv);
            mCurrencyTV = (TextView) itemView.findViewById(R.id.currency_tv);
            mStatusTV = (TextView) itemView.findViewById(R.id.status_tv);
            mLinesTV = (TextView) itemView.findViewById(R.id.lines_tv);
        }

        @Override
        public void setup(Container container) {
            super.setup(container);
            RClient client = (RClient) container.object;
            mTitleTV.setText(client.getName());
            mStatusTV.setText(container.order.isOpen() ? "Н" : "О");
            mDateTV.setText(container.order.getOpenedIn());
            mCurrencyTV.setText(container.order.getCurrencyISO());
            String[] info = container.order.getSummaryDetailed();
            mCostTV.setText(info[1]);
            mLinesTV.setText(info[0]);
        }
    }

    class DescriptionVH extends ViewHolder {

        private TextView mDescriptionTV;

        public DescriptionVH(View itemView) {
            super(itemView);
            mDescriptionTV = (TextView) itemView.findViewById(R.id.description_tv);
        }

        @Override
        public void setup(Container container) {
            super.setup(container);
            mDescriptionTV.setText((String) container.object);
        }
    }

    class ItemVH extends ViewHolder {

        ImageView imageView;
        TextView titleTextView, subtitleTextView;
        TextView costTextView, currencyTextView;

        public ItemVH(View convertView) {
            super(convertView);
            imageView = (ImageView) convertView.findViewById(R.id.items_list_item_image_view);
            titleTextView = (TextView) convertView.findViewById(R.id.items_list_item_title);
            subtitleTextView = (TextView) convertView.findViewById(R.id.items_list_item_subtitle);
            costTextView = (TextView) convertView.findViewById(R.id.items_list_item_cost_text_view);
            currencyTextView = (TextView) convertView.findViewById(R.id.items_list_item_currency_text_view);
        }

        @Override
        public void setup(Container container) {
            super.setup(container);
            Item item = (Item) container.object;
            try {
                if (imageView != null) {
                    imageView.setVisibility(View.VISIBLE);
                    if (imageView.getVisibility() == View.VISIBLE) {
                        // noinspection deprecation
                        imageView.setImageDrawable(itemView.getContext().getResources()
                                .getDrawable(R.drawable.no_photo_icon));
                        if (item.hasPicture()) {
                            String url = item.getImageURL();
                            if (!url.isEmpty()) {
                                Ion.with(itemView.getContext())
                                        .load(url)
                                        .withBitmap()
                                        .placeholder(R.drawable.no_photo_icon)
                                        .intoImageView(imageView);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String title = item.getTitle();
            if (titleTextView != null)
                titleTextView.setText(Html.fromHtml(title.toUpperCase()));
            if (subtitleTextView != null) {
                String subTitle = item.getSubtitle(true);
                if (subTitle.isEmpty())
                    subtitleTextView.setVisibility(View.GONE);
                else
                    subtitleTextView.setText(subTitle);
            }

            Currency orderCurrency = container.order.getCurrency();
            if (currencyTextView != null)
                currencyTextView.setText(orderCurrency.getIso());
            List<ROrderBatchInfo> batchInfos = container.order.getBatchInfos(item);
            Double cost = 0.0;
            for (ROrderBatchInfo batchInfo : batchInfos) {
                cost += orderCurrency.defaultRound(batchInfo.getItemPrice() * batchInfo.getItemsCount(),
                        batchInfo.getItemCurrencyID());
            }

            if (costTextView != null)
                costTextView.setText(String.format(Locale.getDefault(), "%.2f", cost));

        }

    }

    class Container {

        public Object object; // Client, Item or String
        public Type type;
        public Order order;

        public Container(Object object, Type type) {
            this.object = object;
            this.type = type;
        }

        public Container(Object object, Type type, Order order) {
            this(object, type);
            this.order = order;
        }

    }

}
