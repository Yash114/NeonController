package com.gingertech.starbeam.ui;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaCodecInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Range;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gingertech.starbeam.R;
import com.gingertech.starbeam.limelight.LimeLog;
import com.gingertech.starbeam.limelight.binding.video.MediaCodecHelper;
import com.gingertech.starbeam.limelight.preferences.GlPreferences;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.gingertech.starbeam.limelight.preferences.StreamSettings;
import com.gingertech.starbeam.limelight.utils.Dialog;
import com.gingertech.starbeam.limelight.utils.UiHelper;

import java.lang.reflect.Method;
import java.util.Arrays;

public class SettingsPage extends Fragment {

    View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.settings_fragment, container, false);

        return root;
    }
}
