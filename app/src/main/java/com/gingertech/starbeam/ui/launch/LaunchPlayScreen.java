package com.gingertech.starbeam.ui.launch;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PictureInPictureParams;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.TypedValue;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.LaunchOverlayController;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.TextWithImage;
import com.gingertech.starbeam.limelight.LimeLog;
import com.gingertech.starbeam.limelight.binding.PlatformBinding;
import com.gingertech.starbeam.limelight.binding.audio.AndroidAudioRenderer;
import com.gingertech.starbeam.limelight.binding.input.ControllerHandler;
import com.gingertech.starbeam.limelight.binding.input.KeyboardTranslator;
import com.gingertech.starbeam.limelight.binding.input.capture.InputCaptureManager;
import com.gingertech.starbeam.limelight.binding.input.capture.InputCaptureProvider;
import com.gingertech.starbeam.limelight.binding.input.driver.UsbDriverService;
import com.gingertech.starbeam.limelight.binding.input.evdev.EvdevListener;
import com.gingertech.starbeam.limelight.binding.input.touch.AbsoluteTouchContext;
import com.gingertech.starbeam.limelight.binding.input.touch.RelativeTouchContext;
import com.gingertech.starbeam.limelight.binding.input.touch.TouchContext;
import com.gingertech.starbeam.limelight.binding.input.virtual_controller.VirtualController;
import com.gingertech.starbeam.limelight.binding.video.CrashListener;
import com.gingertech.starbeam.limelight.binding.video.MediaCodecDecoderRenderer;
import com.gingertech.starbeam.limelight.binding.video.MediaCodecHelper;
import com.gingertech.starbeam.limelight.binding.video.PerfOverlayListener;
import com.gingertech.starbeam.limelight.nvstream.NvConnection;
import com.gingertech.starbeam.limelight.nvstream.NvConnectionListener;
import com.gingertech.starbeam.limelight.nvstream.StreamConfiguration;
import com.gingertech.starbeam.limelight.nvstream.http.ComputerDetails;
import com.gingertech.starbeam.limelight.nvstream.http.NvApp;
import com.gingertech.starbeam.limelight.nvstream.http.NvHTTP;
import com.gingertech.starbeam.limelight.nvstream.input.KeyboardPacket;
import com.gingertech.starbeam.limelight.nvstream.input.MouseButtonPacket;
import com.gingertech.starbeam.limelight.nvstream.jni.MoonBridge;
import com.gingertech.starbeam.limelight.preferences.GlPreferences;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.gingertech.starbeam.limelight.ui.GameGestures;
import com.gingertech.starbeam.limelight.ui.StreamView;
import com.gingertech.starbeam.limelight.utils.Dialog;
import com.gingertech.starbeam.limelight.utils.ServerHelper;
import com.gingertech.starbeam.limelight.utils.ShortcutHelper;
import com.gingertech.starbeam.limelight.utils.SpinnerDialog;
import com.gingertech.starbeam.limelight.utils.UiHelper;
import com.gingertech.starbeam.ui.layout.LayoutRootPage;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LaunchPlayScreen extends Fragment implements SurfaceHolder.Callback,
        View.OnGenericMotionListener, View.OnTouchListener, NvConnectionListener, EvdevListener, GameGestures, StreamView.InputCallbacks,
        PerfOverlayListener, UsbDriverService.UsbDriverStateListener, View.OnKeyListener {

    View root;
    View backgroundTouchView;
    Vibrator vibrator;

    String host;
    int port;
    int httpsPort;
    int appId;
    String uniqueId;
    String uuid;
    boolean appSupportsHdr;
    byte[] derCertData;
    private String pcName;
    private String appName;

    private View loadingView;
    private View blackoutBackground;

    private GenericCallbackv2 returnCallback;

    HashMap<String, Object> connData = new HashMap<>();

    public LaunchPlayScreen(){}

    public LaunchPlayScreen(HashMap<String,Object> values, @Nullable GenericCallbackv2 returnCallback) {

        connData = values;

        host = (String) values.get("host");
        port = values.containsKey("port") ? (int) values.get("port") : NvHTTP.DEFAULT_HTTP_PORT;
        httpsPort = values.containsKey("httpsPort") ? (int) values.get("httpsPort") : 0;
        appId = values.containsKey("appId") ? (int) values.get("appId") : StreamConfiguration.INVALID_APP_ID;
        uniqueId = (String) values.get("uniqueId");
        uuid = (String) values.get("uuid");
        appSupportsHdr = values.containsKey("appSupportsHdr") && (boolean) values.get("appSupportsHdr");
        derCertData = (byte[]) values.get("derCertData");
        pcName = (String) values.get("pcName");
        appName = (String) values.get("appName");

        UserData.CurrentFragment = UserData.LAUNCH_GAME_PLAY;

        this.returnCallback = returnCallback;
    }

    Boolean isImmersiveMode = false;

    int streamViewWidth = 0;
    int streamViewHeight = 0;
    int marginSpacing = 0;
    TextWithImage immersiveModeIcon, editLayout, reloadButton;
    ImageView immersiveModeIcon_1;

    LaunchOverlayController launchOverlayController;


    void toggleImmersiveMode() {
        vibrator.vibrate(10);

        isImmersiveMode = !isImmersiveMode;

        immersiveModeIcon.setVisibility(isImmersiveMode ? View.GONE : View.VISIBLE);
        immersiveModeIcon_1.setVisibility(!isImmersiveMode ? View.GONE : View.VISIBLE);
        editLayout.setVisibility(isImmersiveMode ? View.GONE : View.VISIBLE);
        reloadButton.setVisibility(isImmersiveMode ? View.GONE : View.VISIBLE);


        if(streamViewWidth == 0) {
            streamViewWidth = streamView.getWidth();
            streamViewHeight = streamView.getHeight();
        }

        if(isImmersiveMode) {

            performanceOverlayView.setVisibility(View.GONE);
            notificationOverlayView.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            blackoutBackground.setVisibility(View.VISIBLE);


        } else {
            streamView.setScaleX(1);

            blackoutBackground.setVisibility(View.INVISIBLE);
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) streamView.getLayoutParams();

        layoutParams.setMargins(0, !isImmersiveMode ? marginSpacing : 0,0, !isImmersiveMode ? marginSpacing : 0);

        streamView.setLayoutParams(layoutParams);

        MainActivity.setImmersiveMode(isImmersiveMode, requireActivity());

        if(prefConfig.fillVideo) {
            if(isImmersiveMode) {

                int height = blackoutBackground.getHeight();
                int width = blackoutBackground.getWidth();
                float f = prefConfig.width * height / prefConfig.height;

                streamView.setScaleX(f / streamView.getWidth());
                streamView.setScaleY(f / streamView.getWidth());

                backgroundTouchView.setScaleX(((width / height) * streamViewHeight) / streamViewWidth);
                backgroundTouchView.setScaleY(((width / height) * streamViewHeight) / streamViewWidth);


            } else {
                streamView.setScaleX(1);
                streamView.setScaleY(1);

                backgroundTouchView.setScaleX(1);
                backgroundTouchView.setScaleY(1);

            }
        }

        backgroundTouchView.setLayoutParams(layoutParams);
    }

    void turnOffImersiveMode() {
        isImmersiveMode = false;
        immersiveModeIcon.setAlpha(1);

        if(streamViewWidth == 0) {
            streamViewWidth = streamView.getMeasuredWidth();
            streamViewHeight = streamView.getMeasuredHeight();
        }

        blackoutBackground.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) streamView.getLayoutParams();

        layoutParams.setMargins(0, !isImmersiveMode ? marginSpacing : 0,0, !isImmersiveMode ? marginSpacing : 0);

        MainActivity.setImmersiveMode(isImmersiveMode, requireActivity());

        streamView.setLayoutParams(layoutParams);

        backgroundTouchView.setLayoutParams(layoutParams);
    }

    public boolean startedFlag = false;
    @Override
    public void onResume() {
        super.onResume();

        try {
            if (startedFlag) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LaunchPlayScreen(connData, returnCallback)).commit();
            }
        } catch (IllegalArgumentException e) {

        }

        startedFlag = true;

        turnOffImersiveMode();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private MixpanelAPI mp;

    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            requireActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)  {

        root = inflater.inflate(R.layout.activity_game, container, false);
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        mp = MixPanel.makeObj(requireContext());
        MixPanel.mpEventTracking(mp, "Launch_Play_Fragment_opened", null);

        Resources r = getResources();
        marginSpacing = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                r.getDisplayMetrics()
        );

        blackoutBackground = root.findViewById(R.id.blackoutBack);

        immersiveModeIcon = root.findViewById(R.id.immersiveModeIcon);
        immersiveModeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "immersive_mode");

                if(streamView == null) {
                    return;
                }

                toggleImmersiveMode();
            }
        });

        reloadButton = root.findViewById(R.id.reloadIcon);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vibrator.vibrate(10);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        trans.replace(R.id.nav_host_fragment, new LaunchPlayScreen(connData, returnCallback)).commit();
                    }
                }).start();

            }
        });

        immersiveModeIcon_1 = root.findViewById(R.id.immersiveModeIcon_1);

        List<Integer> fullPos = SaveClass.getFullscreenIcon(requireContext());

        if(fullPos.get(0) != -1 && fullPos.get(1) != -1) {
            immersiveModeIcon_1.setX(fullPos.get(0));
            immersiveModeIcon_1.setY(fullPos.get(1));
        }

        immersiveModeIcon_1.setOnTouchListener(new View.OnTouchListener() {

            boolean isLongPress;
            int x, y;
            final Handler handler = new Handler();
            Runnable activatedLongPress = new Runnable() {
                @Override
                public void run() {
                    vibrator.vibrate(200);
                    isLongPress = true;
                }
            };
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    handler.postDelayed(activatedLongPress, 1000);

                    x = (int) motionEvent.getX();
                    y = (int) motionEvent.getY();
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                    float X = motionEvent.getX() - x;
                    float Y = motionEvent.getY() - y;

                    if (isLongPress) {
                        immersiveModeIcon_1.setX(immersiveModeIcon_1.getX() + X);
                        immersiveModeIcon_1.setY(immersiveModeIcon_1.getY() + Y);
                    }
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                    List<Integer> fullPos = new ArrayList<>();

                    fullPos.add((int) immersiveModeIcon_1.getTranslationX());
                    fullPos.add((int) immersiveModeIcon_1.getTranslationY());

                    if(getContext() != null) {
                        SaveClass.saveFullscreenIcon(requireContext(), fullPos);
                    }

                    handler.removeCallbacks(activatedLongPress);
                    if(!isLongPress) {
                        MixPanel.mpButtonTracking(mp, "immersive_mode");

                        if (streamView == null) {
                            return true;
                        }

                        toggleImmersiveMode();
//
                    } else {
                        isLongPress = false;
                    }
                }

                return true;
            }
        });

        editLayout = root.findViewById(R.id.editLayout);
        editLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LayoutRootPage(returnCallback, LayoutRootPage.CREATEVIEW)).commit();
            }
        });

        mFirebaseAnalytics.logEvent("Launch_Play_Fragment", new Bundle());

        UiHelper.setLocale(this.requireActivity());

        if(UserData.timesConnected == 0) {
            MixPanel.mpEventTracking(getContext(), "First_Time_Connecting", null);
            mFirebaseAnalytics.logEvent("First_Time_Connecting", new Bundle());
        }

        UserData.timesConnected += 1;
        SaveClass.SaveFlags(requireContext());

        // Listen for UI visibility events
