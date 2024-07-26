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

import java.util.logging.Handler;

public class TabViewController extends ConstraintLayout {

    String text = "";
    boolean selected = false;

    public int position = 0;

    public TabViewController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TabViewController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabViewController, 0, 0);
        text = a.getString(R.styleable.TabViewController_tabText);
        selected = a.getBoolean(R.styleable.TabViewController_selected, false);
        position = a.getInt(R.styleable.TabViewController_index, 0);
        init(context);
    }

    public TabViewController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabViewController, 0, 0);
        text = a.getString(R.styleable.TabViewController_tabText);
        selected = a.getBoolean(R.styleable.TabViewController_selected, false);
        position = a.getInt(R.styleable.TabViewController_index, 0);
        init(context);
    }

    public TabViewController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabViewController, 0, 0);
        text = a.getString(R.styleable.TabViewController_tabText);
        selected = a.getBoolean(R.styleable.TabViewController_selected, false);
        position = a.getInt(R.styleable.TabViewController_index, 0);
        init(context);
    }

    ViewGroup root;

    View selectedView;
    View disselectedView;

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.tab_view, this, true);

        OnClickListener touched = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(genericCallback == null) { return; }
                genericCallback.fire(null);
            }
        };

        selectedView = root.findViewById(R.id.selected);
        ((TextView) selectedView).setText(text);
        selectedView.setOnClickListener(touched);

        disselectedView = root.findViewById(R.id.disselected);
        ((TextView) disselectedView).setText(text);
        disselectedView.setOnClickListener(touched);

        setSelected(selected);

        selectedRunnable = new Runnable() {
            @Override
            public void run() {
                if(selected) {

                    ObjectAnimator a2;
                    a2 = ObjectAnimator.ofFloat(disselectedView, "translationX", disselectedView.getWidth());
                    a2.setDuration(delay);
                    a2.start();

                    ObjectAnimator a1;
                    a1 = ObjectAnimator.ofFloat(selectedView, "translationX", 0);
                    a1.setStartDelay(delay);
                    a1.setDuration(delay);
                    a1.start();

                } else {

                    ObjectAnimator a2;
                    a2 = ObjectAnimator.ofFloat(selectedView, "translationX", selectedView.getWidth());
                    a2.setDuration(delay);
                    a2.start();

                    ObjectAnimator a1;
                    a1 = ObjectAnimator.ofFloat(disselectedView, "translationX", 0);
                    a1.setStartDelay(delay);
                    a1.setDuration(delay);
                    a1.start();
                }
            }
        };

    }

    Runnable selectedRunnable;


    int delay = 200;


    public void setSelected(boolean selected) {

        root.post(selectedRunnable);

        this.selected = selected;
    }

    GenericCallback genericCallback;
    public void setClickAction(GenericCallback genericCallback) {
        this.genericCallback = genericCallback;
    }
}
