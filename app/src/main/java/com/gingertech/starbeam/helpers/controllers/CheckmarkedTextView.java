package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gingertech.starbeam.R;

public class CheckmarkedTextView extends LinearLayout {
    TypedArray typedArrayAttributes;

    String text = "";
    ViewGroup root;

    public CheckmarkedTextView(Context context) {
        super(context);
        init(context);
    }

    public CheckmarkedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.CheckmarkedTextView, 0, 0);
        text = typedArrayAttributes.getString(R.styleable.CheckmarkedTextView_textVal);
        init(context);
    }

    public CheckmarkedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        typedArrayAttributes = context.obtainStyledAttributes(attrs, R.styleable.CheckmarkedTextView, 0, 0);
        text = typedArrayAttributes.getString(R.styleable.CheckmarkedTextView_textVal);
        init(context);
    }

    public CheckmarkedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.checkmarked_text_view, this, true);

        ((TextView) root.findViewById(R.id.textView)).setText(text);

    }
}
