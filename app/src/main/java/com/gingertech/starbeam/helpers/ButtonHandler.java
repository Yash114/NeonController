package com.gingertech.starbeam.helpers;

import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.gingertech.starbeam.helpers.controllers.ButtonID;
import com.gingertech.starbeam.limelight.nvstream.input.ControllerPacket;

import java.util.ArrayList;
import java.util.Objects;

public class ButtonHandler {

    static String commandQualifier = "\"";
    static String alpha = "abcdefghijklmnopqrstuvwxyz";
    static String symb = "`-=[]\\;'/";

    static String othersymb = ".,";
    static String num = "0123456789";

    public static int LEFT_TRIGGER = 55;
    public static int RIGHT_TRIGGER = 56;
    public static int RIGHT_CLICK = 1;
    public static int LEFT_CLICK = 0;
    public static int MIDDLE_CLICK = 2;

    public static int KEYBOARD_TOGGLE = 0;
    public static int GYRO_TOGGLE = 1;
    public static int TRACKPAD_ENABLE = 2;
    public static int ABSOLUTE_MOUSE = 3;
    public static int RELATIVE_MOUSE = 4;



    public static final int NORMAL = 0;
    public static final int SPECIAL = 1;


    public static String[] keybindList = {
            "xbox",
            "xback",
            "xljb",
            "xrjb",
            "xlb",
            "xrb",
            "xstart",
            "xa",
            "xb",
            "xx",
            "xy",
            "xu",
            "xd",
            "xl",
            "xr"
    };



    public static String[] func = {
            "f1",
            "f2",
            "f3",
            "f4",
            "f5",
            "f6",
            "f7",
            "f8",
            "f9",
            "f10",
            "f11",
            "f12"
    };

    public static int[] xbox_keys = {
            110, //XBox button
            109, //back
            106, //left thub
            107, //right_thumb
            102, //LB
            103, //RB
            108, //start
            96,  //A
            97,  //B
            99,  //X
            100, //Y
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT
    };

