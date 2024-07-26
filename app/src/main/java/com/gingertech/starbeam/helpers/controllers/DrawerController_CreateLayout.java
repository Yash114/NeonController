package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;

import java.util.ArrayList;

public class DrawerController_CreateLayout extends ConstraintLayout {

    OnGenericCallbackv2 clickedCallback;

    ViewGroup root;
    ViewGroup viewContainer;
    final ArrayList<TabViewController_create> childViews = new ArrayList<>();

    public boolean isOpen = false;

    public void setCallback(GenericCallbackv2 clickedCallback) {
        this.clickedCallback = clickedCallback;
    }

    public DrawerController_CreateLayout(Context context) {
        super(context);
        init(context);
    }

    public DrawerController_CreateLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawerController_CreateLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    public DrawerController_CreateLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.drawer_view_create, this, true);

        viewContainer = root.findViewById(R.id.container_view);

        int childCount = ((ViewGroup) root.findViewById(R.id.buttonList)).getChildCount();

        for(int i = 0; i < childCount; i++) {

            if ((((ViewGroup) root.findViewById(R.id.buttonList)).getChildAt(i)) instanceof TabViewController_create) {

                TabViewController_create temp = (TabViewController_create) (((ViewGroup) root.findViewById(R.id.buttonList)).getChildAt(i));
                childViews.add(temp);
                final int index = i;
                temp.disableClickAnimation = true;
                temp.setClickAction(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
                    @Override
                    public void onChange(Object value) {
                        selectedTab = index;

                        if (clickedCallback != null) {
                            clickedCallback.onChange(index);
                        }

                        toggleDrawer();
                    }
                }));
            }
        }
    }

    int selectedTab = 0;

    public void toggleDrawer() {
        isOpen = !isOpen;

        ObjectAnimator a2;
        a2 = ObjectAnimator.ofFloat(viewContainer, "translationX", isOpen ? -viewContainer.getWidth() : 0);
        a2.setDuration(500);
        a2.start();

    }
}
