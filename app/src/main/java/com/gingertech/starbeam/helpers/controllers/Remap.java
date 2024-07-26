package com.gingertech.starbeam.helpers.controllers;

import com.gingertech.starbeam.helpers.ButtonHandler;

import java.util.Arrays;

public class Remap {

    public RemapClass.ControllerKey controllerKey;
    RemapClass.AutoCorrectType correctType = RemapClass.AutoCorrectType.SCALAR;
    public String controllerButtonKeybind;

    public Remap(RemapClass.ControllerKey controllerKey, String value) {
        this.controllerKey = controllerKey;
        this.controllerButtonKeybind = value;

        if(Arrays.asList(ButtonHandler.keycodesForJoy).contains(value)) {
            correctType = RemapClass.AutoCorrectType.VECTOR;
        }
    }

    public Remap(RemapClass.ControllerKey controllerKey) {
        this.controllerKey = controllerKey;

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_A)) { controllerButtonKeybind = "xbox_a"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_B)) { controllerButtonKeybind = "xbox_b"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_X)) { controllerButtonKeybind = "xbox_x"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_Y)) { controllerButtonKeybind = "xbox_y"; }

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_UP)) { controllerButtonKeybind = "xbox_up"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_DOWN)) { controllerButtonKeybind = "xbox_down"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_LEFT)) { controllerButtonKeybind = "xbox_left"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_RIGHT)) { controllerButtonKeybind = "xbox_right"; }

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_LEFT_JOY)) { controllerButtonKeybind = "xbox_left_joystick"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_RIGHT_JOY)) { controllerButtonKeybind = "xbox_right_joystick"; }

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_LEFT_JOY_BUTTON)) { controllerButtonKeybind = "x_left_thumb_button"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_RIGHT_JOY_BUTTON)) { controllerButtonKeybind = "x_right_thumb_button"; }

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_RIGHT_SHOULDER)) { controllerButtonKeybind = "xbox_right_shoulder"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_RIGHT_TRIGGER)) { controllerButtonKeybind = "xbox_right_trigger"; }

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_LEFT_SHOULDER)) { controllerButtonKeybind = "xbox_left_shoulder"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_LEFT_TRIGGER)) { controllerButtonKeybind = "xbox_left_trigger"; }

        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_START)) { controllerButtonKeybind = "xbox_start"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_BACK)) { controllerButtonKeybind = "xbox_back"; }
        if(controllerKey.equals(RemapClass.ControllerKey.XBOX_GUIDE)) { controllerButtonKeybind = "xbox_guide"; }

        switch (controllerKey) {
            case XBOX_LEFT_JOY:
            case XBOX_RIGHT_JOY:
                correctType = RemapClass.AutoCorrectType.VECTOR;
        }

    }

}