    public static String[] keyCodesForNormal = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "backspace",
            "tab", "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "[", "]", "\\",
            "caps", "a", "s", "d", "f", "g", "h", "j", "k", "l", ";", "'", "enter",
            "shift", "z", "x", "c", "v", "b", "n", "m", ",", ".", "/", "right_shift",  "`",
            "ctrl", "alt", "space", "alt_right", "right_alt", "ctrl_right", "right_ctrl", "left_alt", "left_ctrl", "left", "up", "down", "right",
            "ins", "delete", "home", "end", "page_up", "page_down", "scrlck", "numlock", "/", "*", "-",
            "+", "enter", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "right_ctrl","click",
            "right_click","left_click","caps","f1","f2","f3","f4","f5","f6","f7","f8","f9","f9","f10","f11",
            "f12","xbox_up","xbox_down","xbox_left","xbox_right","xbox_start","xbox_back","xbox_left_thumb_button",
            "xbox_right_thumb_button","xbox_left_shoulder","xbox_right_shoulder","xbox_a","xbox_b",
            "xbox_x","xbox_y","xbox_left_trigger","xbox_right_trigger", "escape", "xbox_right_bumper", "xbox_left_bumper",
            "scroll_wheel", "mouse_wheel", "keyboard_toggle",
            "gyro_toggle", "xbox_guide", "windows", "right_windows", "left_windows", "gui", "left_gui", "right_gui", "tilde",
    };


    public static String[] keyCodesForMoveables = {
            "w:a:s:d", "xbox_right_joystick", "xbox_left_joystick", "up:left:down:right", "xbox_up:xbox_left:xbox_down:xbox_right", "xlj", "xrj",
            "mouse", "mouse_wheel"
    };

    public static String[] keycodesForJoy = {
            "w:a:s:d", "xbox_right_joystick", "xbox_left_joystick", "up:left:down:right", "xbox_up:xbox_left:xbox_down:xbox_right", "xlj", "xrj",
            "mouse", "mouse&click", "mouse&rclick"
    };

    public static String[] keycodesForJoystick = {
            "w:a:s:d", "xbox_right_joystick", "xbox_left_joystick", "mouse",
    };

    public static String[] keycodesForScrollandCycle = {
            "1:2:3:4:5:6:7:8:9"
    };


    public static int[] xlist = {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_UNKNOWN,
            KeyEvent.KEYCODE_UNKNOWN,
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
    };

    static String[] alpha2 = {
            "", "`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "bksp", "bksp",
            "tab", "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "[", "]", "\\",
            "caps", "a", "s", "d", "f", "g", "h", "j", "k", "l", ";", "'", "enter", "enter",
            "lshift", "", "z", "x", "c", "v", "b", "n", "m", ",", ".", "/", "rshift", "rshift",
            "lctrl", "", "lalt", "space", "ralt", "", "rctrl", "", "", "", "", "", "", "", "", "", "",
            "ins", "del", "", "", "left",
            "home", "end", "", "up", "down",
            "pageup", "pagedown", "", "", "right",
            "numl", "num7", "num4", "num1", "num0", "/", "num8", "num5", "num2", "num0", "*", "num9",
            "num6", "num3", ".", "-", "+", "+", "enter", "enter", "esc", "", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11", "f12",
            "prnsc", "", ""

    };

    public static boolean validate(String keybind, int type) {

        ArrayList<String> keybindString = new ArrayList<>();

        String x = keybind.toLowerCase();

        int p;

        if (type == ButtonID.Normal || type == ButtonID.Sticky) {

            if (x.contains(":") || x.contains("&")) {
                if (x.contains("&")) {
                    while (x.contains("&")) {

                        p = x.indexOf("&");
                        keybindString.add(x.subSequence(0, p).toString());
                        x = x.subSequence(p + 1, x.length()).toString();
                    }
                    keybindString.add(x);

                } else if (x.contains(":")) {
                    p = x.indexOf(":");
                    keybindString.add(x.subSequence(0, p).toString());
                    x = x.subSequence(p + 1, x.length()).toString();
                    keybindString.add(x);
                }
            } else {

                keybindString.add(x);
            }

        } else {

            if (x.contains("&")) {
                while (x.contains("&")) {

                    p = x.indexOf("&");
                    keybindString.add(x.subSequence(0, p).toString());
                    x = x.subSequence(p + 1, x.length()).toString();
                }
                keybindString.add(x);

            } else if (x.contains(":")){

                while (x.contains(":")) {
                    p = x.indexOf(":");
                    keybindString.add(x.subSequence(0, p).toString());
                    x = x.subSequence(p + 1, x.length()).toString();
                }

                keybindString.add(x);
            } else {

                keybindString.add(x);
            }

        }

        if ((type == ButtonID.Normal || type == ButtonID.Sticky) &&
                keybindString.size() != 2 && keybindString.size() != 1) {
            return false;
        }

        if(type == ButtonID.Joy && (keybindString.size() == 2 || keybindString.size() == 1)) {
            String a = keybindString.get(0);
            if(!a.equals("xbox_left_joystick") && !a.equals("xlj")
                    && !a.equals("xrj") && !a.equals("xbox_right_joystick")
                    && !a.equals("mouse")) {

                return false;

            }
        }

        if (type == ButtonID.Joy && keybindString.size() != 4 && keybindString.size() != 2 && keybindString.size() != 1) {
            return false;
        }

        for (String s : keybindString) {

            if(type == ButtonID.Joy && (s.equals("xbox_left_joystick") || s.equals("xlj")
                    || s.equals("xrj") || s.equals("xbox_right_joystick")
                    || s.equals("mouse"))) {
                continue;
            }

            if (s.startsWith("\"") && s.endsWith("\"")) {
                String com = s.replace("\"", "");
                return UserData.Commands.values().stream().anyMatch(co -> Objects.equals(co.name, com));
            }

            if (s.startsWith("<") && s.endsWith(">")) {
                return true;
            }

            if (s.equals("mouseclick") || s.equals("click") || s.equals("lclick") || s.equals("lc") || s.equals("leftc") || s.equals("leftclick") || s.equals("clk") || s.equals("lclk") || s.equals("clkl")  || s.equals("left_click")) {
                continue;
            }

            if (s.equals("rclick") || s.equals("rc") || s.equals("rightc") || s.equals("rightclick") || s.equals("rclk") || s.equals("clkr") || s.equals("right_click")) {
                continue;
            }

            if (getCode(x)[0] != -1) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static int XINPUT = 1;
    public static int KEYBOARD = 0;
    public static int MOUSE = 2;
    public static int INTERNAL = 3;
    public static int LAYOUT = 4;
    public static int TEXT = 5;
    public static int MOUSE_CLICK = 6;



    public static int[] getCode(String key) {

        if (key.length() == 1) {

            if (alpha.contains(key)) {
                return new int[]{alpha.indexOf(key) + KeyEvent.KEYCODE_A, KEYBOARD};
            }

            if (num.contains(key)) {

                return new int[]{num.indexOf(key) + KeyEvent.KEYCODE_0, KEYBOARD};
            }

            if (symb.contains(key)) {
                return new int[]{symb.indexOf(key) + KeyEvent.KEYCODE_MINUS, KEYBOARD};
            }

            if(key.equals("~")) {
                return new int[]{KeyEvent.KEYCODE_GRAVE, KEYBOARD};
            }

            if(key.equals(",")){
                return new int[]{KeyEvent.KEYCODE_COMMA, KEYBOARD};
            }

            if(key.equals(".")){
                return new int[]{KeyEvent.KEYCODE_PERIOD, KEYBOARD};
            }

        } else {

//            if (key.contains("num") && num.contains(Character.toString(key.charAt(3)))) {
//
//                String s = Character.toString(key.charAt(3)), KEYBOARD};
//                if (num.contains(s))
//                    return new int[]{num.indexOf(key.charAt(3)) + 89, KEYBOARD};
//
//                return new int[]{-1, KEYBOARD};
//
//            }
//
//            if (key.contains("#")) {
//
//                int keyCode = Integer.parseInt(key.substring(1)), KEYBOARD};
//
//                if (keyCode >= 0 && keyCode < alpha2.length) {
//                    return new int[]{codePar(alpha2[keyCode]), KEYBOARD};
//                }
//            }

            if(key.equals("enable_mouse_click")) {
                return new int[]{1, MOUSE_CLICK};
            }

            if(key.equals("disable_mouse_click")) {
                return new int[]{0, MOUSE_CLICK};
            }

            if(key.startsWith("?") && key.endsWith("?")) {
                return new int[]{0, TEXT};
            }

            if(key.startsWith("<") && key.endsWith(">")) {
                return new int[]{0, LAYOUT};
            }


            if(key.contains("mouse_absolute")) {
                return new int[]{ABSOLUTE_MOUSE, INTERNAL};
            }

            if(key.contains("mouse_relative")) {
                return new int[]{RELATIVE_MOUSE, INTERNAL};
            }

            if(key.contains("enable_trackpad")) {
                return new int[]{TRACKPAD_ENABLE, INTERNAL};
            }

            if(key.equals("gyro_toggle")) {
                return new int[]{GYRO_TOGGLE, INTERNAL};
            }

            if(key.equals("keyboard_toggle")) {
                return new int[]{KEYBOARD_TOGGLE, INTERNAL};
            }

            if(key.equals("tilde")) {
                return new int[]{KeyEvent.KEYCODE_GRAVE, KEYBOARD};
            }

            if(key.equals("scroll_wheel") || key.equals("mouse_wheel")) {
                return new int[]{MIDDLE_CLICK, MOUSE};
            }

            if (key.equals("mouseclick") || key.equals("click") || key.equals("lclick") || key.equals("lc") || key.equals("leftc") || key.equals("leftclick") || key.equals("clk") || key.equals("lclk") || key.equals("clkl")  || key.equals("left_click")) {
                return new int[]{LEFT_CLICK, MOUSE};

            }

            if (key.equals("rclick") || key.equals("rc") || key.equals("rightc") || key.equals("rightclick") || key.equals("rclk") || key.equals("clkr")  || key.equals("right_click")) {
                return new int[]{RIGHT_CLICK, MOUSE};
            }


            if (key.equals("space") || key.equals("sp")) {

                return new int[]{KeyEvent.KEYCODE_SPACE, KEYBOARD};
            }

            if (key.equals("tab") || key.equals("tb")) {

                return new int[]{KeyEvent.KEYCODE_TAB, KEYBOARD};
            }

            if (key.equals("down")) {

                return new int[]{KeyEvent.KEYCODE_DPAD_DOWN, KEYBOARD};
            }

            if (key.equals("up")) {

                return new int[]{KeyEvent.KEYCODE_DPAD_UP, KEYBOARD};
            }

            if (key.equals("right")) {

                return new int[]{KeyEvent.KEYCODE_DPAD_RIGHT, KEYBOARD};
            }

            if (key.equals("left")) {

                return new int[]{KeyEvent.KEYCODE_DPAD_LEFT, KEYBOARD};
            }

            if (key.equals("shift") || key.equals("left_shift") || key.equals("lshift") || key.equals("leftshift")) {

                return new int[]{KeyEvent.KEYCODE_SHIFT_LEFT, KEYBOARD};
            }

            if (key.equals("rshift") || key.equals("right_shift") || key.equals("rightshift")) {

                return new int[]{KeyEvent.KEYCODE_SHIFT_LEFT, KEYBOARD};
            }

            if (key.equals("enter")) {

                return new int[]{KeyEvent.KEYCODE_ENTER, KEYBOARD};
            }

            if (key.equals("alt") || key.equals("altl") || key.equals("alternate_left") || key.equals("lalt") || key.equals("alt_left") || key.equals("left_alt")) {

                return new int[]{KeyEvent.KEYCODE_ALT_LEFT, KEYBOARD};
            }

            if (key.equals("numl") || key.equals("numlock") || key.equals("num_lock")) {

                return new int[]{KeyEvent.KEYCODE_NUM_LOCK, KEYBOARD};
            }

            if (key.equals("altr") || key.equals("alternate_right") || key.equals("ralt") || key.equals("alt_right") || key.equals("right_alt")) {

                return new int[]{KeyEvent.KEYCODE_ALT_RIGHT, KEYBOARD};
            }

            if (key.equals("bksp") || key.equals("backspace") || key.equals("back_space")) {

                return new int[]{KeyEvent.KEYCODE_BACK, KEYBOARD};
            }

            if (key.equals("caps") || key.equals("capslock") || key.equals("caps_lock") || key.equals("cap_lock") || key.equals("caplock")) {

                return new int[]{KeyEvent.KEYCODE_CAPS_LOCK, KEYBOARD};
            }

            if (key.equals("esc") || key.equals("escape")) {

                return new int[]{KeyEvent.KEYCODE_ESCAPE, KEYBOARD};
            }

            if (key.equals("lctrl") || key.equals("left_ctrl") || key.equals("l_ctrl") || key.equals("left_control") || key.equals("ctrll") || key.equals("control_left") || key.equals("ctrl_left") || key.equals("ctrl") || key.equals("control")) {

                return new int[]{KeyEvent.KEYCODE_CTRL_LEFT, KEYBOARD};
            }

            if (key.equals("rctrl") || key.equals("r_ctrl") || key.equals("right_control") || key.equals("ctrlr") || key.equals("control_right") || key.equals("ctrl_right") || key.equals("right_ctrl")) {

                return new int[]{KeyEvent.KEYCODE_CTRL_RIGHT, KEYBOARD};
            }

            if (key.equals("home") || key.equals("hm")) {
                return new int[]{KeyEvent.KEYCODE_HOME, KEYBOARD};
            }

            if(key.equals("win") || key.equals("windows") || key.equals("left_windows") || key.equals("left_gui") || key.equals("gui")) {
                return new int[]{KeyEvent.KEYCODE_META_LEFT, KEYBOARD};
            }

            if(key.equals("rwin") || key.equals("right_windows") || key.equals("rwindows") || key.equals("right_gui") || key.equals("rgui")) {
                return new int[]{KeyEvent.KEYCODE_META_RIGHT, KEYBOARD};
            }

            if (key.equals("ins")) {
                return new int[]{KeyEvent.KEYCODE_INSERT, KEYBOARD};
            }

            if (key.equals("delete") || key.equals("del")) {
                return new int[]{KeyEvent.KEYCODE_DEL, KEYBOARD};
            }

            if (key.equals("pgup") || key.equals("pageup") || key.equals("page_up") || key.equals("pg_up")) {
                return new int[]{KeyEvent.KEYCODE_PAGE_UP, KEYBOARD};
            }

            if (key.equals("pgdown") || key.equals("pagedown") || key.equals("page_down") || key.equals("pg_down")) {
                return new int[]{KeyEvent.KEYCODE_PAGE_DOWN, KEYBOARD};
            }

            if (key.equals("end")) {
                return new int[]{KeyEvent.KEYCODE_MOVE_END, KEYBOARD};
            }

            if (key.equals("scroll_lock") || key.equals("scrlck") || key.equals("scrolllock")) {
                return new int[]{KeyEvent.KEYCODE_SCROLL_LOCK, KEYBOARD};
            }

            int x = 0;
            for (String func : func) {
                if (key.equals(func)) {
                    return new int[]{KeyEvent.KEYCODE_F1 + x, KEYBOARD};
                }
                x++;
            }


            //Xinput keybinds
            if (key.equals("xup") || key.equals("x_up") || key.equals("xbox_dpad_up") || key.equals("xbox_up") || key.equals("xu")) {
                return new int[]{ControllerPacket.UP_FLAG, XINPUT};
            }

            if (key.equals("xdown") || key.equals("x_down") || key.equals("xbox_dpad_down") || key.equals("xbox_down") || key.equals("xd")) {
                return new int[]{ControllerPacket.DOWN_FLAG, XINPUT};
            }

            if (key.equals("xleft") || key.equals("x_left") || key.equals("xbox_dpad_left") || key.equals("xbox_left") || key.equals("xl")) {
                return new int[]{ControllerPacket.LEFT_FLAG, XINPUT};
            }

            if (key.equals("xright") || key.equals("x_right") || key.equals("xbox_dpad_right") || key.equals("xbox_right") || key.equals("xr")) {
                return new int[]{ControllerPacket.RIGHT_FLAG, XINPUT};
            }

            if (key.equals("xstart") || key.equals("xbox_start") || key.equals("xboxstart")) {
                return new int[]{ControllerPacket.PLAY_FLAG, XINPUT};
            }

            if (key.equals("xback") || key.equals("xbox_back") || key.equals("xboxback")) {
                return new int[]{ControllerPacket.BACK_FLAG, XINPUT};
            }

            if (key.equals("x_xboxbutton") || key.equals("xboxbutton") || key.equals("xbox_button") || key.equals("xbox_guide")) {
                return new int[]{ControllerPacket.SPECIAL_BUTTON_FLAG, XINPUT};
            }

            if (key.equals("x_left_thumb_button") || key.equals("xbox_left_thumb_button") || key.equals("xleftthumbbutton") || key.equals("xboxleftthumbbutton") || key.equals("xthumbbutton") || key.equals("xlftthumbbtn") || key.equals("x_lft_thumb_btn") || key.equals("xbox_lft_thumb_btn") || key.equals("xboxlftthumbbtn") || key.equals("xltb")|| key.equals("xljb")) {
                return new int[]{ControllerPacket.LS_CLK_FLAG, XINPUT};
            }

            if (key.equals("x_right_thumb_button") || key.equals("xbox_right_thumb_button") || key.equals("xrightthumbbutton") || key.equals("xboxrightthumbbutton") || key.equals("xrghtthumbbtn") || key.equals("x_rght_thumb_btn") || key.equals("xbox_rght_thumb_btn") || key.equals("xboxrghtthumbbtn") || key.equals("xrtb") || key.equals("xrjb")) {
                return new int[]{ControllerPacket.RS_CLK_FLAG, XINPUT};
            }

            if (key.equals("xleftshoulder") || key.equals("xbox_left_shoulder") || key.equals("xls")) {
                return new int[]{ControllerPacket.LB_FLAG, XINPUT};
            }

            if (key.equals("xrightshoulder") || key.equals("xbox_right_shoulder") || key.equals("xrs")) {
                return new int[]{ControllerPacket.RB_FLAG, XINPUT};
            }

            if (key.equals("xleftbumper") || key.equals("xbox_left_bumper") || key.equals("xlb")) {
                return new int[]{ControllerPacket.LB_FLAG, XINPUT};
            }

            if (key.equals("xrightbumper") || key.equals("xbox_right_bumper") || key.equals("xrb")) {
                return new int[]{ControllerPacket.RB_FLAG, XINPUT};
            }

            if (key.equals("xguide") || key.equals("xbox_guide") || key.equals("xboxguide")) {
                return new int[]{ControllerPacket.SPECIAL_BUTTON_FLAG, XINPUT};
            }

            if (key.equals("xa") || key.equals("xbox_a") || key.equals("xboxa")) {
                return new int[]{ControllerPacket.A_FLAG, XINPUT};
            }

            if (key.equals("xb") || key.equals("xbox_b") || key.equals("xboxb")) {
                return new int[]{ControllerPacket.B_FLAG, XINPUT};
            }

            if (key.equals("xx") || key.equals("xbox_x") || key.equals("xboxx")) {
                return new int[]{ControllerPacket.X_FLAG, XINPUT};
            }

            if (key.equals("xy") || key.equals("xbox_y") || key.equals("xboxy")) {
                return new int[]{ControllerPacket.Y_FLAG, XINPUT};
            }

            if (key.equals("xlt") || key.equals("xbox_left_trigger")) {
                return new int[]{LEFT_TRIGGER, XINPUT};
            }

            if (key.equals("xrt") || key.equals("xbox_right_trigger")) {
                return new int[]{RIGHT_TRIGGER, XINPUT};
            }

        }

        return new int[]{-1, KEYBOARD};
    }

    public static boolean press(String key, InputConnection ic) {

        if(ic == null) { return false; }

        if (key == null || key.equals("")) {
            return false;
        }

        if (key.equals("not") || key.equals("null") || key.equals("skip") || key.equals("none")) {
            return false;
        }

        String key_corrected = key.toLowerCase();

        if (key_corrected.charAt(0) == 'x' && key_corrected.length() > 1) {
            int out = codePar(key_corrected)[1];
            if(out == -1) { return false; }

            if (out != 44 && out < xlist.length) {
                KeyEvent pn = new KeyEvent(KeyEvent.ACTION_DOWN, xlist[out]);
                pn.setSource(InputDevice.SOURCE_GAMEPAD);
                ic.sendKeyEvent(pn);
            }

        } else {

            int out = code(key_corrected);
            if(out == -1) { return false; }

            if (key.length() == 0) {
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
                return false;
            }

            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, out));
        }

        return true;
    }

    public static void release(String key, InputConnection ic) {

        if(ic == null) { return; }

        if (key == null || key.equals("")) {
            return;
        }

        //Do nothing
        if (key.startsWith("\"") && key.startsWith("\"")) {
            return;
        }

        if (key.equals("not") || key.equals("null") || key.equals("skip") || key.equals("none")) {
            return;
        }

        String key_corrected = key.toLowerCase();

        if (key_corrected.charAt(0) == 'x' && key_corrected.length() > 1) {

            int out = codePar(key_corrected)[1];
            if(out == -1) { return; }

            if (out != 44 && out < xlist.length) {
                KeyEvent pn = new KeyEvent(KeyEvent.ACTION_UP, xlist[out]);
                pn.setSource(InputDevice.SOURCE_GAMEPAD);
                ic.sendKeyEvent(pn);

            }

        } else {
            int out = code(key_corrected);
            if(out == -1) { return; }

            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, out));

        }
    }

    static String num_par = "1234567890";
    static String symb_par = "-=[]\\;'',./";

    public static int[] codePar(String key) {

        if (key.length() == 1) {

            if (alpha.contains(key)) {
                return new int[]{NORMAL, alpha.indexOf(key) + 4};
            }

            if (num_par.contains(key)) {

                return new int[]{NORMAL,  num_par.indexOf(key) + 30};
            }

            if (symb_par.contains(key)) {
                return new int[]{NORMAL, symb_par.indexOf(key) + 45};
            }

        } else {

            if (key.contains("num") && num_par.contains(Character.toString(key.charAt(3)))) {

                String s = Character.toString(key.charAt(3));
                if (num_par.contains(s))
                    return new int[]{NORMAL, num_par.indexOf(key.charAt(3)) + 89};

                return new int[]{NORMAL, -1};

            }

            if (key.contains("#")) {

                int keyCode = Integer.parseInt(key.substring(1));

                if (keyCode >= 0 && keyCode < alpha2.length) {
                    return new int[]{NORMAL, codePar(alpha2[keyCode])[1]};
                }
            }

            if (key.equals("space") || key.equals("sp")) {

                return new int[]{NORMAL, 44};
            }

            if (key.equals("tab") || key.equals("tb")) {

                return new int[]{NORMAL, 43};
            }


            if (key.equals("down")) {

                return new int[]{NORMAL, 81};
            }

            if (key.equals("up")) {

                return new int[]{NORMAL, 82};
            }

            if (key.equals("right")) {

                return new int[]{NORMAL, 79};
            }

            if (key.equals("left")) {

                return new int[]{NORMAL, 80};
            }

            if (key.equals("shift") || key.equals("left_shift") || key.equals("lshift") || key.equals("leftshift")) {

                return new int[]{SPECIAL, 1};
            }

            if (key.equals("rshift") || key.equals("right_shift") || key.equals("rightshift")) {

                return new int[]{SPECIAL, 5};
            }

            if (key.equals("enter")) {

                return new int[]{NORMAL, 40};
            }

            if (key.equals("altl") || key.equals("alternate_left") || key.equals("lalt") || key.equals("left_alt")) {

                return new int[]{SPECIAL, 2};
            }

            if (key.equals("numl") || key.equals("numlock") || key.equals("num lock")) {

                return new int[]{NORMAL, 83};
            }

            if (key.equals("altr") || key.equals("alternate_right") || key.equals("ralt") || key.equals("right_alt")) {

                return new int[]{SPECIAL, 6};
            }

            if (key.equals("gui") || key.equals("windows") || key.equals("lgui") || key.equals("left_gui") || key.equals("left_windows") ) {

                return new int[]{SPECIAL, 3};
            }

            if (key.equals("rgui") || key.equals("right_windows") || key.equals("right_gui")) {

                return new int[]{SPECIAL, 7};
            }

            if (key.equals("bksp") || key.equals("backspace") || key.equals("back space")) {

                return new int[]{NORMAL, 42};
            }

            if (key.equals("caps") || key.equals("capslock") || key.equals("caps lock") || key.equals("cap lock") || key.equals("caplock")) {

                return new int[]{NORMAL, 57};
            }

            if (key.equals("esc") || key.equals("escape")) {

                return new int[]{NORMAL, 41};
            }

            if (key.equals("lctrl") || key.equals("left ctrl") || key.equals("l ctrl") || key.equals("left control") || key.equals("ctrll") || key.equals("control left") || key.equals("ctrl left") || key.equals("ctrl") || key.equals("control")) {

                return new int[]{SPECIAL, 0};
            }

            if (key.equals("rctrl") || key.equals("right ctrl") || key.equals("r ctrl") || key.equals("right control") || key.equals("ctrlr") || key.equals("control right") || key.equals("ctrl right")) {

                return new int[]{SPECIAL, 4};
            }

            if (key.equals("home") || key.equals("hm")) {
                return new int[]{NORMAL, 231};
            }

            if (key.equals("ins")) {
                return new int[]{NORMAL, 73};
            }

            if (key.equals("delete") || key.equals("del")) {
                return new int[]{NORMAL, 76};
            }

            if (key.equals("pgup") || key.equals("pageup") || key.equals("page up") || key.equals("pg up")) {
                return new int[]{NORMAL, 75};
            }

            if (key.equals("pgdown") || key.equals("pagedown") || key.equals("page down") || key.equals("pg down")) {
                return new int[]{NORMAL, 78};
            }

            if (key.equals("end")) {
                return new int[]{NORMAL, 77};
            }

            if (key.equals("scroll lock") || key.equals("scrlck") || key.equals("scrolllock")) {
                return new int[]{NORMAL, 71};
            }

            int x = 0;
            for (String func : func) {
                if (key.equals(func)) {
                    return new int[]{NORMAL, 58 + x};
                }
                x++;
            }

                //Xinput keybinds
                if (key.equals("xup") || key.equals("x_up") || key.equals("xbox_dpad_up") || key.equals("xbox_up") || key.equals("xu")) {
                    return new int[]{NORMAL, 0};
                }

                if (key.equals("xdown") || key.equals("x_down") || key.equals("xbox_dpad_down") || key.equals("xbox_down") || key.equals("xd")) {
                    return new int[]{NORMAL, 1};
                }

                if (key.equals("xleft") || key.equals("x_left") || key.equals("xbox_dpad_left") || key.equals("xbox_left") || key.equals("xl")) {
                    return new int[]{NORMAL, 2};
                }

                if (key.equals("xright") || key.equals("x_right") || key.equals("xbox_dpad_right") || key.equals("xbox_right") || key.equals("xr")) {
                    return new int[]{NORMAL, 3};
                }

                if (key.equals("xstart") || key.equals("xbox_start") || key.equals("xboxstart")) {
                    return new int[]{NORMAL, 4};
                }

                if (key.equals("xback") || key.equals("xbox_back") || key.equals("xboxback")) {
                    return new int[]{NORMAL, 5};
                }

                if (key.equals("x_left_thumb_button") || key.equals("xbox_left_thumb_button") || key.equals("xleftthumbbutton") || key.equals("xboxleftthumbbutton") || key.equals("xthumbbutton") || key.equals("xlftthumbbtn") || key.equals("x_lft_thumb_btn") || key.equals("xbox_lft_thumb_btn") || key.equals("xboxlftthumbbtn") || key.equals("xltb") || key.equals("xlsb") || key.equals("xljb") || key.equals("xlj")) {
                    return new int[]{NORMAL, 6};
                }

                if (key.equals("x_right_thumb_button") || key.equals("xbox_right_thumb_button") || key.equals("xrightthumbbutton") || key.equals("xboxrightthumbbutton") || key.equals("xrghtthumbbtn") || key.equals("x_rght_thumb_btn") || key.equals("xbox_rght_thumb_btn") || key.equals("xboxrghtthumbbtn") || key.equals("xrtb") || key.equals("xrsb") || key.equals("xrjb") || key.equals("xrj")) {
                    return new int[]{NORMAL, 7};
                }

                if (key.equals("xleftshoulder") || key.equals("xbox_left_shoulder") || key.equals("xls") || key.equals("xlb")) {
                    return new int[]{NORMAL, 8};
                }

                if (key.equals("xrightshoulder") || key.equals("xbox_right_shoulder") || key.equals("xrs") || key.equals("xrb")) {
                    return new int[]{NORMAL, 9};
                }

                if (key.equals("xguide") || key.equals("xbox_guide") || key.equals("xboxguide")) {
                    return new int[]{NORMAL, 10};
                }

                if (key.equals("xa") || key.equals("xbox_a") || key.equals("xboxa")) {
                    return new int[]{NORMAL, 12};
                }

                if (key.equals("xb") || key.equals("xbox_b") || key.equals("xboxb")) {
                    return new int[]{NORMAL, 13};
                }

                if (key.equals("xx") || key.equals("xbox_x") || key.equals("xboxx")) {
                    return new int[]{NORMAL, 14};
                }

                if (key.equals("xy") || key.equals("xbox_y") || key.equals("xboxy")) {
                    return new int[]{NORMAL, 15};
                }

                if (key.equals("xlt")) {
                    return new int[]{NORMAL, 16};
                }

                if (key.equals("xrt")) {
                    return new int[]{NORMAL, 17};
                }

        }



                return new int[]{NORMAL, -1};
    }

    private static int code(String key) {

        if (key.length() == 1) {

            if (key.equals("`")) {
                return KeyEvent.KEYCODE_GRAVE;
            }

            if (alpha.contains(key)) {
                return alpha.indexOf(key) + 29;
            }

            if (num.contains(key)) {

                return num.indexOf(key) + 7;
            }
            //TODO test this
            if (symb.contains(key)) {
                return symb.indexOf(key) + 68;
            }

            if (key.equals(",")) {
                return KeyEvent.KEYCODE_COMMA;
            }

            if (key.equals(".")) {

                return KeyEvent.KEYCODE_PERIOD;
            }


        } else {

            if (key.contains("#")) {

                int keyCode = Integer.parseInt(key.substring(1));

                if (keyCode >= 0 && keyCode < alpha2.length) {

                    int out = code(alpha2[keyCode]);
                    Log.e("kk", alpha2[keyCode]);
                    Log.e("kk", String.valueOf(keyCode));
                    return out;

                }
            }

            if (key.contains("num") && num.contains(Character.toString(key.charAt(3)))) {

                String s = Character.toString(key.charAt(3));
                if (num.contains(s))
                    return num.indexOf(key.charAt(3)) + KeyEvent.KEYCODE_NUMPAD_0;

                return -1;

            }

            if (key.equals("space") || key.equals("sp")) {

                return KeyEvent.KEYCODE_SPACE;
            }

            if (key.equals("tab") || key.equals("tb")) {

                return KeyEvent.KEYCODE_TAB;
            }


            if (key.equals("down")) {

                return KeyEvent.KEYCODE_DPAD_DOWN;
            }

            if (key.equals("up")) {

                return KeyEvent.KEYCODE_DPAD_UP;
            }

            if (key.equals("right")) {

                return KeyEvent.KEYCODE_DPAD_RIGHT;
            }

            if (key.equals("left")) {

                return KeyEvent.KEYCODE_DPAD_LEFT;
            }

            if (key.equals("shift") || key.equals("left shift") || key.equals("lshift") || key.equals("leftshift")) {

                return KeyEvent.KEYCODE_SHIFT_LEFT;
            }

            if (key.equals("rshift") || key.equals("right shift") || key.equals("rightshift")) {

                return KeyEvent.KEYCODE_SHIFT_RIGHT;
            }

            //TODO test all these!
            if (key.equals("enter")) {

                return KeyEvent.KEYCODE_ENTER;
            }

            if (key.equals("altl") || key.equals("alternate_left") || key.equals("lalt")) {

                return KeyEvent.KEYCODE_ALT_LEFT;
            }

            if (key.equals("numl") || key.equals("numlock") || key.equals("num lock")) {

                return KeyEvent.KEYCODE_NUM_LOCK;
            }

            if (key.equals("altr") || key.equals("alternate_right") || key.equals("ralt")) {

                return KeyEvent.KEYCODE_ALT_RIGHT;
            }

            if (key.equals("bksp") || key.equals("backspace") || key.equals("back space")) {

                return KeyEvent.KEYCODE_DEL;
            }

            if (key.equals("caps") || key.equals("capslock") || key.equals("caps lock") || key.equals("cap lock") || key.equals("caplock")) {

                return KeyEvent.KEYCODE_CAPS_LOCK;
            }

            if (key.equals("fn") || key.equals("function")) {

                return KeyEvent.KEYCODE_FUNCTION;
            }

            if (key.equals("esc") || key.equals("escape")) {

                return KeyEvent.KEYCODE_ESCAPE;
            }

            if (key.equals("lctrl") || key.equals("left ctrl") || key.equals("l ctrl") || key.equals("left control") || key.equals("ctrll") || key.equals("control left") || key.equals("ctrl left") || key.equals("control") || key.equals("ctrl")) {

                return KeyEvent.KEYCODE_CTRL_LEFT;
            }

            if (key.equals("rctrl") || key.equals("right ctrl") || key.equals("r ctrl") || key.equals("right control") || key.equals("ctrlr") || key.equals("control right") || key.equals("ctrl right")) {

                return KeyEvent.KEYCODE_CTRL_RIGHT;
            }

            if (key.equals("home") || key.equals("hm")) {
                return KeyEvent.KEYCODE_HOME;
            }

            if (key.equals("ins")) {
                return KeyEvent.KEYCODE_INSERT;
            }

            if (key.equals("delete") || key.equals("del")) {
                return KeyEvent.KEYCODE_DEL;
            }

            if (key.equals("pgup") || key.equals("pageup") || key.equals("page up") || key.equals("pg up")) {
                return KeyEvent.KEYCODE_PAGE_UP;
            }

            if (key.equals("pgdown") || key.equals("pagedown") || key.equals("page down") || key.equals("pg down")) {
                return KeyEvent.KEYCODE_PAGE_UP;
            }

            if (key.equals("end")) {
                return KeyEvent.KEYCODE_MOVE_END;
            }

            if (key.equals("scroll lock") || key.equals("scrlck") || key.equals("scrolllock")) {
                return KeyEvent.KEYCODE_SCROLL_LOCK;
            }

            int x = 0;
            for (String func : func) {
                if (key.equals(func)) {
                    return KeyEvent.KEYCODE_F1 + x;
                }
                x++;
            }

        }

        return -1;
    }

    public static Command isCommand(String keyCode) {


        if(keyCode.startsWith(commandQualifier) && keyCode.endsWith(commandQualifier)) {

            String code = keyCode.replace(commandQualifier, "");

            return UserData.Commands.get(code);

        }

        return null;
    }

}
