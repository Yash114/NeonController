package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.gingertech.starbeam.R;
import com.google.android.material.slider.Slider;

public class ColorSliderController extends LinearLayout {

    View root;

    Slider redBar;
    Slider greenBar;
    Slider blueBar;
    Slider alphaBar;

    GenericCallbackv2 colorCallback;

    Color slidedColor = new Color();
    float alpha = 10;



    public ColorSliderController(Context context) {
        super(context);
        init(context);
    }

    public ColorSliderController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ColorSliderController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ColorSliderController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context c) {

        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = inflater.inflate(R.layout.color_slider_view, this, true);

        slidedColor = Color.valueOf(Color.WHITE);

        redBar = root.findViewById(R.id.redBar);
        greenBar = root.findViewById(R.id.greenBar);
        blueBar = root.findViewById(R.id.blueBar);
        alphaBar = root.findViewById(R.id.alphaBar);

        redBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                slidedColor = Color.valueOf(((Slider) view).getValue(), slidedColor.green(), slidedColor.blue());
                if(colorCallback != null) {
                    colorCallback.onChange(slidedColor, alpha);
                }
                return false;
            }
        });

        greenBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                    slidedColor = Color.valueOf(slidedColor.red(), ((Slider) view).getValue(), slidedColor.blue());
                    if(colorCallback != null) {
                        colorCallback.onChange(slidedColor, alpha);
                    }

                return false;
            }
        });


        blueBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                    slidedColor = Color.valueOf(slidedColor.red(), slidedColor.green(), ((Slider) view).getValue());
                    if(colorCallback != null) {
                        colorCallback.onChange(slidedColor, alpha);
                    }

                return false;
            }
        });

        alphaBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                    //TODO fix alpha values
                    alpha = ((Slider) view).getValue();
                    if(colorCallback != null) {
                        colorCallback.onChange(slidedColor, alpha);
                    }

                return false;
            }
        });
    }

    public void setColor(Color color, float alpha) {
        this.slidedColor = color;

        redBar.setValue(slidedColor.red());
        greenBar.setValue(slidedColor.green());
        blueBar.setValue(slidedColor.blue());
        alphaBar.setValue(alpha > 1 ? 1 : alpha);
    }

    public void setOnColorChangeListener(GenericCallbackv2 callbackv2) {
        this.colorCallback = callbackv2;
    }
}
