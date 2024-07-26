package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class DrawerController extends ConstraintLayout {

    GenericCallback genericCallback;

    ViewGroup root;
    ViewGroup viewContainer;
    final HashMap<Integer, TabViewController> childViews = new HashMap<>();

    boolean isOpen = false;
    boolean isFirstClick = true;

    private MixpanelAPI mp;
    public void setCallback(GenericCallback genericCallback) {
        this.genericCallback = genericCallback;
    }

    public DrawerController(Context context) {
        super(context);
        init(context);
    }

    public DrawerController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawerController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    public DrawerController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setMixPanel(MixpanelAPI mp) {
        this.mp = mp;
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.drawer_view, this, true);

        viewContainer = root.findViewById(R.id.container_view);

        int childCount = viewContainer.getChildCount();

        for(int i = 0; i < childCount; i++) {
            TabViewController temp = (TabViewController) viewContainer.getChildAt(i);
            childViews.put(temp.position, temp);
            final int index = temp.position;
            temp.setClickAction(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
                @Override
                public void onChange(Object value) {

                    if(!temp.text.contentEquals(context.getText(R.string.settings)) && !temp.text.equals("Discord")) {
                        temp.setSelected(true);

                        for(TabViewController view : childViews.values()) {
                            if(!Objects.equals(view.text, temp.text)) {
                                view.setSelected(false);
                            }
                        }
                    }

                    if(genericCallback != null){
                        genericCallback.fire(index);
                    }

                    toggleDrawer();
                }
            }));
        }
    }

    public void toggleDrawer() {
        isOpen = !isOpen;

        selectChild(UserData.CurrentPage);

        ObjectAnimator a2;
        a2 = ObjectAnimator.ofFloat(viewContainer, "translationX", isOpen ? -viewContainer.getWidth() : 0);
        a2.setDuration(500);
        a2.start();
    }

    public void openDrawer(boolean open) {

        if(isOpen == open) { return; }

        isOpen = open;

        selectChild(UserData.CurrentPage);

        ObjectAnimator a2;
        a2 = ObjectAnimator.ofFloat(viewContainer, "translationX", isOpen ? -viewContainer.getWidth() : 0);
        a2.setDuration(500);
        a2.start();
    }

    public void selectChild(int tabIndex) {
        for(TabViewController v : childViews.values()) {
            v.setSelected(false);
        }

        if(childViews.get(tabIndex) == null) { return; }
        childViews.get(tabIndex).setSelected(true);
    }
}
