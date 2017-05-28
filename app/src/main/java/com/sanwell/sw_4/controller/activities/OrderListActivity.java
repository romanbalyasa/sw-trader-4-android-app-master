package com.sanwell.sw_4.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.ItemsListAdapter;
import com.sanwell.sw_4.controller.adapters.MenuSpinnerAdapter;
import com.sanwell.sw_4.controller.adapters.SuggestionsAdapter;
import com.sanwell.sw_4.controller.swipedismiss.SwipeDismissListViewTouchListener;
import com.sanwell.sw_4.controller.swipedismiss.SwipeDismissTouchListener;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.OrdersDataModel;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.Order;
import com.sanwell.sw_4.model.interfaces.ItemClickListener;
import com.sanwell.sw_4.model.interfaces.OnSpinnerEventsListener;
import com.sanwell.sw_4.view.CustomEditText;
import com.sanwell.sw_4.view.MenuSpinner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderListActivity extends AppCompatActivity {

    private static final ScheduledExecutorService worker
            = Executors.newSingleThreadScheduledExecutor();

    private MenuSpinnerAdapter menuSpinnerAdapter;
    private MenuSpinner menuSpinner;
    private Order order;
    private Client client;
    private ListView itemsListView;
    private ItemsListAdapter itemsListAdapter;
    private CustomEditText searchBar;
    private ListView suggestionsListView;
    private ImageButton plusButton;
    private ImageView commentImageView;
    private Button commentDoneButton;
    private ImageView fishImageView;
    private TextView orderSummary;
    private Item itemToDelete;
    private Mode mode = Mode.ORDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        SanwellApplication.orderListActivity = this;
        client = Helpers.currentClient;
        if (client == null) {
            onBackPressed();
            return;
        }
        order = Helpers.order();
        if (order == null) {
            order = Helpers.currentClient.getOrder();
            if (order == null) {
                order = Helpers.currentClient.addOrder();
            }
        }

        plusButton = (ImageButton) findViewById(R.id.order_list_plus_button);
        commentDoneButton = (Button) findViewById(R.id.order_list_comment_done);
        commentImageView = (ImageView) findViewById(R.id.order_list_comment_icon);
        fishImageView = (ImageView) findViewById(R.id.order_list_fish);
        suggestionsListView = (ListView) findViewById(R.id.order_list_suggestions_list_view);
        orderSummary = (TextView) findViewById(R.id.order_list_summary_info);

        // manage comment field content
        final EditText commentField = (EditText) findViewById(R.id.order_list_comment);
        commentField.setText(order.getComment());
        commentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (order != null) {
                    order.setComment(commentField.getText().toString())
                            .commit();
                }
            }
        });
        commentField.setEnabled(order.isOpen());

        // manage comment box position / color
        final View commentBox = findViewById(R.id.order_list_comment_box);
        final View root = findViewById(android.R.id.content);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int heightDiff = root.getRootView().getHeight() - root.getHeight();
                boolean isVisible = heightDiff >= 150;
                plusButton.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
                commentImageView.setVisibility(plusButton.getVisibility());
                if (order != null && order.isEmpty()) {
                    fishImageView.setVisibility(plusButton.getVisibility());
                }
                plusButton.setVisibility(order != null && order.isOpen() ? plusButton.getVisibility() : View.GONE);
                commentDoneButton.setVisibility(!isVisible ? View.INVISIBLE : View.VISIBLE);
                final int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    commentBox.setBackgroundDrawable(getResources().getDrawable(isVisible
                            ? R.color.comment_background : R.color.white));
                } else {
                    commentBox.setBackground(getResources().getDrawable(isVisible
                            ? R.color.comment_background : R.color.white));
                }
            }
        });

        initToolbar();
        initializeListView();

        findViewById(R.id.order_list_gray_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; // to not omit touches
            }
        });

        // BRG
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        itemsListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    itemToDelete = itemsListAdapter.getItem(position);
                                    deleteOkCancel(itemToDelete);
                                }
                                itemsListAdapter.notifyDataSetChanged();
                            }
                        });
        itemsListView.setOnTouchListener(touchListener);
        itemsListView.setOnScrollListener(touchListener.makeScrollListener());
        //BRG --------------

    }

    private void refreshSubtitle() {
        if (order.isEmpty()) {
            orderSummary.setText(getString(R.string.empty_word));
        } else {
            orderSummary.setText(Html.fromHtml(order.getSummary()));
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.order_list_activity_toolbar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (order.isEmpty()) {
                updateTitle();
                return;
            }
            actionBar.setDisplayShowTitleEnabled(false);
        }
        String[] data = {"Заказ", "Отказ"};
        menuSpinnerAdapter = new MenuSpinnerAdapter(this, R.layout.title_spinner, data);
        menuSpinner = (MenuSpinner) findViewById(R.id.order_list_menu_spinner);
        menuSpinner.setAdapter(menuSpinnerAdapter);
        menuSpinner.setSpinnerEventsListener(new OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened() {
                findViewById(R.id.order_list_gray_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onSpinnerClosed() {
                findViewById(R.id.order_list_gray_layout).setVisibility(View.INVISIBLE);
            }
        });
        menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                mode = itemPosition == 0 ? Mode.ORDER : Mode.OVERHEAD;
                refreshSubtitle();
                updateTitle();
                if (order.isEmpty()) {
                    return;
                }
                menuSpinnerAdapter.setSelected(itemPosition);
                itemsListAdapter.setMode(mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        String pref = order.isOpen() ? "Заказ открыт " : "Заказ закрыт ";
        if (mode == Mode.OVERHEAD) {
            pref = "Отказы: " + pref;
        }
        plusButton.setVisibility(order.isOpen() ? View.VISIBLE : View.GONE);
        if (order.isEmpty()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(pref + order.getOpenedIn());
                actionBar.setSubtitle(client.getName());
            }
        } else {
            if (menuSpinnerAdapter != null) {
                menuSpinnerAdapter.setTitle(pref + order.getOpenedIn());
                menuSpinnerAdapter.setSubTitle(client.getName());
                menuSpinnerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (itemsListAdapter == null) {
            order = Helpers.currentClient.getOrder();
            if (order == null) return;
            initializeListView();
        }
        if (itemsListAdapter != null)
            itemsListAdapter.notifyDataSetChanged();
    }

    private void initializeListView() {
        refreshSubtitle();
        String html = DataModel.getInstance().getFormattedCurrency();
        ((TextView) findViewById(R.id.order_list_currency_info)).setText(Html.fromHtml(html));
        itemsListView = (ListView) findViewById(R.id.order_list_items_list_view);
        if (order.isEmpty()) {
            itemsListView.setVisibility(View.GONE);
            fishImageView.setVisibility(View.VISIBLE);
            return;
        } else {
            itemsListView.setVisibility(View.VISIBLE);
        }
        itemsListAdapter = new ItemsListAdapter(this, order.getItems(), new ItemClickListener() {
            @Override
            public void onClick(Item item) { // delete button

                /* BRG вывел в отдельную процедуру

                ((TextView) findViewById(R.id.order_list_delete_item_message)).setText(item.getTitle());
                String url = item.getImageURL();
                if (!url.isEmpty())
                    Ion.with(OrderListActivity.this)
                            .load(url)
                            .withBitmap()
                            .placeholder(R.drawable.no_photo_icon)
                            .intoImageView((ImageView) findViewById(R.id.order_list_delete_item_image));
                else
                    ((ImageView) findViewById(R.id.order_list_delete_item_image))
                            .setImageResource(R.drawable.no_photo_icon);

                findViewById(R.id.order_list_delete_item_box).setVisibility(View.VISIBLE);
                findViewById(R.id.order_list_gray_layout).setVisibility(View.VISIBLE);
                */

                deleteOkCancel(item);
                itemToDelete = item;
            }
        });
        itemsListAdapter.setIsInOrderList(true);
        itemsListView.setAdapter(itemsListAdapter);
        View.OnTouchListener hideKeyboardOnTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        };

        fishImageView.setOnTouchListener(hideKeyboardOnTouch);
        findViewById(R.id.order_list_activity_toolbar).setOnTouchListener(hideKeyboardOnTouch);
        itemsListView.setClickable(true);
        itemsListView.setOnTouchListener(hideKeyboardOnTouch);
        if (order.isOpen()) {
            itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (order.isOpen()) {
                        Helpers.currentItem = itemsListAdapter.getItem(i);
                        Intent mainIntent = new Intent(OrderListActivity.this, ItemActivity.class);
                        startActivity(mainIntent);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_list, menu);
        if (order.isEmpty()) {
            menu.getItem(1).setVisible(false); // hide search action
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.order_list_action_delete) {
            findViewById(R.id.order_list_delete_order_box).setVisibility(View.VISIBLE);
            findViewById(R.id.order_list_gray_layout).setVisibility(View.VISIBLE);
            hideSoftKeyboard();
            return true;
        } else if (id == R.id.order_list_action_search) {
            setShowSearchBar(true);
            showSuggestions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (menuSpinner != null)
            menuSpinner.handleOnWindowFocusChanged(hasFocus);
        if (findViewById(R.id.order_list_delete_order_box).getVisibility() == View.VISIBLE
                || findViewById(R.id.order_list_delete_item_box).getVisibility() == View.VISIBLE) {
            cancelDeleting(null);
        }
        hideSoftKeyboard();
    }

    @Override
    public void onBackPressed() {
        checkFish(null);
        View view = this.getCurrentFocus();
        if (view != null && searchBar != null && view == searchBar) {
            hideSearchBar(view);
        } else {
            SanwellApplication.orderListActivity = null;
            if (SanwellApplication.catalogueActivity != null) {
                SanwellApplication.catalogueActivity.finish();
                SanwellApplication.catalogueActivity = null;
            }
            super.onBackPressed();
        }
    }

    public void deleteOrder(View view) {
        cancelDeleting(view);
        OrdersDataModel.getInstance().delete(order.getOrderID());
        onBackPressed();
    }

    public void deleteItem(View view) {
        if (order.isOpen()) {
            order.removeItem(itemToDelete);
            itemsListAdapter.setObjectsList(order.getItems());
            cancelDeleting(null);
            refreshSubtitle();
        }
    }

    public void cancelDeleting(View view) {
        findViewById(R.id.order_list_delete_item_box).setVisibility(View.GONE);
        findViewById(R.id.order_list_delete_order_box).setVisibility(View.GONE);
        findViewById(R.id.order_list_gray_layout).setVisibility(View.GONE);
    }

    public void addItem(View view) {
        if (order.isOpen()) {
            Intent mainIntent = new Intent(this, CatalogueActivity.class);
            startActivity(mainIntent);
        }
    }

    public void setShowSearchBar(Boolean show) {
        findViewById(R.id.order_list_activity_toolbar).setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.order_list_search_bar).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.order_list_menu_spinner).setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        if (!show) {
            if (searchBar != null) {
                searchBar.setText(null);
            }
            itemsListView.setVisibility(View.VISIBLE);
            if (itemsListAdapter != null) {
                itemsListAdapter.setSearchRequest(null);
            }
            hideSoftKeyboard();
        } else {

            if (searchBar == null) {
                searchBar = (CustomEditText) findViewById(R.id.order_list_search_edit_text);
                searchBar.setListener(new CustomEditText.CustomEditTextListener() {
                    @Override
                    public void onBackPressed() {
                        checkFish(null);
                    }
                });
            }
            searchBar.setInputType(InputType.TYPE_CLASS_TEXT);
            searchBar.setCursorVisible(true);
            searchBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSearchBarKeyboard();
                }
            });

            final Animation animationRotateCenter = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
            findViewById(R.id.order_list_search_cross).startAnimation(animationRotateCenter);
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    filterItems(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        SuggestionsAdapter.addSuggestion(OrderListActivity.this, SuggestionsAdapter.ActivityKind.ORDER_LIST, String.valueOf(searchBar.getText()));
                        hideSoftKeyboard();
                        return true;
                    }
                    return false;
                }
            });
            showSearchBarKeyboard();
        }
    }

    // MARK: search

    public void filterItems(String request) {
        if (itemsListAdapter == null) {
            return;
        }
        suggestionsListView.setVisibility(View.GONE);
        itemsListAdapter.setSearchRequest(request);
        Boolean showFish = itemsListAdapter.getCount() == 0;
        itemsListView.setVisibility(showFish ? View.GONE : View.VISIBLE);
        checkFish(false);
    }

    public void hideSoftKeyboard(View view) {
        hideSoftKeyboard();
    }

    private void showSearchBarKeyboard() {
        checkFish(false);
        searchBar.setFocusable(true);
        searchBar.setFocusableInTouchMode(true);
        searchBar.setCursorVisible(true);
        searchBar.requestFocus();
        searchBar.setSelection(searchBar.getText().length());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
        if (searchBar != null) {
            searchBar.setCursorVisible(false);
            searchBar.setFocusable(false);
        }
        suggestionsListView.setVisibility(View.GONE);

        Runnable task = new Runnable() {
            public void run() {
                checkFish(null);
            }
        };
        if (itemsListAdapter != null && itemsListAdapter.getCount() == 0)
            worker.schedule(task, 500, TimeUnit.MILLISECONDS);
    }

    private void checkFish(final Boolean show) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Boolean h = show;
                if (show == null) {
                    h = itemsListAdapter != null
                            && itemsListAdapter.getSearchRequest() != null
                            && !itemsListAdapter.getSearchRequest().isEmpty()
                            && itemsListAdapter.getCount() == 0;
                }
                findViewById(R.id.order_list_not_found_layout).setVisibility(!h ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    public void hideSearchBar(View view) {
        setShowSearchBar(false);
    }

    public void clearSearchBar(View button) {
        if (searchBar != null) {
            if (searchBar.getText() != null && !searchBar.getText().toString().isEmpty()) {
                searchBar.setText(null);
            } else {
                setShowSearchBar(false);
            }
        }
    }

    private void showSuggestions() {
        suggestionsListView.setAdapter(new SuggestionsAdapter(this, SuggestionsAdapter.ActivityKind.ORDER_LIST));
        suggestionsListView.setVisibility(View.VISIBLE);
        suggestionsListView.setClickable(true);
        suggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedSuggestion = (String) suggestionsListView.getAdapter().getItem(i);
                searchBar.setText(selectedSuggestion);
                searchBar.setSelection(searchBar.getText().length());
                hideSoftKeyboard();
            }
        });
    }

    // BRG
    private void deleteOkCancel(Item itemToDelete)
    {
        ((TextView) findViewById(R.id.order_list_delete_item_message)).setText(itemToDelete.getTitle());
        String url = itemToDelete.getImageURL();
        if (!url.isEmpty())
            Ion.with(OrderListActivity.this)
                    .load(url)
                    .withBitmap()
                    .placeholder(R.drawable.no_photo_icon)
                    .intoImageView((ImageView) findViewById(R.id.order_list_delete_item_image));
        else
            ((ImageView) findViewById(R.id.order_list_delete_item_image))
                    .setImageResource(R.drawable.no_photo_icon);

        findViewById(R.id.order_list_delete_item_box).setVisibility(View.VISIBLE);
        findViewById(R.id.order_list_gray_layout).setVisibility(View.VISIBLE);

    }


    public enum Mode {ORDER, OVERHEAD}


}
