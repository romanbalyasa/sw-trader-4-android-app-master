package com.sanwell.sw_4.controller.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sanwell.sw_4.R;
import com.sanwell.sw_4.controller.activities.ItemActivity;
import com.sanwell.sw_4.model.Helpers;
import com.sanwell.sw_4.model.database.DataModel;
import com.sanwell.sw_4.model.database.cores.RBatch;
import com.sanwell.sw_4.model.database.cores.ROrderBatchInfo;
import com.sanwell.sw_4.model.database.objects.Item;
import com.sanwell.sw_4.model.database.objects.ItemPriceInfo;
import com.sanwell.sw_4.model.database.objects.Order;
import com.sanwell.sw_4.view.ComboSeekBar;

import java.util.ArrayList;
import java.util.Locale;

public class ItemFragment extends Fragment {

    private ComboSeekBar comboSeekBar;
    private SeekBar countSeekBar;
    private EditText countInputEditText;
    private Item currentItem;
    private Order order;
    private View itemView, emptyView;
    private ImageButton forwardButton, backwardButton;
    private ArrayList<ItemPriceInfo> priceInfos;
    private String latestPrice = null;
    private TextView priceTextView, priceCurrencyTextView, priceSaleTextView, priceTypeTextView;
    private TextView mMinOrderTV;

    // BRG цена из заказа - начало
    private Double orderPrice = 0.0;
    public boolean setOrderPrice(Double order_Price) {
        this.orderPrice = order_Price;
        return true;
    }
    // BRG - окончание

    public ItemFragment setOrder(Order order) {
        this.order = order;
        return this;
    }

