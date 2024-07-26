package com.gingertech.starbeam.helpers;

import android.content.Context;
import android.graphics.Color;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.controllers.ButtonID;
import com.gingertech.starbeam.helpers.controllers.ButtonID_data;

import java.util.ArrayList;
import java.util.Objects;

public class LayoutClass {

    public String name = "";
    public String description = "Default Controller Layout";

    public boolean isImported = false;

    //When changing name and description keep a reference of the old values
    public String oldName = "";
    public String oldDescription = "";
    public boolean gyroActivated = false;
    public boolean isMouseClickEnabled = true;
    public float gyroSensitivity = 0.5f;
    public float gyroThreshold = 0.5f;

    public float touchSensitivity = 0.5f;
    public float touchSensitivityRef = 0;
    public boolean invertGyro = false;

    public String layoutUUID = "";
    public String ownerUUID = "";
    public String ownerName = "";

    public boolean isTouchTrackpad = true;
    public boolean isGyroMouse = true;
    public boolean isTouchMouse = true;

    public boolean isSplit = false;
    public boolean isSplitSwapped = false;

    public boolean exists = true;

    public String leftTrackpadKeybind = "mouse";
    public String rightTrackpadKeybind = "mouse";

    public boolean invertGyroForOri = false;
    public boolean isLayoutInGroup = false;
    public String groupID = "";

    public ArrayList<ButtonID_data> buttons = new ArrayList<>();


    // -1 = unordered

    public int index = -1;
    public int groupIndex = -1;

    public LayoutClass() {

    }

    public LayoutClass(String name, Context context) {
        this.name = name;

        UpdateOldNames();

        get(context);
    }

    public void copyLayout(LayoutClass layoutClass) {
        this.name = layoutClass.name + "~";
        this.description = layoutClass.description + " copy";
        copySettings(layoutClass);
    }

    private void copySettings(LayoutClass layoutClass) {
        this.gyroActivated = layoutClass.gyroActivated;
        this.buttons = layoutClass.buttons;
        this.gyroSensitivity = layoutClass.gyroSensitivity;
        this.gyroThreshold = layoutClass.gyroThreshold;

        this.isTouchTrackpad = layoutClass.isTouchTrackpad;
        this.isGyroMouse = layoutClass.isGyroMouse;
        this.touchSensitivity = layoutClass.touchSensitivity;
        this.touchSensitivityRef = layoutClass.touchSensitivity;

        this.leftTrackpadKeybind = layoutClass.leftTrackpadKeybind;
        this.rightTrackpadKeybind = layoutClass.rightTrackpadKeybind;

        this.isSplit = layoutClass.isSplit;
        this.isSplitSwapped = layoutClass.isSplitSwapped;
        this.invertGyro = layoutClass.invertGyro;

        this.isLayoutInGroup = layoutClass.isLayoutInGroup;
        this.groupID = layoutClass.groupID;

        UpdateOldNames();
    }

    public LayoutClass(String name, String description, Context context) {
        this.name = name;
        this.description = description;

        UpdateOldNames();

        get(context);
    }

    public void UpdateOldNames() {
        this.oldName = this.name;
        this.oldDescription = this.description;
    }

    public void addToGroup(LayoutGroupClass layoutGroupClass) {
        this.groupID = layoutGroupClass.groupID;
        this.isLayoutInGroup = true;
    }

    public void save(Context context) {

        SaveClass.SaveData(this, context);
        SaveClass.SaveLayouts(context);

        if(!oldName.equals(name) || !oldDescription.equals(description)) {
            UserData.Layouts.remove(UserData.currentLayout);

            UserData.Layouts.add(this);
            SaveClass.SaveData(this, context);
            SaveClass.SaveLayouts(context);
        }

        UserData.setCurrentLayout(context, this);
    }

    public void resetDefaults(Context c) {
        if(Objects.equals(this.name, "Default")) {
            description = c.getString(R.string.default_keyboard_layout);

            makeDefaultLayout(c);
        }
//        else if(Objects.equals(this.name, "Xbox")) {
//
//            description = c.getString(R.string.default_xbox_controller_layout);
//
//            makeXboxDefaultLayout(c);
//        }
    }

