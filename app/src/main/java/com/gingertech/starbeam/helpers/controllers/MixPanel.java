package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;

import androidx.annotation.Nullable;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.helpers.UserData;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MixPanel {

    final private static String mixToken = "054951ba4c264c6f804c798152383940";

    private static JSONObject getGenericData() {
        JSONObject props = new JSONObject();

        try {
            props.put("time_in_session", printDifference(MainActivity.initialTime, Calendar.getInstance().getTime()));

            props.put("is_premium", UserData.isPremiumMode ? "yes" : "no");
            props.put("connected_count", UserData.timesConnected);
            props.put("opened_count", UserData.openCount);
            props.put("connected_count_BT", UserData.timesConnectedBT);
            props.put("layout_count", UserData.layoutsCreated);
            props.put("video_opened", UserData.openedVideo ? "yes" : "no");
            props.put("firstOpenedTime", UserData.firstOpenedTime);

            props.put("from_connect_without_connecting", UserData.back_from_connect_without_connecting ? "yes" : "no");

            if(UserData.currentLayout != null) {
                props.put("current_layout_name", UserData.currentLayout.name);
                props.put("current_layout_description", UserData.currentLayout.description);
                props.put("current_layout_num_buttons", UserData.currentLayout.buttons.size());
            }

            props.put("current_fragment", UserData.CurrentFragment);

        } catch(JSONException j) {

        }

        return props;
    }
    public static void mpButtonTracking(MixpanelAPI mp, String buttonName) {
        if(mp == null) { return; }

        mp.track(buttonName + " clicked", getGenericData());
    }

    public static void mpEventTracking(@Nullable MixpanelAPI mp, String event_name, @Nullable HashMap<String, String> additionalData) {
        if(mp == null) { return; }

        JSONObject props = getGenericData();

        try {

            if (additionalData != null) {
                for (String key : additionalData.keySet()) {
                    props.put(key, additionalData.get(key));
                }
            }

        } catch (JSONException j) {

        }

        mp.track(event_name, props);

    }

    public static void mpEventTracking(Context context, String event_name, @Nullable HashMap<String, String> additionalData) {

        JSONObject props = getGenericData();

        try {

            if (additionalData != null) {
                for (String key : additionalData.keySet()) {
                    props.put(key, additionalData.get(key));
                }
            }

            MixPanel.makeObj(context).track(event_name, props);

        } catch (JSONException j) {

        }

    }

    public static String printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        return elapsedDays + " days, " + elapsedHours + " hours, " + elapsedMinutes + " minutes, " + elapsedSeconds + " seconds";
    }

    public static MixpanelAPI makeObj(Context context) {
        return MixpanelAPI.getInstance(context, mixToken, true);
    }

}
