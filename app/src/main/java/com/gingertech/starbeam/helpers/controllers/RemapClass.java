package com.gingertech.starbeam.helpers.controllers;

import static com.gingertech.starbeam.helpers.controllers.LaunchOverlayController.BUTTONS;
import static com.gingertech.starbeam.helpers.controllers.LaunchOverlayController.CONTINUOUSMOUSE;
import static com.gingertech.starbeam.helpers.controllers.LaunchOverlayController.VECTOR;

import android.util.ArraySet;
import android.util.Log;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.limelight.nvstream.input.ControllerPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

public class RemapClass {


    public enum ControllerKey {
        XBOX_A,
        XBOX_B,
        XBOX_X,
        XBOX_Y,

        XBOX_UP,
        XBOX_DOWN,
        XBOX_LEFT,
        XBOX_RIGHT,

        XBOX_RIGHT_SHOULDER,
        XBOX_RIGHT_TRIGGER,
        XBOX_LEFT_TRIGGER,
        XBOX_LEFT_SHOULDER,

        XBOX_RIGHT_JOY,
        XBOX_LEFT_JOY,
        XBOX_RIGHT_JOY_BUTTON,
        XBOX_LEFT_JOY_BUTTON,

        XBOX_START,
        XBOX_BACK,
        XBOX_GUIDE

    }

    enum AutoCorrectType {
        VECTOR,
        SCALAR
    }

    public ArrayList<Remap> assignments = new ArrayList<>();
    public HashMap<ControllerKey, Remap> assignmentsHash = new HashMap<>();


    public RemapClass() {
        assignments.clear();

        for(ControllerKey p : ControllerKey.values()) {
            assignments.add(new Remap(p));
        }

        if(assignmentsHash.isEmpty()) {
            for(Remap remap : assignments) {
                assignmentsHash.put(remap.controllerKey, remap);
            }
        }
    }

    public RemapClass(ArrayList<Remap> remaps) {
        assignments = remaps;

        for(Remap r : assignments) {
            assignmentsHash.put(r.controllerKey, r);
        }
    }

    private int xboxButtonsTemp = 0;
    public void handleButtonInputs(int xboxButtons, LaunchOverlayController.sendKeyboard connectionSend) {

        if(xboxButtonsTemp != xboxButtons) {

            for(int i = 0; i < 16; i++) {

                // If there is a button that has a different state then update it
                if(((xboxButtons >> i) == 1) ^ ((xboxButtonsTemp >> i) == 1)) {

                    ControllerKey controllerKey = packetToKey(1 << i);
                    Remap r = assignmentsHash.get(controllerKey);

                    boolean pressed = (xboxButtons >> i) == 1;

                    //Send the pressed or depressed button
                    if(r != null) {
                        connectionSend.onChange(r.controllerButtonKeybind, pressed, BUTTONS);
                        Log.i("gg", r.controllerButtonKeybind);

                    } else {
                        Log.i("gg", "not here");
                    }
                }
            }

            xboxButtonsTemp = xboxButtons;
        }
    }

    private MainActivity.vector vectorTemp = new MainActivity.vector((short) 0, (short) 0);
    public void handleTriggerInput(MainActivity.vector v, LaunchOverlayController.sendKeyboard connectionSend) {


        //Left Trigger
        if(v.x != 0 && assignmentsHash.containsKey(ControllerKey.XBOX_LEFT_TRIGGER)) {
            String keybind = assignmentsHash.get(ControllerKey.XBOX_LEFT_TRIGGER).controllerButtonKeybind;
            connectionSend.onChange(keybind, v.x < 0, BUTTONS);
        }

        //Right Trigger
        if(v.y != 0 && assignmentsHash.containsKey(ControllerKey.XBOX_RIGHT_TRIGGER)) {
            String keybind = assignmentsHash.get(ControllerKey.XBOX_RIGHT_TRIGGER).controllerButtonKeybind;
            connectionSend.onChange(keybind, v.y < 0, BUTTONS);
        }


        vectorTemp = v;
    }

    final String[] wasd = {"w", "a", "s", "d"};

    public void handleJoyInput(MainActivity.vector left, MainActivity.vector right, LaunchOverlayController.sendKeyboard connectionSend) {


        circularizedVector leftVector = new circularizedVector(left);
        circularizedVector rightVector = new circularizedVector(right);

        float mouseSensitivity = UserData.currentLayout.touchSensitivity;


        if(assignmentsHash.containsKey(ControllerKey.XBOX_LEFT_JOY)) {
            String keybind = assignmentsHash.get(ControllerKey.XBOX_LEFT_JOY).controllerButtonKeybind;

            if(keybind.equals("w:a:s:d")) {
                wasdControl(leftVector, connectionSend);
            } else if(keybind.equals("mouse")) {

                ButtonID.Vector vector = new ButtonID.Vector(leftVector.x * mouseSensitivity / 1000, -leftVector.y * mouseSensitivity / 1000);

                connectionSend.onChange(keybind, vector, CONTINUOUSMOUSE);
            } else if(keybind.equals("xbox_left_joystick") || keybind.equals("xbox_right_joystick")) {
                ButtonID.Vector vector = new ButtonID.Vector(leftVector.x, leftVector.y);
                connectionSend.onChange(keybind, vector, VECTOR);
            }
        }

        if(assignmentsHash.containsKey(ControllerKey.XBOX_RIGHT_JOY)) {
            String keybind = assignmentsHash.get(ControllerKey.XBOX_RIGHT_JOY).controllerButtonKeybind;

            if(keybind.equals("w:a:s:d")) {
                wasdControl(rightVector, connectionSend);
            } else if(keybind.equals("mouse")) {

                ButtonID.Vector vector = new ButtonID.Vector(rightVector.x * mouseSensitivity / 1000, -rightVector.y * mouseSensitivity / 1000);

                connectionSend.onChange(keybind, vector, CONTINUOUSMOUSE);
            } else if(keybind.equals("xbox_left_joystick") || keybind.equals("xbox_right_joystick")) {
                ButtonID.Vector vector = new ButtonID.Vector(rightVector.x, rightVector.y);
                connectionSend.onChange(keybind, vector, VECTOR);
            }
        }


    }

