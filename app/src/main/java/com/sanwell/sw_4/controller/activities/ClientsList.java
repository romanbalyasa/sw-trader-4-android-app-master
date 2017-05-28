package com.sanwell.sw_4.controller.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.ClientAdapter;
import com.sanwell.sw_4.controller.adapters.SuggestionsAdapter;
import com.sanwell.sw_4.controller.fragments.ClientsListClientFragment;
import com.sanwell.sw_4.controller.fragments.ClientsListInfoFragment;
import com.sanwell.sw_4.model.HTMLWrapper;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.KMLCollector;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.ServerCommunicator;
import com.sanwell.sw_4.model.database.DataBaseMapper;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.OrdersDataModel;
import com.sanwell.sw_4.model.database.SQLDataReader;
import com.sanwell.sw_4.model.database.objects.Client;
import com.sanwell.sw_4.model.interfaces.CompletionHandler;
import com.sanwell.sw_4.model.interfaces.ResultCompletionHandler;

public class ClientsList extends AppCompatActivity {

    private Menu menu;
    private RecyclerView clientsRecyclerView;
    private EditText searchBar;
    private ClientAdapter clientListAdapter;
    private ClientsListClientFragment clientsListClientFragment;
    private ClientsListInfoFragment clientsListInfoFragment;
    private View uploadProgressBox;
    private View grayLayout;
    private ListView suggestionsListView;
    private String prevClientId = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServerCommunicator.getInstance().setContext(this);
        DataModel.getInstance();
        setContentView(R.layout.activity_clients_list);
        // omit interface jumping when keyboard appears
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initToolbar();
        suggestionsListView = (ListView) findViewById(R.id.clients_list_suggestions_list_view);
        KMLCollector.initServices(this);

        clientsRecyclerView = (RecyclerView) findViewById(R.id.clients_recycler_view);
        clientsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        clientsRecyclerView.setHasFixedSize(true);

        checkDB();
        FragmentManager fm = getFragmentManager();

        clientsListClientFragment = (ClientsListClientFragment)
                fm.findFragmentById(R.id.clients_list_fragment);
        clientsListInfoFragment = (ClientsListInfoFragment)
                fm.findFragmentById(R.id.clients_list_info_fragment);
        fm.beginTransaction()
                .hide(clientsListClientFragment)
                .commit();

