package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;

public class TabViewController_create extends ConstraintLayout {

    String text = "";
    String descriptionText = "";
    boolean disableClickAnimation = false;

    public TabViewController_create(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TabViewController_create(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabViewController_create, 0, 0);

        text = a.getString(R.styleable.TabViewController_create_tabText_create);
        descriptionText = a.getString(R.styleable.TabViewController_create_tabText_description);

        init(context);
    }

    public TabViewController_create(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabViewController_create, 0, 0);

        text = a.getString(R.styleable.TabViewController_create_tabText_create);
        descriptionText = a.getString(R.styleable.TabViewController_create_tabText_description);

        init(context);
    }

    public TabViewController_create(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabViewController_create, 0, 0);

        text = a.getString(R.styleable.TabViewController_create_tabText_create);
        descriptionText = a.getString(R.styleable.TabViewController_create_tabText_description);

        init(context);
    }

    ViewGroup root;

    View selectedView;
    View selectedViewDescription;

    boolean selected = true;

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.tab_view_create, this, true);

        OnClickListener touched = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(genericCallback == null) { return; }
                genericCallback.fire(null);
            }
        };

        selectedView = root.findViewById(R.id.tab);
        ((TextView) selectedView).setText(text);

        selectedView.setOnClickListener(touched);

        selectedViewDescription = root.findViewById(R.id.tabDescription);
        ((TextView) selectedViewDescription).setText(descriptionText);

        selectedViewDescription.setOnClickListener(touched);

    }

    GenericCallback genericCallback;
    public void setClickAction(GenericCallback genericCallback) {
        this.genericCallback = genericCallback;
    }
}