    private void wasdControl(circularizedVector circularizedVector, LaunchOverlayController.sendKeyboard connectionSend) {
        if(circularizedVector.magPer > 0.5) {

            String[] PressedGroup = new String[2];
            int count = 0;

            if (circularizedVector.angle > Math.PI + Math.PI / 8 && circularizedVector.angle < Math.PI * 2 - Math.PI / 8) {
                PressedGroup[count] = "w";
                count += 1;
            }
            if (circularizedVector.angle > Math.PI / 2 + Math.PI / 8 && circularizedVector.angle < 3 * Math.PI / 2 - Math.PI / 8) {
                PressedGroup[count] = "a";
                count += 1;
            }
            if (circularizedVector.angle < Math.PI - Math.PI / 8 && circularizedVector.angle > Math.PI / 8) {
                PressedGroup[count] = "s";
                count += 1;
            }
            if (circularizedVector.angle > 3 * Math.PI / 2 + Math.PI / 8 || circularizedVector.angle < Math.PI / 2 - Math.PI / 8) {
                PressedGroup[count] = "d";
                count += 1;
            }

            if(count == 0) {
                for(String v : wasd) {
                    connectionSend.onChange(v, false, BUTTONS);
                }
            } else {

                for(String v : wasd) {
                    connectionSend.onChange(v, Arrays.asList(PressedGroup).contains(v), BUTTONS);
                }
            }
        } else {
            for(String v : wasd) {
                connectionSend.onChange(v, false, BUTTONS);
            }
        }
    }

    private class vector {
        int x = 0;
        int y = 0;

        public vector(MainActivity.vector vector) {
            this.x = vector.x < 0 ? vector.x + 256 : vector.x;
            this.y = vector.y < 0 ? vector.y + 256 : vector.y;
        }
    }

    private class circularizedVector {
        int x = 0;
        int y = 0;

        double angle = 0;

        double magPer = 0;

        public circularizedVector(MainActivity.vector vector) {
            angle = Math.acos((32767 * vector.x) / (32767 * Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2))));

            if(vector.y < 0) {
                angle = 2 * Math.PI - angle;
            }

            double mag = Math.max(Math.abs(vector.x), Math.abs(vector.y));

            magPer = mag / 32767;

            x = (int) Math.floor(mag * Math.cos(angle));
            y = (int) Math.floor(mag * Math.sin(angle));

            angle = 2 * Math.PI - angle;
        }
    }

    private ControllerKey packetToKey(int controllerPacket) {

        Log.i("gg", String.valueOf(controllerPacket));

        switch (controllerPacket) {

            case ControllerPacket.UP_FLAG:
                return ControllerKey.XBOX_UP;

            case ControllerPacket.LS_CLK_FLAG:
                return ControllerKey.XBOX_LEFT_JOY_BUTTON;

            case ControllerPacket.RS_CLK_FLAG:
                return ControllerKey.XBOX_RIGHT_JOY_BUTTON;

            case ControllerPacket.DOWN_FLAG:
                return ControllerKey.XBOX_DOWN;

            case ControllerPacket.LEFT_FLAG:
                return ControllerKey.XBOX_LEFT;

            case ControllerPacket.RIGHT_FLAG:
                return ControllerKey.XBOX_RIGHT;

            case ControllerPacket.A_FLAG:
                return ControllerKey.XBOX_A;

            case ControllerPacket.B_FLAG:
                return ControllerKey.XBOX_B;

            case ControllerPacket.X_FLAG:
                return ControllerKey.XBOX_X;

            //This is the Y button
            case 0x8000:
                return ControllerKey.XBOX_Y;

            case ControllerPacket.BACK_FLAG:
                return ControllerKey.XBOX_BACK;

            case ControllerPacket.SPECIAL_BUTTON_FLAG:
                return ControllerKey.XBOX_GUIDE;

            case ControllerPacket.PLAY_FLAG:
                return ControllerKey.XBOX_START;

            case ControllerPacket.LB_FLAG:
                return ControllerKey.XBOX_LEFT_SHOULDER;

            case ControllerPacket.RB_FLAG:
                return ControllerKey.XBOX_RIGHT_SHOULDER;

            default:
                return ControllerKey.XBOX_UP;

        }
    }
}