    public void makeDefaultLayout(Context c) {

        isSplit = false;
        isTouchMouse = true;
        gyroActivated = false;
        isTouchTrackpad = true;

        buttons.clear();

        int[] displaySize = ScreenSizeHelper.getScreenSize(c);
        int borderMargin = 100;

        ButtonID_data joyButton = new ButtonID_data();
        joyButton.addNewButton(ButtonID.Joy, c);

        joyButton.setSize((int) (displaySize[1] * 0.4));
        joyButton.setPosition(borderMargin, displaySize[1] - joyButton.size - borderMargin);
        joyButton.setKeybind("w:a:s:d");

        joyButton.joystickTouchActivate = true;

        buttons.add(joyButton);

        ButtonID_data jumpButton = new ButtonID_data();
        jumpButton.addNewButton(ButtonID.Normal, c);

        jumpButton.setSize((int) (displaySize[1] * 0.4));
        jumpButton.setPosition(displaySize[0] - jumpButton.size, displaySize[1] - jumpButton.size - borderMargin);
        jumpButton.setKeybind("space");

        buttons.add(jumpButton);

        ButtonID_data escButton = new ButtonID_data();
        escButton.addNewButton(ButtonID.Normal, c);

        escButton.setSize((int) (displaySize[1] * 0.15));
        escButton.setPosition(joyButton.x + joyButton.size / 2 - escButton.size / 2, joyButton.y - escButton.size - borderMargin);
        escButton.setKeybind("esc");

//        buttons.add(escButton);

        ButtonID_data eButton = new ButtonID_data();
        eButton.addNewButton(ButtonID.Normal, c);

        eButton.setSize((int) (displaySize[1] * 0.15));
        eButton.setPosition(joyButton.x + 3 * joyButton.size / 2 - eButton.size, joyButton.y + joyButton.size / 2 - eButton.size / 2 );
        eButton.setKeybind("e");

//        buttons.add(eButton);

        ButtonID_data qButton = new ButtonID_data();
        qButton.addNewButton(ButtonID.Normal, c);

        qButton.setSize((int) (displaySize[1] * 0.15));
        qButton.setPosition((int) ((joyButton.x + 3 * joyButton.size / 2 - qButton.size) * 0.8f), (int) ((joyButton.y - qButton.size - borderMargin) * 1.3f));
        qButton.setKeybind("q");

//        buttons.add(qButton);

        ButtonID_data cButton = new ButtonID_data();
        cButton.addNewButton(ButtonID.Normal, c);

        cButton.setSize((int) (displaySize[1] * 0.15));
        cButton.setPosition(jumpButton.x + jumpButton.size / 2 -  cButton.size / 2, joyButton.y - cButton.size - borderMargin);
        cButton.setKeybind("rclick");

        buttons.add(cButton);

        ButtonID_data shiftButton = new ButtonID_data();
        shiftButton.addNewButton(ButtonID.Sticky, c);

        shiftButton.setSize((int) (displaySize[1] * 0.15));
        shiftButton.setPosition((int) ((jumpButton.x + 3 * jumpButton.size / 2 - jumpButton.size) * 0.8f), (int) ((jumpButton.y - shiftButton.size - borderMargin) * 1.3f));
        shiftButton.setKeybind("shift");

        buttons.add(shiftButton);

        ButtonID_data cycleButton = new ButtonID_data();
        cycleButton.addNewButton(ButtonID.Scroll, c);

        cycleButton.setSize((int) (displaySize[1] * 0.2));
        cycleButton.setPosition(displaySize[0] / 2 - cycleButton.size / 2, displaySize[1] - cycleButton.size - borderMargin);
        cycleButton.setKeybind("1:2:3:4:5:6:7:8:9");

        buttons.add(cycleButton);
    }

