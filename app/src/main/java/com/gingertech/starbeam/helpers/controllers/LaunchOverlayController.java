package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.helpers.ButtonHandler.GYRO_TOGGLE;
import static com.gingertech.starbeam.helpers.ButtonHandler.KEYBOARD_TOGGLE;
import static com.gingertech.starbeam.helpers.ButtonHandler.MIDDLE_CLICK;
import static com.gingertech.starbeam.helpers.ButtonHandler.NORMAL;
import static com.gingertech.starbeam.helpers.ButtonHandler.RIGHT_CLICK;
import static com.gingertech.starbeam.helpers.ButtonHandler.TRACKPAD_ENABLE;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.MouseButton.LEFT;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.MouseButton.MIDDLE;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.MouseButton.RIGHT;
import static com.gingertech.starbeam.helpers.controllers.ButtonID.DOWN;
import static com.gingertech.starbeam.helpers.controllers.ButtonID.UP;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.Command;
import com.gingertech.starbeam.helpers.LayoutClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.limelight.binding.input.KeyboardTranslator;
import com.gingertech.starbeam.limelight.nvstream.NvConnection;
import com.gingertech.starbeam.limelight.nvstream.input.KeyboardPacket;
import com.gingertech.starbeam.limelight.nvstream.input.MouseButtonPacket;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LaunchOverlayController extends ConstraintLayout implements View.OnKeyListener  {

    public LaunchOverlayController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LaunchOverlayController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LaunchOverlayController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LaunchOverlayController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {

        if(connectionType == ConnectionType.BLUETOOTH) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    bluetoothController.SendKeyboard(keyEvent.toString(), true);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    bluetoothController.SendKeyboard(keyEvent.toString(), false);

                }
            });
        }
        return false;
    }


    enum ConnectionType {
        BLUETOOTH,
        WIFI,
    }

    KeyboardTranslator translator;
    ViewGroup root;

    GyroController gyroController;

    LayoutClass currentLayout;

    short xbox_flags = 0x00;
    short lxj = 0;
    short lyj = 0;
    short rxj = 0;
    short ryj = 0;

    byte rt = 0;
    byte lt = 0;

    float sensitivity;

    View backTouch;

    public mouseScrollMovement MouseScrollMovement;
    public mouseMovement MouseListener;
    public buttonMovement WASDListener, UDLRListener, XUDLRListener;
    public xboxMovement RightXboxListener, LeftXboxListener;
    public sendKeyboard sendKeyboard = new sendKeyboard();

    public PreferenceConfiguration prefConfig;

    public ConnectionType connectionType;
    private BluetoothController bluetoothController;

    private GenericCallbackv2 callbackv2;

    private MixpanelAPI mp;
    public void setCallback(GenericCallbackv2 genericCallbackv2) {
        callbackv2 = genericCallbackv2;
    }

    public void setMixPanel(MixpanelAPI mixPanel) {
        this.mp = mixPanel;
    }
    public void setupBluetooth(BluetoothController bluetoothController) {
        this.connectionType = ConnectionType.BLUETOOTH;
        this.bluetoothController = bluetoothController;

    }
    public void setupWifi(KeyboardTranslator translator, NvConnection connection){
        this.connectionType = ConnectionType.WIFI;

        this.translator = translator;
        MainActivity.connection = connection;
    }

    sendKeyboard forXbox = new sendKeyboard();

    public void init(Context context) {

        setOnKeyListener(this);

        UserData.currentLayout.get(context);
        currentLayout = UserData.currentLayout;

        MainActivity.sendKeyboard = forXbox;

        prefConfig = PreferenceConfiguration.readPreferences(context);

        if(PremiumController.hasPermission(PremiumController.Product.MotionControls)) {

            gyroController = new GyroController(new GenericCallbackv2() {
                @Override
                public void onChange(Object value, Object value2) {

                    float xmov = (float) value * (currentLayout.invertGyro ? -1 : 1);
                    float ymov = (float) value2 * (currentLayout.invertGyro ? -1 : 1);

                    float mag = (float) Math.sqrt(Math.pow(-xmov * sensitivity, 2) + Math.pow(-ymov * sensitivity, 2));

                    if(currentLayout.gyroThreshold * (currentLayout.gyroSensitivity * 180 + 0.5) > mag) {
                        return;
                    }

                    if((GyroController.toggleEnabled ^ currentLayout.gyroActivated) || GyroController.gyroActivated) {

                        if(currentLayout.isGyroMouse) {

                                MainActivity.connection.sendMouseMove((short) (-xmov * sensitivity),
                                        (short) (ymov * sensitivity));

                        } else {

                            rxj += (short) ((Math.round( xmov * sensitivity)) * -15);
                            ryj += (short) ((Math.round( ymov * sensitivity)) * -15);

                            if(rxj > 30000) {
                                rxj = (short) 30000;
                            } else if(rxj < -30000) {
                                rxj = (short) -30000;
                            }

                            if(ryj > 30000) {
                                ryj = (short) 30000;
                            } else if(ryj < -30000) {
                                ryj = (short) -30000;
                            }

                            MainActivity.connection.sendControllerInput((byte) 0, (byte) 1, xbox_flags, lt, rt, lxj, lyj, rxj, ryj);
                        }

                    }

                }
            }, context);

            gyroController.startSensor();
        }

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.launch_overlay_layout, this, true);

        MouseScrollMovement = new mouseScrollMovement();

        MouseListener = new mouseMovement();
        WASDListener = new buttonMovement();

        UDLRListener = new buttonMovement(new String[] {"up", "left", "down", "right"});
        XUDLRListener = new buttonMovement(new String[] {"xbox_up","xbox_left", "xbox_down", "xbox_right"});

        RightXboxListener = new xboxMovement(true);
        LeftXboxListener = new xboxMovement(false);
    }

    boolean locked = false;
    boolean updateButtons = false;

    public void makeButtons() {

        Log.e("gg", currentLayout.name);
        if(locked) {
            updateButtons = true;
            return;
        }

        locked = true;

        final LayoutClass target = currentLayout;
        currentLayout.touchSensitivityRef = currentLayout.touchSensitivity;
        sensitivity = (currentLayout.gyroSensitivity * 180) + 0.5f;

        if(target != null) {

            TextView rightSide = root.findViewById(R.id.rightSide);
            TextView leftSide = root.findViewById(R.id.leftSide);

            int numOfViews = target.buttons.size();
            final int[] indexViews = {0};
            final int viewsPerBatch = 1;

            Runnable removeViewRunnable = () -> {

                for(int i = ((ViewGroup) root).getChildCount(); i > 0; i--) {
                    if( root.getChildAt(i) instanceof ButtonID) {
                        ((ButtonID) root.getChildAt(i)).remove();
                    }
                }

                Log.e("buttons", currentLayout.rightTrackpadKeybind.toLowerCase());

                rightSide.setOnTouchListener(new emptyTouchListener());
                leftSide.setOnTouchListener(new emptyTouchListener());

                switch(currentLayout.rightTrackpadKeybind.toLowerCase()) {

                    case ("up:left:down:right"):
                        rightSide.setOnTouchListener(new buttonMovement(new String[] {"up", "left", "down", "right"}));
                        break;

                    case ("w:a:s:d"):
                        rightSide.setOnTouchListener(new buttonMovement(new String[] {"w", "a", "s", "d"}));
                        break;

                    case ("mouse"):
                        rightSide.setOnTouchListener(new mouseMovement());
                        break;

                    case ("xbox_right_joystick"):
                    case ("xrj"):
                        rightSide.setOnTouchListener(new xboxMovement(true));
                        break;

                    case ("xbox_left_joystick"):
                    case ("xlj"):
                        rightSide.setOnTouchListener(new xboxMovement(false));
                        break;

                    case ("xbox_up:xbox_left:xbox_down:xbox_right"):
                        rightSide.setOnTouchListener(new buttonMovement(new String[] {"xbox_up","xbox_left", "xbox_down", "xbox_right"}));
                        break;

//                    default:
//                        rightSide.setOnTouchListener(new OnTouchListener() {
//                            @Override
//                            public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
//                                    sendKeyboard.onChange(currentLayout.rightTrackpadKeybind.toLowerCase(), DOWN, BUTTONS);
//                                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_BUTTON_RELEASE || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
//                                    sendKeyboard.onChange(currentLayout.rightTrackpadKeybind.toLowerCase(), UP, BUTTONS);
//                                }
//
//                                return true;
//                            }
//                        });

                }

                switch(currentLayout.leftTrackpadKeybind.toLowerCase()) {

                    case ("w:a:s:d"):
                        leftSide.setOnTouchListener(new buttonMovement(new String[] {"w", "a", "s", "d"}));
                        break;

                    case ("up:left:down:right"):
                        leftSide.setOnTouchListener(new buttonMovement(new String[] {"up", "left", "down", "right"}));
                        break;

                    case ("mouse"):
                        leftSide.setOnTouchListener(new mouseMovement());
                        break;

                    case ("xbox_right_joystick"):
                    case ("xrj"):
                        leftSide.setOnTouchListener(new xboxMovement(true));
                        break;

                    case ("xbox_left_joystick"):
                    case ("xlj"):
                        leftSide.setOnTouchListener(new xboxMovement(false));
                        break;

                    case ("xbox_up:xbox_left:xbox_down:xbox_right"):
                        leftSide.setOnTouchListener(new buttonMovement(new String[] {"xbox_up","xbox_left", "xbox_down", "xbox_right"}));
                        break;

//                    default:
//                        if(ButtonHandler.validate(currentLayout.leftTrackpadKeybind.toLowerCase(), NORMAL)) {
//                            leftSide.setOnTouchListener(new OnTouchListener() {
//                                @Override
//                                public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
//                                        sendKeyboard.onChange(currentLayout.leftTrackpadKeybind.toLowerCase(), DOWN, BUTTONS);
//                                    } else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_BUTTON_RELEASE || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
//                                        sendKeyboard.onChange(currentLayout.leftTrackpadKeybind.toLowerCase(), UP, BUTTONS);
//                                    }
//
//                                    return true;
//                                }
//                            });
//                        }
                }

            };
            Runnable updateViewRunnable = new Runnable() {
                public void run() {

                    if(target.buttons.size() > 0) {

                        for(int i = 0; i < viewsPerBatch ; i++) {
                            if(i + indexViews[0] == numOfViews) {
                                locked = false;

                                if(updateButtons){
                                    updateButtons = false;
                                    makeButtons();
                                }

                                return;
                            }

                            ButtonID button = new ButtonID(getContext());
                            button.setData(getContext(), target.buttons.get(indexViews[0] + i));
                            button.start(new sendKeyboard(button), getContext());
                            button.setAlpha(min((prefConfig.oscOpacity / 100f), (button.alpha / 10f)));

                            root.addView(button);


                        }

                        indexViews[0] += viewsPerBatch;
                        root.post(this);

                    } else {
                        locked = false;

                        if(updateButtons){
                            updateButtons = false;
                            makeButtons();
                        }
                    }

                }
            };

            root.postDelayed(removeViewRunnable, 1);
            root.postDelayed(updateViewRunnable, 5);
        }
    }
    static final int BUTTONS = 0;
    static final int MOUSE = 1;
    static final int VECTOR = 2;
    static final int CONTINUOUSMOUSE = 3;


    public class sendKeyboard extends GenericCallbackv2 {
        short mouseX;
        short mouseY;

        short keyboard_flags = 0;

        ButtonID buttonIDRef;

        boolean mousemove = false;

        Handler mouseHandler;

        Runnable runMouseMovement = new Runnable() {
            @Override
            public void run() {
                Thread t = new Thread(new Runnable() {
                    boolean run = true;
                    @Override
                    public void run() {
                        while(true) {
                            MainActivity.connection.sendMouseMove(mouseX, mouseY);
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            if(Math.abs(mouseX) < 2 && Math.abs(mouseY) < 2) {
                                mouseX = 0;
                                mouseY = 0;
                            }
                        }
                    }
                });

                t.start();
            }
        };

        public sendKeyboard(ButtonID buttonIDRef) {
            this.buttonIDRef = buttonIDRef;
        }

        public sendKeyboard() {}

        @Override
        public void onChange(Object value) {

            if(value instanceof Boolean) {
                if(buttonIDRef != null) {
                    buttonIDRef.executing((boolean) value);
                }
            }
        }

        @Override
        public void onChange(Object keyCode, Object down, Object type) {

            //Process the input differently depending on weither its a vector or a button input
                switch ((int) type) {

                    //In the case it is buttons
                    case (CONTINUOUSMOUSE):
                        ButtonID.Vector joyVector = (ButtonID.Vector) down;

                        mouseX = (short) Math.round(joyVector.x);
                        mouseY = (short) Math.round(joyVector.y);

                        if(mouseHandler == null) {
                            mouseHandler = new Handler();
                            mouseHandler.post(runMouseMovement);
                        }

                        break;

                    case (BUTTONS):

                        Command activeCommand = ButtonHandler.isCommand((String) keyCode);
                        if (activeCommand != null) {
                            activeCommand.run(new sendKeyboard(buttonIDRef), (boolean) down);
                            return;
                        }

                        //Get the code and determine if its mouse, keyboard or controller
                        int[] result = ButtonHandler.getCode((String) keyCode);

                        int keybind = result[0];
                        int buttonType = result[1];

                        //If it is a keyboard simply translate the key and export
                        if(buttonType == ButtonHandler.KEYBOARD) {

                            if(keybind == KeyEvent.KEYCODE_SHIFT_LEFT || keybind == KeyEvent.KEYCODE_SHIFT_RIGHT) {

                                if((boolean) down) {
                                    keyboard_flags |= KeyboardPacket.MODIFIER_SHIFT;
                                } else {
                                    keyboard_flags = (short) ( (keyboard_flags ^=  KeyboardPacket.MODIFIER_SHIFT) & keyboard_flags);
                                }
                            }

                            if(keybind == KeyEvent.KEYCODE_ALT_LEFT || keybind == KeyEvent.KEYCODE_ALT_RIGHT) {

                                if((boolean) down) {
                                    keyboard_flags |= KeyboardPacket.MODIFIER_ALT;
                                } else {
                                    keyboard_flags = (short) ( (keyboard_flags ^=  KeyboardPacket.MODIFIER_ALT) & keyboard_flags);
                                }
                            }

                            if(keybind == KeyEvent.KEYCODE_CTRL_LEFT || keybind == KeyEvent.KEYCODE_CTRL_RIGHT) {

                                if((boolean) down) {
                                    keyboard_flags |= KeyboardPacket.MODIFIER_CTRL;
                                } else {
                                    keyboard_flags = (short) ( (keyboard_flags ^=  KeyboardPacket.MODIFIER_CTRL) & keyboard_flags);
                                }
                            }

                            short keyMap = translator.translate(keybind, -1);
                            MainActivity.connection.sendKeyboardInput(keyMap, (boolean) down ? KeyboardPacket.KEY_DOWN : KeyboardPacket.KEY_UP, (byte) keyboard_flags, (byte) 0);


                            //If it is a xbox handel it differently
                        } else if(buttonType == ButtonHandler.XINPUT) {

                            if(keybind == ButtonHandler.LEFT_TRIGGER) {

                                lt = (byte) ((boolean) down ? 0xFF : 0x0);

                            } else if(keybind == ButtonHandler.RIGHT_TRIGGER) {

                                rt = (byte) ((boolean) down ? 0xFF : 0x0);

                            } else {

                                short xval = (short) keybind;

                                if((boolean) down) {
                                    xbox_flags |= xval;
                                } else {
                                    xbox_flags = (short) ( (xbox_flags ^= xval) & xbox_flags);
                                }


                            }

                            MainActivity.connection.sendControllerInput((byte) 0, (byte) 1, xbox_flags, lt, rt, lxj, lyj, rxj, ryj);

                            Log.i("xbox", String.valueOf(xbox_flags));

                            //If it is a mouse simply go left or right
                        } else if(buttonType == ButtonHandler.MOUSE) {

                                byte mouseButtonClick = MouseButtonPacket.BUTTON_LEFT;

                                if(keybind == RIGHT_CLICK) {
                                    mouseButtonClick = MouseButtonPacket.BUTTON_RIGHT;

                                } else if(keybind == MIDDLE_CLICK){
                                    mouseButtonClick = MouseButtonPacket.BUTTON_MIDDLE;

                                }

                                if ((boolean) down) {
                                    MainActivity.connection.sendMouseButtonDown(mouseButtonClick);
                                } else {
                                    MainActivity.connection.sendMouseButtonUp(mouseButtonClick);
                                }


                        } else if(buttonType == ButtonHandler.INTERNAL) {

                            if(down instanceof Boolean) {
                                if (keyboardCallback != null && !(boolean) down && keybind == KEYBOARD_TOGGLE) {
                                    keyboardCallback.onChange(null);
                                }

                                if(PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                                    if (keybind == GYRO_TOGGLE) {
                                        GyroController.toggleEnabled = (boolean) down;
                                    }
                                }
                            } else if(down instanceof Integer) {

                                if (PremiumController.hasPermission(PremiumController.Product.MotionControls)) {
                                    if (keybind == GYRO_TOGGLE) {
                                        float s = (int) down == 0 ? currentLayout.gyroSensitivity : (int) down * 0.01f;
                                        sensitivity =  (s * 60) + 0.5f;
                                        GyroController.gyroActivated = (int) down != 0;
                                    }
                                }
                            }

                            if(keybind == TRACKPAD_ENABLE) {
                                if (buttonIDRef != null) {
                                    buttonIDRef.isTrackpad = true;
                                    buttonIDRef.trackPadType = ButtonID.MOUSE;

                                    String code = (String) keyCode;
                                    if(code.contains("^")) {
                                        code = code.substring(code.indexOf("^") + 1);

                                        switch (code){
                                            case ("w:a:s:d"):
                                                buttonIDRef.trackPadType = ButtonID.WASD;
                                                break;

                                            case ("xbox_right_joystick"):
                                            case ("xrj"):
                                                buttonIDRef.trackPadType = ButtonID.rXBOX;
                                                break;

                                            case ("xbox_left_joystick"):
                                            case ("xlj"):
                                                buttonIDRef.trackPadType = ButtonID.lXBOX;
                                                break;

                                            case ("up:left:down:right"):
                                                buttonIDRef.trackPadType = ButtonID.UDLR;
                                                break;

                                            case ("xbox_up:xbox_left:xbox_down:xbox_right"):
                                                buttonIDRef.trackPadType = ButtonID.button_XBOX;
                                                break;

                                            case ("mouse"):
                                                buttonIDRef.trackPadType = ButtonID.MOUSE;
                                                break;

                                            case ("mouse_wheel"):
                                                buttonIDRef.trackPadType = ButtonID.MOUSE_WHEEL;
                                                break;
                                        }
                                    }

                                    if(down instanceof Integer) {
                                        UserData.currentLayout.touchSensitivity = (int) down / 100f;
                                    }
                                }
                            } else if(keybind == ButtonHandler.ABSOLUTE_MOUSE) {
                                UserData.currentLayout.isTouchTrackpad = false;

                            } else if(keybind == ButtonHandler.RELATIVE_MOUSE) {
                                UserData.currentLayout.isTouchTrackpad = true;

                            }

                        } else if(buttonType == ButtonHandler.LAYOUT) {

                            if(!(boolean) down) {

                                String newLayoutName = ((String) keyCode);
                                newLayoutName = newLayoutName.replace("<", "");
                                newLayoutName = newLayoutName.replace(">", "");

                                Log.i("gg", "{" + newLayoutName + "}");

                                LayoutClass potentialLayout = new LayoutClass(newLayoutName, getContext());

                                if(potentialLayout.exists) {

                                    currentLayout = potentialLayout;
                                    makeButtons();
                                }

                            }
                        } else if(buttonType == ButtonHandler.TEXT) {

                            if(buttonIDRef != null) {

                                if ((boolean) down) {

                                    String newLayoutName = ((String) keyCode);
                                    newLayoutName = newLayoutName.replace("?", "");
                                    newLayoutName = newLayoutName.replace("?", "");

                                    String finalNewLayoutName = newLayoutName;
                                    root.post(() -> {
                                        buttonIDRef.nameStorage = buttonIDRef.textView.getText().toString();
                                        buttonIDRef.textView.setText(finalNewLayoutName);
                                    });

                                } else {
                                    root.post(() -> buttonIDRef.textView.setText(buttonIDRef.nameStorage));
                                }
                            }

                        } else if(buttonType == ButtonHandler.MOUSE_CLICK) {

                            UserData.currentLayout.isMouseClickEnabled = keybind == 1;
                        }

                        break;

                    //In the case it is a vector
                    case (VECTOR):

                        ButtonID.Vector moveVector = (ButtonID.Vector) down;

                        if(keyCode.equals("mouse")) {

                            mouseX = (short) Math.round(moveVector.x);
                            mouseY = (short) Math.round(moveVector.y);

                            MainActivity.connection.sendMouseMove(mouseX, mouseY);

                        } else {

                            if (keyCode.equals("xlj") || keyCode.equals("xbox_left_joystick")) {

                                lxj = (short) Math.round(moveVector.x);
                                lyj = (short) Math.round(moveVector.y);


                            } else if (keyCode.equals("xrj") || keyCode.equals("xbox_right_joystick")) {

                                rxj = (short) Math.round(moveVector.x);
                                ryj = (short) Math.round(moveVector.y);
                            }


                            if(rxj > 30000) {
                                rxj = (short) 30000;
                            } else if(rxj < -30000) {
                                rxj = (short) -30000;
                            }

                            if(ryj > 30000) {
                                ryj = (short) 30000;
                            } else if(ryj < -30000) {
                                ryj = (short) -30000;
                            }


                            MainActivity.connection.sendControllerInput((byte) 0, (byte) 1, xbox_flags, lt, rt, lxj, lyj, rxj, ryj);

                                if (lxj * lyj == 0 || rxj * ryj == 0) {
                                    new Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            MainActivity.connection.sendControllerInput((byte) 0, (byte) 1, xbox_flags, lt, rt, lxj, lyj, rxj, ryj);
                                        }
                                    }, 100);
                                }


                        }


                        break;
                }


        }
    };

    private View mBottomView;

    public void setBottomView(View bottomView) {
        mBottomView = bottomView;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(gyroController == null) { return; }

        if(visibility != VISIBLE) {
            gyroController.endSensor();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(gyroController == null) { return; }
        gyroController.endSensor();

        if(this.connectionType == ConnectionType.BLUETOOTH) {
            bluetoothController.destroy();
        }

    }

    public void start() {
        if(gyroController == null) { return; }

        gyroController.startSensor();
    }

    public void end() {
        if(gyroController == null) { return; }

        gyroController.endSensor();
    }

    OnGenericCallbackv2 keyboardCallback;

    public void setKeyboardToggleCallback(OnGenericCallbackv2 keyboardCallback) {
        this.keyboardCallback = keyboardCallback;
    }


    class buttonMovement implements OnTouchListener {

            String Currently_Pressed = "";
        ArrayList<String> PressedGroup = new ArrayList<>();
            ArrayList<String> Previous_PressedGroup = new ArrayList<>();

            String[] buttons = {"w", "a", "s", "d"};
            String Pressed = "";

            float xpos;
            float ypos;

            float xx = 0;
            float yy = 0;

            float distance = 0;
            float angle = 0;

            float final_angle = 0;

            boolean clicked = false;

            float foregroundOriginX, foregroundOriginY;


        ViewGroup joyView;
        View backgroundJoyView;
            View foregroundJoyView;


        public buttonMovement() {

            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            joyView = (ViewGroup) inflater.inflate(R.layout.joystick_layout, LaunchOverlayController.this, false);

            backgroundJoyView = joyView.findViewById(R.id.backgroundJoystick);
            foregroundJoyView = joyView.findViewById(R.id.foregroundJoystick);

            LaunchOverlayController.this.addView(joyView);
        }

        public buttonMovement(String[] buttons) {
            this.buttons = buttons;

            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            joyView = (ViewGroup) inflater.inflate(R.layout.joystick_layout, LaunchOverlayController.this, false);

            backgroundJoyView = joyView.findViewById(R.id.backgroundJoystick);
            foregroundJoyView = joyView.findViewById(R.id.foregroundJoystick);

            LaunchOverlayController.this.addView(joyView);
        }
        @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (event.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    Currently_Pressed = "";
                    Pressed = "";
                    clicked = true;

                    xpos = event.getX();
                    ypos = event.getY();

                    backgroundJoyView.setX(xpos + view.getX() - backgroundJoyView.getWidth() / 2);
                    backgroundJoyView.setY(ypos + view.getY() - backgroundJoyView.getHeight() / 2);

                    foregroundOriginX = xpos + view.getX() - foregroundJoyView.getWidth() / 2;
                    foregroundOriginY = ypos + view.getY() - foregroundJoyView.getHeight() / 2;

                    foregroundJoyView.setX(foregroundOriginX);
                    foregroundJoyView.setY(foregroundOriginY);
                    backgroundJoyView.setVisibility(VISIBLE);

                }

                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

//                    xx = event.getX();
//                    yy = event.getY();
//
//                    float diffX = xx - xpos;
//                    float diffY = yy - ypos;
//
//                    {
//
//                        distance = (float) Math.sqrt(pow(diffX, 2) + pow(diffY, 2));
//                        angle = (float) (Math.atan((diffY) / (diffX)) + Math.PI / 2);
//
//                        final_angle = angle;
//                        if ((xpos - xx) < 0) {
//                            final_angle = (float) Math.PI + angle;
//                        }
//
//                        foregroundJoyView.setX((float) (foregroundOriginX - sigmoid(distance) * Math.sin(final_angle)));
//                        foregroundJoyView.setY((float) (foregroundOriginY + sigmoid(distance) * Math.cos(final_angle)));
//                        foregroundJoyView.setVisibility(VISIBLE);
//
//                        if (distance >= 50) {
//
//                            PressedGroup[0] = "";
//                            PressedGroup[1] = "";
//
//                            int count = 0;
//
//                            if (final_angle < 4 * Math.PI / 3 && final_angle > 2 * Math.PI / 3) {
//                                PressedGroup[count] = buttons[0];
//                                count += 1;
//                            }
//                            if (final_angle < 5 * Math.PI / 6 && final_angle > Math.PI / 6) {
//                                PressedGroup[count] = buttons[1];
//                                count += 1;
//                            }
//                            if (final_angle < Math.PI / 3 || final_angle > 5 * Math.PI / 3) {
//                                PressedGroup[count] = buttons[2];
//                                count += 1;djksf
//                            }
//                            if (final_angle < 11 * Math.PI / 6 && final_angle > 7 * Math.PI / 6) {
//                                PressedGroup[count] = buttons[3];
//                                count += 1;
//                            }
//
//                            if (count == 0) {
//                                return true;
//
//                            }
//
//                            if (!PressedGroup[1].equals("")) {
//                                Pressed = PressedGroup[0] + "&" + PressedGroup[1];
//                            } else {
//                                Pressed = PressedGroup[0];
//                            }
//
//                            if (!Previous_PressedGroup[0].equals(PressedGroup[0]) || !Previous_PressedGroup[1].equals(PressedGroup[1])) {
//
//                                for (String s : Previous_PressedGroup) {
//                                    if (!s.equals("")) {
//                                        sendKeyboard.onChange(s, UP, LaunchOverlayController.BUTTONS);
//                                    }
//                                }
//
//                                Previous_PressedGroup[0] = PressedGroup[0];
//                                Previous_PressedGroup[1] = PressedGroup[1];
//
//                                for (String s : PressedGroup) {
//                                    if (!s.equals("")) {
//                                        sendKeyboard.onChange(s, DOWN, LaunchOverlayController.BUTTONS);
//
//                                    }
//                                }
//
////                                vibrate();
//
//
//                            }
//
//
//                        } else {
//                            for (String s : Previous_PressedGroup) {
//
//                                if (!s.equals("")) {
//                                    sendKeyboard.onChange(s, UP, LaunchOverlayController.BUTTONS);
//                                }
//                            }
//
//                            Previous_PressedGroup[0] = "";
//
//                            Previous_PressedGroup[1] = "";
//
//                            PressedGroup[0] = "";
//                            PressedGroup[1] = "";
//                        }
//                    }

                    xx = event.getX();
                    yy = event.getY();

                    final double xDiff = xpos - xx;
                    final double yDiff = ypos - yy;

                    distance = (float) Math.sqrt(pow(xDiff, 2) + pow(yDiff, 2));
                    double final_angle = Math.acos(xDiff / distance);

                    if(yDiff < 0) {
                        final_angle = 2 * Math.PI - final_angle;
                    }

                    foregroundJoyView.setX((float) (foregroundOriginX - sigmoid(distance) * Math.cos(final_angle)));
                    foregroundJoyView.setY((float) (foregroundOriginY - sigmoid(distance) * Math.sin(final_angle)));
                    foregroundJoyView.setVisibility(VISIBLE);


                    if (distance >= 50) {

                        PressedGroup.clear();

                        if (final_angle < 4 * Math.PI / 3 && final_angle > 2 * Math.PI / 3) {
                            PressedGroup.add(buttons[3]);

                        }
                        if (final_angle < 5 * Math.PI / 6 && final_angle > Math.PI / 6) {
                            PressedGroup.add(buttons[0]);

                        }
                        if (final_angle < Math.PI / 3 || final_angle > 5 * Math.PI / 3) {
                            PressedGroup.add(buttons[1]);

                        }
                        if (final_angle < 11 * Math.PI / 6 && final_angle > 7 * Math.PI / 6) {

                            PressedGroup.add(buttons[2]);
                        }

                        if (PressedGroup.size() == 0) {
                            return true;
                        }

                        boolean hasChanged = Previous_PressedGroup.size() == 0 || Previous_PressedGroup.size() != PressedGroup.size();
                        for (int index = 0; index < Previous_PressedGroup.size(); index++) {

                            if(!PressedGroup.contains(Previous_PressedGroup.get(index))) {
                                sendKeyboard.onChange(Previous_PressedGroup.get(index), UP, LaunchOverlayController.BUTTONS);
                                hasChanged = true;
                            }
                        }

                        if(hasChanged) {
                            for (String s : PressedGroup) {
                                sendKeyboard.onChange(s, DOWN, LaunchOverlayController.BUTTONS);

                            }

                            Previous_PressedGroup.clear();
                            Previous_PressedGroup.addAll(PressedGroup);

                        }


                    }
                }

                if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    foregroundJoyView.setVisibility(GONE);
                    backgroundJoyView.setVisibility(GONE);

                    clicked = false;
                    for (String s : Previous_PressedGroup) {

                        if (!s.equals("")) {
                            sendKeyboard.onChange(s, UP, LaunchOverlayController.BUTTONS);
                        }
                    }

                    Previous_PressedGroup.clear();
                    PressedGroup.clear();
                }

                return true;
            }
        }

    class mouseScrollMovement implements OnTouchListener {

        float y = 0;
        @Override
        public boolean onTouch(View view, MotionEvent event) {

            if (event.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                y = event.getY();
            }

            if(event.getActionMasked() == MotionEvent.ACTION_MOVE) {

                short scrollAmount =  (short) (UserData.currentLayout.touchSensitivity * (5 * (y - event.getY())));
                MainActivity.connection.sendMouseHighResHScroll(scrollAmount);

                y = event.getY();

            }

            return true;
        }
    }


    class mouseMovement implements OnTouchListener {

        float x, y = 0;
        float totalDis = 0;

        boolean clickedFlag, rightClickedFlag, keyboardFlag = false;
        final android.os.Handler clickHandler = new Handler();

        int mouseTrackDis = 50;

        final Runnable clickTimer = new Runnable() {
            @Override
            public void run() {
                bluetoothController.SendMouseButton(LEFT, DOWN);
                clickedFlag = true;
            }
        };

            final Runnable quickClick = new Runnable() {
                @Override
                public void run() {

                    bluetoothController.SendMouseButton(LEFT, DOWN);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    bluetoothController.SendMouseButton(LEFT, UP);

                }
            };

            final Runnable rightClick = new Runnable() {
                @Override
                public void run() {
                    rightClickedFlag = true;

                    bluetoothController.SendMouseButton(BluetoothController.MouseButton.RIGHT, DOWN);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    bluetoothController.SendMouseButton(BluetoothController.MouseButton.RIGHT, UP);

                }
            };
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(connectionType == ConnectionType.WIFI) {

                    if (mBottomView != null) {
                        if (UserData.currentLayout.isTouchTrackpad) {
                            mBottomView.dispatchTouchEvent(motionEvent);
                        } else {
                            return false;

                        }
                    }
                } else {

                    switch (motionEvent.getAction()){

                        case(MotionEvent.ACTION_BUTTON_PRESS):
                        case(MotionEvent.ACTION_DOWN):

                            rightClickedFlag = false;
                            keyboardFlag = false;
                            clickedFlag = false;

                            clickHandler.postDelayed(clickTimer, 500);

                            totalDis = 0;

                            x = motionEvent.getX();
                            y = motionEvent.getY();
                            break;

                        case(MotionEvent.ACTION_MOVE):

                            float xDiff = motionEvent.getX() - x;
                            float yDiff = motionEvent.getY() - y;

                            if(motionEvent.getPointerCount() == 1 && !rightClickedFlag && !keyboardFlag) {

                                totalDis += Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));

                                if (totalDis > mouseTrackDis) {
                                    clickHandler.removeCallbacks(clickTimer);
                                    bluetoothController.SendMouse(new ButtonID.Vector(xDiff, yDiff));
                                }
                            } else if (motionEvent.getPointerCount() == 2 && !keyboardFlag){

                                rightClickedFlag = true;
                                clickHandler.removeCallbacks(clickTimer);

                            } else if(motionEvent.getPointerCount() == 3) {

//                                keyboardFlag = true;
                            }

                            x = motionEvent.getX();
                            y = motionEvent.getY();

                            break;

                        case(MotionEvent.ACTION_BUTTON_RELEASE):
                        case(MotionEvent.ACTION_UP):

                            if(keyboardFlag) {
                                toggleKeyboard();
                            }

                            if(rightClickedFlag) {
                                new Thread(rightClick).start();

                            } else  {
                                clickHandler.removeCallbacks(clickTimer);

                                if (clickedFlag) {
                                    bluetoothController.SendMouseButton(LEFT, UP);
                                }

                                if (!clickedFlag && !rightClickedFlag && totalDis < mouseTrackDis) {
                                    new Thread(quickClick).start();
                                }
                            }

                            break;


                    }
                }

                return true;
            }
        }

        class xboxMovement implements OnTouchListener {

            float x = 0;
            float y = 0;

            float lj = 0;
            float rj = 0;

            float maxDis = 300;

            boolean right;

            float foregroundOriginX, foregroundOriginY;

            ViewGroup joyView;
            View backgroundJoyView;
            View foregroundJoyView;

            xboxMovement(boolean rightOrLeft) {
                this.right = rightOrLeft;

                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                joyView = (ViewGroup) inflater.inflate(R.layout.joystick_layout, LaunchOverlayController.this, false);

                backgroundJoyView = joyView.findViewById(R.id.backgroundJoystick);
                foregroundJoyView = joyView.findViewById(R.id.foregroundJoystick);

                LaunchOverlayController.this.addView(joyView);
            }

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                maxDis = 50 + UserData.currentLayout.touchSensitivity * 150;

                if (event.getActionMasked() == MotionEvent.ACTION_BUTTON_PRESS || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    x = event.getX();
                    y = event.getY();

                    lj = 0;
                    rj = 0;

                    foregroundOriginX = x;
                    foregroundOriginY = y;

                    foregroundOriginX += view.getX();
                    foregroundOriginY += view.getY();
                    backgroundJoyView.setX(foregroundOriginX - backgroundJoyView.getWidth() / 2f);
                    backgroundJoyView.setY(foregroundOriginY - backgroundJoyView.getWidth() / 2f);
                    backgroundJoyView.setVisibility(VISIBLE);

                    foregroundOriginX -= foregroundJoyView.getWidth() / 2f;
                    foregroundOriginY -= foregroundJoyView.getHeight() / 2f;

                    foregroundJoyView.setX(foregroundOriginX);
                    foregroundJoyView.setY(foregroundOriginY);
                    foregroundJoyView.setVisibility(VISIBLE);
                }

                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    float diffX = x - event.getX();
                    float diffY = y - event.getY();

                    float distance = (float) Math.sqrt(pow(diffX, 2) + pow(diffY, 2));
                    float angle = (float) (Math.atan((diffY) / (diffX)) + Math.PI / 2);

                    float final_angle = angle;
                    if ((diffX) < 0) {
                        final_angle = (float) Math.PI + angle;
                    }

                    rj = (float) (min(distance, maxDis) / maxDis * 32766 * Math.cos(final_angle));
                    lj = (float) (min(distance, maxDis) / maxDis * 32766 * Math.sin(final_angle));

                    if (right) {
                        rxj = (short) -lj;
                        ryj = (short) -rj;

                    } else {
                        lxj = (short) -lj;
                        lyj = (short) -rj;
                    }

                    foregroundJoyView.setX((float) (foregroundOriginX - sigmoid(distance) * Math.sin(final_angle)));
                    foregroundJoyView.setY((float) (foregroundOriginY + sigmoid(distance) * Math.cos(final_angle)));

                    if(connectionType == ConnectionType.WIFI) {
                        MainActivity.connection.sendControllerInput((byte) 0, (byte) 1, xbox_flags, lt, rt, lxj, lyj, rxj, ryj);
                    }

                }

                if (event.getActionMasked() == MotionEvent.ACTION_CANCEL || event.getActionMasked() == MotionEvent.ACTION_UP) {

                    if (right) {
                        rxj = (short) 0;
                        ryj = (short) 0;
                    } else {

                        lxj = (short) 0;
                        lyj = (short) 0;

                    }

                    foregroundJoyView.setVisibility(GONE);
                    backgroundJoyView.setVisibility(GONE);

                    if(connectionType == ConnectionType.WIFI) {
                        MainActivity.connection.sendControllerInput((byte) 0, (byte) 1, xbox_flags, lt, rt, lxj, lyj, rxj, ryj);
                    }

                }
                return true;
            }
    }

    class emptyTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    }

    GenericCallbackv2 resetContexts;
    public void setOnResetContexts(GenericCallbackv2 callbackv2) {
        resetContexts = callbackv2;
    }

    private float sigmoid(float val) {
        return (float) (200 / (Math.pow(1.02f, -val) + 1) - 100);
    }

    public void catchMotionEvents(ButtonID button, MotionEvent motionEvent) {
        switch (button.trackPadType) {
            case (ButtonID.MOUSE_WHEEL):
                MouseScrollMovement.onTouch(button, motionEvent);
                break;

            case (ButtonID.MOUSE):
                MouseListener.onTouch(button, motionEvent);
                break;

            case (ButtonID.WASD):
                WASDListener.onTouch(button, motionEvent);
                break;

            case (ButtonID.rXBOX):
                RightXboxListener.onTouch(button, motionEvent);
                break;

            case (ButtonID.button_XBOX):
                XUDLRListener.onTouch(button, motionEvent);
                break;

            case (ButtonID.UDLR):
                UDLRListener.onTouch(button, motionEvent);
                break;
        }
    }

    public void toggleKeyboard() {

        if(getContext() == null) { return; }

        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);
    }

}

