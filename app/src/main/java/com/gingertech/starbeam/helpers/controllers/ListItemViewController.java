package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;

public class ListItemViewController extends ConstraintLayout implements View.OnTouchListener {

    ViewGroup root;

    LayoutClass layoutClass;
    public ListItemViewController(@NonNull Context context, LayoutClass layoutClass) {
        super(context);
        this.layoutClass = layoutClass;
        init(context);
    }

    public ListItemViewController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ListItemViewController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context c) {
        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.layout_preview_view, this, true);
    }
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        return true;
    }

    GenericCallback onClick;

    public void setOnClick(GenericCallback onSuccessListener) {
        this.onClick = onSuccessListener;
    }

}