    public void makeXboxDefaultLayout(Context c) {

        buttons.clear();

        isSplit = false;
        isTouchMouse = true;
        gyroActivated = false;
        isTouchTrackpad = true;

        int[] displaySize = ScreenSizeHelper.getScreenSize(c);
        int borderMargin = 100;

        ButtonID_data rjs = new ButtonID_data();
        rjs.addNewButton(ButtonID.Joy, c);

        rjs.setSize((int) (displaySize[1] * 0.3));
        rjs.setPosition((int) ((displaySize[0] - rjs.size) * 0.8), displaySize[1] - rjs.size - borderMargin);
        rjs.setKeybind("xbox_right_joystick");

        buttons.add(rjs);

        ButtonID_data ljs = new ButtonID_data();
        ljs.addNewButton(ButtonID.Joy, c);

        ljs.setSize((int) (displaySize[1] * 0.3));
        ljs.setPosition(borderMargin, rjs.size);
        ljs.setKeybind("xbox_left_joystick");

        buttons.add(ljs);

        ArrayList<ButtonID_data> b = makeButtons(c, (int) ((displaySize[0] - displaySize[1] * 0.3) * 0.3), (int) (displaySize[1] - displaySize[1] * 0.3 - borderMargin + displaySize[1] * 0.3 / 2 - 40), 0, (int) (displaySize[1] * 0.15), new String[]{"xbox_up", "xbox_down", "xbox_right", "xbox_left"}, null);
        buttons.addAll(b);

        ArrayList<ButtonID_data> b1 = makeButtons(c, (int) (displaySize[0] - (displaySize[1] * 0.15)), (int) (displaySize[1] - displaySize[1] * 0.3 - borderMargin - borderMargin * 2 - 20), 0, (int) (displaySize[1] * 0.15), new String[]{"xbox_y", "xbox_a", "xbox_x", "xbox_b"}, new int[]{Color.YELLOW, Color.GREEN, Color.BLUE, Color.RED});
        buttons.addAll(b1);

        ButtonID_data xlt = new ButtonID_data();
        xlt.addNewButton(ButtonID.Normal, c);

        xlt.setSize((int) (displaySize[1] * 0.3));
        xlt.setPosition((int) (displaySize[1] * 0.4), borderMargin);
        xlt.setKeybind("xbox_left_trigger");

        buttons.add(xlt);

        ButtonID_data xrt = new ButtonID_data();
        xrt.addNewButton(ButtonID.Normal, c);

        xrt.setSize((int) (displaySize[1] * 0.3));
        xrt.setPosition((int) (displaySize[0] / 2 + displaySize[1] * 0.3) + borderMargin, borderMargin);
        xrt.setKeybind("xbox_right_trigger");

        buttons.add(xrt);

        ButtonID_data xrb = new ButtonID_data();
        xrb.addNewButton(ButtonID.Normal, c);

        xrb.setSize((int) (displaySize[1] * 0.15));
        xrb.setPosition((int) (displaySize[0] / 2 + displaySize[1] * 0.3 + borderMargin), borderMargin + xrt.size);
        xrb.setKeybind("xbox_right_bumper");

        buttons.add(xrb);

        ButtonID_data xlb = new ButtonID_data();
        xlb.addNewButton(ButtonID.Normal, c);

        xlb.setSize((int) (displaySize[1] * 0.15));
        xlb.setPosition((int) (xlt.x + xlt.size - displaySize[1] * 0.15), borderMargin + xrt.size);
        xlb.setKeybind("xbox_left_bumper");

        buttons.add(xlb);

    }



    public ArrayList<ButtonID_data> makeButtons(Context c, int x, int y, int spacing, int size, String[] values, int[] colors) {
        ArrayList<ButtonID_data> buttons = new ArrayList<>();
        int[] positionsX = {0, 0, 1, -1}; // X positions for up, down, right, and left
        int[] positionsY = {-1, 1, 0, 0}; // Y positions for up, down, right, and left

        for (int i = 0; i < values.length; i++) {
            ButtonID_data button = new ButtonID_data();
            button.addNewButton(ButtonID.Normal, c);
            button.setSize(size);
            button.setPosition((int) Math.floor(x + size/1.2 * positionsX[i] - size / 2f), (int) Math.floor(y + size/1.2 * positionsY[i] - size / 2f));
            button.setKeybind(values[i]);
            if (colors != null) {
                button.setColor(colors[i]);
            }
            buttons.add(button);
        }
        return buttons;
    }

    public void get(Context context) {
        ArrayList<ButtonID_data> b = SaveClass.ReadData(this, context);
         copySettings(SaveClass.ReadSettings(this, context));

        if(b == null) {
            exists = false;

            resetDefaults(context);
            return;
        }

        exists = true;
        buttons = b;
    }

    public LayoutGroupClass getGroup() {
        if (UserData.LayoutGroups.stream().noneMatch(s -> s.groupID.equals(this.groupID))) {
            return null;
        }

        return UserData.LayoutGroups.stream().filter(s -> s.groupID.equals(this.groupID)).findFirst().get();
    }

    public void removeFromGroup(Context context) {
        LayoutGroupClass groupClass = this.getGroup();

        if(groupClass != null) {
            this.isLayoutInGroup = false;
            this.groupID = "";

            this.save(context);
            groupClass.layoutClasses.remove(this);
            groupClass.save(context);
        }
    }
}