    public EditText getCountInputEditText() {
        return countInputEditText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        currentItem = Helpers.currentItem;
        View view = inflater.inflate(R.layout.fragment_item_price, container, false);
        ((TextView) view.findViewById(R.id.fragment_item_currency)).setText(
                Html.fromHtml(DataModel.getInstance().getFormattedCurrency()));
        forwardButton = (ImageButton) view.findViewById(R.id.fragment_item_default_move_forward);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comboSeekBar.moveForward();
                checkButtonsState();
            }
        });
        backwardButton = (ImageButton) view.findViewById(R.id.fragment_item_default_move_backward);
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comboSeekBar.moveBackward();
                checkButtonsState();
            }
        });
        itemView = view.findViewById(R.id.fragment_item_cost_block);
        emptyView = view.findViewById(R.id.fragment_item_default_block);
        countSeekBar = (SeekBar) view.findViewById(R.id.fragment_item_cost_seek_bar);
        comboSeekBar = (ComboSeekBar) view.findViewById(R.id.fragment_item_default_seek_bar);

        priceTextView = (TextView) view.findViewById(R.id.fragment_item_default_cost);
        priceCurrencyTextView = (TextView) view.findViewById(R.id.fragment_item_default_cost_currency);
        priceSaleTextView = (TextView) view.findViewById(R.id.fragment_item_default_discount);
        priceTypeTextView = (TextView) view.findViewById(R.id.fragment_item_default_price_type);

        mMinOrderTV = (TextView) view.findViewById(R.id.min_order_tv);
        mMinOrderTV.setText(getString(R.string.min_order, currentItem.getOrderMinCount()));
        countInputEditText = (EditText) view.findViewById(R.id.fragment_item_cost_count);
        countInputEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                countInputEditText.setCursorVisible(true);
                countInputEditText.setFocusable(true);
                countInputEditText.requestFocus();
                countInputEditText.setSelection(countInputEditText.getText().length());
                return false;
            }
        });
        countInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countInputEditText.setSelection(countInputEditText.getText().length());
            }
        });
        countInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkMinOrder();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        view.findViewById(R.id.fragment_item_cost_apply_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String count = getCount();
                double c = Item.safeParse(count);
                if (c % currentItem.getMultFactor() != 0) {
                    c = c - c % currentItem.getMultFactor();
                    countInputEditText.setText(String.valueOf((int) (c)));
                }
                ((ItemActivity) getActivity()).deselectItem(true);
            }
        });
        try {
            int max = (int) (10 * Double.parseDouble(currentItem.getStock()));
            countSeekBar.setMax(max);
        } catch (Exception ignored) {
        }
        countSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                countInputEditText.setText(String.valueOf(i / 10));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.incrementProgressBy(seekBar.getKeyProgressIncrement());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Уменьшить количество заказываемого товара
        view.findViewById(R.id.fragment_item_cost_seek_bar_min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double value = 0;
                Log.wtf("SW4","Minus one pressed");
                double step = currentItem.getMultFactor();
                if (step == 0)
                    step = 1;
                try {
                    value = Integer.parseInt(countInputEditText.getText().toString());
                    if (value >= 1) {
                        value -= step;
                    }
                } catch (Exception ignored) {
                }
                countSeekBar.setProgress((int) (value * 10));
                countInputEditText.setText(String.valueOf((int) (value)));
            }
        });
        //Увеличить количество заказываемого товара
        view.findViewById(R.id.fragment_item_cost_seek_bar_max).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double value = 0;
                double step = currentItem.getMultFactor();
                if (step == 0)
                    step = 1;
                try {
                    value = Integer.parseInt(countInputEditText.getText().toString());
                    value += step;
                } catch (Exception ignored) {
                }
                countSeekBar.setProgress((int) (value * 10));
                countInputEditText.setText(String.valueOf((int) (value)));
            }
        });

        TextView goalTextView = (TextView) view.findViewById(R.id.fragment_item_info_goal);
        goalTextView.setText("");
        TextView fact = (TextView) view.findViewById(R.id.fragment_item_info_sold);
        fact.setText("Продано: "
                + (int) currentItem.getFact()
                + " ("
                + (int) (currentItem.getPlan() == 0 ? 0 : 100.0 * currentItem.getFact() / currentItem.getPlan())
                + "%)");
        TextView planned = (TextView) view.findViewById(R.id.fragment_item_info_planned);
        planned.setText("По плану: " + (int) currentItem.getPlan());

        checkMinOrder();
        return view;
    }

    private void checkMinOrder() {
        if (currentItem == null || mMinOrderTV == null || countInputEditText == null) return;
        if (currentItem.getOrderMinCount() > Item.safeParse(getCount())) {
            mMinOrderTV.setBackgroundResource(R.drawable.min_order_bg);
            countInputEditText.setTextColor(Color.RED);
        } else {
            mMinOrderTV.setBackgroundResource(R.color.white_lilac);
            countInputEditText.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPrice();
    }

    private void refreshPrice() {
        ArrayList<Integer> points = new ArrayList<>();
        priceInfos = new ArrayList<>();
        if (this.orderPrice != 0.0)
        {currentItem.setOrderPrice(this.orderPrice);} // BRG
        Integer selection = currentItem.fillPricePointsArray(points, priceInfos,
                order.getCurrency(), Helpers.currentClient.getId());
        // BRG установка категории цены из уже записанного заказа - начало


        // BRG установка категории цены из уже записанного заказа - окончание
        comboSeekBar.setAdapter(points);
        comboSeekBar.setSelection(selection);
        checkButtonsState();
    }

    private void checkButtonsState() {
        int selection = comboSeekBar.getSelection();
        if (selection >= priceInfos.size()) {
            return;
        }
        ItemPriceInfo currentInfo = priceInfos.get(selection);
        priceTextView.setText(currentInfo.getPrice());
        priceCurrencyTextView.setText(currentInfo.getCurrency());
        if (currentInfo.getSale().isEmpty()) {
            priceSaleTextView.setVisibility(View.INVISIBLE);
        } else {
            priceSaleTextView.setVisibility(View.VISIBLE);
            priceSaleTextView.setText(getString(R.string.item_discount_prefix, currentInfo.getSale()));
        }

        priceTypeTextView.setText(getString(R.string.item_price_type_prefix, currentInfo.getName()));
        forwardButton.setEnabled(true);
        backwardButton.setEnabled(true);
        if ((selection + 1) == comboSeekBar.getArrayCount()) {
            forwardButton.setEnabled(false);
        }
        if (selection == 0) {
            backwardButton.setEnabled(false);
        }
    }

    public String getCount() {
        return countInputEditText == null ? "0" : countInputEditText.getText().toString();
    }

    public String getPrice() {
        latestPrice = priceTextView == null ? "0" : priceTextView.getText().toString();
        return latestPrice;
    }

    public void setCurrentItem(boolean item, RBatch batch) {
        latestPrice = priceTextView == null ? "0" : priceTextView.getText().toString();
        if (item && emptyView.getVisibility() == View.GONE) {
            refreshPrice();
        }
        countInputEditText.setText("0");
        countSeekBar.setProgress(0);
        if (batch != null) {
            countSeekBar.setMax(batch.getStock() * 10);
            ROrderBatchInfo batchInfo = order.getBatchInfo(currentItem.getItemID(),
                    batch.getBatchID());
            if (batchInfo != null) {
                double v = batchInfo.getItemsCount();
                countSeekBar.setProgress((int) (10 * v));
                if (v % 1 == 0) {
                    countInputEditText.setText(String.valueOf((int) v));
                } else {
                    countInputEditText.setText(String.format(Locale.US, "%.2f", v));
                }
            }
        }
        itemView.setVisibility(!item ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(item ? View.GONE : View.VISIBLE);
    }

    public String getPriceCode() {
        int selection = comboSeekBar.getSelection();
        if (selection >= priceInfos.size()) {
            return "";
        }
        return priceInfos.get(selection).getPriceCode();
    }

    public String getSuggestedPriceCode() {
        int selection = comboSeekBar.getSelection();
        if (selection >= priceInfos.size()) {
            return "";
        }
        return priceInfos.get(selection).getSuggestedPriceCode();
    }
}