        View.OnTouchListener hideKeyboardListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard();
                return false;
            }
        };
        findViewById(R.id.clients_list_fragment).setOnTouchListener(hideKeyboardListener);
        findViewById(R.id.clients_list_list_frame).setOnTouchListener(hideKeyboardListener);

        findViewById(R.id.clients_list_gray_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true; // to not omit touches
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.clients_activity_toolbar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (clientListAdapter.getSelectedItem() != -1) {
                    clientListAdapter.setSelectedItem(-1);
                    setSelectedClient(null);
                    return true;
                }
                hideSoftKeyboard();
                return false;
            }
        });
        uploadProgressBox = findViewById(R.id.clients_list_refresh_db);
        grayLayout = findViewById(R.id.clients_list_gray_layout);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void checkDB() {
        if (SQLDataReader.isDBFileExist()) {
            initializeClientsList();
            ServerCommunicator.getInstance().checkDataBaseVersion(new ResultCompletionHandler() {
                @Override
                public void handle(String result) {
                    checkMenuButtonsState();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clientsListInfoFragment.setDBState(null);
                        }
                    });
                }
            });
        } else {
            showDeviceNumberDialogue();
        }
    }

    private void showDeviceNumberDialogue() {
        final View view = LayoutInflater.from(this).inflate(R.layout.input_dialogue_layout, null);
        new android.support.v7.app.AlertDialog.Builder(this)
                .setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = ((EditText) view.findViewById(R.id.input_edit_text)).getText().toString();
                        if (text.isEmpty()) {
                            showDeviceNumberDialogue();
                        } else {
                            Helpers.edit_sp(Helpers.DEVICE_USER_ID, text);
                            findViewById(R.id.clients_list_refresh_db_cancel).setVisibility(View.INVISIBLE);
                            checkConnectionOnInitialRefresh();
                        }
                    }
                })
                .setView(view)
                .setCancelable(false)
                .setMessage("Введите номер устройства")
                .show();
    }

    private void checkConnectionOnInitialRefresh() {
        if (!ServerCommunicator.hasInternetConnection()) {
            new android.support.v7.app.AlertDialog.Builder(ClientsList.this, R.style.CommonAlertDialog)
                    .setMessage("Для загрузки базы данных необходимо подключение к интернет. Проверьте ваше подключение и повторите попытку")
                    .setPositiveButton("Повторить попытку", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnectionOnInitialRefresh();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            refreshDB(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMenuButtonsState();
    }

    private void checkMenuButtonsState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (menu == null) {
                    return;
                }
                boolean hasConnection = ServerCommunicator.hasInternetConnection();
                menu.getItem(1).setIcon(hasConnection && OrdersDataModel.getInstance().hasOpenedOrders()
                        ? R.drawable.upload_icon
                        : R.drawable.upload_icon_inactive);
                menu.getItem(2).setIcon(hasConnection && !ServerCommunicator.getInstance().isDbActual()
                        ? R.drawable.refresh_icon_black
                        : R.drawable.refresh_icon_black_inactive);
                clientsListInfoFragment.setDBState(null);
            }
        });
    }

    private void initializeClientsList() {
        clientListAdapter = new ClientAdapter(DataModel.getInstance().getClients(), new ClientAdapter.ClientClickListener() {
            @Override
            public void onClick(Client client) {
                if (clientListAdapter != null) {
                    if (clientsListClientFragment != null) {
                        setSelectedClient(client);
                        Helpers.currentClient = client;
                    }
                }
                hideSoftKeyboard();
            }
        });
        clientsRecyclerView.setAdapter(clientListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_clients_list, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        clientsListClientFragment.handleOnWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent mainIntent = new Intent(this, SettingsActivity.class);
            startActivity(mainIntent);
            return true;
        } else if (id == R.id.action_upload) {
            if (!ServerCommunicator.hasInternetConnection()) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(this, R.style.CommonAlertDialog);
                builder.setMessage("Для отправки заказов необходимо стабильное подключение к интернет");
                builder.setTitle("Нет подключения к интернет");
                builder.setNegativeButton("Понятно", null);
                builder.show();
            } else if (!OrdersDataModel.getInstance().hasOpenedOrders()) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(this, R.style.CommonAlertDialog);
                builder.setMessage("Для отправки заказа необходимо его создать");
                builder.setTitle("Нет активных заказов.");
                builder.setNegativeButton("Понятно", null);
                builder.show();
            } else {
                findViewById(R.id.clients_list_upload_order_box).setVisibility(View.VISIBLE);
                grayLayout.setVisibility(View.VISIBLE);
            }
            return true;
        } else if (id == R.id.action_search) {
            setShowSearchBar(true);
            showSuggestions();
            return true;
        } else if (id == R.id.action_refresh) {
            if (!ServerCommunicator.hasInternetConnection()) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(this, R.style.CommonAlertDialog);
                builder.setMessage("Для загрузки базы данных необходимо стабильное подключение к интернет");
                builder.setTitle("Нет подключения к интернет");
                builder.setNegativeButton("Понятно", null);
                builder.show();
            } else if (ServerCommunicator.getInstance().isDbActual()) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(this, R.style.CommonAlertDialog);
                builder.setMessage("На вашем устройстве самая свежая база данных");
                builder.setTitle("База данных актуальна");
                builder.setNegativeButton("Понятно", null);
                builder.show();
            } else {
                findViewById(R.id.clients_list_refresh_db_cancel).setVisibility(View.VISIBLE);
                refreshDBButtonAction(null);
            }
            return true;
        } else if (id == R.id.action_orders) {
            if (!OrdersDataModel.getInstance().hasOrders()) {
                android.support.v7.app.AlertDialog.Builder builder =
                        new android.support.v7.app.AlertDialog.Builder(this, R.style.CommonAlertDialog);
                builder.setTitle("Нет заказов.");
                builder.setMessage("Список заказов пуст.");
                builder.setNegativeButton("Понятно", null);
                builder.show();
            } else {
                startActivity(new Intent(this, OrdersTotalListActivity.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshDB(View view) {
        findViewById(R.id.clients_list_prerefresh_db).setVisibility(View.INVISIBLE);
        grayLayout.setVisibility(View.VISIBLE);
        uploadProgressBox.setVisibility(View.VISIBLE);
        final TextView textView = ((TextView) findViewById(R.id.clients_list_refresh_db_status));
        final String prefix = getString(R.string.downloaded_prefix);
        final ProgressBar progressBar =
                (ProgressBar) findViewById(R.id.clients_list_refresh_db_progress_bar);
        progressBar.setProgress(0);
        final ServerCommunicator communicator = ServerCommunicator.getInstance();
        communicator.checkDataBaseVersion(new ResultCompletionHandler() {
            @Override
            public void handle(String result) {
                checkMenuButtonsState();
                communicator.downloadDataBase(new ServerCommunicator.ProgressBlock() {
                    @Override
                    public void onProgressChanged(int percentage, long downloaded) {
                        textView.setText(Html.fromHtml(prefix +
                                HTMLWrapper.red(ServerCommunicator.getInstance().getRelatedDownloadLength(downloaded, false)) + " из " +
                                HTMLWrapper.red(ServerCommunicator.getInstance().getTotalDownloadLength())));
                        progressBar.setProgress(percentage);
                    }

                    @Override
                    public void onFinish(String result) {
                        if (result != null) {
                            checkMenuButtonsState();
                            progressBar.setIndeterminate(true);
                            DataModel.getInstance().realm =
                                    new DataBaseMapper(SanwellApplication.applicationContext,
                                            DataModel.getInstance().realm).map();
                            progressBar.setIndeterminate(false);
                            clientsListClientFragment.manageCurrencySpinner();
                            communicator.getURLSJSON(new ServerCommunicator.ProgressBlock() {
                                @Override
                                public void onProgressChanged(int percentage, long downloaded) {
                                    textView.setText(Html.fromHtml(
                                            prefix +
                                                    HTMLWrapper.red(communicator.getRelatedDownloadLength(downloaded, true))
                                                    + " из " +
                                                    HTMLWrapper.red(communicator.getTotalDownloadLength())
                                    ));
                                    progressBar.setProgress(percentage);
                                }

                                @Override
                                public void onFinish(String result) {
                                    if (result != null && result.length() != 0) {
                                        new DataBaseMapper(SanwellApplication.applicationContext,
                                                DataModel.getInstance().realm).readURLS(result);
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            initializeClientsList();
                                            clientsListInfoFragment.setDBState(null);
                                            uploadProgressBox.setVisibility(View.INVISIBLE);
                                            grayLayout.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }

                            });
                        } else { // db is up to date or error occurred
                            uploadProgressBox.setVisibility(View.INVISIBLE);
                            grayLayout.setVisibility(View.INVISIBLE);
                        }
                        checkMenuButtonsState();
                    }
                });
            }
        });


    }

    public void cancelDBRefresh(View button) {
        grayLayout.setVisibility(View.GONE);
        uploadProgressBox.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        View view = this.getCurrentFocus();
        if (view != null && searchBar != null && view == searchBar) {
            hideSearchBar(view);
        } else if (clientListAdapter != null && clientListAdapter.getSelectedItem() != -1) {
            clientListAdapter.setSelectedItem(-1);
            setSelectedClient(null);
        } else {
            super.onBackPressed();
        }
    }

    public void cancelRefreshing(View view) {
        findViewById(R.id.clients_list_prerefresh_db).setVisibility(View.INVISIBLE);
        grayLayout.setVisibility(View.INVISIBLE);
    }

    public void refreshDBButtonAction(View button) {
        findViewById(R.id.clients_list_prerefresh_db).setVisibility(View.VISIBLE);
        grayLayout.setVisibility(View.VISIBLE);
    }

    public void orderButtonAction(View button) {
        Intent mainIntent = new Intent(this, CatalogueActivity.class);
        startActivity(mainIntent);
    }

    public void uploadOrders(final View view) {
        OrdersDataModel.getInstance().uploadOrders(new CompletionHandler() {
            @Override
            public void onCompletion(Boolean isCompleted) {
                cancelUploading(view);
            }
        });
    }

    // MARK: search

    public void cancelUploading(View view) {
        findViewById(R.id.clients_list_upload_order_box).setVisibility(View.GONE);
        grayLayout.setVisibility(View.GONE);
    }

    public void setShowSearchBar(Boolean show) {
        findViewById(R.id.clients_activity_toolbar).setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.clients_list_search_bar).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        if (!show) {
            clientsRecyclerView.setVisibility(View.VISIBLE);
            if (searchBar != null) {
                searchBar.setText(null);
            }
            findViewById(R.id.client_list_fish_layout_no_result).setVisibility(View.INVISIBLE);
            if (clientListAdapter != null) {
                clientListAdapter.setSearchRequest(null);
            }
            hideSoftKeyboard();
        } else {
            if (searchBar == null)
                searchBar = (EditText) findViewById(R.id.clients_list_search_edit_text);
            searchBar.setInputType(InputType.TYPE_CLASS_TEXT);
            searchBar.setCursorVisible(true);
            final Animation animationRotateCenter = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
            findViewById(R.id.clients_list_search_cross).startAnimation(animationRotateCenter);
            searchBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchBar.setCursorVisible(true);
                }
            });
            // Removed by scream on 2016-10-26
            /*searchBar.addTextChangedListener(new TextWatcher() {
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
            });*/
            searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        SuggestionsAdapter.addSuggestion(ClientsList.this, SuggestionsAdapter.ActivityKind.CLIENTS_LIST, String.valueOf(searchBar.getText()));
                        hideSoftKeyboard();
                        filterItems(String.valueOf(searchBar.getText()));
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
        if (clientListAdapter == null) {
            return;
        }
        if (clientListAdapter.getSelectedItem() != -1) {
            clientListAdapter.setSelectedItem(-1);
            setSelectedClient(null);
        }
        suggestionsListView.setVisibility(View.GONE);
        clientListAdapter.setSearchRequest(request);
        Boolean showFish = clientListAdapter.getItemCount() == 0;
        clientsRecyclerView.setVisibility(showFish ? View.GONE : View.VISIBLE);
        findViewById(R.id.client_list_fish_layout_no_result).setVisibility(showFish ?
                View.VISIBLE : View.INVISIBLE);
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
    }

    public void hideSearchBar(View view) {
        setShowSearchBar(false);
    }

    public void clearSearchBar(View button) {
        if (searchBar != null) {
            if (searchBar.getText() != null && !searchBar.getText().toString().isEmpty()) {
                searchBar.setText(null);
                setShowSearchBar(true);
                showSuggestions();
            } else {
                setShowSearchBar(false);
            }
        }
    }

    private void showSuggestions() {
        suggestionsListView.setAdapter(new SuggestionsAdapter(this,
                SuggestionsAdapter.ActivityKind.CLIENTS_LIST));
        suggestionsListView.setVisibility(View.VISIBLE);
        suggestionsListView.setClickable(true);
        suggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedSuggestion = (String) suggestionsListView.getAdapter().getItem(i);
                searchBar.setText(selectedSuggestion);
                searchBar.setSelection(searchBar.getText().length());
                hideSoftKeyboard();
                filterItems(String.valueOf(searchBar.getText()));
            }
        });
    }

    private void setSelectedClient(Client client) {
        Fragment show, hide;
        if (client == null || client.isSectionHeader()) {
            show = clientsListInfoFragment;
            hide = clientsListClientFragment;
        } else {
            hide = clientsListInfoFragment;
            show = clientsListClientFragment;
        }
        if (client != null && !prevClientId.equals(client.getId()) && client.hasOverdueInvoiceDebts()) {
            Toast.makeText(
                    SanwellApplication.applicationContext,
                    "У данного клиента есть просроченные платежи",
                    Toast.LENGTH_LONG
            ).show();
            prevClientId = client.getId();
        }
        clientsListClientFragment.setSelectedClient(client);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .show(show)
                .hide(hide)
                .commit();
    }

}
