package com.sanwell.sw_4.controller.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.activities.OrderListActivity;
import com.sanwell.sw_4.controller.adapters.ClientsListOrderAdapter;
import com.sanwell.sw_4.controller.adapters.CurrencySpinnerAdapter;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.OrdersDataModel;
import com.sanwell.sw_4.model.database.cores.ROrderBatchInfo;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Order;
import com.sanwell.sw_4.model.interfaces.OnSpinnerEventsListener;
import com.sanwell.sw_4.view.CurrencySpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientsListClientFragment extends Fragment {

    private Client selectedClient;
    private SwipeMenuListView orderListView;
    private CurrencySpinner orderCurrencySpinner;
    private RelativeLayout openOrderInfoLayout;
    private RelativeLayout orderInfoLayout;
    private CurrencySpinnerAdapter currencySpinnerAdapter;
    private ClientsListOrderAdapter clientsListOrderAdapter;

    private TextView openedIn, costSummary, numberOfItems, overheadItems;
    private View commentIconView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients_list, container, false);
        initLayout(view);
        setSelectedClient(null);
        manageCurrencySpinner();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                openOrderInfoLayout.setOnTouchListener(null);
                orderInfoLayout.setOnTouchListener(null);
                Order o = Helpers.currentClient.getOrder();
                if (o != null) {
                    Helpers.orderId = o.getOrderID();
                } else {
                    Helpers.orderId = Helpers.currentClient.addOrder().getOrderID();
                }
                startActivity(new Intent(getActivity(), OrderListActivity.class));
                return true;
            }
        };
        openOrderInfoLayout.setOnTouchListener(listener);
        orderInfoLayout.setOnTouchListener(listener);
        setSelectedClient(selectedClient);
    }

    private void initLayout(View view) {
        orderListView = (SwipeMenuListView) view.findViewById(R.id.fragment_clients_list_order_list_view);
        orderCurrencySpinner = (CurrencySpinner) view.findViewById(R.id.fragment_clients_list_order_currency_spinner);
        openOrderInfoLayout = (RelativeLayout) view.findViewById(R.id.fragment_clients_list_open_order_info_layout);
        orderInfoLayout = (RelativeLayout) view.findViewById(R.id.fragment_clients_list_order_info_layout);
        openedIn = (TextView) view.findViewById(R.id.fragment_clients_list_open_order_info_date_text_view);
        costSummary = (TextView) view.findViewById(R.id.fragment_clients_list_open_order_info_bill_text_view);
        numberOfItems = (TextView) view.findViewById(R.id.fragment_clients_list_open_order_info_count_text_view);
        overheadItems = (TextView) view.findViewById(R.id.fragment_clients_list_open_order_info_reject_text_view);
        commentIconView = view.findViewById(R.id.comment_icon_view);
    }

    public void setSelectedClient(Client client) {
        selectedClient = client != null && client.isSectionHeader() ? null : client;
        if (selectedClient == null) {
            return;
        }
        setupOrderList();
        Order order = selectedClient.getOrder();
        if (order != null) {
            orderCurrencySpinner.setIsClickAvailable(!order.isOpen());
            openOrderInfoLayout.setVisibility(View.VISIBLE);
            orderInfoLayout.setVisibility(View.GONE);

            orderCurrencySpinner.setSelection(currencySpinnerAdapter.getPos(order.getCurrencyISO()));
            String openedInDate = new SimpleDateFormat("HH:mm").format(order.getOppeningDate());
            openedIn.setText(getString(R.string.opened_in, openedInDate));
            if (order.getComment().isEmpty()) {
                commentIconView.setVisibility(View.GONE);
            } else {
                commentIconView.setVisibility(View.VISIBLE);
            }
            if (order.isEmpty()) {
                costSummary.setText("ПУСТОЙ");
                overheadItems.setText("");
                numberOfItems.setText("");
            } else {
                List<ROrderBatchInfo> batchInfos = order.getBatchInfos();
                Double cost = 0.0;
                double numberOfItems = 0, overheadItems = 0;
                Currency currency = order.getCurrency();
                for (ROrderBatchInfo info : batchInfos) {
                    double c = info.getItemsCount();
                    double p = info.getItemPrice();
                    cost += currency.defaultRound(c * p, info.getItemCurrencyID());
                    numberOfItems += info.getItemsCount();
                    overheadItems += info.getOverheadItemsCount();
                }
                this.overheadItems.setText("Отказ: " + Helpers.stringFromDouble(overheadItems) + " поз.");
                this.numberOfItems.setText(", " + Helpers.stringFromDouble(numberOfItems) + " поз.");
                costSummary.setText(String.format(Locale.getDefault(), "%.2f %s", cost, currency.getIso()));
            }
        } else {
            orderCurrencySpinner.setSelection(0);
            orderCurrencySpinner.setIsClickAvailable(true);
            openOrderInfoLayout.setVisibility(View.GONE);
            orderInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    public void handleOnWindowFocusChanged(Boolean hasFocus) {
        if (orderCurrencySpinner != null) {
            orderCurrencySpinner.handleOnWindowFocusChanged(hasFocus);
        }
    }

    public void manageCurrencySpinner() {
        currencySpinnerAdapter = new CurrencySpinnerAdapter(getActivity(),
                R.layout.currency_spinner,
                new ArrayList<>(DataModel.getInstance().getCurrencies().values()));
        orderCurrencySpinner.setAdapter(currencySpinnerAdapter);
        orderCurrencySpinner.setSpinnerEventsListener(new OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened() {
                getActivity().findViewById(R.id.clients_list_gray_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onSpinnerClosed() {
                getActivity().findViewById(R.id.clients_list_gray_layout).setVisibility(View.INVISIBLE);
            }
        });
        orderCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                currencySpinnerAdapter.setSelected(itemPosition);
                currencySpinnerAdapter.notifyDataSetChanged();
                Helpers.currencyID = currencySpinnerAdapter.getItemID(itemPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupOrderList() {
        clientsListOrderAdapter = new ClientsListOrderAdapter(getActivity(), selectedClient);
        orderListView.setAdapter(clientsListOrderAdapter);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                if (menu.getViewType() == ClientsListOrderAdapter.EntryType.ORDER_STATE_LOCAL.ordinal()
                        || menu.getViewType() == ClientsListOrderAdapter.EntryType.ORDER_STATE_UPLOADED.ordinal()) {
                    SwipeMenuItem delete_Item = new SwipeMenuItem(getActivity());
                    delete_Item.setBackground(new ColorDrawable(Color.rgb(232, 67, 65)));

                    Resources r = getResources();
                    int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 164, r.getDisplayMetrics());
                    delete_Item.setWidth(px);
                    delete_Item.setIcon(R.drawable.delete_icon_white);

                    menu.addMenuItem(delete_Item);
                }
            }
        };
        orderListView.setMenuCreator(creator);
        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int type = clientsListOrderAdapter.getItemViewType(i);
                if (type == ClientsListOrderAdapter.EntryType.ORDER_STATE_LOCAL.ordinal()
                        || type == ClientsListOrderAdapter.EntryType.ORDER_STATE_UPLOADED.ordinal()) {
                    Order order = clientsListOrderAdapter.orderAtIndex(i);
                    if (order != null) {
                        Helpers.orderId = order.getOrderID();
                    }
                    Intent mainIntent = new Intent(getActivity(), OrderListActivity.class);
                    startActivity(mainIntent);
                }
            }
        });
        orderListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int i, SwipeMenu swipeMenu, int i2) {
                Order ord = clientsListOrderAdapter.orderAtIndex(i);
                if (ord != null) {
                    OrdersDataModel.getInstance().delete(ord.getOrderID());
                    clientsListOrderAdapter.setClient(clientsListOrderAdapter.getClient());
                }
                return false;
            }
        });
        orderListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int i) {
            }

            @Override
            public void onSwipeEnd(int i) {
                if (clientsListOrderAdapter.getItemViewType(i) != ClientsListOrderAdapter.EntryType.ORDER_STATE_LOCAL.ordinal()
                        && clientsListOrderAdapter.getItemViewType(i) != ClientsListOrderAdapter.EntryType.ORDER_STATE_UPLOADED.ordinal()) {
                    orderListView.smoothCloseMenu();
                }
            }
        });


    }

}

