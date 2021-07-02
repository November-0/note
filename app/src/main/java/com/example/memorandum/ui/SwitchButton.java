package com.example.memorandum.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.memorandum.R;

/**
 * Created by Boogie on 2021/06/06.
 */

public class SwitchButton extends FrameLayout {

    private final ImageView openImage;
    private final ImageView closeImage;

    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
        Drawable openDrawable = ta.getDrawable(R.styleable.SwitchButton_switchOpenImage);
        Drawable closeDrawable = ta.getDrawable(R.styleable.SwitchButton_switchCloseImage);

        int switchStatus = ta.getInt(R.styleable.SwitchButton_switchStatus, 0);
        ta.recycle();
        LayoutInflater.from(context).inflate(R.layout.widget_switch_button, this);

        openImage = (ImageView) findViewById(R.id.iv_switch_open);
        closeImage = (ImageView) findViewById(R.id.iv_switch_close);

        if (openDrawable != null) {
            openImage.setImageDrawable(openDrawable);
        }
        if (closeDrawable != null) {
            closeImage.setImageDrawable(closeDrawable);
        }
        if (switchStatus == 1) {
            closeSwitch();
        }
        if (switchStatus == 0) {
            openSwitch();
        }

    }

    /**
     * is switch open
     */
    public boolean isSwitchOpen() {
        return openImage.getVisibility() == View.VISIBLE;
    }

    public void openSwitch() {
        openImage.setVisibility(View.VISIBLE);
        closeImage.setVisibility(View.INVISIBLE);
    }

    public void closeSwitch() {
        openImage.setVisibility(View.INVISIBLE);
        closeImage.setVisibility(View.VISIBLE);
    }

}
