package com.gingertech.starbeam;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.BluetoothController;
import com.gingertech.starbeam.helpers.controllers.FireFunctions;
import com.gingertech.starbeam.helpers.controllers.GenericCallback;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.HelpController;
import com.gingertech.starbeam.helpers.controllers.LaunchOverlayController;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallback;
import com.gingertech.starbeam.helpers.controllers.DrawerController;
import com.gingertech.starbeam.helpers.controllers.OnGenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.OnboardingTutorialController;
import com.gingertech.starbeam.helpers.controllers.PremiumController;
import com.gingertech.starbeam.helpers.controllers.TextWithImage;
import com.gingertech.starbeam.limelight.nvstream.NvConnection;
import com.gingertech.starbeam.limelight.nvstream.input.ControllerPacket;
import com.gingertech.starbeam.limelight.preferences.PreferenceConfiguration;
import com.gingertech.starbeam.ui.BluetoothPage;
import com.gingertech.starbeam.ui.CommandsPage;
import com.gingertech.starbeam.ui.PremiumPage;
import com.gingertech.starbeam.ui.RemapPage;
import com.gingertech.starbeam.ui.SettingsPage;
import com.gingertech.starbeam.ui.launch.LaunchComputerList;
import com.gingertech.starbeam.ui.layout.LayoutRootPage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    PremiumController premiumController = new PremiumController();

    public enum Pages {
        PremiumPage
    }

    public static LaunchOverlayController.sendKeyboard sendKeyboard;

    public final static int LAYOUTS = 0;
    public final static int PLAY = 1;
    public final static int SETTINGS = 2;
    public final static int PREMIUM = 3;
    public final static int DISCORD = 4;
    public final static int COMMANDS = 5;
    public final static int REMAP = 7;


    public static int maxLayouts = 1;

    public static NvConnection connection;
    public static FirebaseAnalytics mFirebaseAnalytics;
    short xboxFlags = 0;

    boolean isUp = false;
    boolean isDown = false;
    boolean isRight = false;
    boolean isLeft = false;

    static public WindowManager WM;

    public static class vector {
        public short x = 0;
        public short y = 0;

        public vector(short x, short y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "vector{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    vector xlj = new vector((byte) 0, (byte) 0);
    vector xrj = new vector((byte) 0, (byte) 0);
    vector xt = new vector((byte) 0, (byte) 0);

    boolean xboxMessageTried = false;
    boolean xboxMessagePlay = false;

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK) {

            if (MainActivity.connection == null || UserData.CurrentFragment != UserData.LAUNCH_GAME_PLAY) {
                return super.onGenericMotionEvent(event);
            }

            final int xHat = (int) event.getAxisValue(MotionEvent.AXIS_HAT_X);

            if (xHat == 1 && !isRight) {
                isRight = true;
                xboxFlags |= ControllerPacket.RIGHT_FLAG;

            } else if (xHat == -1 && !isLeft) {
                isLeft = true;
                xboxFlags |= ControllerPacket.LEFT_FLAG;

            } else if (xHat == 0 && (isRight || isLeft)) {

                if (isRight) {
                    xboxFlags = (short) ((xboxFlags ^= ControllerPacket.RIGHT_FLAG) & xboxFlags);
                }

                if (isLeft) {
                    xboxFlags = (short) ((xboxFlags ^= ControllerPacket.LEFT_FLAG) & xboxFlags);
                }

                isRight = false;
                isLeft = false;
            }

            final int yHat = (int) -event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            if (yHat == 1 && !isUp) {
                isUp = true;
                xboxFlags |= ControllerPacket.UP_FLAG;

            } else if (yHat == -1 && !isDown) {
                isDown = true;
                xboxFlags |= ControllerPacket.DOWN_FLAG;

            } else if (yHat == 0 && (isUp || isDown)) {

                if (isUp) {
                    xboxFlags = (short) ((xboxFlags ^= ControllerPacket.UP_FLAG) & xboxFlags);
                }

                if (isDown) {
                    xboxFlags = (short) ((xboxFlags ^= ControllerPacket.DOWN_FLAG) & xboxFlags);
                }

                isUp = false;
                isDown = false;
            }

            final int historySize = event.getHistorySize();

            handleController(event, historySize - 1);
            handleTrigger(event, historySize - 1);

            if(sendKeyboard != null) {
                UserData.mRemap.handleButtonInputs(Short.toUnsignedInt(xboxFlags), sendKeyboard);
            }


            return true;
        }

        return super.onGenericMotionEvent(event);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {

            if (MainActivity.connection == null || UserData.CurrentFragment != UserData.LAUNCH_GAME_PLAY) {
                Bundle b = new Bundle();

                if (!xboxMessageTried) {
                    MixPanel.mpEventTracking(getApplicationContext(), "xbox_tried", null);
                    xboxMessageTried = true;
                }

                return false;
            }

            Bundle b = new Bundle();

            if (!xboxMessagePlay) {
                MixPanel.mpEventTracking(getApplicationContext(), "xbox_play", null);
                xboxMessagePlay = true;
            }

            if (event.getRepeatCount() == 0) {

                for (int o = 0; o < ButtonHandler.xbox_keys.length; o++) {

                    if (ButtonHandler.xbox_keys[o] == event.getKeyCode()) {

                        String keyCode = ButtonHandler.keybindList[o];
                        int[] result = ButtonHandler.getCode(keyCode);
                        int keybind = result[0];

                        short xval = (short) keybind;

                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            xboxFlags |= xval;
                            Log.i("hu", "down");

                        } else if (event.getAction() == KeyEvent.ACTION_UP) {
                            xboxFlags = (short) ((xboxFlags ^= xval) & xboxFlags);
                            Log.i("hu", "up");
                        }

                        break;
                    }
                }

                if(sendKeyboard != null) {
                    UserData.mRemap.handleButtonInputs(Short.toUnsignedInt(xboxFlags), sendKeyboard);
                }

                return false;

            }
        }

        return super.dispatchKeyEvent(event);

    }

    private void handleController(MotionEvent event, int historyPos) {
        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);


        float x1 = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Z, historyPos);


        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);

        float y1 = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_RZ, historyPos);


        xlj.x = (short) Math.round(x * 32767);
        xlj.y = (short) Math.round(y * -32767);

        xrj.x = (short) Math.round(x1 * 32767);
        xrj.y = (short) Math.round(y1 * -32767);

        if(sendKeyboard != null) {
            UserData.mRemap.handleJoyInput(xlj, xrj, sendKeyboard);
        }
    }

    public void handleTrigger(MotionEvent event, int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_GAS, historyPos);


        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_BRAKE, historyPos);


        xt.x = (byte) (y * 255);
        xt.y = (byte) (x * 255);

        if(sendKeyboard != null) {
            UserData.mRemap.handleTriggerInput(xt, sendKeyboard);
        }

    }

    private float getCenteredAxis(MotionEvent event, InputDevice device, int axis, int historyPos) {

        if(device == null) { return 0; }

        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis) :
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public MenuInflater menuInflater;
    public static FragmentManager fragmentManager;
    public FragmentTransaction trans;

    DrawerController drawerController;

    TextView TitleView;

    ViewGroup navBar;

    View CommandsPopup;

    public static HelpController helpController;

    final public GenericCallbackv2 NavCallback = new GenericCallbackv2() {

        @Override
        public void onChange(Object value) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawerController.openDrawer(false);
                }
            });

            if (value instanceof String) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (TitleView != null) {
                            TitleView.setText((String) value);
                        }
                    }
                });

            } else if ((int) value == -1) {

                findViewById(R.id.drawerToggle).setVisibility(View.GONE);

                if (MainActivity.activeTutorial) {
                    helpController.beginHelp(UserData.LAYOUTS_CREATE);
                    MainActivity.activeTutorial = false;
                }

            } else if ((int) value == -2) {
                findViewById(R.id.drawerToggle).setVisibility(View.VISIBLE);

            } else if ((int) value == -3) {
                findViewById(R.id.drawerToggle).setVisibility(View.GONE);

            } else if ((int) value == -4) {
                findViewById(R.id.drawerToggle).setVisibility(View.VISIBLE);

            } else if ((int) value == -5) {
                ((DrawerController) findViewById(R.id.drawer)).openDrawer(false);

            } else if ((int) value != 0) {
                MainActivity.this.changeTab((int) value);
            }
        }
    };


    public static int uiFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    View logoView_premium;
    View logoView_normal;
    View logoView;

    View reviewPopup;

    Vibrator vibrator;

    ReviewInfo reviewInfo;
    ReviewManager manager;

    TextWithImage helpButton;

    ObjectAnimator a1;
    ObjectAnimator a2;
    boolean openPremium = false;

    private MixpanelAPI mp;

    public static Date initialTime;

    boolean iconsShown = false;
    int duration = 500;

    public static boolean activeTutorial = false;

    boolean toShowReviewPopup = false;

    //Default constructor, some devices will crash without this
    public MainActivity() {
    }


    public void showReviewPopupNextChance() {
        toShowReviewPopup = true;
    }

    private void showReviewPopup() {

        if (!toShowReviewPopup) {
            return;
        }
        if (UserData.hasReviewed) {
            return;
        }

        vibrator.vibrate(20);
        reviewPopup.setVisibility(View.VISIBLE);

        toShowReviewPopup = false;

        Bundle b = new Bundle();
        b.putString("name", "review_popup");
        mFirebaseAnalytics.logEvent("Popup", b);

        MixPanel.mpEventTracking(getApplicationContext(), "review_popped_up", null);

    }

    private void initializePremium() {

        premiumController.setup(MainActivity.this, new GenericCallbackv2() {
            @Override
            public void onChange(Object value) {

                if (value instanceof Boolean) {
                    UserData.isPremiumMode = (boolean) value;
                }
            }

            @Override
            public void onChange(Object value, Object value2) {
                Log.e("billing", "purchased did");

                if ((boolean) value) {
                    UserData.isPremiumMode = true;
                    Toast.makeText(getApplicationContext(), R.string.Thank_you_for_your_purchase, Toast.LENGTH_SHORT).show();
                    UpdatePremiumView();
                }

                trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new PremiumPage(premiumController)).commit();

            }
        });


    }

    void UpdatePremiumView() {
        findViewById(R.id.premiumTab).setVisibility(UserData.isPremiumMode && PremiumController.hasPermission(PremiumController.Product.AllAccess) ? View.GONE : View.VISIBLE);
        logoView_premium.setVisibility(UserData.isPremiumMode ? View.VISIBLE : View.GONE);
        logoView_normal.setVisibility(!UserData.isPremiumMode ? View.VISIBLE : View.GONE);
    }

    void SetupViews() {
        reviewPopup = findViewById(R.id.reviewPopup);
        TitleView = findViewById(R.id.mainTitleText);

        logoView = findViewById(R.id.logoView);
        logoView_premium = findViewById(R.id.logoView1);
        logoView_normal = findViewById(R.id.logoView2);

        helpButton = findViewById(R.id.helpButton);
        drawerController = findViewById(R.id.drawer);
        helpController = findViewById(R.id.helpLayout);
        navBar = findViewById(R.id.navBar);

        CommandsPopup = findViewById(R.id.commandPopup);

        findViewById(R.id.premiumTab).setVisibility(UserData.isPremiumMode && PremiumController.hasPermission(PremiumController.Product.AllAccess) ? View.GONE : View.VISIBLE);
    }

    void SetupControllers() {
        initialTime = Calendar.getInstance().getTime();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        drawerController.setMixPanel(mp);
        helpController.setMixPanel(mp);
    }

    void SetupVariables() {
        menuInflater = getMenuInflater();
        fragmentManager = getSupportFragmentManager();
        mp = MixPanel.makeObj(getApplicationContext());
        openPremium = getIntent().getBooleanExtra("open_premium", false);

        trans = fragmentManager.beginTransaction();
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    void SetupListeners() {

        CommandsPopup.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommandsPopup.setVisibility(View.GONE);
            }
        });

        reviewPopup.findViewById(R.id.reviewClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewPopup.setVisibility(View.GONE);

                Bundle b = new Bundle();
                b.putString("title", "review_close");
                mFirebaseAnalytics.logEvent("Review_Handle", b);

                MixPanel.mpButtonTracking(mp, "review_popup_close");

            }
        });

        reviewPopup.findViewById(R.id.reviewClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reviewPopup.setVisibility(View.GONE);

                ReviewManager manager = ReviewManagerFactory.create(MainActivity.this);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // We can get the ReviewInfo object
                        ReviewInfo reviewInfo = task.getResult();

                        Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo);
                        flow.addOnCompleteListener(task1 -> {
                            Bundle b = new Bundle();
                            b.putString("title", "review_review");
                            mFirebaseAnalytics.logEvent("Review_Handle", b);

                            MixPanel.mpButtonTracking(mp, "review_popup_review");

                            SaveClass.SaveHasReviewed(getApplicationContext());
                        });
                    } else {
                        // There was some problem, log or handle the error code.
                    }
                });
            }
        });

        drawerController.setCallback(new GenericCallback().setOnGenericCallback(new OnGenericCallback() {
            @Override
            public void onChange(Object value) {
                changeTab((int) value);
            }
        }));

        findViewById(R.id.drawerToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserData.CurrentFragment == UserData.LOGO || UserData.CurrentFragment == UserData.HOME) {
                    return;
                }
                drawerController.toggleDrawer();
                vibrator.vibrate(10);

            }
        });

        findViewById(R.id.helpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpButtonTracking(mp, "help_open");

                helpController.beginHelp(UserData.CurrentFragment);

                if (a1 != null && a2 != null) {
                    a1.end();
                    a2.end();
                }
            }
        });
    }

    void SetupReminders() {
        if (UserData.timesConnected == 0 && !UserData.isPremiumMode) {
            FirebaseMessaging.getInstance().subscribeToTopic("reminder_to_connect")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e("subscribed", task.isSuccessful() ? "successfully subscribed to reminder_to_connect" : "failure to subscribe to reminder_to_connect");
                        }
                    });
        }

        if (UserData.timesConnected > 0 && !UserData.isPremiumMode) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("reminder_to_connect")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e("subscribed", task.isSuccessful() ? "successfully unsubscribed from reminder_to_connect" : "failure to unsubscribed from reminder_to_connect");
                        }
                    });

            FirebaseMessaging.getInstance().subscribeToTopic("reminder_to_use")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e("subscribed", task.isSuccessful() ? "successfully subscribed to reminder_to_use" : "failure subscribed to reminder_to_use");
                        }
                    });
        }

        if (UserData.timesConnected > 0 && UserData.isPremiumMode) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("reminder_to_connect")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e("subscribed", task.isSuccessful() ? "successfully unsubscribed from reminder_to_connect" : "failure to unsubscribed from reminder_to_connect");
                        }
                    });

            FirebaseMessaging.getInstance().unsubscribeFromTopic("reminder_to_use")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e("subscribed", task.isSuccessful() ? "successfully unsubscribed to reminder_to_use" : "failure unsubscribed to reminder_to_use");
                        }
                    });

        }
    }

    void SetupOther() {
        initializePremium();
        askNotificationPermission();
    }

    void SetupOpeningSequence() {

        //If this is the first time the user opens the app
        if (UserData.openCount == 0) {

            showNavIcons(true);

            helpController.beginHelp(UserData.LAYOUTS_LIST_firsttime);
            trans.replace(R.id.nav_host_fragment, new LayoutRootPage(NavCallback)).commit();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    logoView_premium.setVisibility(UserData.isPremiumMode ? View.VISIBLE : View.GONE);
                    logoView_normal.setVisibility(!UserData.isPremiumMode ? View.VISIBLE : View.GONE);

                }
            });


        } else {
            trans.replace(R.id.nav_host_fragment, openPremium ? new PremiumPage() : new LayoutRootPage(NavCallback)).commit();

            showNavIcons(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    logoView.setVisibility(UserData.isPremiumMode ? View.VISIBLE : View.GONE);
                    logoView_premium.setVisibility(UserData.isPremiumMode ? View.VISIBLE : View.GONE);
                    logoView_normal.setVisibility(!UserData.isPremiumMode ? View.VISIBLE : View.GONE);
                }
            });


        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.root_layout);

