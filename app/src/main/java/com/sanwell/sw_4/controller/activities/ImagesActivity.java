package com.sanwell.sw_4.controller.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.adapters.FullScreenImageAdapter;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.objects.ImageUrl;

import java.util.ArrayList;

public class ImagesActivity extends AppCompatActivity {
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        final Toolbar myToolbar = (Toolbar) findViewById(R.id.images_activity_toolbar);
        myToolbar.setNavigationIcon(R.drawable.back_icon_white);

        ArrayList<ImageUrl> urls = new ArrayList<>();
        if (Helpers.currentItem != null) {
            urls = Helpers.currentItem.getImageURLS();
            myToolbar.setSubtitle(Helpers.currentItem.getTitle());
        }

        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mViewPager = (ViewPager) findViewById(R.id.images_pager);
        mViewPager.setAdapter(new FullScreenImageAdapter(this, urls));

        int page = 0;
        if (Helpers.currentItem != null && Helpers.currentItem.getPickedImageThumb() != null) {
            page = Helpers.currentItem.getPickedImageThumb();
            ActionBar actionBar1 = getSupportActionBar();
            if (actionBar1 != null) {
                actionBar1.setTitle("Изображение " + (page + 1) + " из " + urls.size());
            }
        }
        final ArrayList<ImageUrl> finalUrls = urls;
        mViewPager.setCurrentItem(page, false);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ActionBar actionBar1 = getSupportActionBar();
                if (actionBar1 != null) {
                    actionBar1.setTitle("Изображение " + (position + 1) + " из " + finalUrls.size());
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (Helpers.currentItem != null) {
            Helpers.currentItem.setPickedImageThumb(mViewPager.getCurrentItem());
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_images, menu);
        return true;
    }

}
