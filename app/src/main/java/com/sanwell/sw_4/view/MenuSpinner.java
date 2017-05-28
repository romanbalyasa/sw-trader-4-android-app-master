package com.sanwell.sw_4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

import com.sanwell.sw_4.model.interfaces.OnSpinnerEventsListener;

public class MenuSpinner extends Spinner {

    private OnSpinnerEventsListener mListener;
    private boolean mOpenInitiated = false;

    public MenuSpinner(Context context) {
        super(context);
    }

    public MenuSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
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

    public boolean handleOnWindowFocusChanged(Boolean hasFocus) {
        if (hasBeenOpened() && hasFocus) {
            performClosedEvent();
            return true;
        }
        return false;
    }

    public boolean hasBeenOpened() {
        return mOpenInitiated;
    }

}