//        MixPanel.mpEventTracking(mp, "Main Activity Launched", null);

        getWindow().getDecorView().setSystemUiVisibility(uiFlag);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        UserData.setup(getApplicationContext());

        SetupVariables();
        SetupViews();
        SetupControllers();
        SetupListeners();
        SetupOther();

        SetupReminders();

        SetupOpeningSequence();

        if (UserData.timesConnected > 5 && !UserData.hasReviewed) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        showReviewPopupNextChance();
                    }
                }, 30000);
            }
        }

    private void showNavIcons(boolean show) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator a1;
                a1 = ObjectAnimator.ofFloat(navBar, "alpha", show ? 1 : 0);
                a1.setDuration(duration);
                a1.start();
            }
        });

        iconsShown = show;
    }


    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                final ActivityResultLauncher<String> requestPermissionLauncher =
                        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (isGranted) {
                                // FCM SDK (and your app) can post notifications.
                            }
                        });

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onResume() {
        getWindow().getDecorView().setSystemUiVisibility(uiFlag);

        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        getWindow().getDecorView().setSystemUiVisibility(uiFlag);

        super.onWindowFocusChanged(hasFocus);
    }

    private void changeTab(int newTab) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

        //If the user clicks on the same tab
        if (UserData.CurrentFragment == newTab && newTab != MainActivity.PLAY) {
            return;
        }

        vibrator.vibrate(10);

        if(fragmentManager.isDestroyed()) {
            fragmentManager = getSupportFragmentManager();
        }

        trans = fragmentManager.beginTransaction();
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);


        //Set the current thread to the selected thread
        switch (newTab) {

            case MainActivity.LAYOUTS:

                if(UserData.timesConnected == 0 && UserData.back_from_connect_without_connecting && !UserData.back_from_connect_without_connecting_done) {

                    UserData.back_from_connect_without_connecting = false;
                    UserData.back_from_connect_without_connecting_done = true;
                }

                trans.replace(R.id.nav_host_fragment, new LayoutRootPage(NavCallback)).commit();
                TitleView.setText(R.string.layouts);
//                mAdView.setVisibility(View.VISIBLE);

                break;

            case MainActivity.PLAY:

                    trans.replace(R.id.nav_host_fragment, new LaunchComputerList(NavCallback)).commit();
                    TitleView.setText(R.string.WiFi_Play);

                break;

            case MainActivity.DISCORD:
                MixPanel.mpEventTracking(getApplicationContext(), "Discord_Fragment_opened", null);

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/VbjxkKhTqh")));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), R.string.Error_Creating_Layout_ID, Toast.LENGTH_SHORT).show();
                }

                break;

            case MainActivity.PREMIUM:
                trans.replace(R.id.nav_host_fragment, new PremiumPage(premiumController)).commit();
                TitleView.setText(R.string.premium);

                break;

            case MainActivity.SETTINGS:
                MixPanel.mpEventTracking(getApplicationContext(), "Options_Fragment_opened", null);

                TitleView.setText("Settings");

                trans.replace(R.id.nav_host_fragment, new SettingsPage()).commit();

                break;

            case MainActivity.COMMANDS:

                if(!PremiumController.hasPermission(PremiumController.Product.CommandEditor)) {
                    vibrator.vibrate(100);
                    Snackbar r = Snackbar.make(TitleView, R.string.You_need_to_upgrade, LENGTH_INDEFINITE);
                    r.setAction(R.string.view, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UserData.selectedPremiumObject = PremiumController.Product.CommandEditor;
                            changeTab(MainActivity.PREMIUM);
                        }
                    });
                    r.show();

                } else {

                    trans.replace(R.id.nav_host_fragment, new CommandsPage(NavCallback)).commit();
                    TitleView.setText(R.string.Commands);
                }
                break;

            case MainActivity.REMAP:

                trans.replace(R.id.nav_host_fragment, new RemapPage()).commit();
                TitleView.setText(R.string.Remap);

                break;

        }

        if (newTab != MainActivity.DISCORD)  {
            UserData.CurrentFragment = newTab;
            UserData.CurrentPage = newTab;

            drawerController.selectChild(newTab);
        }

        helpController.endHelp_cont();

        //Shows review popup if there has been one qued

        showReviewPopup();
        MainActivity.setImmersiveMode(false, MainActivity.this);

        findViewById(R.id.drawerToggle).setVisibility(View.VISIBLE);

            }
        });
