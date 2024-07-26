package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.widget.TextView;

public class mTextView extends androidx.appcompat.widget.AppCompatTextView {
    public mTextView(Context context) {
        super(context);

        this.setLineSpacing(0, 0.6f);
    }
}
