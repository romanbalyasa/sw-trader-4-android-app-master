package com.sanwell.sw_4.controller.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.database.objects.ImageUrl;

import java.util.ArrayList;

/*
 * Created by Roman Kyrylenko on 05/12/15.
 */
public class FullScreenImageAdapter extends PagerAdapter {

    private ArrayList<ImageUrl> _imagePaths;
    private LayoutInflater inflater;
    private Context context;

    public FullScreenImageAdapter(Context context, ArrayList<ImageUrl> imagePaths) {
        _imagePaths = imagePaths;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public int getCount() {
        return this._imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final ImageView imgDisplay;

        View viewLayout = inflater.inflate(R.layout.fullscreen_image, container, false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);

        ImageUrl rImageUrl = _imagePaths.get(position);
        String url = rImageUrl.getUrl();
        Ion.with(context)
                .load(url)
                .withBitmap()
                .intoImageView(imgDisplay)
                .setCallback(new FutureCallback<ImageView>() {
                    @Override
                    public void onCompleted(Exception e, ImageView result) {

                    }
                });
        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}