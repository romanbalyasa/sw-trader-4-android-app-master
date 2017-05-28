package com.sanwell.sw_4.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

public class ComboSeekBar extends SeekBar {

    private CustomThumbDrawable mThumb; // unused
    private List<Dot> mDots = new ArrayList<>();
    private Dot prevSelected = null;
    private boolean isSelected = false;

    public ComboSeekBar(Context context) {
        super(context);
    }

    public ComboSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        int[] attrsArray = new int[]{android.R.attr.layout_height};
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
        int layout_height = ta.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
        ta.recycle();

        mThumb = new CustomThumbDrawable();
        setThumb(mThumb);
        setProgressDrawable(new CustomDrawable(this.getProgressDrawable(), this, layout_height, mDots));
        setPadding(0, 0, 0, 0);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        isSelected = false;
        return super.onTouchEvent(event);
    }

    public synchronized int getSelection() {
        for (Dot dot : mDots) {
            if (dot.isSelected)
                return dot.id;
        }
        return 0;
    }

    public synchronized void setSelection(int position) {
        if ((position < 0) || (position >= mDots.size())) {
            return;
        }
        for (Dot dot : mDots) {
            dot.isSelected = dot.id == position;
        }
        isSelected = true;
        invalidate();
    }

    public synchronized int getArrayCount() {
        return mDots.size();
    }

    public void setAdapter(List<Integer> dots) {
        mDots.clear();
        int index = 0;
        for (Integer position : dots) {
            Dot dot = new Dot();
            dot.id = index++;
            dot.percentage = position;
            mDots.add(dot);
        }
        initDotsCoordinates();
    }

    @Override
    public void setThumb(Drawable thumb) {

    }

    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        if ((mThumb != null) && (mDots.size() > 1)) {
            if (isSelected) {
                for (Dot dot : mDots) {
                    if (dot.isSelected) {
                        Rect bounds = mThumb.copyBounds();
                        bounds.right = dot.mX;
                        bounds.left = dot.mX;
                        mThumb.setBounds(bounds);
                        break;
                    }
                }
            } else {
                int intervalWidth = mDots.get(1).mX - mDots.get(0).mX;
                Rect bounds = mThumb.copyBounds();
                // find nearest dot
                if ((mDots.get(mDots.size() - 1).mX - bounds.centerX()) < 0) {
                    bounds.right = mDots.get(mDots.size() - 1).mX;
                    bounds.left = mDots.get(mDots.size() - 1).mX;
                    mThumb.setBounds(bounds);
                    for (Dot dot : mDots) {
                        dot.isSelected = false;
                    }
                    mDots.get(mDots.size() - 1).isSelected = true;
                    handleClick(mDots.get(mDots.size() - 1));
                } else {
                    for (int i = 0; i < mDots.size(); i++) {
                        if (Math.abs(mDots.get(i).mX - bounds.centerX()) <= (intervalWidth / 2)) {
                            bounds.right = mDots.get(i).mX;
                            bounds.left = mDots.get(i).mX;
                            mThumb.setBounds(bounds);
                            mDots.get(i).isSelected = true;
                            handleClick(mDots.get(i));
                        } else {
                            mDots.get(i).isSelected = false;
                        }
                    }
                }
            }
        }
        super.onDraw(canvas);
    }

    private void handleClick(Dot selected) {
        if ((prevSelected == null) || (!prevSelected.equals(selected))) {
            prevSelected = selected;
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CustomDrawable d = (CustomDrawable) getProgressDrawable();

        int thumbHeight = mThumb == null ? 0 : mThumb.getIntrinsicHeight();
        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = d.getIntrinsicWidth();
            dh = Math.max(thumbHeight, d.getIntrinsicHeight());
        }

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
    }

    private void initDotsCoordinates() {
        double oneDP = toPix(1); // offset for first and last lines
        double width = (getWidth() - 2 * oneDP) / 100.0;
        for (Dot dot : mDots) {
            dot.mX = (int) (oneDP + width * dot.percentage);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initDotsCoordinates();
    }

    public void moveBackward() {
        int selectedDotID = 0;
        for (Dot dot : mDots) {
            if (dot.isSelected) {
                selectedDotID = dot.id;
                dot.isSelected = false;
                break;
            }
        }
        selectedDotID--;
        if (selectedDotID < 0) {
            selectedDotID = 0;
        }
        setSelection(selectedDotID);
        invalidate();
    }

    public void moveForward() {
        int selectedDotID = 0;
        for (Dot dot : mDots) {
            if (dot.isSelected) {
                selectedDotID = dot.id;
                dot.isSelected = false;
                break;
            }
        }
        selectedDotID++;
        if (selectedDotID >= mDots.size()) {
            selectedDotID = mDots.size() - 1;
        }
        setSelection(selectedDotID);
        invalidate();
    }

    private float toPix(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                getContext().getResources().getDisplayMetrics());
    }

    public static class Dot {
        public int id;
        public int mX;
        public int percentage;
        public boolean isSelected = false;

        @Override
        public boolean equals(Object o) {
            return o instanceof Dot && ((Dot) o).id == id;
        }
    }

}