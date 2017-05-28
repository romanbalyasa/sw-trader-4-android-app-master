package com.sanwell.sw_4.controller.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.SanwellApplication;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RImageUrl;

import java.io.File;

import io.realm.RealmResults;

public class SettingsActivity extends AppCompatActivity {

    private Boolean refresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_activity_toolbar);
        myToolbar.setNavigationIcon(R.drawable.back_icon);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final EditText numberEditText = (EditText) findViewById(R.id.setting_device_number_edit_text);
        numberEditText.setText(Helpers.read_sp(Helpers.DEVICE_USER_ID, ""));
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (Helpers.isValidEmail(numberEditText.getText()))
                    Helpers.edit_sp(Helpers.DEVICE_USER_ID, numberEditText.getText().toString());
            }

        });

        final EditText emailEditText = (EditText) findViewById(R.id.settings_email);
        emailEditText.setText(Helpers.read_sp(Helpers.DEVICE_USER_EMAIL_KEY, Helpers.DEFAULT_EMAIL));
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Helpers.edit_sp(Helpers.DEVICE_USER_EMAIL_KEY, emailEditText.getText().toString());
            }

        });
        String versionName = "v 1";
        try {
            versionName = "v " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.setting_toolbar_version)).setText(versionName);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    public void refreshDBButtonAction(View view) {
        findViewById(R.id.settings_gray_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.settings_refresh_db).setVisibility(View.VISIBLE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.settings_refresh_db_progress_bar);
        RealmResults<RImageUrl> rList = DataModel.getInstance()
                .realm
                .where(RImageUrl.class)
                .findAll();
        Double totalCount = (double) rList.size();
        progressBar.setProgress(0);
        downloadImage(0, totalCount, rList, progressBar);
    }

    private void downloadImage(final int position, final Double totalCount,
                               final RealmResults<RImageUrl> list,
                               final ProgressBar progressBar) {
        if (!refresh || position >= list.size()) {
            findViewById(R.id.settings_gray_layout).setVisibility(View.INVISIBLE);
            findViewById(R.id.settings_refresh_db).setVisibility(View.INVISIBLE);
            refresh = true;
            return;
        }
        progressBar.setProgress((int) (100 * ((double) position / totalCount)));

        RImageUrl imageUrl = list.get(position);
        System.out.println("start " + imageUrl.getUrl());
        String filename = imageUrl.getItemID() + ".jpg";
        Ion.with(this)
                .load(imageUrl.getUrl())
                .write(new File(SanwellApplication.applicationContext.getFilesDir() + "/"
                        + filename))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                        downloadImage(position + 1, totalCount, list, progressBar);
                    }
                });

    }

    public void cancelDBRefresh(View view) {
        refresh = false;
        Ion.getDefault(this).cancelAll();
        findViewById(R.id.settings_gray_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.settings_refresh_db).setVisibility(View.INVISIBLE);
    }
}
