package com.gingertech.starbeam.limelight.binding;

import android.content.Context;

import com.gingertech.starbeam.limelight.binding.crypto.AndroidCryptoProvider;
import com.gingertech.starbeam.limelight.nvstream.http.LimelightCryptoProvider;

public class PlatformBinding {
    public static LimelightCryptoProvider getCryptoProvider(Context c) {
        return new AndroidCryptoProvider(c);
    }
}
