package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;


public class TextWithImage extends LinearLayout {

    String text = "";
    int image = 0;
    int color;

    public TextWithImage(Context context) {
        super(context);
    }

    public TextWithImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextWithImage, 0, 0);

        text = a.getString(R.styleable.TextWithImage_iconText);
        image = a.getResourceId(R.styleable.TextWithImage_iconImg, 0);
        color = a.getColor(R.styleable.TextWithImage_iconTint, ContextCompat.getColor(context, R.color.primary));

        init(context);
    }

    public TextWithImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextWithImage, 0, 0);

        text = a.getString(R.styleable.TextWithImage_iconText);
        image = a.getResourceId(R.styleable.TextWithImage_iconImg, 0);
        color = a.getColor(R.styleable.TextWithImage_iconTint, ContextCompat.getColor(context, R.color.primary));


        init(context);
    }

    public TextWithImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextWithImage, 0, 0);

        text = a.getString(R.styleable.TextWithImage_iconText);
        image = a.getResourceId(R.styleable.TextWithImage_iconImg, 0);
        color = a.getColor(R.styleable.TextWithImage_iconTint, ContextCompat.getColor(context, R.color.primary));

        init(context);
    }

    ViewGroup root;

    public void init(Context c) {

        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.text_with_icon_view, this, true);

        ((TextView) root.findViewById(R.id.text)).setText(text);
        ((TextView) root.findViewById(R.id.text)).setTextColor(color);
        ((ImageView) root.findViewById(R.id.image)).setImageDrawable(ContextCompat.getDrawable(c, image));
        ((ImageView) root.findViewById(R.id.image)).setImageTintList(ColorStateList.valueOf(color));
    }
}
