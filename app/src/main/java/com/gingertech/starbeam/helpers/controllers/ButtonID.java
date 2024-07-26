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
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
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
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ButtonID extends RelativeLayout implements View.OnClickListener {

    enum BUTTON_IMAGE_TYPE {
        DEFAULT,
        APP_PROVIDED,
        USER_PROVIDED
    }

    static int[] imageList = {R.drawable.ic_button, R.drawable.ic_stickybutton, R.drawable.ic_cyclebutton, R.drawable.ic_joybutton, R.drawable.ic_scroll_button };

    final int onColor = Color.DKGRAY;
    int offColor = Color.GRAY;

    public static final boolean DOWN = true;
    public static final boolean UP = false;

    final public static int Normal = 0;
    final public static int Sticky = 1;
    final public static int Cycle = 2;
    final public static int Joy = 3;
    final public static int Scroll = 4;

    final public static int Multi = 5;
    final public static int Gyro = 6;

    MotionEvent downEvent;
    public boolean isTrackpad = false;
    public int trackPadType = MOUSE;

    public final static int MOUSE_WHEEL = 6;
    public final static int MOUSE = 0;
    public final static int WASD = 1;
    public final static int rXBOX = 2;
    public final static int lXBOX = 3;
    public final static int button_XBOX = 4;
    public final static int UDLR = 5;

    public int type = 0;

    public int x = 0;
    public int y = 0;
    public int size = 0;
    public float textSize = 0;
    public int color = -1;

    public float sensitivity = 0.5f;
    public Bitmap buttonImage;
    public String imageID = "";

    public String keybind = "";
    public String buttonName = "";
    public boolean buttonNameVisible = true;
    public boolean joystickTouchActivate = false;
    public boolean verticalScroll = false;

    public ArrayList<String> cycle_binds = new ArrayList<>();
    public String releaseKey = "";

    public ImageView imageView;
    public TextView textView;

    public String nameStorage;
    public boolean isHapticFeebackEnabled = true;

    public int tintMode = 0;

    public int joystickDeadzone = 0;
    public int alpha = 10;

    public boolean isCommand = false;

    Vibrator vibrator = null;

    public ButtonID.BUTTON_IMAGE_TYPE buttonImageType = ButtonID.BUTTON_IMAGE_TYPE.DEFAULT;


    static class Vector {
        double x = 0;
        double y = 0;

        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public void copy(ButtonID b) {

        setPosition(b.x, b.y);
        setSize(b.size);
        setColor(b.color);
        setKeybind(b.keybind);

    }

    public ButtonID(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    public ButtonID(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public ButtonID(Context context) {

        super(context);

        init(context);
    }

    public void setAlpha_(int alpha) {
        this.alpha = alpha;
    }

    public void init(Context c){

        LayoutInflater inflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.button_compound_layout, this, true);

        root.setClickable(false);
        root.setFocusable(false);
        root.setDuplicateParentStateEnabled(true);

        imageView = new ImageView(c);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        root.addView(imageView, 0);

        textView = root.findViewById(R.id.textView);

        root.setOnClickListener(this);

    }

    private boolean delete = false;
    private boolean isExecuting = false;

    public void executing(boolean isExecuting) {

        this.isExecuting = isExecuting;

        if(!this.isExecuting && delete) {
            ((ViewGroup) getParent()).removeView(this);
        }

    }

    public void remove() {
        if(isExecuting) {
            delete = true;
        } else {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    public void setData(Context context, ButtonID_data data) {

        this.textSize = data.textSize;
        this.setType(data.type);
        this.addNewButton(this.type, context);

        this.buttonNameVisible = data.buttonNameVisible;
        this.buttonName = data.buttonName;

        if(data.keybind.contains("<") && data.keybind.contains(">")) {
            this.setKeybind(data.keybind);
        } else {
            this.setKeybind(data.keybind.toLowerCase());
        }

        this.setPosition(data.x, data.y);
        this.setSize(data.size);
        this.alpha = data.alpha;
        this.sensitivity = data.sensitivity;
        this.isHapticFeebackEnabled = data.isHapticFeebackEnabled;
        this.joystickTouchActivate = data.joystickTouchActivate;
        this.verticalScroll = data.verticalScroll;

        this.imageID = data.imageID;

        if (Objects.equals(this.imageID, "")) {
            data.buttonImageType = BUTTON_IMAGE_TYPE.DEFAULT;
        } else {
            data.buttonImageType = BUTTON_IMAGE_TYPE.USER_PROVIDED;
        }

        if(data.buttonImageType == BUTTON_IMAGE_TYPE.USER_PROVIDED) {

            this.setImage(SaveClass.ReadImage(context, this.imageID), this.imageID);

        } else if(data.buttonImageType == BUTTON_IMAGE_TYPE.DEFAULT){

            resetImage(context);

        }

        updateTransformations();
        updateText();

        this.setTintMode(data.tintMode);
        this.setColor(data.color);
    }

    public void setData_dupl(Context context, ButtonID_data data) {

        this.textSize = data.textSize;
        this.setType(data.type);
        this.addNewButton(this.type, context);

        this.buttonNameVisible = data.buttonNameVisible;
        this.buttonName = data.buttonName;

        this.setTintMode(data.tintMode);
        this.setColor(data.color);

        if(data.keybind.contains("<") && data.keybind.contains(">")) {
            this.setKeybind(data.keybind);
        } else {
            this.setKeybind(data.keybind.toLowerCase());
        }

        this.setPosition(data.x, data.y);
        this.setSize(data.size);
        this.alpha = data.alpha;
        this.sensitivity = data.sensitivity;
        this.isHapticFeebackEnabled = data.isHapticFeebackEnabled;
        this.joystickTouchActivate = data.joystickTouchActivate;
        this.verticalScroll = data.verticalScroll;

        if(data.buttonImageType == BUTTON_IMAGE_TYPE.USER_PROVIDED) {

            this.imageID = UUID.randomUUID().toString();

            StringBuilder out = new StringBuilder();
            for(char i : this.imageID.toCharArray()) {

                if(i != ('-')) {
                    out.append(i);
                }
            }

            this.setImage(SaveClass.ReadImage(context, data.imageID), out.toString());


        } else if (data.buttonImageType == BUTTON_IMAGE_TYPE.DEFAULT) {

            this.setImage_withKeybind(ContextCompat.getDrawable(context, imageList[this.type]), context);

        }

        updateTransformations();
        updateText();
    }

    public ButtonID_data getData(Context context) {
        ButtonID_data button = new ButtonID_data();

        button.textSize = this.textView.getTextSize();
        button.tintMode = this.tintMode;
        button.alpha = this.alpha;
        button.sensitivity = this.sensitivity;
        button.isHapticFeebackEnabled = this.isHapticFeebackEnabled;
        button.joystickTouchActivate = this.joystickTouchActivate;
        button.verticalScroll = this.verticalScroll;

        button.buttonNameVisible = this.buttonNameVisible;
        button.buttonName = this.buttonName;

        button.addNewButton(this.type, context);
        button.setPosition(this.x, this.y);
        button.setSize(this.size);
        button.setColor(this.color);

        if(this.keybind.contains("<") && this.keybind.contains(">")) {
            button.setKeybind(this.keybind);
        } else {
            button.setKeybind(this.keybind.toLowerCase());
        }

        button.imageID = this.imageID;
        SaveClass.SaveImage(this.buttonImage, imageID, context);

        return button;
    }

    public boolean withinBounds(float x, float y){

        int[] drawnLocation = new int[2];
        this.getLocationOnScreen(drawnLocation);

        int centerX = drawnLocation[0] + size / 2;
        int centerY = drawnLocation[1] + size / 2;

        float distance = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

        return distance <= (float) size / 2;
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
        resetImage(c);

        if(textSize > 0) {

            float scaledDensity = c.getResources().getDisplayMetrics().scaledDensity;
            textView.setTextSize(textSize / scaledDensity);
        }

        keybind = "";
    }

    public void setTintMode(int tintMode) {

        this.tintMode = tintMode;

        if ( this.tintMode == 0) {
            this.imageView.clearColorFilter();
            this.imageView.setImageTintMode(PorterDuff.Mode.MULTIPLY);
            this.imageView.setImageTintList(ColorStateList.valueOf(color));
        } else {
            this.imageView.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            this.imageView.setColorFilter(color);
        }
    }

    public void setType(int t) {

        this.type = t;

    }

    public void setSize(int size) {
        this.size = size;
        this.setLayoutParams(new RelativeLayout.LayoutParams(size, size));
    }


    public void setColor(int color) {

        this.color = color;
        setTintMode(tintMode);

        this.offColor = this.color;

    }

    public void setPosition(int x, int y) {

        this.x = x;
        this.y = y;

        this.setX(x);
        this.setY(y);
    }

    GenericCallbackv2 sendHandler;

    public void start(GenericCallbackv2 callback,Context context) {

        sendHandler = callback;
        PreferenceConfiguration prefConfig = PreferenceConfiguration.readPreferences(context);

        joystickDeadzone = prefConfig.deadzonePercentage;


        switch (this.type) {

            case 0:
                NormalButtonInitialize();
                break;

            case 1:
                StickyButtonInitialize();
                break;

            case 2:
                CycleButtonInitialize();
                break;

            case 3:
                JoyButtonInitialize();
                break;

            case 4:
                ScrollButtonInitialize();
                break;
        }

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

    }

    public void setKeybind(String s) {

        this.keybind = s;

        this.textView.setText(s.toUpperCase());

        if(!this.buttonName.equals("")) {
            this.textView.setText(this.buttonName.toUpperCase());
        }

        if(!this.imageID.equals("")) {
            this.textView.setText("");
        }
    }

    public void resetImage(Context context) {

        final Drawable d = AppCompatResources.getDrawable(context, imageList[type]);

        imageView.setImageDrawable(d);
        this.imageID = "";

        buttonImageType = BUTTON_IMAGE_TYPE.DEFAULT;

        setColor(ContextCompat.getColor(context, R.color.primary));
        setTintMode(1);
    }

    public void setImage(Bitmap d, String id) {

        if(!id.equals("")) {
            this.imageID = id;
            this.buttonImage = d;
            this.imageView.setImageBitmap(d);
        }
    }

    public void updateText() {
        this.textView.setText(buttonNameVisible ? (buttonName.length() > 0 ? buttonName : keybind) : "");


    }

    public void setImage(Drawable d, Context c) {

        if(d == null) {
            resetImage(c);
            return;
        }

        this.imageView.setImageDrawable(d);
        this.buttonImage = drawableToBitmap(d);
        this.textView.setText("");

        if(this.imageID.equals("")) {
            this.imageID = UUID.randomUUID().toString();

            StringBuilder out = new StringBuilder();
            for(char i : this.imageID.toCharArray()) {

                if(i != ('-')) {
                    out.append(i);
                }
            }

            this.imageID = out.toString();
        }
    }

    public void setImage_withKeybind(Drawable d, Context c) {

        if(d == null) {
            resetImage(c);
            return;
        }

        this.imageView.setImageDrawable(d);
        this.buttonImage = drawableToBitmap(d);
//
//        if(this.imageID.equals("")) {
//            this.imageID = UUID.randomUUID().toString();
//
//            StringBuilder out = new StringBuilder();
//            for(char i : this.imageID.toCharArray()) {
//
//                if(i != ('-')) {
//                    out.append(i);
//                }
//            }
//
//            this.imageID = out.toString();
//        }
    }

    public void updateTransformations() {

        this.setPosition((int) this.getX(), (int) this.getY());
        this.setSize(this.getLayoutParams().width);
    }

    public void NormalButtonInitialize(){

        this.setOnTouchListener(normalListener);

        String x = this.keybind;
        this.cycle_binds.clear();
        int p;
        if (x.contains("&")) {
            while (x.contains("&")) {

                p = x.indexOf("&");
                this.cycle_binds.add(x.subSequence(0, p).toString());
                x = x.subSequence(p + 1, x.length()).toString();
            }
            this.cycle_binds.add(x);
        } else if (x.contains(":")) {
            p = x.indexOf(":");
            this.keybind = x.subSequence(0, p).toString();
            x = x.subSequence(p + 1, x.length()).toString();
            this.releaseKey = x;
        }

    }

    public void StickyButtonInitialize(){

        String x = this.keybind;
        this.cycle_binds.clear();
        int p;
        if (x.contains("&")) {
            while (x.contains("&")) {

                p = x.indexOf("&");
                this.cycle_binds.add(x.subSequence(0, p).toString());
                x = x.subSequence(p + 1, x.length()).toString();
            }
            this.cycle_binds.add(x);
        }

        this.setOnTouchListener(new stickyListener());

    }

    public void CycleButtonInitialize(){

        String x = this.keybind;
        this.cycle_binds.clear();
        int p;
        if (x.contains(":")) {
            while (x.contains(":")) {

                p = x.indexOf(":");
                this.cycle_binds.add(x.subSequence(0, p).toString());
                x = x.subSequence(p + 1, x.length()).toString();
            }

            this.cycle_binds.add(x);

        }

        this.setOnTouchListener(new cycleListener(this));
    }

    public void JoyButtonInitialize(){

        String x = this.keybind;
        this.cycle_binds.clear();
        int p;
        if (x.contains(":")) {
            while (x.contains(":")) {

                p = x.indexOf(":");
                this.cycle_binds.add(x.subSequence(0, p).toString());
                x = x.subSequence(p + 1, x.length()).toString();
            }
            this.cycle_binds.add(x);
        } else if(x.contains("&")) {
            while (x.contains("&")) {
                p = x.indexOf("&");
                this.cycle_binds.add(x.subSequence(0, p).toString());
                x = x.subSequence(p + 1, x.length()).toString();
            }
            this.cycle_binds.add(x);
        }

        this.setOnTouchListener(new joyListener(this));
    }

    public void ScrollButtonInitialize(){

        String x = this.keybind;
        this.cycle_binds.clear();
        int p;
        if (x.contains(":")) {
            while (x.contains(":")) {
                p = x.indexOf(":");
                this.cycle_binds.add(x.subSequence(0, p).toString());
                x = x.subSequence(p + 1, x.length()).toString();
            }
            this.cycle_binds.add(x);
        }

        this.setOnTouchListener(new scrollListener(this));

    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    OnTouchListener normalListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            ButtonID b = (ButtonID) view;

            if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {

                b.imageView.setColorFilter(onColor);

                if (b.cycle_binds.size() > 1) {

                    for (String s : b.cycle_binds) {
                        sendKeyboard(s, DOWN);
                    }

                } else {

                    sendKeyboard(b.keybind, DOWN);

                }

                vibrate();
            }

            if(motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if(isTrackpad) {
                    passToMouseMovement(motionEvent);
                }
            }

            if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {

                endMouseMovement(motionEvent);

                 resetColorFilter(b);

                if (b.cycle_binds.size() > 1) {

                    for (String s : b.cycle_binds) {

                        sendKeyboard(s, UP);

                    }
                } else {

                    if (!b.releaseKey.equals("")) {

                        sendKeyboard(b.keybind, UP);
                        sendKeyboard(b.releaseKey, DOWN);

                        try {
                            Thread.sleep(250);
                            sendKeyboard(b.releaseKey, UP);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {

                        sendKeyboard(b.keybind, UP);
                    }
                }
            }

            return true;
        }
    };

    @Override
    public void onClick(View view) {

    }

    class stickyListener implements OnTouchListener {
        boolean click = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            ButtonID b = (ButtonID) view;

            if(motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if(isTrackpad) {
                    passToMouseMovement(motionEvent);
                }
            }

            if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {

                downEvent = motionEvent;

                endMouseMovement(motionEvent);

                vibrate();

                click = !click;

                if (click) {
                    b.imageView.setColorFilter(onColor);

                    if (b.cycle_binds.size() > 1) {
                        for (String s : b.cycle_binds) {
                            sendKeyboard(s, DOWN);

                        }
                    } else {
                        sendKeyboard(b.keybind, DOWN);
                    }

                } else {


                    resetColorFilter(b);

                    if (b.cycle_binds.size() > 1) {
                        for (String s : b.cycle_binds) {
                            sendKeyboard(s, UP);


                        }
                    } else {
                        sendKeyboard(b.keybind, UP);
                    }
                }
            }

            return true;
        }
    }

    class cycleListener implements OnTouchListener {

        int count = 0;
        int keybindSize = 0;

        public cycleListener(ButtonID b) {
            keybindSize = b.cycle_binds.size();
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            ButtonID b = (ButtonID) view;
            if (b.cycle_binds.size() != 0) {
                if (b.cycle_binds.get(count) != null) {
                    String pressed = b.cycle_binds.get(count);

                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS || motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        downEvent = motionEvent;

                        sendKeyboard(pressed, DOWN);
                        vibrate();
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                        if(isTrackpad) {
                            passToMouseMovement(motionEvent);
                        }
                    }

                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {

                         resetColorFilter(b);

                        b.textView.setText(b.cycle_binds.get((count + 1) % keybindSize).toUpperCase());
                        sendKeyboard(pressed, UP);

                        count += 1;
                        count = count % keybindSize;
                    }
                }
            }

            return false;
        }
    }

    class joyListener implements OnTouchListener {
        int q;

        float xpos;
        float ypos;

        Vibrator n;

        String Currently_Pressed = "";
        ArrayList<String>  PressedGroup = new ArrayList<>();
        ArrayList<String>  Previous_PressedGroup = new ArrayList<>();
        ArrayList<String> Starting_PressedGroup = new ArrayList<>();


        String Pressed = "";

        float xx = 0;
        float yy = 0;

        float distance = 0;
        float angle = 0;

        float final_angle = 0;
        int joystickSize = 300;

        boolean clicked = false;
        float sensitivity;
        float maxDisMouse;
        float maxDisXbox;


        joyListener(ButtonID a) {
            q = a.cycle_binds.size();
            sensitivity = a.sensitivity;

            maxDisMouse = 50 + (1 - sensitivity) * 1000;
            maxDisXbox = 50 + (1 - sensitivity) * 250;

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            ButtonID b = (ButtonID) v;

            if (event.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                downEvent = event;

                Currently_Pressed = "";
                Pressed = "";
                clicked = true;

                if(b.joystickTouchActivate && cycle_binds.size() >= 4) {
                    xpos = event.getX();
                    ypos = event.getY();

                    double centerY = b.size / 2f;
                    double centerX = b.size / 2f;

                    final double xDiff = centerX - xpos;
                    final double yDiff = centerY - ypos;

                    distance = (float) Math.sqrt(pow(xDiff, 2) + pow(yDiff, 2));

                    if(distance > 25) {
                        double final_angle = Math.acos(xDiff / distance);

                        if(yDiff < 0) {
                            final_angle = 2 * Math.PI - final_angle;
                        }

                        if (final_angle < 4 * Math.PI / 3 && final_angle > 2 * Math.PI / 3) {
                            Starting_PressedGroup.add(b.cycle_binds.get(3));

                        }
                        if (final_angle < 5 * Math.PI / 6 && final_angle > Math.PI / 6) {
                            Starting_PressedGroup.add(b.cycle_binds.get(0));

                        }
                        if (final_angle < Math.PI / 3 || final_angle > 5 * Math.PI / 3) {
                            Starting_PressedGroup.add(b.cycle_binds.get(1));

                        }
                        if (final_angle < 11 * Math.PI / 6 && final_angle > 7 * Math.PI / 6) {

                            Starting_PressedGroup.add(b.cycle_binds.get(2));
                        }

                        if (!Starting_PressedGroup.isEmpty()) {
                            for (String s : Starting_PressedGroup) {
                                sendKeyboard(s, DOWN);
                            }
                        }

                        b.imageView.setColorFilter(onColor);

                        if (Starting_PressedGroup.size() == 2) {
                            Pressed = Starting_PressedGroup.get(0) + "&" + Starting_PressedGroup.get(1);
                        } else {
                            Pressed = Starting_PressedGroup.get(0);
                        }

                        b.textView.setText(Pressed.toUpperCase(Locale.ROOT));

                    }
                }

                if(q == 2) {
                    sendKeyboard(b.cycle_binds.get(1), DOWN);
                }

                vibrate();

                ypos = xpos = b.size / 2f;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                if(isTrackpad) {
                    passToMouseMovement(event);
                } else {

                    xx = event.getX();
                    yy = event.getY();

                    if (b.cycle_binds.size() == 2) {
                        if (b.cycle_binds.get(0).equalsIgnoreCase("mouse")) {

                            distance = (float) (Math.sqrt(pow(xpos - xx, 2) + pow(ypos - yy, 2)));
                            angle = (float) (Math.atan((ypos - yy) / (xpos - xx)));

                            final_angle = angle;
                            if ((xpos - xx) < 0) {
                                final_angle = (float) Math.PI + angle;
                            }

                            if (distance > 50) {

                                distance -= 50;

                                Vector vec = new Vector(
                                        -Math.min(distance, maxDisMouse) / maxDisMouse * 50 * Math.cos(final_angle),
                                        -Math.min(distance, maxDisMouse) / maxDisMouse * 50 * Math.sin(final_angle));

                                sendVector(b.cycle_binds.get(0), vec);
                            }
                        }

                    } else if (b.keybind.equalsIgnoreCase("mouse")) {

                        distance = (float) (Math.sqrt(pow(xpos - xx, 2) + pow(ypos - yy, 2)));
                        angle = (float) (Math.atan((ypos - yy) / (xpos - xx)));

                        final_angle = angle;
                        if ((xpos - xx) < 0) {
                            final_angle = (float) Math.PI + angle;
                        }

                        if (distance > 50) {

                            distance -= 50;

                            Vector vec = new Vector(
                                    -Math.min(distance, maxDisMouse) / maxDisMouse * 50 * Math.cos(final_angle),
                                    -Math.min(distance, maxDisMouse) / maxDisMouse * 50 * Math.sin(final_angle));

                            sendVector(b.keybind.toLowerCase(), vec);
                        }

                    } else if (b.keybind.equalsIgnoreCase("xrj") || b.keybind.equalsIgnoreCase("xbox_right_joystick") ||
                            b.keybind.equalsIgnoreCase("xlj") || b.keybind.equalsIgnoreCase("xbox_left_joystick")) {

                        distance = (float) (Math.sqrt(pow(xpos - xx, 2) + pow(ypos - yy, 2)));
                        angle = (float) (Math.atan((ypos - yy) / (xpos - xx)));

                        final_angle = angle;
                        if ((xpos - xx) < 0) {
                            final_angle = (float) Math.PI + angle;
                        }

                        if (distance > 50) {

                            distance -= 50;

                            Vector vec = new Vector(
                                    -Math.min(distance, maxDisXbox) / maxDisXbox * 32766 * Math.cos(final_angle),
                                    Math.min(distance, maxDisXbox) / maxDisXbox * 32766 * Math.sin(final_angle));

                            sendVector(b.keybind.toLowerCase(), vec);
                        } else {

                            sendVector(b.keybind.toLowerCase(), new Vector(0, 0));

                            resetColorFilter(b);

                        }

                    } else {

                        final double xDiff = xpos - xx;
                        final double yDiff = ypos - yy;

                        distance = (float) Math.sqrt(pow(xDiff, 2) + pow(yDiff, 2));
                        double final_angle = Math.acos(xDiff / distance);

                        if(yDiff < 0) {
                            final_angle = 2 * Math.PI - final_angle;
                        }

                        if (distance >= 100 && (q == 4 || q == 5)) {

                            if(!Starting_PressedGroup.isEmpty()) {
                                for(String s : Starting_PressedGroup) {
                                    sendKeyboard(s, UP);
                                }

                                Starting_PressedGroup.clear();
                            }

                            PressedGroup.clear();

                            if (final_angle < 4 * Math.PI / 3 && final_angle > 2 * Math.PI / 3) {
                                PressedGroup.add(b.cycle_binds.get(3));

                            }
                            if (final_angle < 5 * Math.PI / 6 && final_angle > Math.PI / 6) {
                                PressedGroup.add(b.cycle_binds.get(0));

                            }
                            if (final_angle < Math.PI / 3 || final_angle > 5 * Math.PI / 3) {
                                PressedGroup.add(b.cycle_binds.get(1));

                            }
                            if (final_angle < 11 * Math.PI / 6 && final_angle > 7 * Math.PI / 6) {

                                PressedGroup.add(b.cycle_binds.get(2));
                            }

                            if (PressedGroup.size() == 0) {
                                return true;
                            }

                            b.imageView.setColorFilter(onColor);


                            if (PressedGroup.size() == 2) {
                                Pressed = PressedGroup.get(0) + "&" + PressedGroup.get(1);
                            } else {
                                Pressed = PressedGroup.get(0);
                            }

                            b.textView.setText(Pressed.toUpperCase(Locale.ROOT));

                            boolean hasChanged = Previous_PressedGroup.size() != PressedGroup.size();
                            for (int index = 0; index < Previous_PressedGroup.size(); index++) {

                                if (!PressedGroup.contains(Previous_PressedGroup.get(index))) {
                                    hasChanged = true;
                                    sendKeyboard(Previous_PressedGroup.get(index), UP);
                                }
                            }

                            if(hasChanged) {
                                for (String s : PressedGroup) {
                                    sendKeyboard(s, DOWN);
                                }

                                Previous_PressedGroup.clear();
                                Previous_PressedGroup.addAll(PressedGroup);
                                vibrate();

                            }

                        } else {
                            resetColorFilter(b);
                        }
                    }
                }

                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {

                endMouseMovement(event);

                 resetColorFilter(b);

                if(!Starting_PressedGroup.isEmpty()) {
                    for(String s : Starting_PressedGroup) {
                        sendKeyboard(s, UP);
                    }

                    Starting_PressedGroup.clear();
                }

                if(q == 2) {
                    sendKeyboard(b.cycle_binds.get(1), UP);
                    vibrate();
                }

                if (b.keybind.equalsIgnoreCase("xlj") || b.keybind.equalsIgnoreCase("xbox_left_joystick")) {
                    sendVector(b.keybind.toLowerCase(), new Vector(0, 0));

                }

                if (b.keybind.equalsIgnoreCase("xrj") || b.keybind.equalsIgnoreCase("xbox_right_joystick")) {
                    sendVector(b.keybind.toLowerCase(), new Vector(0, 0));
                }

                if (b.imageID.equals("")) {
                    ((ButtonID) v).textView.setText(b.keybind.toUpperCase());
                } else {
                    ((ButtonID) v).textView.setText("");
                }

                clicked = false;
                    for (String s : Previous_PressedGroup) {

                        if (!s.equals("")) {
                            sendKeyboard(s, UP);

                        }
                    }

                    Previous_PressedGroup.clear();
                    PressedGroup.clear();
            }

            return false;

        }
    }

    class scrollListener implements OnTouchListener {

        ButtonID a;
        int q;

        float xpos, ypos;

        String Currently_Pressed = "";
        int current_index = 0;
        int theIndex = 0;

        double separateDistance = 150;

        boolean changed = false;

        scrollListener(ButtonID a) {

            q = a.cycle_binds.size();

            if (q != 0) {
                Currently_Pressed = a.cycle_binds.get(0);
            }

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (q != 0) {

                ButtonID b = (ButtonID) v;

                if (event.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    downEvent = event;

                    b.imageView.setColorFilter(onColor);

                    if (Currently_Pressed != null || !Currently_Pressed.equals("") && !changed) {
                        sendKeyboard(Currently_Pressed, DOWN);
                        vibrate();
                    }

                    xpos = event.getX();
                    ypos = event.getY();

                }

                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                    if(isTrackpad) {
                        passToMouseMovement(event);
                    } else {

                        int distance;

                        if(verticalScroll) {

                            if (Math.abs(ypos - event.getY()) >= 50) {
                                distance = (int) Math.floor((double) Math.abs(ypos - event.getY()) / separateDistance);
                                sendKeyboard(Currently_Pressed, UP);

                                int index = (ypos - event.getY()) < 0 ? (current_index + distance) % b.cycle_binds.size() : (10 * b.cycle_binds.size() - distance + current_index) % b.cycle_binds.size();
                                if (!Currently_Pressed.equals(b.cycle_binds.get(index))) {

                                    theIndex = index;
                                    Currently_Pressed = b.cycle_binds.get(index);
                                    ((ButtonID) v).textView.setText(Currently_Pressed.toUpperCase());
                                    changed = true;

                                    vibrate();
                                }
                            }

                        } else {

                            if (Math.abs(xpos - event.getX()) >= 50) {
                                distance = (int) Math.floor((double) Math.abs(xpos - event.getX()) / separateDistance);
                                sendKeyboard(Currently_Pressed, UP);

                                int index = (xpos - event.getX()) < 0 ? (current_index + distance) % b.cycle_binds.size() : (10 * b.cycle_binds.size() - distance + current_index) % b.cycle_binds.size();
                                if (!Currently_Pressed.equals(b.cycle_binds.get(index))) {

                                    theIndex = index;
                                    Currently_Pressed = b.cycle_binds.get(index);
                                    ((ButtonID) v).textView.setText(Currently_Pressed.toUpperCase());
                                    changed = true;

                                    vibrate();
                                }
                            }
                        }
                    }
                }

                if (event.getActionMasked() == MotionEvent.ACTION_UP) {

                    endMouseMovement(event);

                    ((ButtonID) v).textView.setText(Currently_Pressed.toUpperCase());
                     resetColorFilter(b);

                    current_index = theIndex;

                    if (changed) {
                        sendKeyboard(Currently_Pressed, DOWN);
                    }

                    sendKeyboard(Currently_Pressed, UP);

                    changed = false;

                }
            }

            return true;
        }
    }

    void sendKeyboard(String code, Boolean down) {
        if(sendHandler != null) {
            sendHandler.onChange(code, down, LaunchOverlayController.BUTTONS);
        }
    }

    void sendMouse(String code, Boolean down) {

        if(sendHandler != null) {
            sendHandler.onChange(code, down, LaunchOverlayController.MOUSE);
        }
    }

    void sendVector(String code, Vector dir) {

        if(sendHandler != null) {
            sendHandler.onChange(code, dir, LaunchOverlayController.VECTOR);
        }
    }

    boolean canVibrate = true;
    public void setVibration(boolean vibrate) {
        canVibrate = vibrate;
    }

    void vibrate() {
        if(canVibrate && isHapticFeebackEnabled) {
            vibrator.vibrate(5);
        }
    }
    
    void resetColorFilter(ButtonID b) {
        
        if(this.tintMode == 0) {
            b.imageView.clearColorFilter();
        } else {
            b.imageView.setColorFilter(offColor);
        }
    }


    boolean firstPass = true;
    void passToMouseMovement(MotionEvent motionEvent) {

        LaunchOverlayController passUP = ((LaunchOverlayController) this.getParent());

        if(firstPass) {
            firstPass = false;

            MotionEvent gg = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, motionEvent.getX(), motionEvent.getY(), 0);
            passUP.catchMotionEvents(this, gg);

            gg = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, motionEvent.getX() + 10, motionEvent.getY(), 0);
            passUP.catchMotionEvents(this, gg);

            gg = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, motionEvent.getX() - 10, motionEvent.getY(), 0);
            passUP.catchMotionEvents(this, gg);

        } else {

            passUP.catchMotionEvents(this, motionEvent);
        }
    }

    void endMouseMovement(MotionEvent motionEvent) {

        if(isTrackpad) {
            UserData.currentLayout.touchSensitivity = UserData.currentLayout.touchSensitivityRef;

            LaunchOverlayController passUP = ((LaunchOverlayController) this.getParent());

            firstPass = true;
            isTrackpad = false;

            MotionEvent gg = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, motionEvent.getX(), motionEvent.getY(), 0);
            passUP.catchMotionEvents(this, gg);
        }
    }
}