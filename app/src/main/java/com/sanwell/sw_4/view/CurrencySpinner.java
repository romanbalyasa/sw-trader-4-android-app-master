package com.sanwell.sw_4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.model.interfaces.OnSpinnerEventsListener;

/*
 * Created by Roman Kyrylenko on 04/12/15.
 */
public class CurrencySpinner extends Spinner {

    private OnSpinnerEventsListener mListener;
    private boolean mOpenInitiated = false;
    private boolean isClickAvailable = true;
    private Context mContext;

    public CurrencySpinner(Context context) {
        super(context);
        mContext = context;
    }

    public CurrencySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public CurrencySpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setIsClickAvailable(boolean isClickAvailable) {
        this.isClickAvailable = isClickAvailable;
    }

    @Override
    public boolean performClick() {
        if (!isClickAvailable) {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(mContext, R.style.CommonAlertDialog);
            builder.setMessage("Предварительно необходимо закрыть открытый в данный момент заказ");
            builder.setTitle("Выбор валюты не доступен");
            builder.setNegativeButton("Понятно", null);
            builder.show();
            return true;
        }
        mOpenInitiated = true;
        if (mListener != null) {
            mListener.onSpinnerOpened();
        }
        return super.performClick();
    }

    public void setSpinnerEventsListener(OnSpinnerEventsListener onSpinnerEventsListener) {
        mListener = onSpinnerEventsListener;
    }

    public void performClosedEvent() {
        mOpenInitiated = false;
        if (mListener != null) {
            mListener.onSpinnerClosed();
        }
    }

    public void handleOnWindowFocusChanged(Boolean hasFocus) {
        if (hasBeenOpened() && hasFocus) {
            performClosedEvent();
        }
    }

    public boolean hasBeenOpened() {
        return mOpenInitiated;
    }

}