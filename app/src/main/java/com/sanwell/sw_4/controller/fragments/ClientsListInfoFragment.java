package com.sanwell.sw_4.controller.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.ServerCommunicator;

import java.util.HashMap;

/**
 * Created by Roman Kyrylenko on 2/14/16.
 */
public class ClientsListInfoFragment extends Fragment {

    private TextView dollarTV, euroTV, rubTV;

    public void setDBState(View view) {
        View v = view;
        if (v == null) {
            v = getView();
        }
        if (v == null) {
            return;
        }
        if (ServerCommunicator.getInstance().isDbActual()) {
            ((TextView) v.findViewById(R.id.fragment_clients_list_db_title))
                    .setText(getActivity()
                            .getString(R.string.db_up_to_date));
            v.findViewById(R.id.fragment_clients_list_refresh_db_button).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.fragment_clients_list_refresh_db_button_icon).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) v.findViewById(R.id.fragment_clients_list_db_title))
                    .setText(getActivity()
                            .getString(R.string.db_old));
            v.findViewById(R.id.fragment_clients_list_refresh_db_button).setVisibility(View.VISIBLE);
            v.findViewById(R.id.fragment_clients_list_refresh_db_button_icon).setVisibility(View.VISIBLE);
        }
        ((TextView) v.findViewById(R.id.fragment_clients_list_db_subtitle))
                .setText(ServerCommunicator.getFormatedLastUpdateDate());
        setupCurrency();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_list_info, container, false);
        initLayout(view);
        setDBState(view);
        return view;
    }

    private void initLayout(View view) {
        rubTV = (TextView) view.findViewById(R.id.fragment_clients_list_rub_text_view);
        euroTV = (TextView) view.findViewById(R.id.fragment_clients_list_eu_text_view);
        dollarTV = (TextView) view.findViewById(R.id.fragment_clients_list_dollar_text_view);
    }

    private void setupCurrency() {
        HashMap<String, Currency> currencies = DataModel.getInstance().getCurrencies();
        TextView arr_ids[] = {
                dollarTV,
                rubTV,
                euroTV
        };
        int itr = 0;
        for (Currency currency : currencies.values()) {
            if (currency.getRate() == 1.0) {
                continue;
            }
            if (itr >= arr_ids.length) {
                return;
            }
            TextView textView = arr_ids[itr++];
            textView.setText(Html.fromHtml("1 " + currency.getIso().toUpperCase() +
                    " = " + HTMLWrapper.red("" + currency.getRate())));
            textView.setVisibility(View.VISIBLE);
        }
    }

}
