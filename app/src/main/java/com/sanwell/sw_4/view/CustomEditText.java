package com.sanwell.sw_4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Roman Kyrylenko on 2/28/16.
 */

public class CustomEditText extends EditText {

    private CustomEditTextListener listener;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(CustomEditTextListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (listener != null) {
                listener.onBackPressed();
            }
        }
        return false;
    }

    public interface CustomEditTextListener {
        void onBackPressed();
    }
}