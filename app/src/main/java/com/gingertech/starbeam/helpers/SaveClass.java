package com.gingertech.starbeam.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.ArraySet;
import android.util.Log;

import com.gingertech.starbeam.helpers.controllers.ButtonID_data;
import com.gingertech.starbeam.helpers.controllers.LayoutGroupView;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.Remap;
import com.gingertech.starbeam.helpers.controllers.RemapClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

public class SaveClass {

    static String filename = "39r8ybciu1oni0ev2g979ebau1w";
    private static final char[] alphabet = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    int index = 0;

    public static void GetOpenCount(Context context) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        UserData.openCount = sp.getInt("openCount", 0);

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putInt("openCount", UserData.openCount + 1);
        editor.apply();

        if(UserData.openCount == 0) {

            // Get the current date and time
            Date currentDate = new Date();

            // Define the date and time format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            UserData.firstOpenedTime = dateFormat.format(currentDate);

            // Save first opened time
            editor.putString("firstOpenedTime", UserData.firstOpenedTime);

        }

    }

    public static void DeleteLayout(LayoutClass layoutClass, Context context) {
        String currentLayoutName = layoutClass.name;

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        int number = sp.getInt(currentLayoutName + "number_buttons", 0);

        editor.remove(currentLayoutName + "number_buttons");
        editor.remove(currentLayoutName + "gyro");
        editor.remove(currentLayoutName + "touchTrack");
        editor.remove(currentLayoutName + "gyroSense");
        editor.remove(currentLayoutName + "touchSense");
        editor.remove(currentLayoutName + "touchMouse");

        editor.remove(currentLayoutName + "isSplit");
        editor.remove(currentLayoutName + "isSplitSwapped");
        editor.remove(currentLayoutName + "isMouseClickEnabled");


        if(number > 0) {

            for (int x = 0; x < number; x++) {

                ButtonID_data b = layoutClass.buttons.get(x);
                editor.remove(currentLayoutName + x + "xpos");
                editor.remove(currentLayoutName + x + "ypos");
                editor.remove(currentLayoutName + x + "size");
                editor.remove(currentLayoutName + x + "str");
                editor.remove(currentLayoutName + x + "type");
                editor.remove(currentLayoutName + x + "color");
                editor.remove(currentLayoutName + x + "tint");
                editor.remove(currentLayoutName + x + "sensitivity");
                editor.remove(currentLayoutName + x + "ts");
                editor.remove(currentLayoutName + x + "image");
                editor.remove(currentLayoutName + x + "alpha");
                editor.remove(currentLayoutName + x + "showName");
                editor.remove(currentLayoutName + x + "buttonName");
                editor.remove(currentLayoutName + x + "haptic");

            }
        }

        editor.apply();
    }

    public static void GetTests(Context context) {
        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
//        UserData.OpeningTest_2 = sp.getBoolean("OpeningTest_2", new Random().nextInt(2) == 0);
//        UserData.OpeningTest_2 = sp.getBoolean("OpeningTest_2", false);

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
//        editor.putBoolean("OpeningTest_2", UserData.OpeningTest_2);
        editor.apply();
    }

    public static void GetHasReviewed(Context context) {

        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        UserData.hasReviewed = sp.getBoolean("hasReviewed", false);

    }

    public static void SaveHasReviewed(Context context) {

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        UserData.hasReviewed = true;

        editor.putBoolean("hasReviewed", true);

        editor.apply();
    }

    public static void GetFlags(Context context) {

        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        UserData.layoutsEdited = sp.getInt("layoutsEdited", 0);
        UserData.layoutsCreated = sp.getInt("layoutsCreated", 0);
        UserData.timesConnected = sp.getInt("timesConnected", 0);
        UserData.timesConnectedBT = sp.getInt("timesConnectedBT", 0);

        UserData.firstOpenedTime = sp.getString("firstOpenedTime", "");

        UserData.wasPremium = sp.getBoolean("wasPremium", false);
        UserData.isNativeMode = sp.getBoolean("nativeMode", true);
    }

    public static void SaveFlags(Context context) {

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        UserData.hasReviewed = true;

        editor.putInt("layoutsEdited", UserData.layoutsEdited);
        editor.putInt("layoutsCreated", UserData.layoutsCreated);
        editor.putInt("timesConnected", UserData.timesConnected);
        editor.putInt("timesConnectedBT", UserData.timesConnectedBT);

        editor.putBoolean("wasPremium", UserData.wasPremium);
        editor.putBoolean("nativeMode", UserData.isNativeMode);
        editor.putInt("playmode", UserData.PlayMode);

        editor.apply();
    }


    public static void GetCurrentLayout(Context context) {

        SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        UserData.currentLayoutName = sp.getString("currentLayout", "Default");

    }

    public static void SaveCurrentLayout(Context context) {

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        UserData.currentLayoutName = UserData.currentLayout.name;

        editor.putString("currentLayout", UserData.currentLayoutName);

        MixPanel.mpEventTracking(context, "saved_current_layout", null);

        editor.apply();
    }

    public static void ResetCurrentLayout(Context context) {

        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        UserData.currentLayoutName = "Default";

        editor.putString("currentLayout", UserData.currentLayoutName);

        MixPanel.mpEventTracking(context, "reset_current_layout", null);

        editor.apply();
    }



    public static void SaveLayouts(Context c){

        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        ArraySet<String> s = new ArraySet<>();

        for(LayoutClass layouts : UserData.Layouts) {
            s.add(layouts.name + "," + layouts.description);
        }

        editor.putStringSet("SavedGamePads", s);
        editor.apply();
    }

    public static void SaveCommands(Context c){

        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        ArraySet<String> s = new ArraySet<>();

        for(Command command : UserData.Commands.values()) {
            s.add(command.UUID);
        }

        editor.putStringSet("SavedCommands", s);
        editor.apply();
    }

    public static void SaveRemap(Context c, RemapClass remapClass) {
        HashMap<String, String> data = new HashMap<>();

        for(Remap r : remapClass.assignments) {
            data.put(r.controllerKey.name(), r.controllerButtonKeybind);
        }

        SaveMap(c, "remap", data);
    }

    public static RemapClass GetRemap(Context c) {
        HashMap<String, String> data = GetMap(c, "remap");

        RemapClass remapClass = new RemapClass();

        for(String d : data.keySet()) {
            Remap remap = remapClass.assignmentsHash.get(RemapClass.ControllerKey.valueOf(d));
            if(remap != null) {
                remapClass.assignments.remove(remap);

                remap.controllerButtonKeybind = data.get(d);
                remapClass.assignmentsHash.put(remap.controllerKey, remap);

                remapClass.assignments.add(remap);
            }
        }

        return remapClass;
    }

    private static String replaceCommandWithUUID(Context c, Command command) {

        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        String code = sp.getString("SavedCommands" + command.name + "," + command.description, "");

        editor.remove("SavedCommands" + command.name + "," + command.description);

        editor.apply();

        String commandUUID = UUID.randomUUID().toString();

        if (!code.equals("")) {

            HashMap<String, String> data = new HashMap<>();

            data.put("name", command.name);
            data.put("code", code);
            data.put("description", command.description);
            data.put("isExternal", command.isDownloaded ? "1" : "0");

            SaveMap(c, commandUUID, data);
        }

        return commandUUID;

    }

    public static void updateAllCommands(Context c) {

        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        Set<String> commandNames = sp.getStringSet("SavedCommands", Collections.emptySet());
        Set<String> commandUUIDs = new HashSet<>();

        for(String names : commandNames) {
            if(names == null) { continue; }
            int comma = names.indexOf(",");

//            If the given code "name" is a UUID or old ref system
            if(comma == -1) {
                commandUUIDs.add(names);
                continue;
            }

            String name = names.substring(0, comma);
            String description = names.substring(comma + 1);

            commandUUIDs.add(replaceCommandWithUUID(c, new Command(name, description)));
        }

        editor.putStringSet("SavedCommands", commandUUIDs);

    }

    public static void GetCommands(Context c) {

        updateAllCommands(c);

        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        Set<String> commandUUIDs = sp.getStringSet("SavedCommands", Collections.emptySet());

        UserData.Commands.clear();

        for(String ids : commandUUIDs) {
            Command newCommand = new Command(c, ids);

            UserData.Commands.put(newCommand.name, newCommand);
        }
    }

    public static void DeleteCommand(Context c, Command command) {
        DeleteMap(c, command.UUID);

        UserData.Commands.remove(command.name);
        SaveCommands(c);
    }

    public static void SaveCommand(Context c, Command command) {

        HashMap<String, String> data = new HashMap<>();
        data.put("name", command.name);
        data.put("code", command.code);
        data.put("description", command.description);
        data.put("isExternal", command.isDownloaded ? "1" : "0");

        SaveMap(c, command.UUID, data);
        GetCommands(c);
    }

    public static HashMap<String, String> GetCommand(Context c, String UUID) {

        return GetMap(c, UUID);
    }

    public static void SaveBluetooth(Context c, String bluetoothAddr) {
        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        editor.putString("defaultBluetooth", bluetoothAddr);

        UserData.defaultBluetoothAddr = bluetoothAddr;

        editor.apply();
    }

    public static void GetBluetooth(Context c) {
        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        UserData.defaultBluetoothAddr = sp.getString("defaultBluetooth", "");
    }


    private static void SaveMap(Context c, String key, HashMap<String, String> values) {

        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        editor.putStringSet(key, values.keySet());

        for(Map.Entry<String, String> item : values.entrySet()) {
            editor.putString(key + "," + item.getKey(), item.getValue());
        }

        editor.apply();

    }

    private static void DeleteMap(Context c, String key) {

        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        Set<String> values = sp.getStringSet(key, Collections.emptySet());

        for(String item : values) {
            editor.remove(key + "," + item);
        }

        editor.remove(key);

        editor.apply();

    }

    private static HashMap<String, String> GetMap(Context c, String key) {

        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        HashMap<String, String> output = new HashMap<>();
        Set<String> values = sp.getStringSet(key, Collections.emptySet());

        for(String item : values) {
            output.put(item, sp.getString(key + "," + item, ""));
        }

        return output;
    }

    public static void saveFullscreenIcon(Context c, List<Integer> v) {
        if(v.size() != 2) { return; }

        HashMap<String, String> pos = new HashMap<>();

        pos.put("x", v.get(0).toString());
        pos.put("y", v.get(1).toString());

        SaveMap(c, "fullscreenIconLoc", pos);
    }

    public static List<Integer> getFullscreenIcon(Context c) {

        HashMap<String, String> pos = GetMap(c, "fullscreenIconLoc");
        List<Integer> v = new ArrayList<>();

        String xloc = pos.get("x");
        if(xloc != null) {
            if (!Objects.equals(xloc, "")) {
                v.add(Integer.parseInt(xloc));
            } else {
                v.add(-1);
            }
        } else {
            v.add(-1);
        }

        String yloc = pos.get("y");
        if(yloc != null) {
            if (!Objects.equals(yloc, "")) {
                v.add(Integer.parseInt(yloc));
            } else {
                v.add(-1);
            }
        } else {
            v.add(-1);
        }

        return v;
    }

    public static ArrayList<LayoutClass> GetGampads(Context c){

        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        Set<String> layoutNames = sp.getStringSet("SavedGamePads", Collections.emptySet());

        ArrayList<LayoutClass> s = new ArrayList<>();

        if(layoutNames.size() == 0) {

            LayoutClass defaultlayout = new LayoutClass("Default", c);
            s.add(defaultlayout);
//            s.add(new LayoutClass("Xbox", c));

            SaveLayouts(c);

            UserData.currentLayout = defaultlayout;

        } else {

            for (String title : layoutNames) {

                String[] strings = title.split(",");

                LayoutClass layout = new LayoutClass(strings[0], strings[1], c);
                layout.gyroActivated = sp.getBoolean(strings[0] + "gyro", false);
                layout.gyroSensitivity = sp.getFloat(strings[0] + "gyroSense", 0.5f);
                layout.gyroThreshold = sp.getFloat(strings[0] + "gyroThresh", 0.5f);

                layout.touchSensitivity = sp.getFloat(strings[0] + "touchSense", 0.5f);
                layout.isTouchMouse = sp.getBoolean(strings[0] + "touchMouse", true);

                layout.isTouchTrackpad = sp.getBoolean(strings[0] + "touchTrack", true);
                layout.isGyroMouse = sp.getBoolean(strings[0] + "gyroMouse", true);

                layout.isSplit = sp.getBoolean(strings[0] + "isSplit", false);
                layout.isSplitSwapped = sp.getBoolean(strings[0] + "isSplitSwapped", false);

                layout.isMouseClickEnabled =  sp.getBoolean(strings[0] + "isMouseClickEnabled", true);

                layout.isLayoutInGroup =  sp.getBoolean(strings[0] + "isLayoutInGroup", false);
                layout.groupID =  sp.getString(strings[0] + "groupID", "");

                s.add(layout);

                if (UserData.currentLayoutName.equals(strings[0])) {
                    UserData.currentLayout = layout;
                }
            }
        }

        return s;
    }

    public static void SaveLayoutGroups(Context c) {
        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        ArraySet<String> ids = new ArraySet<>();
        for(LayoutGroupClass group : UserData.LayoutGroups) {

            Log.i("group", group.groupID);

            ids.add(group.groupID);
        }

        editor.putStringSet("layoutGroups", ids);
        editor.apply();

    }

    public static void GetLayoutSaveGroups(Context c) {
        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        Set<String> layoutGroupIDs = sp.getStringSet("layoutGroups", new ArraySet<>());
        for(String layoutGroupID : layoutGroupIDs) {

            if(!layoutGroupID.equals("")) {
                UserData.LayoutGroups.add(GetLayoutGroup(c, UUID.fromString(layoutGroupID)));
            }
        }
    }

    public static void SaveLayoutGroup(Context c, LayoutGroupClass layoutGroupClass) {
        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        editor.putString(layoutGroupClass.groupID + "name", layoutGroupClass.name);
        editor.putString(layoutGroupClass.groupID + "id", layoutGroupClass.groupID);

        editor.putString(layoutGroupClass.groupID + "description", layoutGroupClass.description);
        editor.putStringSet(layoutGroupClass.groupID + "layouts", layoutGroupClass.layoutClasses.stream().map(s -> s.name).collect(Collectors.toSet()));

        if(!UserData.LayoutGroups.stream().anyMatch(s -> s.groupID.equals(layoutGroupClass.groupID))) {
            UserData.LayoutGroups.add(layoutGroupClass);
        }


        SaveClass.SaveLayoutGroups(c);

        editor.apply();
    }

    public static LayoutGroupClass GetLayoutGroup(Context c, UUID layoutID) {
        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        String layoutIDString = layoutID.toString();

        String groupName = sp.getString(layoutIDString + "name", "");
        String groupID = sp.getString(layoutIDString + "id", "");
        String groupDescription = sp.getString(layoutIDString + "description", "");
        Set<String> groupLayouts = sp.getStringSet(layoutIDString + "layouts", new ArraySet<>());

        return new LayoutGroupClass(groupName, groupDescription, groupLayouts, groupID);
    }

    public static void SaveData(LayoutClass layoutClass, Context c) {
        SharedPreferences.Editor editor = c.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();

        String currentLayoutName = layoutClass.name;

        int number = layoutClass.buttons.size();
        editor.putInt(currentLayoutName + "number_buttons", number);

        editor.putBoolean(currentLayoutName + "gyro", layoutClass.gyroActivated);
        editor.putFloat(currentLayoutName + "gyroSense", layoutClass.gyroSensitivity);
        editor.putFloat(currentLayoutName + "gyroThresh", layoutClass.gyroThreshold);

        editor.putBoolean(currentLayoutName + "invertGyro", layoutClass.invertGyro);

        editor.putBoolean(currentLayoutName + "touchTrack", layoutClass.isTouchTrackpad);
        editor.putBoolean(currentLayoutName + "gyroMouse", layoutClass.isGyroMouse);
        editor.putBoolean(currentLayoutName + "touchMouse", layoutClass.isTouchMouse);

        editor.putFloat(currentLayoutName + "touchSense", layoutClass.touchSensitivity);

        editor.putBoolean(currentLayoutName + "isSplitSwapped", layoutClass.isSplitSwapped);
        editor.putBoolean(currentLayoutName + "isSplit", layoutClass.isSplit);

        editor.putBoolean(currentLayoutName + "isMouseClickEnabled", layoutClass.isMouseClickEnabled);

        editor.putString(currentLayoutName + "leftTrackpadKeybind", layoutClass.leftTrackpadKeybind);
        editor.putString(currentLayoutName + "rightTrackpadKeybind", layoutClass.rightTrackpadKeybind);

        editor.putBoolean(currentLayoutName + "isLayoutInGroup", layoutClass.isLayoutInGroup);
        editor.putString(currentLayoutName + "groupID", layoutClass.groupID);


        editor.putBoolean(currentLayoutName + "isImported", layoutClass.isImported);

        editor.putString(currentLayoutName + "layoutUUID", layoutClass.layoutUUID);
        for (int x = 0; x < number; x++) {

            ButtonID_data b = layoutClass.buttons.get(x);

            editor.putInt(currentLayoutName + x + "xpos", b.x);
            editor.putInt(currentLayoutName + x + "ypos", b.y);
            editor.putInt(currentLayoutName + x + "size", b.size);
            editor.putString(currentLayoutName + x + "str", b.keybind);
            editor.putInt(currentLayoutName + x + "type",  b.type);
            editor.putInt(currentLayoutName + x + "color", b.color);
            editor.putInt(currentLayoutName + x + "tint", b.tintMode);
            editor.putFloat(currentLayoutName + x + "sensitivity", b.sensitivity);
            editor.putFloat(currentLayoutName + x + "ts", b.textSize);
            editor.putString(currentLayoutName + x + "image", b.imageID);
            editor.putInt(currentLayoutName + x + "alpha", b.alpha);
            editor.putBoolean(currentLayoutName + x + "showName", b.buttonNameVisible);
            editor.putString(currentLayoutName + x + "buttonName", b.buttonName);
            editor.putBoolean(currentLayoutName + x + "haptic", b.isHapticFeebackEnabled);
            editor.putBoolean(currentLayoutName + x + "verticalScroll", b.verticalScroll);

            editor.putBoolean(currentLayoutName + x + "joyTouchDetect", b.joystickTouchActivate);


        }

        editor.apply();
    }

    public static LayoutClass ReadSettings(LayoutClass layoutClass, Context c) {

        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        layoutClass.gyroActivated = sp.getBoolean(layoutClass.name + "gyro", false);
        layoutClass.gyroSensitivity = sp.getFloat(layoutClass.name+ "gyroSense", 0.5f);
        layoutClass.gyroThreshold = sp.getFloat(layoutClass.name+ "gyroThresh", 0.15f);

        layoutClass.invertGyro = sp.getBoolean(layoutClass.name + "invertGyro", false);


        layoutClass.touchSensitivity = sp.getFloat(layoutClass.name + "touchSense", 0.5f);
        layoutClass.isTouchMouse = sp.getBoolean(layoutClass.name + "touchMouse", true);

        layoutClass.isTouchTrackpad = sp.getBoolean(layoutClass.name + "touchTrack", true);
        layoutClass.isGyroMouse = sp.getBoolean(layoutClass.name + "gyroMouse", true);

        layoutClass.isSplit = sp.getBoolean(layoutClass.name + "isSplit", false);
        layoutClass.isSplitSwapped = sp.getBoolean(layoutClass.name + "isSplitSwapped", false);

        layoutClass.isMouseClickEnabled = sp.getBoolean(layoutClass.name + "isMouseClickEnabled", true);

        layoutClass.isImported = sp.getBoolean(layoutClass.name + "isImported", false);
        layoutClass.layoutUUID = sp.getString(layoutClass.name + "layoutUUID", "");

        layoutClass.leftTrackpadKeybind = sp.getString(layoutClass.name + "leftTrackpadKeybind", "mouse");
        layoutClass.rightTrackpadKeybind = sp.getString(layoutClass.name + "rightTrackpadKeybind", "mouse");

        return layoutClass;
    }

    public static ArrayList<ButtonID_data> ReadData(LayoutClass layoutClass, Context c) {

        String currentLayoutName = layoutClass.name;

        SharedPreferences sp = c.getSharedPreferences(filename, Context.MODE_PRIVATE);

        int j = sp.getInt(currentLayoutName + "number_buttons", -1);

        if(j == -1) {
            return null;
        }

        ArrayList<ButtonID_data> buttons = new ArrayList<>();

        for (int x = 0; x < j; x++) {

            ButtonID_data button = new ButtonID_data();
            button.textSize = sp.getFloat(currentLayoutName + x + "ts", -1);
            button.tintMode = sp.getInt(currentLayoutName + x + "tint", 1);
            button.addNewButton(sp.getInt(currentLayoutName + x + "type", 0), c);
            button.setPosition(sp.getInt(currentLayoutName + x + "xpos", 0), sp.getInt(currentLayoutName + x + "ypos", 0));
            button.setSize(sp.getInt(currentLayoutName + x + "size", 0));
            button.setColor(sp.getInt(currentLayoutName + x + "color", -1));
            button.setKeybind(sp.getString(currentLayoutName + x + "str", ""));
            button.alpha = (sp.getInt(currentLayoutName + x + "alpha", 10));
            button.buttonName = (sp.getString(currentLayoutName + x + "buttonName", ""));
            button.buttonNameVisible = (sp.getBoolean(currentLayoutName + x + "showName", true));
            button.isHapticFeebackEnabled = (sp.getBoolean(currentLayoutName + x + "haptic", true));
            button.joystickTouchActivate = (sp.getBoolean(currentLayoutName + x + "joyTouchDetect", true));
            button.verticalScroll = (sp.getBoolean(currentLayoutName + x + "verticalScroll", false));

            button.sensitivity = sp.getFloat(currentLayoutName + x + "sensitivity", 0.5f);


            String id = sp.getString(currentLayoutName + x + "image", "");
            button.setImage(id);

            buttons.add(button);

        }

        return buttons;
    }

    public static Bitmap ReadImage(Context a, String ImageID) {

        if(!ImageID.equals("")) {

            try {
                File f = new File(a.getExternalFilesDir(null) + "/saved_images/" + ImageID + ".png");
                return BitmapFactory.decodeStream(new FileInputStream(f));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void SaveImage(Bitmap finalBitmap, String ImageID, Context a) {

        if(!ImageID.equals("")) {

            File f = new File(a.getExternalFilesDir(null)+ "/" + "saved_images");
            f.mkdirs();

            File fileTo = new File(f + "/" + ImageID + ".png");

            try {
                FileOutputStream fos = new FileOutputStream(fileTo);
                // Use the compress method on the BitMap object to write image to the OutputStream
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
