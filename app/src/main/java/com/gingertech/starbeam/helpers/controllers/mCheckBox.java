package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gingertech.starbeam.R;

public class mCheckBox extends androidx.constraintlayout.widget.ConstraintLayout {

    public ViewGroup root;
    public boolean activated = false;

    public boolean clickable = true;

    TypedArray typedArrayAttributes;

    public mCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.mCheckBox, 0, 0);
        clickable = typedArrayAttributes.getBoolean(R.styleable.mCheckBox_checkboxCheckable, false);
        activated = typedArrayAttributes.getBoolean(R.styleable.mCheckBox_checkboxChecked, true);
        typedArrayAttributes.recycle();
        init(context);
    }

    public mCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.mCheckBox, 0, 0);
        clickable = typedArrayAttributes.getBoolean(R.styleable.mCheckBox_checkboxCheckable, false);
        activated = typedArrayAttributes.getBoolean(R.styleable.mCheckBox_checkboxChecked, true);
        typedArrayAttributes.recycle();
        init(context);
    }

    public mCheckBox(Context context) {
        super(context);
        init(context);
    }

    public void init(Context c){
        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.checkbox, this, true);

        root.findViewById(R.id.checkbox).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clickAction();
            }
        });

        root.findViewById(R.id.checkbox_anti).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clickAction();
            }
        });

        setActivated(activated);

    }

    public void setActivated(boolean activated) {
        root.findViewById(R.id.checkbox)
                .setVisibility(activated ? VISIBLE : INVISIBLE);

        root.findViewById(R.id.checkbox_anti)
                .setVisibility(!activated ? VISIBLE : INVISIBLE);

        this.activated = activated;
    }

    public void toggle(){
        this.activated = !this.activated;
        setActivated(this.activated);
    }

    public void clickAction() {

        if(clickable) {
            this.toggle();

            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(SCALE_Y, 1.1f);
            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(SCALE_X, 1.1f);
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setRepeatCount(1);
            animator.setDuration(100);
            animator.start();
        }

        if(clickCallback != null) {
            clickCallback.onChange(null);
        }
    }

    OnGenericCallbackv2 clickCallback;
    public void setOnClickAction(OnGenericCallbackv2 genericCallbackv2) {
        this.clickCallback = genericCallbackv2;
    }

}
