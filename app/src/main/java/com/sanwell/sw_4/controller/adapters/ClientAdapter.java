package com.sanwell.sw_4.controller.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.database.objects.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {

    private ArrayList<Client> objectsList = new ArrayList<>();
    private ArrayList<Client> sortedList = new ArrayList<>();
    private Integer selectedItem;
    private String searchRequest;
    private ClientClickListener clickListener;

    public ClientAdapter(ArrayList<Client> products, ClientClickListener clickListener) {
        objectsList = products;
        this.clickListener = clickListener;
        selectedItem = -1;
        extractHeaders();
    }

    private void extractHeaders() {
        if (searchRequest != null && !searchRequest.isEmpty()) {
            sortedList = new ArrayList<>();
            for (Client client : objectsList) {
                if (!client.isSectionHeader()
                        && client.getName().toLowerCase().contains(searchRequest.toLowerCase())) {
                    sortedList.add(client);
                }
            }
        } else {
            sortedList = new ArrayList<>(objectsList);
        }
        HashSet<String> headers = new HashSet<>();
        for (Client client : sortedList) {
            String clientName = client.getName();
            if (!clientName.isEmpty() && Character.isLetter(clientName.charAt(0))) {
                headers.add(clientName.substring(0, 1));
            } else {
                headers.add("#");
            }
        }
        for (String header : headers) {
            Client client = new Client();
            client.setName(header);
            client.setIsSectionHeader(true);
            sortedList.add(client);
        }
        Collections.sort(sortedList);
    }

    public Integer getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Integer selectedItem) {
        if (this.selectedItem.equals(selectedItem)) {
            return;
        }
        notifyItemChanged(this.selectedItem);
        this.selectedItem = selectedItem;
        notifyItemChanged(this.selectedItem);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.clients_list_item, parent, false);
        return new ViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Client client = sortedList.get(position);
        if (client.isSectionHeader()) {
            holder.header.setVisibility(View.VISIBLE);
            holder.row.setVisibility(View.GONE);
            holder.headerTextView.setText(client.getName());
        } else {
            holder.row.setTag(position);
            holder.row.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Integer pos = (Integer) v.getTag();
                    if (pos != null) {
                        setSelectedItem(pos);
                        if (clickListener != null) {
                            // fixme: called 2-3 times
                            clickListener.onClick(sortedList.get(pos));
                        }
                    }
                    return true;
                }
            });
            holder.header.setVisibility(View.GONE);
            holder.row.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(Html.fromHtml(SuggestionsAdapter.
                    highlightSearchRequest(searchRequest, client.getName())));
            holder.percentageTextView.setText(client.getPlanPercentage() + "%");
            String debt = client.getDebtString();
            if (debt.isEmpty()) {
                holder.debtTextView.setVisibility(View.GONE);
            } else {
                holder.debtTextView.setVisibility(View.VISIBLE);
                holder.debtTextView.setText(Html.fromHtml(debt));
            }
            holder.selector.setVisibility(position == selectedItem ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return sortedList.size();
    }

    public void setSearchRequest(String searchRequest) {
        this.searchRequest = searchRequest;
        extractHeaders();
        notifyDataSetChanged();
    }

    public interface ClientClickListener {
        void onClick(Client client);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View selector, row, header;
        TextView percentageTextView, debtTextView, headerTextView, titleTextView;

        public ViewHolder(View view) {
            super(view);
            headerTextView = (TextView) view.findViewById(R.id.clients_list_header_text_view);
            titleTextView = (TextView) view.findViewById(R.id.clients_list_item_title);
            percentageTextView = (TextView) view.findViewById(R.id.clients_list_item_percentage);
            debtTextView = (TextView) view.findViewById(R.id.clients_list_item_debt);
            selector = view.findViewById(R.id.clients_list_item_selector);
            row = view.findViewById(R.id.clients_list_item_row);
            header = view.findViewById(R.id.clients_list_item_header);
        }

    }
}