//        requireActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);

        // Change volume button behavior
        requireActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
//
        // Start the spinner
        spinner = SpinnerDialog.displayDialog(requireActivity(), getResources().getString(R.string.conn_establishing_title),
                getResources().getString(R.string.conn_establishing_msg), true);

        // Read the stream preferences
        prefConfig = PreferenceConfiguration.readPreferences(requireActivity());
        tombstonePrefs = requireActivity().getSharedPreferences("DecoderTombstone", 0);


        // Enter landscape unless we're on a square screen
        setPreferredOrientationForCurrentDisplay();


        // Listen for non-touch events on the game surface
        streamView = root.findViewById(R.id.surfaceView);
//        streamView.setOnGenericMotionListener(this);
        streamView.setOnKeyListener(this);
//        streamView.setInputCallbacks(this);

//        if (true) {
        // Allow the activity to layout under notches if the fill-screen option
        // was turned on by the user or it's a full-screen native resolution
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                requireActivity().getWindow().getAttributes().layoutInDisplayCutoutMode =
//                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                requireActivity().getWindow().getAttributes().layoutInDisplayCutoutMode =
//                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//            }
//        }

        // Listen for touch events on the background touch view to enable trackpad mode
        // to work on areas outside of the StreamView itself. We use a separate View
        // for this rather than just handling it at the Activity level, because that
        // allows proper touch splitting, which the OSC relies upon.
        backgroundTouchView = root.findViewById(R.id.backgroundTouchView);
        backgroundTouchView.setOnTouchListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request unbuffered input event dispatching for all input classes we handle here.
            // Without this, input events are buffered to be delivered in lock-step with VBlank,
            // artificially increasing input latency while streaming.
            streamView.requestUnbufferedDispatch(
                    InputDevice.SOURCE_CLASS_BUTTON | // Keyboards
                            InputDevice.SOURCE_CLASS_JOYSTICK | // Gamepads
                            InputDevice.SOURCE_CLASS_POINTER | // Touchscreens and mice (w/o pointer capture)
                            InputDevice.SOURCE_CLASS_POSITION | // Touchpads
                            InputDevice.SOURCE_CLASS_TRACKBALL // Mice (pointer capture)
            );
            backgroundTouchView.requestUnbufferedDispatch(
                    InputDevice.SOURCE_CLASS_BUTTON | // Keyboards
                            InputDevice.SOURCE_CLASS_JOYSTICK | // Gamepads
                            InputDevice.SOURCE_CLASS_POINTER | // Touchscreens and mice (w/o pointer capture)
                            InputDevice.SOURCE_CLASS_POSITION | // Touchpads
                            InputDevice.SOURCE_CLASS_TRACKBALL // Mice (pointer capture)
            );
        }

        notificationOverlayView = root.findViewById(R.id.notificationOverlay);

        performanceOverlayView = root.findViewById(R.id.performanceOverlay);

        inputCaptureProvider = InputCaptureManager.getInputCaptureProvider(requireActivity(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            streamView.setOnCapturedPointerListener(new View.OnCapturedPointerListener() {
                @Override
                public boolean onCapturedPointer(View view, MotionEvent motionEvent) {
                    return handleMotionEvent(view, motionEvent);
                }
            });
        }

        // Warn the user if they're on a metered connection
        ConnectivityManager connMgr = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr.isActiveNetworkMetered()) {
            displayTransientMessage(getResources().getString(R.string.conn_metered));
        }

        // Make sure Wi-Fi is fully powered up
        WifiManager wifiMgr = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            highPerfWifiLock = wifiMgr.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Moonlight High Perf Lock");
            highPerfWifiLock.setReferenceCounted(false);
            highPerfWifiLock.acquire();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                lowLatencyWifiLock = wifiMgr.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "Moonlight Low Latency Lock");
                lowLatencyWifiLock.setReferenceCounted(false);
                lowLatencyWifiLock.acquire();
            }
        } catch (SecurityException e) {
            // Some Samsung Galaxy S10+/S10e devices throw a SecurityException from
            // WifiLock.acquire() even though we have android.permission.WAKE_LOCK in our manifest.
            e.printStackTrace();
        }

        X509Certificate serverCert = null;
        try {
            if (derCertData != null) {
                serverCert = (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(new ByteArrayInputStream(derCertData));
            }
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        if (appId == StreamConfiguration.INVALID_APP_ID) {

            Toast.makeText(requireContext(), R.string.Invalid_App, Toast.LENGTH_LONG).show();

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return root;
        }

        // Report this shortcut being used
        ComputerDetails computer = new ComputerDetails();
        computer.name = pcName;
        computer.uuid = uuid;
        shortcutHelper = new ShortcutHelper(requireActivity());
        shortcutHelper.reportComputerShortcutUsed(computer);
        if (appName != null) {
            // This may be null if launched from the "Resume Session" PC context menu item
            shortcutHelper.reportGameLaunched(computer, new NvApp(appName, appId, appSupportsHdr));
        }

        // Initialize the MediaCodec helper before creating the decoder
        GlPreferences glPrefs = GlPreferences.readPreferences(requireActivity());
        MediaCodecHelper.initialize(requireActivity(), glPrefs.glRenderer);

        // Check if the user has enabled HDR
        boolean willStreamHdr = false;
        if (prefConfig.enableHdr) {
            // Start our HDR checklist
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Display display = requireActivity().getWindowManager().getDefaultDisplay();
                Display.HdrCapabilities hdrCaps = display.getHdrCapabilities();

                // We must now ensure our display is compatible with HDR10
                if (hdrCaps != null) {
                    // getHdrCapabilities() returns null on Lenovo Lenovo Mirage Solo (vega), Android 8.0
                    for (int hdrType : hdrCaps.getSupportedHdrTypes()) {
                        if (hdrType == Display.HdrCapabilities.HDR_TYPE_HDR10) {
                            willStreamHdr = true;
                            break;
                        }
                    }
                }

                if (!willStreamHdr) {
                    // Nope, no HDR for us :(
                    Toast.makeText(requireActivity(), R.string.Display_does_not_support_HDR10, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireActivity(), R.string.HDR_requires_Android_7, Toast.LENGTH_LONG).show();
            }
        }

        // Check if the user has enabled performance stats overlay
        if (prefConfig.enablePerfOverlay) {
            performanceOverlayView.setVisibility(View.VISIBLE);
        }

        decoderRenderer = new MediaCodecDecoderRenderer(
                requireActivity(),
                prefConfig,
                new CrashListener() {
                    @Override
                    public void notifyCrash(Exception e) {
                        // The MediaCodec instance is going down due to a crash
                        // let's tell the user something when they open the app again

                        // We must use commit because the app will crash when we return from this function
                        tombstonePrefs.edit().putInt("CrashCount", tombstonePrefs.getInt("CrashCount", 0) + 1).commit();
                        reportedCrash = true;
                    }
                },
                tombstonePrefs.getInt("CrashCount", 0),
                connMgr.isActiveNetworkMetered(),
                willStreamHdr,
                glPrefs.glRenderer,
                this);

        // Display a message to the user if HEVC was forced on but we still didn't find a decoder
        if (prefConfig.videoFormat == PreferenceConfiguration.FormatOption.FORCE_HEVC && !decoderRenderer.isHevcSupported()) {
            Toast.makeText(requireContext(), "No HEVC decoder found", Toast.LENGTH_LONG).show();
        }

        // Display a message to the user if AV1 was forced on but we still didn't find a decoder
        if (prefConfig.videoFormat == PreferenceConfiguration.FormatOption.FORCE_AV1 && !decoderRenderer.isAv1Supported()) {
            Toast.makeText(requireContext(), "No AV1 decoder found", Toast.LENGTH_LONG).show();
        }

        int gamepadMask = ControllerHandler.getAttachedControllerMask(requireActivity());
        if (!prefConfig.multiController) {
            // Always set gamepad 1 present for when multi-controller is
            // disabled for games that don't properly support detection
            // of gamepads removed and replugged at runtime.
            gamepadMask = 1;
        }
        if (prefConfig.onscreenController) {
            // If we're using OSC, always set at least gamepad 1.
            gamepadMask |= 1;
        }

        // Set to the optimal mode for streaming
        float displayRefreshRate = prepareDisplayForRendering();
        LimeLog.info("Display refresh rate: " + displayRefreshRate);

        // If the user requested frame pacing using a capped FPS, we will need to change our
        // desired FPS setting here in accordance with the active display refresh rate.
        int roundedRefreshRate = Math.round(displayRefreshRate);
        int chosenFrameRate = prefConfig.fps;
        if (prefConfig.framePacing == PreferenceConfiguration.FRAME_PACING_CAP_FPS) {
            if (prefConfig.fps >= roundedRefreshRate) {
                if (prefConfig.fps > roundedRefreshRate + 3) {
                    // Use frame drops when rendering above the screen frame rate
                    prefConfig.framePacing = PreferenceConfiguration.FRAME_PACING_BALANCED;
                    LimeLog.info("Using drop mode for FPS > Hz");
                } else if (roundedRefreshRate <= 49) {
                    // Let's avoid clearly bogus refresh rates and fall back to legacy rendering
                    prefConfig.framePacing = PreferenceConfiguration.FRAME_PACING_BALANCED;
                    LimeLog.info("Bogus refresh rate: " + roundedRefreshRate);
                } else {
                    chosenFrameRate = roundedRefreshRate - 1;
                    LimeLog.info("Adjusting FPS target for screen to " + chosenFrameRate);
                }
            }
        }

        // H.264 is always supported
        int supportedVideoFormats = MoonBridge.VIDEO_FORMAT_H265;
//        if (decoderRenderer.isHevcSupported()) {
//            supportedVideoFormats |= MoonBridge.VIDEO_FORMAT_H265;
//            if (willStreamHdr && decoderRenderer.isHevcMain10Hdr10Supported()) {
//                supportedVideoFormats |= MoonBridge.VIDEO_FORMAT_H265_MAIN10;
//            }
//        }
//        if (decoderRenderer.isAv1Supported()) {
//            supportedVideoFormats |= MoonBridge.VIDEO_FORMAT_AV1_MAIN8;
//            if (willStreamHdr && decoderRenderer.isAv1Main10Supported()) {
//                supportedVideoFormats |= MoonBridge.VIDEO_FORMAT_AV1_MAIN10;
//            }
//        }

        StreamConfiguration config = new StreamConfiguration.Builder()
                .setResolution(prefConfig.width, prefConfig.height)
                .setLaunchRefreshRate(prefConfig.fps)
                .setRefreshRate(chosenFrameRate)
                .setApp(new NvApp(appName != null ? appName : "app", appId, appSupportsHdr))
                .setBitrate(prefConfig.bitrate)
                .setEnableSops(prefConfig.enableSops)
                .enableLocalAudioPlayback(prefConfig.playHostAudio)
                .setMaxPacketSize(1392)
                .setRemoteConfiguration(StreamConfiguration.STREAM_CFG_AUTO) // NvConnection will perform LAN and VPN detection
                .setHevcBitratePercentageMultiplier(75)
                .setAv1BitratePercentageMultiplier(75)
                .setSupportedVideoFormats(supportedVideoFormats)
                .setAttachedGamepadMask(gamepadMask)
                .setClientRefreshRateX100((int)(displayRefreshRate * 100))
                .setAudioConfiguration(prefConfig.audioConfiguration)
                .setAudioEncryption(true)
                .setColorSpace(decoderRenderer.getPreferredColorSpace())
                .setColorRange(decoderRenderer.getPreferredColorRange())
                .setPersistGamepadsAfterDisconnect(false)
                .build();

        // Initialize the connection
        conn = new NvConnection(requireActivity(),
                new ComputerDetails.AddressTuple(host, port),
                httpsPort, uniqueId, config,
                PlatformBinding.getCryptoProvider(requireActivity()), serverCert);
        controllerHandler = new ControllerHandler(requireActivity(), conn, this, prefConfig);
        keyboardTranslator = new KeyboardTranslator();

        launchOverlayController = root.findViewById(R.id.container);
        launchOverlayController.setCallback(returnCallback);

        launchOverlayController.setupWifi(keyboardTranslator, conn);
        launchOverlayController.setBottomView(root.findViewById(R.id.backgroundTouchView));
        launchOverlayController.makeButtons();
        launchOverlayController.setKeyboardToggleCallback(new OnGenericCallbackv2() {
            @Override
            public void onChange(Object value) {
                toggleKeyboard();
            }

            @Override
            public void onChange(Object value, Object value2) {

            }

            @Override
            public void onChange(Object value, Object value2, Object value3) {
            }
        });

        InputManager inputManager = (InputManager) requireActivity().getSystemService(Context.INPUT_SERVICE);
        inputManager.registerInputDeviceListener(controllerHandler, null);
        inputManager.registerInputDeviceListener(keyboardTranslator, null);

        // Initialize touch contexts
        for (int i = 0; i < 2; i++) {
                touchContextMapRef[i] = new RelativeTouchContext(conn, i,
                        REFERENCE_HORIZ_RES, REFERENCE_VERT_RES,
                        streamView, prefConfig);
        }

        for (int i = 0; i < 2; i++) {
                touchContextMapAbs[i] = new AbsoluteTouchContext(conn, i, streamView);

        }

        // Use sustained performance mode on N+ to ensure consistent
        // CPU availability
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requireActivity().getWindow().setSustainedPerformanceMode(true);
        }

        if (prefConfig.onscreenController) {
            // create virtual onscreen controller
            virtualController = new VirtualController(controllerHandler,
                    (FrameLayout) streamView.getParent(),
                    requireActivity());
            virtualController.refreshLayout();
            virtualController.show();
        }

        if (!decoderRenderer.isAvcSupported()) {
            if (spinner != null) {
                spinner.dismiss();
                spinner = null;
            }

            // If we can't find an AVC decoder, we can't proceed
            Dialog.displayDialog(requireActivity(), getResources().getString(R.string.conn_error_title),
                     getResources().getString(R.string.This_device_or_ROM_doesn), true);

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return root;
        }

        // The connection will be started when the surface gets created
        streamView.getHolder().addCallback(this);
        setupTouchpad();

        loadingView = root.findViewById(R.id.discoveringView);

        turnOffImersiveMode();

        return root;
    }

    private void setupTouchpad() {

        ViewGroup container = root.findViewById(R.id.container);


    }

    private int lastButtonState = 0;

    //     Only 2 touches are supported
    public final TouchContext[] touchContextMapRef = new TouchContext[2];
    public final TouchContext[] touchContextMapAbs = new TouchContext[2];

    private long threeFingerDownTime = 0;

    public static final int REFERENCE_HORIZ_RES = 1280;
    public static final int REFERENCE_VERT_RES = 720;

    private static final int STYLUS_DOWN_DEAD_ZONE_DELAY = 100;
    private static final int STYLUS_DOWN_DEAD_ZONE_RADIUS = 20;

    private static final int STYLUS_UP_DEAD_ZONE_DELAY = 150;
    private static final int STYLUS_UP_DEAD_ZONE_RADIUS = 50;

    private static final int THREE_FINGER_TAP_THRESHOLD = 300;

    private ControllerHandler controllerHandler;
    private KeyboardTranslator keyboardTranslator;
    private VirtualController virtualController;

    public PreferenceConfiguration prefConfig;
    private SharedPreferences tombstonePrefs;

    public NvConnection conn;
    private SpinnerDialog spinner;
    private boolean displayedFailureDialog = false;
    private boolean connecting = false;
    private boolean connected = false;
    private final boolean autoEnterPip = false;
    private boolean surfaceCreated = false;
    private boolean attemptedConnection = false;
    private int suppressPipRefCount = 0;
    private float desiredRefreshRate;

    private InputCaptureProvider inputCaptureProvider;
    private int modifierFlags = 0;
    private boolean grabbedInput = true;
    private boolean grabComboDown = false;
    public StreamView streamView;
    private long lastAbsTouchUpTime = 0;
    private long lastAbsTouchDownTime = 0;
    private float lastAbsTouchUpX, lastAbsTouchUpY;
    private float lastAbsTouchDownX, lastAbsTouchDownY;

    private boolean isHidingOverlays;
    private TextView notificationOverlayView;
    private int requestedNotificationOverlayVisibility = View.GONE;
    private TextView performanceOverlayView;

    private ShortcutHelper shortcutHelper;

    private MediaCodecDecoderRenderer decoderRenderer;
    private boolean reportedCrash;

    private WifiManager.WifiLock highPerfWifiLock;
    private WifiManager.WifiLock lowLatencyWifiLock;


    private void setPreferredOrientationForCurrentDisplay() {
        Display display = requireActivity().getWindowManager().getDefaultDisplay();

        // For semi-square displays, we use more complex logic to determine which orientation to use (if any)
        if (PreferenceConfiguration.isSquarishScreen(display)) {
            int desiredOrientation = Configuration.ORIENTATION_UNDEFINED;

            // OSC doesn't properly support portrait displays, so don't use it in portrait mode by default
            if (prefConfig.onscreenController) {
                desiredOrientation = Configuration.ORIENTATION_LANDSCAPE;
            }

            // For native resolution, we will lock the orientation to the one that matches the specified resolution
            if (PreferenceConfiguration.isNativeResolution(prefConfig.width, prefConfig.height)) {
                if (prefConfig.width > prefConfig.height) {
                    desiredOrientation = Configuration.ORIENTATION_LANDSCAPE;
                } else {
                    desiredOrientation = Configuration.ORIENTATION_PORTRAIT;
                }
            }

            if (desiredOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                } else {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
            } else if (desiredOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                } else {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            } else {
                // If we don't have a reason to lock to portrait or landscape, allow any orientation
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                } else {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
            }
        } else {
            // For regular displays, we always request landscape
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
            } else {
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Set requested orientation for possible new screen size
//        setPreferredOrientationForCurrentDisplay();

        if (virtualController != null) {
            // Refresh layout of OSC for possible new screen size
            virtualController.refreshLayout();
        }

        // Hide on-screen overlays in PiP mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (requireActivity().isInPictureInPictureMode()) {
                isHidingOverlays = true;

                if (virtualController != null) {
                    virtualController.hide();
                }

                performanceOverlayView.setVisibility(View.GONE);
                notificationOverlayView.setVisibility(View.GONE);

                // Update GameManager state to indicate we're in PiP (still gaming, but interruptible)
                UiHelper.notifyStreamEnteringPiP(requireActivity());
            } else {
                isHidingOverlays = false;

                // Restore overlays to previous state when leaving PiP

                if (virtualController != null) {
                    virtualController.show();
                }

                if (prefConfig.enablePerfOverlay) {
                    performanceOverlayView.setVisibility(View.VISIBLE);
                }

                notificationOverlayView.setVisibility(requestedNotificationOverlayVisibility);

                // Update GameManager state to indicate we're out of PiP (gaming, non-interruptible)
                UiHelper.notifyStreamExitingPiP(requireActivity());
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private PictureInPictureParams getPictureInPictureParams(boolean autoEnter) {
        PictureInPictureParams.Builder builder =
                new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(prefConfig.width, prefConfig.height))
                        .setSourceRectHint(new Rect(
                                streamView.getLeft(), streamView.getTop(),
                                streamView.getRight(), streamView.getBottom()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(autoEnter);
            builder.setSeamlessResizeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (appName != null) {
                builder.setTitle(appName);
                if (pcName != null) {
                    builder.setSubtitle(pcName);
                }
            } else if (pcName != null) {
                builder.setTitle(pcName);
            }
        }

        return builder.build();
    }

    private void updatePipAutoEnter() {

        //        if (!prefConfig.enablePip) {
//            return;
//        }
//
//        boolean autoEnter = connected && suppressPipRefCount == 0;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requireActivity().setPictureInPictureParams(getPictureInPictureParams(autoEnter));
//        } else {
//            autoEnterPip = autoEnter;
//        }
    }

    public void setMetaKeyCaptureState(boolean enabled) {
        // This uses custom APIs present on some Samsung devices to allow capture of
        // meta key events while streaming.
        try {
            Class<?> semWindowManager = Class.forName("com.samsung.android.view.SemWindowManager");
            Method getInstanceMethod = semWindowManager.getMethod("getInstance");
            Object manager = getInstanceMethod.invoke(null);

            if (manager != null) {
                Class<?>[] parameterTypes = new Class<?>[2];
                parameterTypes[0] = ComponentName.class;
                parameterTypes[1] = boolean.class;
                Method requestMetaKeyEventMethod = semWindowManager.getDeclaredMethod("requestMetaKeyEvent", parameterTypes);
                requestMetaKeyEventMethod.invoke(manager, requireActivity().getComponentName(), enabled);
            } else {
                LimeLog.warning("SemWindowManager.getInstance() returned null");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //    @Override
//    public void onUserLeaveHint() {
//        super.onUserLeaveHint();
//
//        // PiP is only supported on Oreo and later, and we don't need to manually enter PiP on
//        // Android S and later. On Android R, we will use onPictureInPictureRequested() instead.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//            if (autoEnterPip) {
//                try {
//                    // This has thrown all sorts of weird exceptions on Samsung devices
//                    // running Oreo. Just eat them and close gracefully on leave, rather
//                    // than crashing.
//                    requireActivity().enterPictureInPictureMode(getPictureInPictureParams(false));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    @Override
//    @TargetApi(Build.VERSION_CODES.R)
//    public boolean onPictureInPictureRequested() {
//        // Enter PiP when requested unless we're on Android 12 which supports auto-enter.
//        if (autoEnterPip && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            requireActivity().enterPictureInPictureMode(getPictureInPictureParams(false));
//        }
//        return true;
//    }
//
//
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        requireActivity().onWindowFocusChanged(hasFocus);

        // We can't guarantee the state of modifiers keys which may have
        // lifted while focus was not on us. Clear the modifier state.
        this.modifierFlags = 0;

        // With Android native pointer capture, capture is lost when focus is lost,
        // so it must be requested again when focus is regained.
        inputCaptureProvider.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return handleKeyDown(event) || super.requireActivity().onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return handleKeyMultiple(event) || super.requireActivity().onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return handleKeyUp(event) || super.requireActivity().onKeyUp(keyCode, event);
    }
//
//    @Override
//    public boolean onGenericMotionEvent(MotionEvent event) {
//        return handleMotionEvent(null, event) || super.onGenericMotionEvent(event);
//
//    }

    private boolean isRefreshRateEqualMatch(float refreshRate) {
        return refreshRate >= prefConfig.fps &&
                refreshRate <= prefConfig.fps + 3;
    }

    private boolean isRefreshRateGoodMatch(float refreshRate) {
        return refreshRate >= prefConfig.fps &&
                Math.round(refreshRate) % prefConfig.fps <= 3;
    }

    private boolean shouldIgnoreInsetsForResolution(int width, int height) {
        // Never ignore insets for non-native resolutions
        if (!PreferenceConfiguration.isNativeResolution(width, height)) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            for (Display.Mode candidate : display.getSupportedModes()) {
                // Ignore insets if this is an exact match for the display resolution
                if ((width == candidate.getPhysicalWidth() && height == candidate.getPhysicalHeight()) ||
                        (height == candidate.getPhysicalWidth() && width == candidate.getPhysicalHeight())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean mayReduceRefreshRate() {
        return prefConfig.framePacing == PreferenceConfiguration.FRAME_PACING_CAP_FPS ||
                prefConfig.framePacing == PreferenceConfiguration.FRAME_PACING_MAX_SMOOTHNESS ||
                (prefConfig.framePacing == PreferenceConfiguration.FRAME_PACING_BALANCED && prefConfig.reduceRefreshRate);
    }

    private float prepareDisplayForRendering() {
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams windowLayoutParams = requireActivity().getWindow().getAttributes();
        float displayRefreshRate;

        // On M, we can explicitly set the optimal display mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Display.Mode bestMode = display.getMode();
            boolean isNativeResolutionStream = PreferenceConfiguration.isNativeResolution(prefConfig.width, prefConfig.height);
            boolean refreshRateIsGood = isRefreshRateGoodMatch(bestMode.getRefreshRate());
            boolean refreshRateIsEqual = isRefreshRateEqualMatch(bestMode.getRefreshRate());

            LimeLog.info("Current display mode: " + bestMode.getPhysicalWidth() + "x" +
                    bestMode.getPhysicalHeight() + "x" + bestMode.getRefreshRate());

            for (Display.Mode candidate : display.getSupportedModes()) {
                boolean refreshRateReduced = candidate.getRefreshRate() < bestMode.getRefreshRate();
                boolean resolutionReduced = candidate.getPhysicalWidth() < bestMode.getPhysicalWidth() ||
                        candidate.getPhysicalHeight() < bestMode.getPhysicalHeight();
                boolean resolutionFitsStream = candidate.getPhysicalWidth() >= prefConfig.width &&
                        candidate.getPhysicalHeight() >= prefConfig.height;

                LimeLog.info("Examining display mode: " + candidate.getPhysicalWidth() + "x" +
                        candidate.getPhysicalHeight() + "x" + candidate.getRefreshRate());

                if (candidate.getPhysicalWidth() > 4096 && prefConfig.width <= 4096) {
                    // Avoid resolutions options above 4K to be safe
                    continue;
                }

                // On non-4K streams, we force the resolution to never change unless it's above
                // 60 FPS, which may require a resolution reduction due to HDMI bandwidth limitations,
                // or it's a native resolution stream.
                if (prefConfig.width < 3840 && prefConfig.fps <= 60 && !isNativeResolutionStream) {
                    if (display.getMode().getPhysicalWidth() != candidate.getPhysicalWidth() ||
                            display.getMode().getPhysicalHeight() != candidate.getPhysicalHeight()) {
                        continue;
                    }
                }

                // Make sure the resolution doesn't regress unless if it's over 60 FPS
                // where we may need to reduce resolution to achieve the desired refresh rate.
                if (resolutionReduced && !(prefConfig.fps > 60 && resolutionFitsStream)) {
                    continue;
                }

                if (mayReduceRefreshRate() && refreshRateIsEqual && !isRefreshRateEqualMatch(candidate.getRefreshRate())) {
                    // If we had an equal refresh rate and this one is not, skip it. In min latency
                    // mode, we want to always prefer the highest frame rate even though it may cause
                    // microstuttering.
                    continue;
                } else if (refreshRateIsGood) {
                    // We've already got a good match, so if this one isn't also good, it's not
                    // worth considering at all.
                    if (!isRefreshRateGoodMatch(candidate.getRefreshRate())) {
                        continue;
                    }

                    if (mayReduceRefreshRate()) {
                        // User asked for the lowest possible refresh rate, so don't raise it if we
                        // have a good match already
                        if (candidate.getRefreshRate() > bestMode.getRefreshRate()) {
                            continue;
                        }
                    } else {
                        // User asked for the highest possible refresh rate, so don't reduce it if we
                        // have a good match already
                        if (refreshRateReduced) {
                            continue;
                        }
                    }
                } else if (!isRefreshRateGoodMatch(candidate.getRefreshRate())) {
                    // We didn't have a good match and this match isn't good either, so just don't
                    // reduce the refresh rate.
                    if (refreshRateReduced) {
                        continue;
                    }
                } else {
                    // We didn't have a good match and this match is good. Prefer this refresh rate
                    // even if it reduces the refresh rate. Lowering the refresh rate can be beneficial
                    // when streaming a 60 FPS stream on a 90 Hz device. We want to select 60 Hz to
                    // match the frame rate even if the active display mode is 90 Hz.
                }

                bestMode = candidate;
                refreshRateIsGood = isRefreshRateGoodMatch(candidate.getRefreshRate());
                refreshRateIsEqual = isRefreshRateEqualMatch(candidate.getRefreshRate());
            }

            LimeLog.info("Best display mode: " + bestMode.getPhysicalWidth() + "x" +
                    bestMode.getPhysicalHeight() + "x" + bestMode.getRefreshRate());

            // Only apply new window layout parameters if we've actually changed the display mode
            if (display.getMode().getModeId() != bestMode.getModeId()) {
                // If we only changed refresh rate and we're on an OS that supports Surface.setFrameRate()
                // use that instead of using preferredDisplayModeId to avoid the possibility of triggering
                // bugs that can cause the system to switch from 4K60 to 4K24 on Chromecast 4K.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        display.getMode().getPhysicalWidth() != bestMode.getPhysicalWidth() ||
                        display.getMode().getPhysicalHeight() != bestMode.getPhysicalHeight()) {
                    // Apply the display mode change
                    windowLayoutParams.preferredDisplayModeId = bestMode.getModeId();
                    requireActivity().getWindow().setAttributes(windowLayoutParams);
                } else {
                    LimeLog.info("Using setFrameRate() instead of preferredDisplayModeId due to matching resolution");
                }
            } else {
                LimeLog.info("Current display mode is already the best display mode");
            }

            displayRefreshRate = bestMode.getRefreshRate();
        }
        // On L, we can at least tell the OS that we want a refresh rate
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float bestRefreshRate = display.getRefreshRate();
            for (float candidate : display.getSupportedRefreshRates()) {
                LimeLog.info("Examining refresh rate: " + candidate);

                if (candidate > bestRefreshRate) {
                    // Ensure the frame rate stays around 60 Hz for <= 60 FPS streams
                    if (prefConfig.fps <= 60) {
                        if (candidate >= 63) {
                            continue;
                        }
                    }

                    bestRefreshRate = candidate;
                }
            }

            LimeLog.info("Selected refresh rate: " + bestRefreshRate);
            windowLayoutParams.preferredRefreshRate = bestRefreshRate;
            displayRefreshRate = bestRefreshRate;

            // Apply the refresh rate change
            requireActivity().getWindow().setAttributes(windowLayoutParams);
        } else {
            // Otherwise, the active display refresh rate is just
            // whatever is currently in use.
            displayRefreshRate = display.getRefreshRate();
        }

        // From 4.4 to 5.1 we can't ask for a 4K display mode, so we'll
        // need to hint the OS to provide one.
        boolean aspectRatioMatch = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // On KitKat and later (where we can use the whole screen via immersive mode), we'll
            // calculate whether we need to scale by aspect ratio or not. If not, we'll use
            // setFixedSize so we can handle 4K properly. The only known devices that have
            // >= 4K screens have exactly 4K screens, so we'll be able to hit this good path
            // on these devices. On Marshmallow, we can start changing to 4K manually but no
            // 4K devices run 6.0 at the moment.
            Point screenSize = new Point(0, 0);
            display.getSize(screenSize);

            double screenAspectRatio = ((double) screenSize.y) / screenSize.x;
            double streamAspectRatio = ((double) prefConfig.height) / prefConfig.width;
            if (Math.abs(screenAspectRatio - streamAspectRatio) < 0.001) {
                LimeLog.info("Stream has compatible aspect ratio with output display");
                aspectRatioMatch = true;
            }
        }

        if (prefConfig.stretchVideo || aspectRatioMatch) {
            // Set the surface to the size of the video
            streamView.getHolder().setFixedSize(prefConfig.width, prefConfig.height);
        } else {
            // Set the surface to scale based on the aspect ratio of the stream
            streamView.setDesiredAspectRatio((double) prefConfig.width / (double) prefConfig.height);
        }

        // Set the desired refresh rate that will get passed into setFrameRate() later
        desiredRefreshRate = displayRefreshRate;

        if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION) ||
                requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            // TVs may take a few moments to switch refresh rates, and we can probably assume
            // it will be eventually activated.
            // TODO: Improve this
            return displayRefreshRate;
        } else {
            // Use the lower of the current refresh rate and the selected refresh rate.
            // The preferred refresh rate may not actually be applied (ex: Battery Saver mode).
            return Math.min(requireActivity().getWindowManager().getDefaultDisplay().getRefreshRate(), displayRefreshRate);
        }
    }

    @SuppressLint("InlinedApi")
    private final Runnable hideSystemUi = new Runnable() {
        @Override
        public void run() {
            // TODO: Do we want to use WindowInsetsController here on R+ instead of
            // SYSTEM_UI_FLAG_IMMERSIVE_STICKY? They seem to do the same thing as of S...

            if(getActivity() == null) {
                if(returnCallback != null) {
                    returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                }

                return;
            }

            // In multi-window mode on N+, we need to drop our layout flags or we'll
            // be drawing underneath the system UI.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && requireActivity().isInMultiWindowMode()) {
                requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            // Use immersive mode on 4.4+ or standard low profile on previous builds
            else {
                requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    };

    private void hideSystemUi(int delay) {
        Handler h = requireActivity().getWindow().getDecorView().getHandler();
        if (h != null) {
            h.removeCallbacks(hideSystemUi);
            h.postDelayed(hideSystemUi, delay);
        }
    }

//    @Override
//    @TargetApi(Build.VERSION_CODES.N)
//    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
//        super.onMultiWindowModeChanged(isInMultiWindowMode);
//
//        // In multi-window, we don't want to use the full-screen layout
//        // flag. It will cause us to collide with the system UI.
//        // This function will also be called for PiP so we can cover
//        // that case here too.
//        if (isInMultiWindowMode) {
//            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//            // Disable performance optimizations for foreground
//            requireActivity().getWindow().setSustainedPerformanceMode(false);
//            decoderRenderer.notifyVideoBackground();
//        } else {
//            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//            // Enable performance optimizations for foreground
//            requireActivity().getWindow().setSustainedPerformanceMode(true);
//            decoderRenderer.notifyVideoForeground();
//        }
//
//        // Correct the system UI visibility flags
//        hideSystemUi(50);
//    }

    @Override
    public void onStop() {
        super.onStop();

        Dialog.closeDialogs();

        if (virtualController != null) {
            virtualController.hide();
        }

        if (conn != null) {
            int videoFormat = decoderRenderer.getActiveVideoFormat();

            displayedFailureDialog = true;
            stopConnection();

            InputManager inputManager = (InputManager) requireActivity().getSystemService(Context.INPUT_SERVICE);
            if (controllerHandler != null) {
                inputManager.unregisterInputDeviceListener(controllerHandler);
            }
            if (keyboardTranslator != null) {
                inputManager.unregisterInputDeviceListener(keyboardTranslator);
            }

            if (lowLatencyWifiLock != null) {
                lowLatencyWifiLock.release();
            }
            if (highPerfWifiLock != null) {
                highPerfWifiLock.release();
            }

            // Destroy the capture provider
            inputCaptureProvider.destroy();

            if (prefConfig.enableLatencyToast) {
                int averageEndToEndLat = decoderRenderer.getAverageEndToEndLatency();
                int averageDecoderLat = decoderRenderer.getAverageDecoderLatency();
                String message = null;
                if (averageEndToEndLat > 0) {
                    message = getResources().getString(R.string.conn_client_latency) + " " + averageEndToEndLat + " ms";
                    if (averageDecoderLat > 0) {
                        message += " (" + getResources().getString(R.string.conn_client_latency_hw) + " " + averageDecoderLat + " ms)";
                    }
                } else if (averageDecoderLat > 0) {
                    message = getResources().getString(R.string.conn_hardware_latency) + " " + averageDecoderLat + " ms";
                }
            }

            // Clear the tombstone count if we terminated normally
            if (!reportedCrash && tombstonePrefs.getInt("CrashCount", 0) != 0) {
                tombstonePrefs.edit()
                        .putInt("CrashCount", 0)
                        .putInt("LastNotifiedCrashCount", 0)
                        .apply();
            }
        }

//        requireActivity().finish();
    }

    private void setInputGrabState(boolean grab) {
        // Grab/ungrab the mouse cursor


//        if (grab) {
//            inputCaptureProvider.enableCapture();
//        } else {
//            inputCaptureProvider.disableCapture();
//        }
//
//        // Grab/ungrab system keyboard shortcuts
//        setMetaKeyCaptureState(grab);

        grabbedInput = grab;
    }

    private final Runnable toggleGrab = new Runnable() {
        @Override
        public void run() {
            if(getActivity() == null) {
                if(returnCallback != null) {
                    returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                }

                return;
            }

            setInputGrabState(!grabbedInput);
        }
    };

    // Returns true if the key stroke was consumed
    private boolean handleSpecialKeys(int androidKeyCode, boolean down) {
        int modifierMask = 0;

        if (androidKeyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                androidKeyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
            modifierMask = KeyboardPacket.MODIFIER_CTRL;
        } else if (androidKeyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
                androidKeyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            modifierMask = KeyboardPacket.MODIFIER_SHIFT;
        } else if (androidKeyCode == KeyEvent.KEYCODE_ALT_LEFT ||
                androidKeyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            modifierMask = KeyboardPacket.MODIFIER_ALT;
        } else if (androidKeyCode == KeyEvent.KEYCODE_META_LEFT ||
                androidKeyCode == KeyEvent.KEYCODE_META_RIGHT) {
            modifierMask = KeyboardPacket.MODIFIER_META;
        }

        if (down) {
            this.modifierFlags |= modifierMask;
        } else {
            this.modifierFlags &= ~modifierMask;
        }

        // Check if Ctrl+Alt+Shift+Z is pressed
        if (androidKeyCode == KeyEvent.KEYCODE_Z &&
                (modifierFlags & (KeyboardPacket.MODIFIER_CTRL | KeyboardPacket.MODIFIER_ALT | KeyboardPacket.MODIFIER_SHIFT)) ==
                        (KeyboardPacket.MODIFIER_CTRL | KeyboardPacket.MODIFIER_ALT | KeyboardPacket.MODIFIER_SHIFT)) {
            if (down) {
                // Now that we've pressed the magic combo
                // we'll wait for one of the keys to come up
                grabComboDown = true;
            } else {
                // Toggle the grab if Z comes up
                Handler h = requireActivity().getWindow().getDecorView().getHandler();
                if (h != null) {
                    h.postDelayed(toggleGrab, 250);
                }

                grabComboDown = false;
            }

            return true;
        }
        // Toggle the grab if control or shift comes up
        else if (grabComboDown) {
            Handler h = requireActivity().getWindow().getDecorView().getHandler();
            if (h != null) {
                h.postDelayed(toggleGrab, 250);
            }

            grabComboDown = false;
            return true;
        }

        // Not a special combo
        return false;
    }

    // We cannot simply use modifierFlags for all key event processing, because
    // some IMEs will not generate real key events for pressing Shift. Instead
    // they will simply send key events with isShiftPressed() returning true,
    // and we will need to send the modifier flag ourselves.
    private byte getModifierState(KeyEvent event) {
        // Start with the global modifier state to ensure we cover the case
        // detailed in https://github.com/moonlight-stream/moonlight-android/issues/840
        byte modifier = getModifierState();
        if (event.isShiftPressed()) {
            modifier |= KeyboardPacket.MODIFIER_SHIFT;
        }
        if (event.isCtrlPressed()) {
            modifier |= KeyboardPacket.MODIFIER_CTRL;
        }
        if (event.isAltPressed()) {
            modifier |= KeyboardPacket.MODIFIER_ALT;
        }
        if (event.isMetaPressed()) {
            modifier |= KeyboardPacket.MODIFIER_META;
        }
        return modifier;
    }

    private byte getModifierState() {
        return (byte) modifierFlags;
    }
    @Override
    public boolean handleKeyDown(KeyEvent event) {
        // Pass-through virtual navigation keys
        if ((event.getFlags() & KeyEvent.FLAG_VIRTUAL_HARD_KEY) != 0) {
            return false;
        }

        // Handle a synthetic back button event that some Android OS versions
        // create as a result of a right-click. This event WILL repeat if
        // the right mouse button is held down, so we ignore those.
        int eventSource = event.getSource();
        if ((eventSource == InputDevice.SOURCE_MOUSE ||
                eventSource == InputDevice.SOURCE_MOUSE_RELATIVE) &&
                event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            // Send the right mouse button event if mouse back and forward
            // are disabled. If they are enabled, handleMotionEvent() will take
            // care of this.
            if (!prefConfig.mouseNavButtons) {
                conn.sendMouseButtonDown(MouseButtonPacket.BUTTON_RIGHT);
            }

            // Always return true, otherwise the back press will be propagated
            // up to the parent and finish the activity.
            return true;
        }

        boolean handled = false;

        if (ControllerHandler.isGameControllerDevice(event.getDevice())) {
            // Always try the controller handler first, unless it's an alphanumeric keyboard device.
            // Otherwise, controller handler will eat keyboard d-pad events.
            handled = controllerHandler.handleButtonDown(event);
        }

        // Try the keyboard handler if it wasn't handled as a game controller
        if (!handled) {
            // Let this method take duplicate key down events
            if (handleSpecialKeys(event.getKeyCode(), true)) {
                return true;
            }

            // Pass through keyboard input if we're not grabbing
            if (!grabbedInput) {
                return false;
            }

            // We'll send it as a raw key event if we have a key mapping, otherwise we'll send it
            // as UTF-8 text (if it's a printable character).
            short translated = keyboardTranslator.translate(event.getKeyCode(), event.getDeviceId());
            if (translated == 0) {
                // Make sure it has a valid Unicode representation and it's not a dead character
                // (which we don't support). If those are true, we can send it as UTF-8 text.
                //
                // NB: We need to be sure this happens before the getRepeatCount() check because
                // UTF-8 events don't auto-repeat on the host side.
                int unicodeChar = event.getUnicodeChar();
                if ((unicodeChar & KeyCharacterMap.COMBINING_ACCENT) == 0 && (unicodeChar & KeyCharacterMap.COMBINING_ACCENT_MASK) != 0) {
                    conn.sendUtf8Text("" + (char) unicodeChar);
                    return true;
                }

                return false;
            }

            // Eat repeat down events
            if (event.getRepeatCount() > 0) {
                return true;
            }

            conn.sendKeyboardInput(translated, KeyboardPacket.KEY_DOWN, getModifierState(event),
                    keyboardTranslator.hasNormalizedMapping(event.getKeyCode(), event.getDeviceId()) ? 0 : MoonBridge.SS_KBE_FLAG_NON_NORMALIZED);
        }

        return true;
    }

    @Override
    public boolean handleKeyUp(KeyEvent event) {
        // Pass-through virtual navigation keys
        if ((event.getFlags() & KeyEvent.FLAG_VIRTUAL_HARD_KEY) != 0) {
            return false;
        }

        // Handle a synthetic back button event that some Android OS versions
        // create as a result of a right-click.
        int eventSource = event.getSource();
        if ((eventSource == InputDevice.SOURCE_MOUSE ||
                eventSource == InputDevice.SOURCE_MOUSE_RELATIVE) &&
                event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            // Send the right mouse button event if mouse back and forward
            // are disabled. If they are enabled, handleMotionEvent() will take
            // care of this.
            if (!prefConfig.mouseNavButtons) {
                conn.sendMouseButtonUp(MouseButtonPacket.BUTTON_RIGHT);
            }

            // Always return true, otherwise the back press will be propagated
            // up to the parent and finish the activity.
            return true;
        }

        boolean handled = false;
        if (ControllerHandler.isGameControllerDevice(event.getDevice())) {
            // Always try the controller handler first, unless it's an alphanumeric keyboard device.
            // Otherwise, controller handler will eat keyboard d-pad events.
            handled = controllerHandler.handleButtonUp(event);
        }

        // Try the keyboard handler if it wasn't handled as a game controller
        if (!handled) {
            if (handleSpecialKeys(event.getKeyCode(), false)) {
                return true;
            }

            // Pass through keyboard input if we're not grabbing
            if (!grabbedInput) {
                return false;
            }

            short translated = keyboardTranslator.translate(event.getKeyCode(), event.getDeviceId());
            if (translated == 0) {
                // If we sent this event as UTF-8 on key down, also report that it was handled
                // when we get the key up event for it.
                int unicodeChar = event.getUnicodeChar();
                return (unicodeChar & KeyCharacterMap.COMBINING_ACCENT) == 0 && (unicodeChar & KeyCharacterMap.COMBINING_ACCENT_MASK) != 0;
            }

            conn.sendKeyboardInput(translated, KeyboardPacket.KEY_UP, getModifierState(event),
                    keyboardTranslator.hasNormalizedMapping(event.getKeyCode(), event.getDeviceId()) ? 0 : MoonBridge.SS_KBE_FLAG_NON_NORMALIZED);
        }

        return true;
    }

    private boolean handleKeyMultiple(KeyEvent event) {
        // We can receive keys from a software keyboard that don't correspond to any existing
        // KEYCODE value. Android will give those to us as an ACTION_MULTIPLE KeyEvent.
        //
        // Despite the fact that the Android docs say this is unused since API level 29, these
        // events are still sent as of Android 13 for the above case.
        //
        // For other cases of ACTION_MULTIPLE, we will not report those as handled so hopefully
        // they will be passed to us again as regular singular key events.
        if (event.getKeyCode() != KeyEvent.KEYCODE_UNKNOWN || event.getCharacters() == null) {
            return false;
        }

        conn.sendUtf8Text(event.getCharacters());
        return true;
    }

    private TouchContext getTouchContext(int actionIndex) {
        if (actionIndex < 2) {

            if(UserData.currentLayout.isTouchTrackpad) {

                return touchContextMapRef[actionIndex];
            } else {
                return touchContextMapAbs[actionIndex];
            }

        } else {
            return null;
        }
    }

    @Override
    public void toggleKeyboard() {
        LimeLog.info("Toggling keyboard overlay");
        InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);
    }

    // Returns true if the event was consumed
    // NB: View is only present if called from a view callback


    private boolean handleMotionEvent(View view, MotionEvent event) {
        // Pass through keyboard input if we're not grabbing
        if (!grabbedInput) {
            return false;
        }

                if (virtualController != null &&
                        (virtualController.getControllerMode() == VirtualController.ControllerMode.MoveButtons ||
                                virtualController.getControllerMode() == VirtualController.ControllerMode.ResizeButtons)) {
                    // Ignore presses when the virtual controller is being configured
                    return true;
                }

                // If this is the parent view, we'll offset our coordinates to appear as if they
                // are relative to the StreamView like our StreamView touch events are.
                float xOffset, yOffset;
                if (!UserData.currentLayout.isTouchTrackpad) {
                    xOffset = 0;
                    yOffset = -view.getY();
                } else {
                    xOffset = 0.f;
                    yOffset = 0.f;
                }

                int actionIndex = event.getActionIndex();

                int eventX = (int) (event.getX(actionIndex) + xOffset);
                int eventY = (int) (event.getY(actionIndex) + yOffset);

                // Special handling for 3 finger gesture
                if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN &&
                        event.getPointerCount() == 3) {
                    // Three fingers down
                    threeFingerDownTime = event.getEventTime();

                    // Cancel the first and second touches to avoid
                    // erroneous events
                    for (TouchContext aTouchContext : touchContextMapRef) {
                        aTouchContext.cancelTouch();
                    }

                    for (TouchContext aTouchContext : touchContextMapAbs) {
                        aTouchContext.cancelTouch();
                    }

                    return true;
                }

                TouchContext context = getTouchContext(actionIndex);
                if (context == null) {
                    return false;
                }

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_DOWN:
                        for (TouchContext touchContext : UserData.currentLayout.isTouchTrackpad ? touchContextMapRef : touchContextMapAbs) {
                            touchContext.setPointerCount(event.getPointerCount());
                        }
                        context.touchDownEvent(eventX, eventY, event.getEventTime(), true);
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        if (event.getPointerCount() == 1 &&
                                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || (event.getFlags() & MotionEvent.FLAG_CANCELED) == 0)) {
                            // All fingers up
                            if (event.getEventTime() - threeFingerDownTime < THREE_FINGER_TAP_THRESHOLD) {
                                // This is a 3 finger tap to bring up the keyboard
                                toggleKeyboard();
                                return true;
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (event.getFlags() & MotionEvent.FLAG_CANCELED) != 0) {
                            context.cancelTouch();
                        } else {
                            context.touchUpEvent(eventX, eventY, event.getEventTime());
                        }

                        for (TouchContext touchContext : UserData.currentLayout.isTouchTrackpad ? touchContextMapRef : touchContextMapAbs) {
                            touchContext.setPointerCount(event.getPointerCount() - 1);
                        }
                        if (actionIndex == 0 && event.getPointerCount() > 1 && !context.isCancelled()) {
                            // The original secondary touch now becomes primary
                            context.touchDownEvent(
                                    (int) (event.getX(1) + xOffset),
                                    (int) (event.getY(1) + yOffset),
                                    event.getEventTime(), false);
                        }

                        break;
                    case MotionEvent.ACTION_MOVE:
                        // ACTION_MOVE is special because it always has actionIndex == 0
                        // We'll call the move handlers for all indexes manually

                            // First process the historical events
                            for (int i = 0; i < event.getHistorySize(); i++) {
                                for (TouchContext aTouchContextMap : UserData.currentLayout.isTouchTrackpad ? touchContextMapRef : touchContextMapAbs) {
                                    if (aTouchContextMap.getActionIndex() < event.getPointerCount()) {
                                        aTouchContextMap.touchMoveEvent(
                                                (int) (event.getHistoricalX(aTouchContextMap.getActionIndex(), i) + xOffset),
                                                (int) (event.getHistoricalY(aTouchContextMap.getActionIndex(), i) + yOffset),
                                                event.getHistoricalEventTime(i));
                                    }
                                }
                            }

                            // Now process the current values
                            for (TouchContext aTouchContextMap : UserData.currentLayout.isTouchTrackpad ? touchContextMapRef : touchContextMapAbs) {
                                if (aTouchContextMap.getActionIndex() < event.getPointerCount()) {
                                    aTouchContextMap.touchMoveEvent(
                                            (int) (event.getX(aTouchContextMap.getActionIndex()) + xOffset),
                                            (int) (event.getY(aTouchContextMap.getActionIndex()) + yOffset),
                                            event.getEventTime());
                                }
                            }

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        for (TouchContext aTouchContext : UserData.currentLayout.isTouchTrackpad ? touchContextMapRef : touchContextMapAbs) {
                            aTouchContext.cancelTouch();
                            aTouchContext.setPointerCount(0);
                        }
                        break;
                    default:
                        return true;
                }

        // Unknown class
        return true;
    }


    private void updateMousePosition(View touchedView, MotionEvent event) {
        // X and Y are already relative to the provided view object
        float eventX, eventY;

        // For our StreamView itself, we can use the coordinates unmodified.
        if (touchedView == streamView) {
            eventX = event.getX(0);
            eventY = event.getY(0);
        } else {
            // For the containing background view, we must subtract the origin
            // of the StreamView to get video-relative coordinates.
            eventX = event.getX(0) - streamView.getX();
            eventY = event.getY(0) - streamView.getY();
        }

        if (event.getPointerCount() == 1 && event.getActionIndex() == 0 &&
                (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER ||
                        event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS)) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_EXIT:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (event.getEventTime() - lastAbsTouchUpTime <= STYLUS_UP_DEAD_ZONE_DELAY &&
                            Math.sqrt(Math.pow(eventX - lastAbsTouchUpX, 2) + Math.pow(eventY - lastAbsTouchUpY, 2)) <= STYLUS_UP_DEAD_ZONE_RADIUS) {
                        // Enforce a small deadzone between touch up and hover or touch down to allow more precise double-clicking
                        return;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    if (event.getEventTime() - lastAbsTouchDownTime <= STYLUS_DOWN_DEAD_ZONE_DELAY &&
                            Math.sqrt(Math.pow(eventX - lastAbsTouchDownX, 2) + Math.pow(eventY - lastAbsTouchDownY, 2)) <= STYLUS_DOWN_DEAD_ZONE_RADIUS) {
                        // Enforce a small deadzone between touch down and move or touch up to allow more precise double-clicking
                        return;
                    }
                    break;
            }
        }

        // We may get values slightly outside our view region on ACTION_HOVER_ENTER and ACTION_HOVER_EXIT.
        // Normalize these to the view size. We can't just drop them because we won't always get an event
        // right at the boundary of the view, so dropping them would result in our cursor never really
        // reaching the sides of the screen.
        eventX = Math.min(Math.max(eventX, 0), streamView.getWidth());
        eventY = Math.min(Math.max(eventY, 0), streamView.getHeight());

        conn.sendMousePosition((short) eventX, (short) eventY, (short) streamView.getWidth(), (short) streamView.getHeight());

    }

    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        return requireActivity().onGenericMotionEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Tell the OS not to buffer input events for us
                //
                // NB: This is still needed even when we call the newer requestUnbufferedDispatch()!
                view.requestUnbufferedDispatch(event);
            }
        }

        return handleMotionEvent(view, event);
    }

    @Override
    public void stageStarting(final String stage) {

        if(getActivity() == null) {
            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }
//        requireActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                if(getResources() == null) { return; }
//
//                if (spinner != null) {
//                    spinner.setMessage(getResources().getString(R.string.conn_starting) + " " + stage);
//                }
//            }
//        });
    }

    @Override
    public void stageComplete(String stage) {
    }

    private void stopConnection() {

        if(getActivity() == null) {

            launchOverlayController.end();

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        if (connecting || connected) {
            connecting = connected = false;
            updatePipAutoEnter();

            controllerHandler.stop();

            // Update GameManager state to indicate we're no longer in game
            UiHelper.notifyStreamEnded(requireActivity());

            // Stop may take a few hundred ms to do some network I/O to tell
            // the server we're going away and clean up. Let it run in a separate
            // thread to keep things smooth for the UI. Inside moonlight-common,
            // we prevent another thread from starting a connection before and
            // during the process of stopping this one.
            new Thread() {
                public void run() {
                    conn.stop();
                }
            }.start();
        }
    }

    @Override
    public void stageFailed(final String stage, final int portFlags, final int errorCode) {
        // Perform a connection test if the failure could be due to a blocked port
        // This does network I/O, so don't do it on the main thread.
        final int portTestResult = MoonBridge.testClientConnectivity(ServerHelper.CONNECTION_TEST_SERVER, 443, portFlags);

        if(getActivity() == null) {

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (spinner != null) {
                    spinner.dismiss();
                    spinner = null;
                }

                if (!displayedFailureDialog) {
                    displayedFailureDialog = true;
                    LimeLog.severe(stage + " failed: " + errorCode);

                    // If video initialization failed and the surface is still valid, display extra information for the user
                    if (stage.contains("video") && streamView.getHolder().getSurface().isValid()) {
                        Toast.makeText(requireActivity(), getResources().getText(R.string.video_decoder_init_failed), Toast.LENGTH_LONG).show();
                    }

                    String dialogText = getResources().getString(R.string.conn_error_msg) + " " + stage + " (error " + errorCode + ")";

                    if (portFlags != 0) {
                        dialogText += "\n\n" + getResources().getString(R.string.check_ports_msg) + "\n" +
                                MoonBridge.stringifyPortFlags(portFlags, "\n");
                    }

                    if (portTestResult != MoonBridge.ML_TEST_RESULT_INCONCLUSIVE && portTestResult != 0) {
                        dialogText += "\n\n" + getResources().getString(R.string.nettest_text_blocked);
                    }

                    Dialog.displayDialog(requireActivity(), getResources().getString(R.string.conn_error_title), dialogText, true);
                }
            }
        });
    }

    @Override
    public void connectionTerminated(final int errorCode) {
        // Perform a connection test if the failure could be due to a blocked port
        // This does network I/O, so don't do it on the main thread.
        final int portFlags = MoonBridge.getPortFlagsFromTerminationErrorCode(errorCode);
        final int portTestResult = MoonBridge.testClientConnectivity(ServerHelper.CONNECTION_TEST_SERVER, 443, portFlags);

        if(getActivity() == null) {

            launchOverlayController.end();

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Let the display go to sleep now
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Ungrab input
                setInputGrabState(false);

                if (!displayedFailureDialog) {
                    displayedFailureDialog = true;
                    LimeLog.severe("Connection terminated: " + errorCode);
                    stopConnection();

                    // Display the error dialog if it was an unexpected termination.
                    // Otherwise, just finish the activity immediately.
                    if (errorCode != MoonBridge.ML_ERROR_GRACEFUL_TERMINATION) {
                        String message;

                        if (portTestResult != MoonBridge.ML_TEST_RESULT_INCONCLUSIVE && portTestResult != 0) {
                            // If we got a blocked result, that supersedes any other error message
                            message = getResources().getString(R.string.nettest_text_blocked);
                        } else {
                            switch (errorCode) {
                                case MoonBridge.ML_ERROR_NO_VIDEO_TRAFFIC:
                                    message = getResources().getString(R.string.no_video_received_error);
                                    break;

                                case MoonBridge.ML_ERROR_NO_VIDEO_FRAME:
                                    message = getResources().getString(R.string.no_frame_received_error);
                                    break;

                                case MoonBridge.ML_ERROR_UNEXPECTED_EARLY_TERMINATION:
                                case MoonBridge.ML_ERROR_PROTECTED_CONTENT:
                                    message = getResources().getString(R.string.early_termination_error);
                                    break;

                                case MoonBridge.ML_ERROR_FRAME_CONVERSION:
                                    message = getResources().getString(R.string.frame_conversion_error);
                                    break;

                                default:
                                    message = getResources().getString(R.string.conn_terminated_msg);
                                    break;
                            }
                        }

                        if (portFlags != 0) {
                            message += "\n\n" + getResources().getString(R.string.check_ports_msg) + "\n" +
                                    MoonBridge.stringifyPortFlags(portFlags, "\n");
                        }

                        Dialog.displayDialog(requireActivity(), getResources().getString(R.string.conn_terminated_title),
                                message, true);
                    } else {

                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction trans = fragmentManager.beginTransaction();
                        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                        trans.replace(R.id.nav_host_fragment, new LaunchComputerList(returnCallback)).commit();

                    }
                }
            }
        });
    }

    @Override
    public void connectionStatusUpdate(final int connectionStatus) {

        if(getActivity() == null) {

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (prefConfig.disableWarnings) {
                    return;
                }

                if (connectionStatus == MoonBridge.CONN_STATUS_POOR) {
                    if (prefConfig.bitrate > 5000) {
                        notificationOverlayView.setText(getResources().getString(R.string.slow_connection_msg));
                    } else {
                        notificationOverlayView.setText(getResources().getString(R.string.poor_connection_msg));
                    }

                    requestedNotificationOverlayVisibility = View.VISIBLE;
                } else if (connectionStatus == MoonBridge.CONN_STATUS_OKAY) {
                    requestedNotificationOverlayVisibility = View.GONE;
                }

                if (!isHidingOverlays) {
                    notificationOverlayView.setVisibility(requestedNotificationOverlayVisibility);
                }
            }
        });
    }

    @Override
    public void connectionStarted() {
        if(getActivity() == null) {

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(UserData.timesConnected < 3) {
                    Snackbar s = Snackbar.make(root, R.string.Move_the_mouse, BaseTransientBottomBar.LENGTH_INDEFINITE);

                    s.setAction(R.string.Got_it, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            s.dismiss();
                        }
                    });

                    s.show();
                }

                if (spinner != null) {
                    spinner.dismiss();
                    spinner = null;
                }

                connected = true;
                connecting = false;
                updatePipAutoEnter();

                // Hide the mouse cursor now after a short delay.
                // Doing it before dismissing the spinner seems to be undone
                // when the spinner gets displayed. On Android Q, even now
                // is too early to capture. We will delay a second to allow
                // the spinner to dismiss before capturing.
                Handler h = new Handler();

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(getActivity() == null) {

                            if(returnCallback != null) {
                                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                            }

                            return;
                        }

                        setInputGrabState(true);
                    }
                }, 500);

                Context c = getContext();
                if(c != null) {
                    // Keep the display on
                    requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    // Update GameManager state to indicate we're in game
                    UiHelper.notifyStreamConnected(requireActivity());


                    loadingView.setVisibility(View.GONE);

                    hideSystemUi(1000);

                    launchOverlayController.start();
                }
            }
        });
    }

    @Override
    public void displayMessage(final String message) {
        if(getActivity() == null) {

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void displayTransientMessage(final String message) {
        if (!prefConfig.disableWarnings) {
            if(getActivity() == null) {

                if(returnCallback != null) {
                    returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
                }

                return;
            }
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void rumble(short controllerNumber, short lowFreqMotor, short highFreqMotor) {
        LimeLog.info(String.format((Locale) null, "Rumble on gamepad %d: %04x %04x", controllerNumber, lowFreqMotor, highFreqMotor));

        controllerHandler.handleRumble(controllerNumber, lowFreqMotor, highFreqMotor);
    }

    @Override
    public void rumbleTriggers(short controllerNumber, short leftTrigger, short rightTrigger) {

    }

    @Override
    public void setHdrMode(boolean enabled, byte[] hdrMetadata) {
        LimeLog.info("Display HDR mode: " + (enabled ? "enabled" : "disabled"));
        decoderRenderer.setHdrMode(enabled, hdrMetadata);
    }

    @Override
    public void setMotionEventState(short controllerNumber, byte motionType, short reportRateHz) {

    }

    @Override
    public void setControllerLED(short controllerNumber, byte r, byte g, byte b) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!surfaceCreated) {
            throw new IllegalStateException("Surface changed before creation!");
        }

        if (!attemptedConnection) {
            attemptedConnection = true;

            // Update GameManager state to indicate we're "loading" while connecting
            UiHelper.notifyStreamConnecting(requireActivity());

            decoderRenderer.setRenderTarget(holder);
            conn.start(new AndroidAudioRenderer(requireActivity(), prefConfig.enableAudioFx),
                    decoderRenderer, LaunchPlayScreen.this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        float desiredFrameRate;

        surfaceCreated = true;

        // Android will pick the lowest matching refresh rate for a given frame rate value, so we want
        // to report the true FPS value if refresh rate reduction is enabled. We also report the true
        // FPS value if there's no suitable matching refresh rate. In that case, Android could try to
        // select a lower refresh rate that avoids uneven pull-down (ex: 30 Hz for a 60 FPS stream on
        // a display that maxes out at 50 Hz).
        if (mayReduceRefreshRate() || desiredRefreshRate < prefConfig.fps) {
            desiredFrameRate = prefConfig.fps;
        } else {
            // Otherwise, we will pretend that our frame rate matches the refresh rate we picked in
            // prepareDisplayForRendering(). This will usually be the highest refresh rate that our
            // frame rate evenly divides into, which ensures the lowest possible display latency.
            desiredFrameRate = desiredRefreshRate;
        }

        // Tell the OS about our frame rate to allow it to adapt the display refresh rate appropriately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // We want to change frame rate even if it's not seamless, since prepareDisplayForRendering()
            // will not set the display mode on S+ if it only differs by the refresh rate. It depends
            // on us to trigger the frame rate switch here.
            holder.getSurface().setFrameRate(desiredFrameRate,
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                    Surface.CHANGE_FRAME_RATE_ALWAYS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            holder.getSurface().setFrameRate(desiredFrameRate,
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!surfaceCreated) {
            throw new IllegalStateException("Surface destroyed before creation!");
        }

        launchOverlayController.end();

        if (attemptedConnection) {
            // Let the decoder know immediately that the surface is gone
            decoderRenderer.prepareForStop();

            if (connected) {
                stopConnection();
            }
        }

    }

    @Override
    public void mouseMove(int deltaX, int deltaY) {
        conn.sendMouseMove((short) deltaX, (short) deltaY);
    }

    @Override
    public void mouseButtonEvent(int buttonId, boolean down) {
        byte buttonIndex;

        switch (buttonId) {
            case EvdevListener.BUTTON_LEFT:
                buttonIndex = MouseButtonPacket.BUTTON_LEFT;
                break;
            case EvdevListener.BUTTON_MIDDLE:
                buttonIndex = MouseButtonPacket.BUTTON_MIDDLE;
                break;
            case EvdevListener.BUTTON_RIGHT:
                buttonIndex = MouseButtonPacket.BUTTON_RIGHT;
                break;
            case EvdevListener.BUTTON_X1:
                buttonIndex = MouseButtonPacket.BUTTON_X1;
                break;
            case EvdevListener.BUTTON_X2:
                buttonIndex = MouseButtonPacket.BUTTON_X2;
                break;
            default:
                LimeLog.warning("Unhandled button: " + buttonId);
                return;
        }

        if (down) {
            conn.sendMouseButtonDown(buttonIndex);
        } else {
            conn.sendMouseButtonUp(buttonIndex);
        }
    }

    @Override
    public void mouseVScroll(byte amount) {
        conn.sendMouseScroll(amount);
    }

    @Override
    public void mouseHScroll(byte amount) {
        conn.sendMouseHScroll(amount);
    }

    @Override
    public void keyboardEvent(boolean buttonDown, short keyCode) {
        Log.i("keyboard", "keyevent");

        short keyMap = keyboardTranslator.translate(keyCode, -1);
        if (keyMap != 0) {
            // handleSpecialKeys() takes the Android keycode
            if (handleSpecialKeys(keyCode, buttonDown)) {
                return;
            }

            if (buttonDown) {
                conn.sendKeyboardInput(keyMap, KeyboardPacket.KEY_DOWN, getModifierState(), (byte) 0);
            } else {
                conn.sendKeyboardInput(keyMap, KeyboardPacket.KEY_UP, getModifierState(), (byte) 0);
            }
        }
    }
//
//    @Override
//    public void onSystemUiVisibilityChange(int visibility) {
//        // Don't do anything if we're not connected
//        if (!connected) {
//            return;
//        }
//
//        // This flag is set for all devices
//        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//            hideSystemUi(2000);
//        }
//        // This flag is only set on 4.4+
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
//                (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
//            hideSystemUi(2000);
//        }
//        // This flag is only set before 4.4+
//        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
//                (visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0) {
//            hideSystemUi(2000);
//        }
//    }

    @Override
    public void onPerfUpdate(final String text) {
        if(getActivity() == null) {

            if(returnCallback != null) {
                returnCallback.onChange(UserData.LAUNCH_COMPUTER_LIST);
            }

            return;
        }

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                performanceOverlayView.setText(text);
            }
        });
    }

    @Override
    public void onUsbPermissionPromptStarting() {
        // Disable PiP auto-enter while the USB permission prompt is on-screen. This prevents
        // us from entering PiP while the user is interacting with the OS permission dialog.
        suppressPipRefCount++;
        updatePipAutoEnter();
    }

    @Override
    public void onUsbPermissionPromptCompleted() {
        suppressPipRefCount--;
        updatePipAutoEnter();
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        switch (keyEvent.getAction()) {
            case KeyEvent.ACTION_DOWN:
                return handleKeyDown(keyEvent);
            case KeyEvent.ACTION_UP:
                return handleKeyUp(keyEvent);
            case KeyEvent.ACTION_MULTIPLE:
                return handleKeyMultiple(keyEvent);
            default:
                return false;
        }
    }
}

