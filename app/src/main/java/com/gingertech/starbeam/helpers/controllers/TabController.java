package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;

public class TabController extends ConstraintLayout {

    int iconRef = 0;
    String iconText = "";

    GenericCallback genericCallback;

    public TabController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TabController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabController, 0, 0);

        iconText = a.getString(R.styleable.TabController_text);
        iconRef = a.getResourceId(R.styleable.TabController_icon_img, 0);

        init(context);
    }

    public TabController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabController, 0, 0);

        iconText = a.getString(R.styleable.TabController_text);
        iconRef = a.getResourceId(R.styleable.TabController_icon_img, 0);
        init(context);
    }

    public TabController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabController, 0, 0);

        iconText = a.getString(R.styleable.TabController_text);
        iconRef = a.getResourceId(R.styleable.TabController_icon_img, 0);

        init(context);
    }

    ViewGroup root;
    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.creation_tabs, this, true);

        ImageView img = root.findViewById(R.id.img);
        TextView txt = root.findViewById(R.id.text);

        if(iconRef == 0) {
            img.setVisibility(GONE);
        } else {
            img.setImageDrawable(ContextCompat.getDrawable(context, iconRef));
            img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    genericCallback.fire(null);
                }
            });
        }

        txt.setText(iconText);
        txt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                genericCallback.fire(null);
            }
        });
    }

    public void setGenericCallback(GenericCallback genericCallback) {
        this.genericCallback = genericCallback;
    }

    boolean clicked = false;
    public void setClick(Boolean down) {
        if(clicked != down) {

            ObjectAnimator a1 = ObjectAnimator.ofFloat(root, "scaleX", clicked ? 0.9f : 1);
            a1.setDuration(400);
            a1.start();

            ObjectAnimator a2 = ObjectAnimator.ofFloat(root, "scaleY", clicked ? 0.9f : 1);
            a2.setDuration(400);
            a2.start();

            clicked = down;
        }
    }
}
