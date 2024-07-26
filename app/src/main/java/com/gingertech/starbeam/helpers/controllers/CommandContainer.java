package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.helpers.controllers.LaunchOverlayController.BUTTONS;

import android.content.Context;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.UserData;

import java.util.ArrayList;

public class CommandContainer {

    public int type;

    public int parameter = 0;
    public ArrayList<String> values = new ArrayList<>();

    private String parameterDefault = " time ";
    private String valueDefault = " keybinds ";

    public String valueHint = "space";

    public final static int AUTO_NORMAL = 0;
    public final static int AUTO_LAYOUTS = 1;
    public final static int AUTO_COMMANDS = 2;
    public final static int AUTO_MOVEABLES = 3;
    public final static int AUTO_NONE = 4;



    public static String[] commands = {
            "tap",
            "press",
            "release",
            "set_text",
            "delay",
            "make_trackpad",
            "disable_trackpad",
            "enable_gyro",
            "disable_gyro",
            "switch_layout",
            "reset_layout",
            "reset_text",
            "run_command",
            "mouse_absolute",
            "mouse_relative",
            "enable_mouse_click",
            "disable_mouse_click",

    };

    int[] descriptions = {
            (R.string.tap_description),
            (R.string.press_description),
            (R.string.release_description),
            (R.string.set_text_description),
            (R.string.delay_description),
            (R.string.etrackpad_description),
            (R.string.dtrackpad_description),
            (R.string.egyro_description),
            (R.string.dgyro_description),
            (R.string.switch_description),
            (R.string.rswitch_description),
            (R.string.rtext_description),
            (R.string.command_description),
            (R.string.mouse_absolute),
            (R.string.mouse_relative),
            (R.string.emouse_click),
            (R.string.dmouse_click),

    };
    final public static int TAP = 0;
    final public static int PRESS = 1;
    final public static int RELEASE = 2;
    final public static int sTEXT = 3;
    final public static int DELAY = 4;
    final public static int eTRACKPAD = 5;
    final public static int dTRACKPAD = 6;
    final public static int eGYRO = 7;
    final public static int dGYRO = 8;
    final public static int sLAYOUT = 9;
    final public static int rLAYOUT = 10;
    final public static int rTEXT = 11;
    final public static int COMMAND = 12;
    final public static int aMouse = 13;
    final public static int rMouse = 14;
    final public static int eMouseClick = 15;
    final public static int dMouseClick = 16;



    public int maxParameter = 100;
    public boolean hasParameter = false;
    public boolean hasValues = false;
    public boolean multipleKeybinds = true;
    public int keybindType = AUTO_NORMAL;

    public String command;
    public String description;
    public String valueDescription;
    public String parameterDescription;

    public CommandContainer(Context context, int type) {
        this.type = type;

        description = context.getString(descriptions[type]);

        setup(context);
        command = commands[type];
    }

    public CommandContainer(Context context, CommandContainer ref) {
        this.type = ref.type;

        description = context.getString(descriptions[type]);

        setup(context);

        command = commands[type];

        values.addAll(ref.values);

        parameter = ref.parameter;
    }

    private void setup(Context context) {

        valueDescription = context.getString(R.string.setKeybinds);

        switch (type) {
            case (TAP) :
            case (eTRACKPAD):
                hasParameter = true;
                hasValues = true;
                break;

            case (PRESS):
            case (RELEASE):
            case (sLAYOUT):
            case (sTEXT):
            case (COMMAND):
                hasValues = true;
                break;

            case (DELAY):
            case (eGYRO):
                hasParameter = true;
                break;

        }


        switch (type) {
            case (TAP):
            case (DELAY):
                parameterDescription = context.getString(R.string.setTime);
                parameterDefault = " time ";
                maxParameter = 10000;
                break;

            case (eTRACKPAD):
                valueDescription = "Set Keybind";
                valueDefault = "keybind";
            case (eGYRO):
                parameterDescription = context.getString(R.string.setSensitivity);
                parameterDefault = " sens ";
                maxParameter = 100;
                break;

            case (sLAYOUT):
            case (sTEXT):
                valueDescription = "Set Name";
                valueDefault = " name ";
                break;

            case (COMMAND):
                valueDescription = " Set Command ";
                valueDefault = " command ";
                break;



        }

        switch (type) {
            case (eTRACKPAD):
                keybindType = AUTO_MOVEABLES;
                multipleKeybinds = false;
                break;

            case (sLAYOUT):
                valueHint = "driving_layout";

                keybindType = AUTO_LAYOUTS;
                multipleKeybinds = false;
                break;

            case (sTEXT):
                valueHint = "jump";
                keybindType = AUTO_NONE;
                multipleKeybinds = false;
                break;

           case (COMMAND):
               valueHint = "command";
               keybindType = AUTO_COMMANDS;
               multipleKeybinds = false;
               break;
        }

        command = commands[type];
    }

