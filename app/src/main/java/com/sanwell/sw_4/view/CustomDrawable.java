package com.sanwell.sw_4.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import java.util.List;

public class CustomDrawable extends Drawable {
    private final ComboSeekBar mySlider;
    private final Drawable myBase;

    private final Paint unselectedLinePaint;
    private int height;
    private List<ComboSeekBar.Dot> mDots;
    private Paint selectLinePaint;
    private Paint verticalLinePaint;

    public CustomDrawable(Drawable base, ComboSeekBar slider, int height, List<ComboSeekBar.Dot> dots) {
        mySlider = slider;
        myBase = base;
        mDots = dots;

        this.height = height;
        unselectedLinePaint = new Paint();
        unselectedLinePaint.setColor(Color.rgb(229, 229, 229));
        unselectedLinePaint.setStrokeWidth(height / 3);

        selectLinePaint = new Paint();
        selectLinePaint.setColor(Color.rgb(232, 67, 65));
        selectLinePaint.setStrokeWidth(height / 3);

        verticalLinePaint = new Paint();
        verticalLinePaint.setStrokeWidth(toPix(1));
        verticalLinePaint.setColor(Color.rgb(123, 123, 123));
    }

    private float toPix(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, mySlider.getContext().getResources().getDisplayMetrics());
    }

    @Override
    protected final void onBoundsChange(Rect bounds) {
        myBase.setBounds(bounds);
    }

    @Override
    protected final boolean onStateChange(int[] state) {
        invalidateSelf();
        return false;
    }

    @Override
    public final boolean isStateful() {
        return true;
    }

    @Override
    public final void draw(Canvas canvas) {
        int height = this.getIntrinsicHeight() / 2;
        if (mDots.size() == 0) {
            canvas.drawLine(0, height, getBounds().right, height, unselectedLinePaint);
            return;
        }
        for (ComboSeekBar.Dot dot : mDots) {
            if (dot.isSelected) {
                canvas.drawLine(mDots.get(0).mX, height, dot.mX, height, selectLinePaint);
                canvas.drawLine(dot.mX, height, mDots.get(mDots.size() - 1).mX, height, unselectedLinePaint);
            }
            canvas.drawLine(dot.mX, 0, dot.mX, canvas.getHeight(), verticalLinePaint);
        }
    }

    @Override
    public final int getIntrinsicHeight() {
        return height;
    }

    @Override
    public final int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }
}