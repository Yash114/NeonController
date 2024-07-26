package com.gingertech.starbeam.helpers;

import android.content.Context;
import android.util.Log;

import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.gingertech.starbeam.helpers.controllers.RemapClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class UserData {

    public static String Username;

    public static boolean isPremiumMode = false;
    public static ArrayList<LayoutClass> Layouts = new ArrayList<>();
    public static ArrayList<LayoutGroupClass> LayoutGroups = new ArrayList<>();

    public static HashMap<String, Command> Commands = new HashMap<>();

    public static RemapClass mRemap = new RemapClass();

    public static String firstOpenedTime = "";

    public static String defaultBluetoothAddr;


    public final static int BLUETOOTH = 0;
    public final static int WIFI = 1;

    public static int PlayMode = WIFI;


    public static PremiumController.Product selectedPremiumObject;

    public static LayoutClass currentLayout;
    public static String currentGroupID = "";

    public static LayoutClass testLayout;

    public static String currentLayoutName = "Default";
    public static Boolean wasPremium = false;
    public static Boolean isNativeMode = true;
    public static boolean openedVideo = true;

    public static boolean back_from_connect_without_connecting = false;
    public static boolean back_from_connect_without_connecting_done = false;

    public static int layoutsEdited = 0;
    public static int layoutsCreated = 0;
    public static int timesConnected = 0;
    public static int timesConnectedBT = 0;

    public static int videoTestIndex = 0;


    public static boolean hasReviewed = false;

    public static int openCount = 0;

    public static final int LOGO = -2;

    public static final int HOME = -1;
    public static final int LAYOUTS = 0;


    public static final int OPTIONS = 1;
    public static final int LAUNCH = 2;
    public static final int PREMIUM = 3;
    public static final int DISCORD = 4;

    public static final int LAYOUTS_LIST = 6;
    public static final int LAYOUTS_LIST_firsttime = 11;
    public static final int LAYOUTS_CREATE_firsttime = 15;

    public static final int LAUNCH_GAME_PLAY = 10;


    public static final int LAYOUTS_CREATE = 7;
    public static final int LAYOUTS_TEST = 12;

    public static final int LAUNCH_COMPUTER_LIST = 8;
    public static final int LAUNCH_BLUETOOTH_PLAY = 34;

    public static final int LAUNCH_GAME_SELECT = 9;
    public static final int LAUNCH_GAME_PLAY_FirstTime = 18;

    public static final int COMMANDS_LIST = 19;
    public static final int COMMANDS_EDITOR = 20;
    public static final int REMAP = 21;


    public static int CurrentFragment = HOME;
    public static int CurrentPage = LAYOUTS;

    public static final boolean A = true;
    public static final boolean B = false;


    public static void setup(Context context) {

        SaveClass.GetOpenCount(context);
        SaveClass.GetHasReviewed(context);
        SaveClass.GetFlags(context);
        SaveClass.GetTests(context);

        if(wasPremium && !isPremiumMode) {
            SaveClass.ResetCurrentLayout(context);
            wasPremium = false;
            SaveClass.SaveFlags(context);
        }

        SaveClass.GetCurrentLayout(context);
        Layouts = SaveClass.GetGampads(context);
        mRemap = SaveClass.GetRemap(context);

        SaveClass.GetLayoutSaveGroups(context);

        SaveClass.GetCommands(context);

        Log.e("groups", String.valueOf(LayoutGroups.size()));

    }

    public static void setCurrentLayout(Context context, LayoutClass currentLayout) {

        if(currentLayout == null) {
            return;
        }

        UserData.currentLayout = currentLayout;
        UserData.currentLayoutName = currentLayout.name;
        SaveClass.SaveCurrentLayout(context);
    }

}
