package com.sanwell.sw_4.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.ItemsListAdapter;
import com.sanwell.sw_4.controller.adapters.MenuSpinnerAdapter;
import com.sanwell.sw_4.controller.adapters.SuggestionsAdapter;
import com.sanwell.sw_4.controller.fragments.CatalogueFragment;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.interfaces.ItemClickListener;
import com.sanwell.sw_4.model.interfaces.OnSpinnerEventsListener;
import com.sanwell.sw_4.model.interfaces.RefreshItemsListCallback;
import com.sanwell.sw_4.view.MenuSpinner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CatalogueActivity extends AppCompatActivity {

    private MenuSpinner menuSpinner;
    private CatalogueFragment groupFragment;
    private View fishLayout;
    private EditText searchBar;
    private ListView suggestionsListView;
    private ItemsListAdapter itemsListAdapter;
    private MenuSpinnerAdapter menuSpinnerAdapter;
    private ListView itemsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SanwellApplication.catalogueActivity != null
                && SanwellApplication.catalogueActivity != this) {
            SanwellApplication.catalogueActivity.finish();
        }
        SanwellApplication.catalogueActivity = this;
        if (Helpers.currentClient == null) {
            onBackPressed();
            return;
        }
        Client client = Helpers.currentClient;
        setContentView(R.layout.activity_catalogue);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        fishLayout = findViewById(R.id.catalogue_fish_layout);
        itemsListView = (ListView) findViewById(R.id.catalogue_items_list_view);
        groupFragment = (CatalogueFragment) getFragmentManager().findFragmentById(R.id.catalogue_fragment);

        suggestionsListView = (ListView) findViewById(R.id.catalogue_suggestions_list_view);
        final Context context = this;
        groupFragment.setRefreshItemsListCallback(new RefreshItemsListCallback() {
            @Override
            public void refresh(ArrayList<Item> list) {
                if (list != null && list.size() != 0) {
                    itemsListAdapter = new ItemsListAdapter(context, list, new ItemClickListener() {
                        @Override
                        public void onClick(Item item) {
                            Helpers.currentItem = item;
                            startActivity(new Intent(context, ItemActivity.class));
                        }
                    });
                    itemsListAdapter.setIsFullListShowed(groupFragment.getIsCommonCatalogueSelected());
                    itemsListAdapter.setSearchRequest(groupFragment.getSearchRequest());
                    itemsListView.setAdapter(itemsListAdapter);
                } else {
                    itemsListAdapter = null;
                }
                showListView();
            }
        });
        itemsListView.setClickable(true);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (itemsListAdapter != null) {
                    Helpers.currentItem = itemsListAdapter.getItem(i);
                    startActivity(new Intent(context, ItemActivity.class));
                }
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.catalogue_activity_toolbar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            String[] data = {getResources().getString(R.string.goods_plan_percentage,
                    client.getPlanPercentage()), getResources().getString(R.string.catalogue)};
            String[] expanded = {getResources().getString(R.string.goods_plan_title,
                    client.getPlanPercentage()), getResources().getString(R.string.catalogue)};
            menuSpinnerAdapter = new MenuSpinnerAdapter(this, R.layout.title_spinner, data, expanded);
            menuSpinnerAdapter.setSubTitle(client.getName());
            menuSpinner = (MenuSpinner) findViewById(R.id.catalogue_menu_spinner);
            menuSpinner.setAdapter(menuSpinnerAdapter);
            menuSpinner.setSpinnerEventsListener(new OnSpinnerEventsListener() {
                @Override
                public void onSpinnerOpened() {
                    findViewById(R.id.catalogue_gray_layout).setVisibility(View.VISIBLE);
                }

                @Override
                public void onSpinnerClosed() {
                    findViewById(R.id.catalogue_gray_layout).setVisibility(View.INVISIBLE);
                }
            });
            menuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                    if (itemPosition == 0) {
                        if (itemsListAdapter != null) {
                            itemsListAdapter.setIsFullListShowed(true);
                        }
                    } else {
                        if (itemsListAdapter != null) {
                            itemsListAdapter.setIsFullListShowed(false);
                        }
                    }
                    menuSpinnerAdapter.setSelected(itemPosition);
                    groupFragment.setIsCommonCatalogueSelected(itemPosition != 0);
                    groupFragment.setDefaultDefaultChain();
                    groupFragment.refreshListView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

        ((TextView) findViewById(R.id.catalogue_cost_text_view))
                .setText(Html.fromHtml(DataModel.getInstance().getFormattedCurrency()));

        View.OnTouchListener hideKeyboardListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                return false;
            }
        };
        findViewById(R.id.catalogue_fragment).setOnTouchListener(hideKeyboardListener);
        findViewById(R.id.catalogue_frame).setOnTouchListener(hideKeyboardListener);

    }

    @Override
    public void onBackPressed() {
        View view = this.getCurrentFocus();
        if (view != null && searchBar != null && view == searchBar) {
            hideSearchBar(view);
            return;
        }
//        if (!groupFragment.handleHistoryBack()) {
        if (SanwellApplication.orderListActivity != null) {
            SanwellApplication.orderListActivity.finish();
            SanwellApplication.orderListActivity = null;
        }
        Intent mainIntent = new Intent(this, OrderListActivity.class);
        startActivity(mainIntent);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.catalogue_menu_action_search) {
            setShowSearchBar(true);
            showSuggestions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalogue, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        menuSpinner.handleOnWindowFocusChanged(hasFocus);
    }

    // MARK: search

    public void setShowSearchBar(Boolean show) {
        findViewById(R.id.catalogue_activity_toolbar).setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.catalogue_search_bar).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.catalogue_menu_spinner).setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        if (groupFragment != null) {
            groupFragment.setDefaultDefaultChain();
        }
        if (itemsListAdapter != null) {
            itemsListAdapter.setSearchRequest(null);
        }
        if (groupFragment != null) {
            groupFragment.setSearchRequest(null);
            if (groupFragment.groupAdapter != null) {
                groupFragment.groupAdapter.setSelectedCategory(-1);
            }
        }
        if (!show) {
            itemsListView.setVisibility(View.VISIBLE);
            hideSoftKeyboard();
            if (searchBar != null) {
                searchBar.setText(null);
            }
        } else {
            if (searchBar == null)
                searchBar = (EditText) findViewById(R.id.catalogue_search_edit_text);
            searchBar.setCursorVisible(true);
            searchBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchBar.setCursorVisible(true);
                }
            });
            if (groupFragment != null)
                groupFragment.backToRoot();
            final Animation animationRotateCenter = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
            findViewById(R.id.catalogue_search_cross).startAnimation(animationRotateCenter);
            searchBar.setInputType(InputType.TYPE_CLASS_TEXT);

            searchBar.addTextChangedListener(new TextWatcher() {

                private final long DELAY = 400;
                private Timer timer = new Timer();

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(final CharSequence charSequence, int i, int i2, int i3) {
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            filterItems(charSequence.toString());
                                        }
                                    });
                                }
                            },
                            DELAY
                    );
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        SuggestionsAdapter.addSuggestion(CatalogueActivity.this, SuggestionsAdapter.ActivityKind.CATALOGUE, String.valueOf(searchBar.getText()));
                        hideSoftKeyboard();
                        return true;
                    }
                    return false;
                }
            });
            searchBar.requestFocus();
            searchBar.setSelection(searchBar.getText().length());
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void filterItems(String request) {
        suggestionsListView.setVisibility(View.GONE);
        if (groupFragment != null) {
            groupFragment.setSearchRequest(request);
        }
        showListView();
    }

    private void showListView() {
        Boolean showFish = true;
        if (itemsListAdapter == null) {
            itemsListView.setVisibility(View.GONE);
            fishLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.catalogue_fish_layout_no_result).setVisibility(View.GONE);
            findViewById(R.id.catalogue_fish_image_view).setVisibility(View.VISIBLE);
        } else {
            showFish = itemsListAdapter.getCount() == 0;
        }
        itemsListView.setVisibility(showFish ? View.GONE : View.VISIBLE);
        fishLayout.setVisibility(showFish ? View.VISIBLE : View.INVISIBLE);
        if (!showFish) {
            findViewById(R.id.catalogue_fish_layout_no_result).setVisibility(View.GONE);
            findViewById(R.id.catalogue_fish_image_view).setVisibility(View.VISIBLE);
        } else {
            Boolean isEmpty = groupFragment == null || groupFragment.getCount() == 0;
            findViewById(R.id.catalogue_fish_layout_no_result).setVisibility((isEmpty) ? View.VISIBLE : View.GONE);
            findViewById(R.id.catalogue_fish_image_view).setVisibility((isEmpty) ? View.GONE : View.VISIBLE);
        }
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
        }
        suggestionsListView.setVisibility(View.GONE);
        showListView();
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
        suggestionsListView.setAdapter(new SuggestionsAdapter(this, SuggestionsAdapter.ActivityKind.CATALOGUE));
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

}
