package com.sanwell.sw_4.controller.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.OrdersListRVAdapter;

/*
 * Created by Roman Kyrylenko on 23/08/16.
 */
public class OrdersTotalListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_order_list);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_activity_toolbar);
        myToolbar.setNavigationIcon(R.drawable.back_icon);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(new OrdersListRVAdapter(this));

    }

}
