package com.gingertech.starbeam.helpers.controllers;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.LayoutClass;

import java.util.Locale;
import java.util.UUID;

public class LayoutPreviewTabController_forgroup extends ConstraintLayout {
    Context context;

    public LayoutClass layout;

    String ID = "";

    boolean editable = false;


    public LayoutPreviewTabController_forgroup(@NonNull Context context) {
        super(context);
        init(context, false);
    }
    public LayoutPreviewTabController_forgroup(@NonNull Context context, boolean locked) {
        super(context);
        init(context, locked);
    }

    public LayoutPreviewTabController_forgroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LayoutPreviewTabController, 0, 0);
        init(context, false);
    }

    public LayoutPreviewTabController_forgroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LayoutPreviewTabController, 0, 0);
        init(context, false);
    }

    public LayoutPreviewTabController_forgroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LayoutPreviewTabController, 0, 0);
        init(context, false);

    }

    ViewGroup root;


    void setEditable(boolean editable) {
        this.editable = editable;
    }
    void initNew(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        root = (ViewGroup) inflater.inflate(R.layout.layout_preview_view_forgroup, this, true);

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickedCallback != null) {
                    clickedCallback.fire(ListViewController.EDIT);
                }
            }
        });
    }
    void init(Context context, Boolean locked) {

        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.layout_preview_view_forgroup, this, true);

        this.ID = UUID.randomUUID().toString();

//            root.findViewById(R.id.parent).setOnTouchListener(new OnTouchListener() {
//                final Handler handler = new Handler();
//
//                int x = 0;
//                int y = 0;
//
//                boolean tapOverride = false;
//
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                        handler.postDelayed(mLongPressed, 500);
//
//                        x = (int) motionEvent.getX();
//                        y = (int) motionEvent.getY();
//
//                    }
//
//                    if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//                        float X = motionEvent.getX() - x;
//                        float Y = motionEvent.getY() - y;
//                        double distance = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2));
//
//                        if(distance > 50) {
//                            handler.removeCallbacks(mLongPressed);
//                            tapOverride = true;
//                        }
//
//                    }
//
//                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                        handler.removeCallbacks(mLongPressed);
//
//                        if(!tapOverride) {
//                            if(!isClicked) {
//
//                                if (clickedCallback != null) {
//                                    clickedCallback.fire(layout);
//                                }
//                            } else {
//                                if(clickedCallback != null) {
//                                    clickedCallback.fire(ListViewController.LAUNCH);
//                                }
//                            }
//                        }
//
//                        tapOverride = false;
//                    }
//
//                    if(motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
//                        handler.removeCallbacks(mLongPressed);
//                        tapOverride = false;
//                    }
//
//
//                        return true;
//                }
//
//                final Runnable mLongPressed = new Runnable() {
//                    public void run() {
//
//                        if(getContext() != null) {
//                            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
//                            v.vibrate(20);
//                        }
//
//                        if(clickedCallback != null) {
//                            clickedCallback.fire(ListViewController.DUPLICATE);
//                        }
//
//                        tapOverride = true;
//                    }
//                };
//            });

            root.findViewById(R.id.removeGroupButton).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(clickedCallback != null) {
                        Log.e("group", "here clicked");

                        clickedCallback.fire(-1);
                    }
                }
            });
    }
    ObjectAnimator a3;

    void setup(LayoutClass layout) {

        this.layout = layout;
        ((TextView) root.findViewById(R.id.titleText)).setText(layout.name);
        ((TextView) root.findViewById(R.id.descriptionText)).setText(layout.description);

        root.findViewById(R.id.downloaded).setVisibility(layout.isImported ? VISIBLE : GONE);

        a3 = ObjectAnimator.ofFloat(root.findViewById(R.id.launch_button), "translationY", 15);
        a3.setDuration(250);
        a3.setRepeatCount(ValueAnimator.INFINITE);
        a3.setRepeatMode(ValueAnimator.REVERSE);
    }

    boolean isClicked = false;
    public void setClick(boolean clicked) {
        if(clicked == isClicked) {
            return;
        }

        isClicked = clicked;

        root.findViewById(R.id.parent).setBackground(
                ContextCompat.getDrawable(this.context,
                        isClicked ? R.drawable.stroke_rounded_rect_white : R.drawable.basic_black_border));

        int color = ContextCompat.getColor(context, clicked ? R.color.black : R.color.primary);
        ((TextView) root.findViewById(R.id.titleText)).setTextColor(color);
        ((TextView) root.findViewById(R.id.descriptionText)).setTextColor(color);

    }

    public void showAddToGroup(boolean addToGroup) {
        View v = root.findViewById(R.id.addToGroup);
        if(v != null) {

               v.setVisibility(addToGroup ? VISIBLE : GONE);
        }
    }

    public void toggleClick(){
        setClick(!this.isClicked);
    }

    GenericCallback clickedCallback;

    public void setClickedCallback(GenericCallback clickedCallback) {
        this.clickedCallback = clickedCallback;
    }

}
