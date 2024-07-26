package com.gingertech.starbeam.helpers.controllers;

import static java.lang.Math.pow;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.SaveClass;

import java.util.ArrayList;
import java.util.UUID;


public class ButtonID_data {

    int offColor = Color.GRAY;

    public static final boolean UP = false;

    final public static int Normal = 0;

    public int type = 0;

    public int x = 0;
    public int y = 0;
    public int size = 0;
    public float textSize = 0;
    public int color = -1;
    public ButtonID.BUTTON_IMAGE_TYPE buttonImageType = ButtonID.BUTTON_IMAGE_TYPE.DEFAULT;


    public String imageID = "";

    public String keybind = "";

    public int alpha = 255;
    public float sensitivity = 0.5f;
    public boolean buttonNameVisible = true;
    public boolean isHapticFeebackEnabled = true;
    public boolean joystickTouchActivate = false;
    public boolean verticalScroll = false;

    public String buttonName = "";


    public int tintMode = 0;

    public static int ButtonID_BUTTON_IMAGE_TYPE_CONVERTER(ButtonID.BUTTON_IMAGE_TYPE buttonImageType) {

        if(buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.DEFAULT) {
            return 0;
        }

        if(buttonImageType == ButtonID.BUTTON_IMAGE_TYPE.USER_PROVIDED) {
            return 1;
        }

        return 2;
    }

    public static ButtonID.BUTTON_IMAGE_TYPE ButtonID_BUTTON_IMAGE_TYPE_CONVERTER_inv(int buttonImageType) {

        if(buttonImageType == 0) {
            return ButtonID.BUTTON_IMAGE_TYPE.DEFAULT;
        }

        if(buttonImageType == 1) {
            return ButtonID.BUTTON_IMAGE_TYPE.APP_PROVIDED;
        }

        return ButtonID.BUTTON_IMAGE_TYPE.USER_PROVIDED;

    }

    public void copy(ButtonID_data b) {

        setPosition(b.x, b.y);
        setSize(b.size);
        setColor(b.color);
        setKeybind(b.keybind);
//        this.tintMode = b.tintMode;

    }


    public void updateValues() {
        setPosition(x, y);
        setSize(size);
        setColor(color);
    }

    public void addNewButton(int type, Context c) {

        this.type = type;

        color = ContextCompat.getColor(c, R.color.primary);
        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, c.getResources().getDisplayMetrics());
        x = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, c.getResources().getDisplayMetrics());
        y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, c.getResources().getDisplayMetrics());

        setPosition(x, y);
        setSize(size);
        setColor(color);
        setTintMode(tintMode);

        keybind = "";
    }

    public void setTintMode(int tintMode) {

        this.tintMode = tintMode;

    }

    public void setType(int t) {

        this.type = t;

    }

    public void setSize(int size) {
        this.size = size;
    }


    public void setColor(int color) {

        this.color = color;
        setTintMode(tintMode);

        this.offColor = this.color;

    }

    public void setPosition(int x, int y) {

        this.x = x;
        this.y = y;

    }

    public void setKeybind(String s) {
        this.keybind = s;
    }


    public void setImage(String id) {

        if(!id.equals("")) {
            this.imageID = id;
        }
    }
}