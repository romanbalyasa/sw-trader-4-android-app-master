package com.sanwell.sw_4.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.StocksAdapter;
import com.sanwell.sw_4.controller.fragments.ItemFragment;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.objects.Currency;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.Order;

import java.util.Locale;

public class ItemActivity extends AppCompatActivity {

    private Item currentItem;
    private Order currentOrder;
    private StocksAdapter stocksAdapter;
    private ItemFragment itemFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_item);
        itemFragment = (ItemFragment) getFragmentManager().findFragmentById(R.id.item_fragment_right);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.item_activity_toolbar);
        myToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
        currentItem = Helpers.currentItem;
        currentOrder = Helpers.currentClient.getOrder();
        if (currentOrder == null) {
            currentOrder = Helpers.currentClient.addOrder();
        }
        itemFragment.setOrder(currentOrder);
        ImageButton imageButton = (ImageButton) findViewById(R.id.fragment_item_image_button);
        imageButton.setVisibility(currentItem.getImageURLS().size() == 0
                ? View.INVISIBLE : View.VISIBLE);
        if (imageButton.getVisibility() == View.VISIBLE) {
            Ion.with(imageButton)
                    .placeholder(R.drawable.no_photo_icon)
                    .load(currentItem.getImageThumbURL());
        }
        myToolbar.setNavigationIcon(R.drawable.back_icon);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(currentItem.getTitle());
            actionBar.setSubtitle(Helpers.currentClient.getName());
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initializeListView();
        findViewById(R.id.item_frame).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                deselectItem(false);
                return false;
            }
        });
        String available = getString(R.string.available_string, currentItem.getStock(),
                currentItem.getMeasurement(),
                currentItem.getNumberOfItemsInPackage());
        ((TextView) findViewById(R.id.item_available_info)).setText(Html.fromHtml(available));
        refreshItemInfo();
    }

    private void refreshItemInfo() {
        double count = currentOrder.getItemsCount(currentItem);
        String c = "x" + Helpers.stringFromDouble(count);
        Currency currency = currentOrder.getCurrency();
        if (currentItem.isFixedCurrency()) {
            currency = new Currency(currentItem.getCurrencyId());
        }
        Double cost = currentOrder.getItemPrice(currentItem.getItemID(), currency, currentItem.getCurrencyId());
        if (cost != null)
        {itemFragment.setOrderPrice(cost);} // BRG

        String sCost;
        if (cost == null) {
            cost = currency.defaultRound(currentItem.getCost(), currentItem.getCurrencyId());
            sCost = String.format(Locale.US, "%.3f", cost);
        } else {
            sCost = currency.sDefaultRound(cost);
        }

        ((TextView) findViewById(R.id.item_toolbar_price)).setText(sCost);

        ((TextView) findViewById(R.id.item_toolbar_currency)).setText(currency.getIso());

        ((TextView) findViewById(R.id.item_toolbar_count)).setText(c);

        ((TextView) findViewById(R.id.item_toolbar_total_price))
                .setText(String.format(Locale.US, "%.3f %s", cost * count, currency.getIso()));

        if (currentItem.getExpandedStatuses().isEmpty() && currentItem.getImageURLS().size() == 0) {
            View view = findViewById(R.id.fragment_item_image_block);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            params.height = 0;
            view.setLayoutParams(params);
        } else {
            ((TextView) findViewById(R.id.fragment_item_image_caption)).setText(currentItem.getExpandedStatuses());
        }
    }

    private void initializeListView() {
        ListView stockListView = (ListView) findViewById(R.id.item_frame_list_view);
        stocksAdapter = new StocksAdapter(this, currentItem, currentOrder);
        stockListView.setAdapter(stocksAdapter);
        stockListView.setClickable(true);
        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                stocksAdapter.setSelectedItem(i);
                itemFragment.setCurrentItem(true, stocksAdapter.getSelectedBatch());
                hideSoftKeyboard();
            }
        });
    }

    public void deselectItem(boolean apply) {
        if (apply && stocksAdapter != null && currentOrder != null && itemFragment != null) {
            String count = itemFragment.getCount();
            double c = Item.safeParse(count);
            if (c != 0) {
                currentOrder.addItem(currentItem);
                String currencyId = currentItem.getCurrencyId();
                if (!currentItem.isFixedCurrency()) {
                    currencyId = currentOrder.getCurrencyID();
                }
                currentOrder.addItemsFromBatch(
                        stocksAdapter.getSelectedBatch(),
                        count,
                        itemFragment.getPrice(),
                        currencyId,
                        itemFragment.getPriceCode(),
                        itemFragment.getSuggestedPriceCode());
            }
        }
        if (stocksAdapter != null) {
            stocksAdapter.setSelectedItem(-1);
            itemFragment.setCurrentItem(false, null);
        }
        refreshItemInfo();
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
        if (itemFragment != null && itemFragment.getCountInputEditText() != null) {
            itemFragment.getCountInputEditText().setCursorVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    public void itemButton(View button) {
        startActivity(new Intent(this, ImagesActivity.class));
    }

}
