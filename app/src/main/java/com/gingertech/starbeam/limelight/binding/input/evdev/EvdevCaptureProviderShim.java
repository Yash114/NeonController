package com.gingertech.starbeam.limelight.binding.input.evdev;


import android.app.Activity;

import com.gingertech.starbeam.BuildConfig;
import com.gingertech.starbeam.limelight.binding.input.capture.InputCaptureProvider;

public class EvdevCaptureProviderShim {
    public static boolean isCaptureProviderSupported() {
        return false;
    }

    // We need to construct our capture provider using reflection because it isn't included in non-root builds
    public static InputCaptureProvider createEvdevCaptureProvider(Activity activity, EvdevListener listener) {
        try {
            Class providerClass = Class.forName("com.limelight.binding.input.evdev.EvdevCaptureProvider");
            return (InputCaptureProvider) providerClass.getConstructors()[0].newInstance(activity, listener);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}