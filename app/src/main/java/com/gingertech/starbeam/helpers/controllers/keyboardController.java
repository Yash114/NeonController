package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.helpers.ButtonHandler.GYRO_TOGGLE;
import static com.gingertech.starbeam.helpers.ButtonHandler.KEYBOARD_TOGGLE;
import static com.gingertech.starbeam.helpers.ButtonHandler.MIDDLE_CLICK;
import static com.gingertech.starbeam.helpers.ButtonHandler.RIGHT_CLICK;
import static com.gingertech.starbeam.helpers.controllers.LaunchOverlayController.BUTTONS;
import static com.gingertech.starbeam.helpers.controllers.LaunchOverlayController.VECTOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.limelight.nvstream.input.KeyboardPacket;
import com.gingertech.starbeam.limelight.nvstream.input.MouseButtonPacket;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;


public class keyboardController extends InputMethodService {

    View inflatedView;
    ViewGroup Container;
    Drawable nClicked;

    InputConnection ic;
    Vibrator n;

    LayoutClass currentLayout;



    @SuppressLint("ServiceCast")
    @Override
    public View onCreateInputView() {
        // get the KeyboardView and add our Keyboard layout to it

        n = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        inflatedView = getLayoutInflater().inflate(R.layout.gamepad_layout, null);

        UserData.setup(getApplicationContext());
        currentLayout = UserData.currentLayout;

        makeButtons();

        return inflatedView;
    }

    private final GenericCallbackv2 sendKeyboard = new GenericCallbackv2() {

        final short zero = 0;

        short lxj = 0;
        short lyj = 0;
        short rxj = 0;
        short ryj = 0;

        final byte rt = 0;
        final byte lt = 0;

        final short xbox_flags = 0x00;

        final short keyboard_flags = 0;

        boolean errorMessageShown = false;

        @Override
        public void onChange(Object keyCode, Object down, Object type) {

            //Process the input differently depending on weither its a vector or a button input
            switch ((int) type) {

                //In the case it is buttons
                case (BUTTONS):

                    //Get the code and determine if its mouse, keyboard or controller
                    int[] result = ButtonHandler.getCode((String) keyCode);

                    int keybind = result[0];
                    int buttonType = result[1];

                    //If it is a keyboard simply translate the key and export
                    if(buttonType == ButtonHandler.KEYBOARD || buttonType == ButtonHandler.XINPUT) {

                        if ((boolean) down) {
                            ButtonHandler.press((String) keyCode, ic);
                        } else {
                            ButtonHandler.release((String) keyCode, ic);
                        }

                    }  else if(buttonType == ButtonHandler.MOUSE){

                        if(!errorMessageShown) {
                            Toast.makeText(getApplicationContext(), R.string.Keybind_is_Unavailable, Toast.LENGTH_SHORT).show();
                            errorMessageShown = true;
                            vibrate(500);

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    errorMessageShown = false;
                                }
                            }, 10000);
                        }
                    } else if(buttonType == ButtonHandler.INTERNAL) {

                        if(!errorMessageShown) {
                            Toast.makeText(getApplicationContext(), R.string.Keybind_is_Unavailable, Toast.LENGTH_SHORT).show();
                            errorMessageShown = true;
                            vibrate(500);

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    errorMessageShown = false;
                                }
                            }, 10000);
                        }
                    } else if(buttonType == ButtonHandler.LAYOUT) {

                        if(!(boolean) down) {

                            String newLayoutName = ((String) keyCode).toLowerCase();
                            newLayoutName = newLayoutName.replace("<", "");
                            newLayoutName = newLayoutName.replace(">", "");

                            LayoutClass potentialLayout = new LayoutClass(newLayoutName, getApplicationContext());
                            if(!potentialLayout.exists) {
                                currentLayout = potentialLayout;
                                makeButtons();
                            } else {

                                StringBuilder b = new StringBuilder();
                                b.append(getResources().getString(R.string.Unable_to_find));
                                b.append(newLayoutName);
                                b.append(getResources().getString(R.string.space_layout));

                                Toast.makeText(getApplicationContext(), b.toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    }

                    break;

                //In the case it is a vector
                case (VECTOR):

                    ButtonID.Vector joyVector = (ButtonID.Vector) down;

                    if(keyCode.equals("xlj") || keyCode.equals("xbox_left_joystick") ) {

                        lxj = (short) Math.round(joyVector.x);
                        lyj = (short) Math.round(joyVector.y);

                    } else if(keyCode.equals("xrj") || keyCode.equals("xbox_right_joystick")) {

                        rxj = (short) Math.round(joyVector.x);
                        ryj = (short) Math.round(joyVector.y);
                    }


                    if(!errorMessageShown) {
                        Toast.makeText(getApplicationContext(), R.string.Keybind_is_Unavailable, Toast.LENGTH_SHORT).show();
                        errorMessageShown = true;
                        vibrate(500);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                errorMessageShown = false;
                            }
                        }, 10000);
                    }

                    break;
            }
        }
    };


    public void makeButtons() {

        Container = inflatedView.findViewById(R.id.container);
//        Container.setBackgroundColor(Color.TRANSPARENT);

        Container.removeViews(1, Container.getChildCount() - 1);

        LayoutClass target = currentLayout;
        if(target != null) {

            //Gets the lowest y position
            int height = 500;
            int highest = 0;

            for(ButtonID_data bb : target.buttons) {
                if(bb.y < height) {
                    height = bb.y;
                }
            }

            for(ButtonID_data bb : target.buttons) {

                ButtonID button = new ButtonID(getApplicationContext());

                button.setData(getApplicationContext(), bb);
                button.start(sendKeyboard, getApplicationContext());
                button.setAlpha(button.alpha / 10f);
                Container.addView(button);
                button.setY(button.getY() - height);

                int h = (int) button.getY() + button.getLayoutParams().height;
                if (h > highest) {
                    highest = h;
                }
            }

            inflatedView.findViewById(R.id.backsize).getLayoutParams().height = highest;
        }

        ic = getCurrentInputConnection();

    }

    @Override
    public boolean onEvaluateInputViewShown() {
        inflatedView = getLayoutInflater().inflate(R.layout.gamepad_layout, null);

        makeButtons();
        return super.onEvaluateInputViewShown();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
    
    public void vibrate(int sec){
        n.vibrate(sec);
    }
}


