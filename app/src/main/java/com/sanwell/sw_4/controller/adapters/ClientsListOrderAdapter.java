package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.model.database.cores.RInvoice;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Debt;
import com.sanwell.sw_4.model.database.objects.Order;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Roman Kyrylenko on 03/12/15.
 */
public class ClientsListOrderAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private Client client;
    private List<ClientInfo> list;

    public ClientsListOrderAdapter(Context context, Client client) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setClient(client);
    }

    @Nullable
    public Order orderAtIndex(int index) {
        if (index >= 0 && index < list.size()) {
            return list.get(index).order;
        }
        return null;
    }

    @Override
    public int getCount() {
        return list.size();
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
    public int getViewTypeCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0)
            return list.get(position).type.ordinal();
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ClientInfo clientInfo = list.get(i);
        int id = clientInfo.getViewID();
        if (client == null || id == -1) {
            return null;
        }
        View view1 = inflater.inflate(id, null, false);
        if (view1 == null) {
            return null;
        }
        switch (clientInfo.type) {
            case TITLE:
                ((TextView) view1.findViewById(R.id.fragment_clients_list_client_title_text_view))
                        .setText(clientInfo.content);
                ((TextView) view1.findViewById(R.id.fragment_clients_list_client_subtitle_text_view))
                        .setText(clientInfo.subcontent);
                break;
            case DEBT_TITLE:
                view1.findViewById(R.id.fragment_clients_list_client_debt_title)
                        .setVisibility(View.VISIBLE);
                break;
            case DEBT_ENTRY: {
                TextView textView1 = (TextView)
                        view1.findViewById(R.id.fragment_clients_list_client_debt_title_text_view);
                TextView textView2 = (TextView)
                        view1.findViewById(R.id.fragment_clients_list_client_debt_value_text_view);
                textView1.setVisibility(View.VISIBLE);
                textView1.setText(Html.fromHtml(clientInfo.content));
                textView2.setVisibility(View.VISIBLE);
                textView2.setText(Html.fromHtml(clientInfo.subcontent));
                break;
            }
            case PAYMENT_HISTORY_TITLE: {
                view1.findViewById(R.id.client_fragment_payment_history_title)
                        .setVisibility(View.VISIBLE);
                view1.findViewById(R.id.client_fragment_payment_history_content)
                        .setVisibility(View.GONE);
                break;
            }
            case PAYMENT_HISTORY_ENTRY: {
                view1.findViewById(R.id.client_fragment_payment_history_title)
                        .setVisibility(View.GONE);
                view1.findViewById(R.id.client_fragment_payment_history_content)
                        .setVisibility(View.VISIBLE);
                ((TextView) view1.findViewById(R.id.client_fragment_payment_history_date)).setText(Html.fromHtml(clientInfo.content));
                ((TextView) view1.findViewById(R.id.client_fragment_payment_history_cost)).setText(Html.fromHtml(clientInfo.subcontent));
                break;
            }
            case ORDER_STATE_LOCAL: {
                Order order = clientInfo.order;
                if (order != null) {
                    TextView uploadedDate = (TextView) view1.findViewById(R.id.client_fragment_order_uploaded_date);
                    uploadedDate.setText(context.getString(R.string.not_uploaded_order_date, order.formattedOpenDate()));
                    String[] summary = order.getLocalSummary();
                    ((TextView) view1.findViewById(R.id.client_fragment_order_uploaded_cost)).setText(summary[0]);
                    ((TextView) view1.findViewById(R.id.number_of_items_text_view)).setText(summary[1]);
                    ((TextView) view1.findViewById(R.id.client_fragment_order_uploaded_reject)).setText(summary[2]);
                }
                break;
            }
            case ORDER_STATE_UPLOADED: {
                Order order = clientInfo.order;
                view1.findViewById(R.id.client_fragment_order_state_comment)
                        .setVisibility(order.getComment().isEmpty() ? View.INVISIBLE : View.VISIBLE);
                ((TextView) view1.findViewById(R.id.client_fragment_order_uploaded_date))
                        .setText(context.getString(R.string.uploaded_order_date, order.formattedOpenDate()));
                ((TextView) view1.findViewById(R.id.client_fragment_order_uploaded_cost))
                        .setText(order.getUploadedSummary());
            }
        }

        return view1;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
        list = new ArrayList<>();
        if (client != null) {
            list.add(new ClientInfo(EntryType.TITLE,
                    client.getName(),
                    client.getComments()));
            List<Debt> oDebts = client.getDebts();
            List<Debt> debts = new ArrayList<>();
            for (Debt debt : oDebts) {
                if (debt.getDebt() != 0) {
                    debts.add(debt);
                }
            }
            oDebts.clear();
            if (debts.size() != 0) {
                list.add(new ClientInfo(EntryType.DEBT_TITLE, context.getString(R.string.debt_title)));
                for (Debt debt : debts) {
                    if (debt.getDebt() != 0 || debt.getPrepayment() != 0) {
                        String sDebt = ((debt.getDebt() >= 0) ? String.valueOf(debt.getDebt()) : HTMLWrapper.green(String.valueOf(-debt.getDebt())));
                        list.add(new ClientInfo(EntryType.DEBT_ENTRY, sDebt, debt.getCurrency()));
                        if (debt.getPrepayment() != 0) {
                            String sPrepayment = HTMLWrapper.green(String.valueOf(debt.getPrepayment()));
                            list.add(new ClientInfo(EntryType.DEBT_ENTRY, sPrepayment, debt.getCurrency()));
                        }
                    }
                }
            }
            List<RInvoice> invoices = client.getInvoices();
            if (!invoices.isEmpty()) {
                ClientInfo clientInfo = new ClientInfo(EntryType.PAYMENT_HISTORY_TITLE, "", "");
                list.add(clientInfo);
            }
            for (RInvoice invoice : invoices) {
                Currency currency = new Currency(invoice.getCurrencyID());
                String debt, date;
                if (RInvoice.hasOverdueDebt(invoice)) {
                    debt = HTMLWrapper.red(invoice.getDebt() + " " + currency.getIso());
                    date = HTMLWrapper.red(invoice.getInvoiceDate());
                } else {
                    debt = invoice.getDebt() + " " + currency.getIso();
                    date = invoice.getInvoiceDate();
                }
                list.add(new ClientInfo(EntryType.PAYMENT_HISTORY_ENTRY, date, debt));
            }
            List<Order> orders = client.getLastFiveOrders();
            for (Order order : orders) {
                ClientInfo clientInfo = new ClientInfo(!order.isOpen()
                        ? EntryType.ORDER_STATE_UPLOADED
                        : EntryType.ORDER_STATE_LOCAL, "", "");
                clientInfo.id = order.getOrderID();
                clientInfo.order = order;
                list.add(clientInfo);
            }
        }
        notifyDataSetChanged();
    }

    public enum EntryType {
        TITLE, DEBT_TITLE, DEBT_ENTRY, ORDER_STATE_LOCAL, ORDER_STATE_UPLOADED,
        PAYMENT_HISTORY_TITLE, PAYMENT_HISTORY_ENTRY
    }

    class ClientInfo {

        Order order;
        String id;
        EntryType type;
        String content;
        String subcontent;

        public ClientInfo(EntryType type, String content) {
            this.type = type;
            this.content = content;
        }

        public ClientInfo(EntryType type, String content, String subcontent) {
            this.type = type;
            this.content = content;
            this.subcontent = subcontent;
        }

        public int getViewID() {
            switch (type) {
                case TITLE:
                    return R.layout.client_fragment_title_list_item;
                case DEBT_ENTRY:
                case DEBT_TITLE:
                    return R.layout.client_fragment_debt_item;
                case PAYMENT_HISTORY_ENTRY:
                case PAYMENT_HISTORY_TITLE:
                    return R.layout.client_fragment_payment_history_item;
                case ORDER_STATE_UPLOADED:
                    return R.layout.client_fragment_order_state_uploaded_item;
                case ORDER_STATE_LOCAL:
                    return R.layout.client_fragment_order_state_local_item;
            }
            return -1;
        }

    }


}