    public String getText() {

        String parameterText = String.valueOf(parameter);
        StringBuilder valuesText = new StringBuilder();

        if(values.isEmpty()) {

            valuesText = new StringBuilder();
        } else {

            for(String s : values) {
                valuesText.append(" ").append(s);
            }

        }

        return command + ":" +
                (hasParameter ? "(" + parameterText + ")" : "") +
                (!hasParameter ? "()" : "") +
                (hasValues ? "{" + valuesText + " }" : "");
    }

    public String getText(boolean defaultOnly) {

        if(defaultOnly) {
            return command + ":" +
                    (hasParameter ? "(" + parameterDefault + ")" : "") +
                    (!hasParameter ? "()" : "") +
                    (hasValues ? "{" + valueDefault + "}" : "");
        }

        String parameterText = String.valueOf(parameter);
        StringBuilder valuesText = new StringBuilder();

        if(values.isEmpty()) {

            valuesText = new StringBuilder();
        } else {

            for(String s : values) {
                valuesText.append(" ").append(s);
            }

        }

        return command + ":" +
                (hasParameter ? "(" + parameterText + ")" : "") +
                (!hasParameter ? "()" : "") +
                (hasValues ? "{" + valuesText + " }" : "");
    }

    public void run(GenericCallbackv2 callbackv2) {

            switch (command) {
                case ("press"):
                    press(callbackv2);
                    break;

                case ("release"):
                    release(callbackv2);
                    break;

                case ("delay"):
                    delay();
                    break;

                case ("tap"):
                    press(callbackv2);
                    delay();
                    release(callbackv2);
                    break;

                case ("switch_layout"):
                    if(!values.isEmpty()) {
                        callbackv2.onChange("<" + values.get(0) + ">", false, BUTTONS);
                    }
                    break;

                case ("reset_layout"):
                    callbackv2.onChange("<" + UserData.currentLayoutName + ">", false, BUTTONS);
                    break;

                case ("set_text"):
                    if(!values.isEmpty()) {
                        callbackv2.onChange("?" + values.get(0) + "?", true, BUTTONS);
                    }
                    break;

                case ("reset_text"):
                    callbackv2.onChange("??", false, BUTTONS);
                    break;

                case ("run_command"):
                    if(!values.isEmpty()) {
                        callbackv2.onChange("\"" + values.get(0) + "\"", true, BUTTONS);
                    }
                    break;

                case ("enable_gyro"):
                    callbackv2.onChange("gyro_toggle", parameter, BUTTONS);
                    break;

                case ("disable_gyro"):
                    callbackv2.onChange("gyro_toggle", 0, BUTTONS);
                    break;

                case ("mouse_absolute"):
                    callbackv2.onChange("mouse_absolute", 0, BUTTONS);
                    break;

                case ("mouse_relative"):
                    callbackv2.onChange("mouse_relative", 0, BUTTONS);
                    break;

                case ("make_trackpad"):
                    if(values.isEmpty()) {
                        callbackv2.onChange("enable_trackpad", parameter, BUTTONS);
                    } else {
                        callbackv2.onChange("enable_trackpad^" + values.get(0), parameter, BUTTONS);
                    }
                    break;

                case ("enable_mouse_click"):
                    callbackv2.onChange("enable_mouse_click", 1, BUTTONS);
                    break;

                case ("disable_mouse_click"):
                    callbackv2.onChange("disable_mouse_click", 0, BUTTONS);
                    break;
            }
    }

    private void press(GenericCallbackv2 callbackv2) {
        values.forEach(s -> callbackv2.onChange(s, true, BUTTONS));
    }

    private void release(GenericCallbackv2 callbackv2) {
        values.forEach(s -> callbackv2.onChange(s, false, BUTTONS));
    }

    private void delay() {
        try {
            Thread.sleep(parameter);
        } catch (InterruptedException e) {}
    }

    public String getValues(Boolean defaultOnly) {

        if(defaultOnly) {
            return "{ " + valueDefault + " }";
        } else {
            if (values.size() > 0) {

                StringBuilder stringBuilder = new StringBuilder("{ " + values.get(0));
                values.subList(1, values.size()).forEach(s -> stringBuilder.append(" ").append(s));
                return (stringBuilder.append(" }").toString());
            } else {

                return "{ }";
            }
        }
    }

    public String getParameter(Boolean defaultOnly) {

        if(defaultOnly) {
            return "( " + parameterDefault + " )";
        } else {
            return "( " + parameter + " )";
        }
    }
}