//        showInterstitialAd();
    }

    public static void setImmersiveMode(Boolean immersive, Activity activity) {
        activity.findViewById(R.id.navBar).setVisibility(immersive ? View.GONE : View.VISIBLE);
        activity.findViewById(R.id.helpButton).setVisibility(immersive ? View.GONE : View.VISIBLE);

    }

    private PreferenceConfiguration previousPrefs;
    private int previousDisplayPixelCount;

    // HACK for Android 9
    public static DisplayCutout displayCutoutP;

    public void reloadSettings() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Display.Mode mode = getWindowManager().getDefaultDisplay().getMode();
//            previousDisplayPixelCount = mode.getPhysicalWidth() * mode.getPhysicalHeight();
//        }
//        getFragmentManager().beginTransaction().replace(
//                R.id.stream_settings, new StreamSettings.SettingsFragment()
//        ).commitAllowingStateLoss();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // We have to use this hack on Android 9 because we don't have Display.getCutout()
        // which was added in Android 10.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            // Insets can be null when the activity is recreated on screen rotation
            // https://stackoverflow.com/questions/61241255/windowinsets-getdisplaycutout-is-null-everywhere-except-within-onattachedtowindo
            WindowInsets insets = getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                displayCutoutP = insets.getDisplayCutout();
            }
        }

        reloadSettings();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Display.Mode mode = getWindowManager().getDefaultDisplay().getMode();

            // If the display's physical pixel count has changed, we consider that it's a new display
            // and we should reload our settings (which include display-dependent values).
            //
            // NB: We aren't using displayId here because that stays the same (DEFAULT_DISPLAY) when
            // switching between screens on a foldable device.
            if (mode.getPhysicalWidth() * mode.getPhysicalHeight() != previousDisplayPixelCount) {
                reloadSettings();
            }
        }
    }


    @Override
    public void onBackPressed() {
         changeTab(UserData.LAYOUTS);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if(grantResults.length != 0) {
//            if (requestCode == BluetoothController.BLUETOOTH_PERMISSION_REQUEST) {
//                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//
//                    BluetoothController.ConnectPermissionState = BluetoothController.Permission.Denied;
//
//                } else {
//                    BluetoothController.ConnectPermissionState = BluetoothController.Permission.Accepted;
//
//                }
//            }
//        }
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
