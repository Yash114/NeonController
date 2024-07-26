package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.LayoutGroupClass;
import com.gingertech.starbeam.helpers.UserData;

import java.util.HashMap;
import java.util.Optional;

public class LayoutGroupView extends ConstraintLayout {

    View root;
    LayoutGroupClass groupClass;
    boolean isClicked = false;

    Context context;

    HashMap<String, View> groupID_View = new HashMap<>();

    public LayoutGroupView(Context context) {
        super(context);
        init(context);
    }

    public LayoutGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LayoutGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LayoutGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.layout_group_view, this, true);

        this.context = context;

        setClick(false);
    }

    void setup(LayoutGroupClass layout, Context context) {

        this.groupClass = layout;

        ((TextView) root.findViewById(R.id.titleText)).setText(layout.name);
        ((TextView) root.findViewById(R.id.descriptionText)).setText(layout.description);

        root.findViewById(R.id.downloaded).setVisibility(layout.isImported ? VISIBLE : GONE);

        root.findViewById(R.id.parent).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isClicked) { return; }

//                    Toast.makeText(context,getContext().getText(R.string.this_layout_is_locked_try_premium_to_unlock), Toast.LENGTH_LONG).show();
                if(clickedCallback != null) {

                    if(groupClass.layoutClasses.size() > 0) {

                        ViewGroup layouts = ((ViewGroup) root.findViewById(R.id.layouts));
                        LayoutPreviewTabController layoutView = (LayoutPreviewTabController) layouts.getChildAt(layouts.getChildCount() - 1);

                        LayoutClass g = layoutView.layout;

                            clickedCallback.fire(g);

                            try {
                                LayoutPreviewTabController p = (LayoutPreviewTabController) groupID_View.get(g.groupID);

                                ViewGroup viewGroup = ((ViewGroup) root.findViewById(R.id.layouts));
                                for(int i = 0; i < viewGroup.getChildCount(); i++) {
                                    LayoutPreviewTabController v = (LayoutPreviewTabController) viewGroup.getChildAt(i);

                                    if(v != view) {
                                        v.setClick(false);
                                    }
                                }

                                p.setClick(true);

                            } catch (NullPointerException ignored) { }
                    } else {

                        clickedCallback.fire(null);
                    }

                }


                setClick(true);
            }
        });

        for(LayoutClass layoutClass : groupClass.layoutClasses) {
            LayoutPreviewTabController l = new LayoutPreviewTabController(context, false);
            l.setup(layoutClass, getContext());
            l.setClick(layoutClass == UserData.currentLayout);
            l.showRemoveGroup(true);
            l.setClickedCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
                @Override
                public void onChange(Object value) {

                    if(value instanceof Boolean) {
                        if(!((Boolean) value)) {
                            clickedCallback.fire(-1);
                        }
                    }

                    if(!l.isClicked) {

                        l.setClick(true);
                        ViewGroup viewGroup = ((ViewGroup) root.findViewById(R.id.layouts));

                        for (int i = 0; i < viewGroup.getChildCount(); i++) {
                            LayoutPreviewTabController v = (LayoutPreviewTabController) viewGroup.getChildAt(i);

                            if (v != l) {
                                v.setClick(false);
                            }
                        }

                        UserData.setCurrentLayout(getContext(), layoutClass);
                        clickedCallback.fire(layoutClass);

                    }
                }
            }));



            ((ViewGroup) root.findViewById(R.id.layouts)).addView(l);
            groupID_View.put(layoutClass.groupID, l);

        }

    }

    public void setClick(boolean clicked) {

        isClicked = clicked;

        root.findViewById(R.id.layouts).setVisibility(isClicked ? VISIBLE : GONE);


//        root.findViewById(R.id.parent).setBackground(
//                ContextCompat.getDrawable(context,
//                        isClicked ? R.drawable.stroke_rounded_rect_white : R.drawable.basic_black_border));

        int colorBackground = ContextCompat.getColor(context, !clicked ? R.color.grey : R.color.primary);

        ((TextView) root.findViewById(R.id.titleText)).setTextColor(colorBackground);
        ((TextView) root.findViewById(R.id.descriptionText)).setTextColor(colorBackground);
        root.findViewById(R.id.parent).setBackgroundTintList(ColorStateList.valueOf(colorBackground));

    }

    GenericCallback clickedCallback;

    public void setClickedCallback(GenericCallback clickedCallback) {
        this.clickedCallback = clickedCallback;
    }
}
