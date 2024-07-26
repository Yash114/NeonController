package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;

import java.util.ArrayList;

public class LayoutEditorController extends ConstraintLayout {

    public LayoutEditorController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LayoutEditorController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LayoutEditorController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LayoutEditorController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    ViewGroup root;

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.layout_creation_view, this, true);

        DrawerController_CreateLayout drawer = root.findViewById(R.id.drawer);
        drawer.setCallback(new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {

            }
        });


    }


}
