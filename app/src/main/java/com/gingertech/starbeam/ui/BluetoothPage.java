package com.gingertech.starbeam.ui;

import static com.gingertech.starbeam.MainActivity.mFirebaseAnalytics;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gingertech.starbeam.MainActivity;
import com.gingertech.starbeam.R;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.gingertech.starbeam.helpers.controllers.BluetoothController;
import com.gingertech.starbeam.helpers.controllers.BluetoothListItemController;
import com.gingertech.starbeam.helpers.controllers.GenericCallbackv2;
import com.gingertech.starbeam.helpers.controllers.LaunchOverlayController;
import com.gingertech.starbeam.helpers.controllers.MixPanel;
import com.gingertech.starbeam.ui.layout.LayoutRootPage;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothPage extends Fragment {

    View root;
    LinearLayout bluetoothDeviceList;
    BluetoothController bluetoothController;
    View MakeDiscoverableView;

    LaunchOverlayController launchOverlayController;

    boolean isWorkingQuestionFlag = false;
    boolean firstTimeConnecting = true;

    enum States {
        NOCONNECTION,
        WAITING,
        CONNECTING,
        CONNECTED,
        SHOWERROR,
        INSTRUCT
    }

    States currentState = States.NOCONNECTION;

    View immersiveModeIcon_1;
    View Options;

    MixpanelAPI mp;

    GenericCallbackv2 returnCallback;
    GenericCallbackv2 bluetoothCallback;

    Set<BluetoothDevice> localBTDevices = new ArraySet<>();

    Vibrator vibrator;

    boolean shownFirstTime = false;
    public BluetoothPage(GenericCallbackv2 returnCallback) {
        this.returnCallback = returnCallback;
    }

    public BluetoothPage() {

    }

    ProgressBar bar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);


        mp = MixPanel.makeObj(requireContext());
        MixPanel.mpEventTracking(mp, "Opened_Bluetooth_Page", null);

        UserData.CurrentFragment = UserData.LAUNCH_BLUETOOTH_PLAY;

        UserData.PlayMode = UserData.BLUETOOTH;
        SaveClass.SaveFlags(getContext());

        root = inflater.inflate(R.layout.bluetooth_page, container, false);

        bar = root.findViewById(R.id.progress_bar);


        bluetoothDeviceList = root.findViewById(R.id.bluetoothList);
        MakeDiscoverableView = root.findViewById(R.id.discoverableButton);

        launchOverlayController = root.findViewById(R.id.controller);
        launchOverlayController.setCallback(returnCallback);

        updateViews();

        Options = root.findViewById(R.id.settings);

        Options.findViewById(R.id.immersiveModeIcon_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                immersiveModeIcon_1.setVisibility(View.VISIBLE);
                Options.setVisibility(View.GONE);

                MixPanel.mpEventTracking(mp, "Closed_Bluetooth_Options", null);
                vibrator.vibrate(10);

            }
        });

        Options.findViewById(R.id.changeDevice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothController.destroy();

                root.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentState = States.NOCONNECTION;
                        updateViews();
                        vibrator.vibrate(10);
                    }
                }, 150);


                MixPanel.mpEventTracking(mp, "Change_Bluetooth_Device", null);

            }
        });

        Options.findViewById(R.id.editController).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                FragmentTransaction trans = fragmentManager.beginTransaction();
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                trans.replace(R.id.nav_host_fragment, new LayoutRootPage(returnCallback, LayoutRootPage.CREATEVIEW)).commit();

                vibrator.vibrate(10);

                MixPanel.mpEventTracking(mp, "Opened_Edit_Controller", null);

            }
        });

        root.findViewById(R.id.wifiplay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MixPanel.mpEventTracking(mp, "Opened_Wifi_Play", null);

                UserData.PlayMode = UserData.WIFI;
                returnCallback.onChange(MainActivity.PLAY);
                vibrator.vibrate(10);
            }
        });

        root.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setDiscovering();
                vibrator.vibrate(10);

            }
        });

        Options.findViewById(R.id.wifiplayC).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MixPanel.mpEventTracking(mp, "Opened_Wifi_Play", null);

                UserData.PlayMode = UserData.WIFI;
                returnCallback.onChange(MainActivity.PLAY);
                vibrator.vibrate(10);
            }
        });

        immersiveModeIcon_1 = root.findViewById(R.id.immersiveModeIcon_1);
        immersiveModeIcon_1.setOnTouchListener(new View.OnTouchListener() {

            boolean isLongPress;
            int x, y, ypos;
            final Handler handler = new Handler();
            Runnable activatedLongPress = new Runnable() {
                @Override
                public void run() {
                    vibrator.vibrate(10);
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

                    handler.removeCallbacks(activatedLongPress);
                    if (!isLongPress) {

                        immersiveModeIcon_1.setVisibility(View.GONE);
                        Options.setVisibility(View.VISIBLE);

                        MixPanel.mpEventTracking(mp, "Opened_Bluetooth_Options", null);


                    } else {
                        isLongPress = false;
                    }
                }

                return true;
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Snackbar s = Snackbar.make(getActivity().findViewById(R.id.controller), "PC game control requires bluetooth connection", BaseTransientBottomBar.LENGTH_INDEFINITE);
                s.setAction("OK!", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bluetoothController.StartBluetooth(requireActivity(), bluetoothCallback);
                        vibrator.vibrate(10);
                    }
                });

                s.show();
            }
        };

        bluetoothCallback = new GenericCallbackv2() {
            @SuppressLint("MissingPermission")
            @Override
            public void onChange(Object value) {
                super.onChange(value);

                if (value instanceof BluetoothController.Permission) {
                    if (value.equals(BluetoothController.Permission.Denied)) {
                        root.post(runnable);

                    } else {

                        root.post(new Runnable() {
                            @Override
                            public void run() {
                                startBluetoothDiscovery();
                            }
                        });

                    }
                } else if (value instanceof BluetoothController.State) {

                    if (value.equals(BluetoothController.State.CONNECTED)) {
                        currentState = States.CONNECTED;
                        updateViews();

                        vibrator.vibrate(10);


                        if (getContext() != null) {
                            SaveClass.SaveBluetooth(getContext(), bluetoothController.myComputer.getAddress());
                        }

                        MixPanel.mpEventTracking(mp, "Bluetooth_Connected", null);

                        Bundle b = new Bundle();
                        mFirebaseAnalytics.logEvent("Bluetooth_Connected", b);

                    }

                    if (value.equals(BluetoothController.State.CONNNECTING)) {
                        currentState = States.CONNECTING;
                        updateViews();

                        MixPanel.mpEventTracking(mp, "Bluetooth_Connecting", null);

                    }

                    if (value.equals(BluetoothController.State.RESTART)) {
                        bluetoothController.createBluetooth(getContext());
                    }

                        if (value.equals(BluetoothController.State.DISCOVERING)) {
                        currentState = States.WAITING;
                        updateViews();

                        MixPanel.mpEventTracking(mp, "Bluetooth_Connecting", null);

                    }

                    if (value.equals(BluetoothController.State.ERROR)) {

                        currentState = States.SHOWERROR;
                        vibrator.vibrate(10);

                        updateViews();
                        MixPanel.mpEventTracking(mp, "Bluetooth_Error_Classic", null);

                    }

                    if (value.equals(BluetoothController.State.NOCONNECTION)) {
                        vibrator.vibrate(10);

                        currentState = States.NOCONNECTION;
                        updateViews();

                        MixPanel.mpEventTracking(mp, "Bluetooth_No_Connection", null);

                    }

                    if (value.equals(BluetoothController.State.DISCONNECTED)) {

                        MixPanel.mpEventTracking(mp, "Bluetooth_Disconnected", null);

                        vibrator.vibrate(100);

                        root.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Disconnected From Device", Toast.LENGTH_LONG).show();
                            }
                        });

                        currentState = States.NOCONNECTION;
                        updateViews();
                    }
                }
            }
        };

        root.findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice bluetoothDevice = bluetoothController.myComputer;
                bluetoothController.destroy();
                bluetoothController.ConnectToDevice(bluetoothDevice.getAddress(), requireContext());
                MixPanel.mpEventTracking(mp, "Bluetooth_Limbo_Done", null);

                vibrator.vibrate(10);
            }
        });

        root.findViewById(R.id.cancel1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothController.destroy();
                bluetoothCallback.onChange(BluetoothController.State.NOCONNECTION);

                MixPanel.mpEventTracking(mp, "Bluetooth_Connection_Cancel", null);

                vibrator.vibrate(100);
            }
        });

        root.findViewById(R.id.cancel2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothController.destroy();
                bluetoothCallback.onChange(BluetoothController.State.NOCONNECTION);

                MixPanel.mpEventTracking(mp, "Bluetooth_Connection_Cancel", null);

                vibrator.vibrate(100);

            }
        });

        root.findViewById(R.id.firstTimeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
                updateViews();
            }
        });


        bluetoothController = new BluetoothController(getContext(), mp);
        bluetoothController.StartBluetooth(requireActivity(), bluetoothCallback);

        return root;
    }

    private void startBluetoothDiscovery() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if (bluetoothDeviceList.getChildCount() != bluetoothController.GetDevices().size()) {

                    if (getContext() == null || getActivity() == null) {
                        this.cancel();
                        return;
                    }
                    requireActivity().runOnUiThread(new Runnable() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void run() {

                            if (getContext() == null || getActivity() == null) {
                                return;
                            }

                            localBTDevices = bluetoothController.GetDevices();
                            if(localBTDevices.size() == bluetoothDeviceList.getChildCount()) { return; }

                            if(shownFirstTime && currentState == States.NOCONNECTION) {
                                vibrator.vibrate(10);
                            }

                            HashMap<String, String> map = new HashMap<>();
                            map.put("device count", String.valueOf(localBTDevices.size()));
                            MixPanel.mpEventTracking(mp, "Bluetooth_Device_Count_Update", null);

                            bluetoothDeviceList.removeAllViews();

                            root.findViewById(R.id.noComputersFound).setVisibility(localBTDevices.isEmpty() ? View.VISIBLE : View.GONE);

                            for (BluetoothDevice bluetoothDevice : localBTDevices) {

                                Log.e("BTprofiles", "Name :" + bluetoothDevice.getName() + " Class: " + bluetoothDevice.getBluetoothClass().getDeviceClass());

                                BluetoothListItemController bluetoothListItemController = new BluetoothListItemController(getContext());
                                bluetoothListItemController.setup(bluetoothDevice);

                                bluetoothDeviceList.addView(bluetoothListItemController);
                                bluetoothListItemController.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        bluetoothController.ConnectToDevice(((BluetoothListItemController) view).bluetoothDevice.getAddress(), getContext());
                                        firstTimeConnecting = false;

                                        HashMap<String, String> map = new HashMap<>();
                                        map.put("device name", bluetoothDevice.getName());
                                        map.put("device class", String.valueOf(bluetoothDevice.getBluetoothClass().getDeviceClass()));

                                        MixPanel.mpEventTracking(mp, "Bluetooth_Device_Clicked", map);

                                        vibrator.vibrate(10);

                                    }
                                });

                            }
                        }
                    });

                    if(currentState == States.CONNECTED) {
                        this.cancel();
                    }
                }
            }
        }, 0, 500);

        if(!UserData.defaultBluetoothAddr.isEmpty()) {
            bluetoothController.ConnectToDevice(UserData.defaultBluetoothAddr, getContext());
        }
    }

    int period = 50;
    Timer timer = new Timer();
    boolean isRunningTimer = false;

    private void updateViews() {

        timer.cancel();

        root.post(new Runnable() {
            @Override
            public void run() {

                root.findViewById(R.id.immersiveModeIcon_1).setVisibility(View.GONE);
                root.findViewById(R.id.settings).setVisibility(View.GONE);


                if(currentState == States.NOCONNECTION) {

                    if(!shownFirstTime && UserData.timesConnectedBT == 0) {
                        root.findViewById(R.id.firstTimeConnect).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.deviceSearchView).setVisibility(View.GONE);

                        shownFirstTime = true;

                    } else {
                        root.findViewById(R.id.deviceSearchView).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.firstTimeConnect).setVisibility(View.GONE);
                    }

                    setDiscovering();

                    launchOverlayController.setVisibility(View.GONE);
                    root.findViewById(R.id.errorView).setVisibility(View.GONE);

                    root.findViewById(R.id.wifiplay).setVisibility(View.VISIBLE);

                    root.findViewById(R.id.discoveringView).setVisibility(View.GONE);
                    root.findViewById(R.id.waitingView).setVisibility(View.GONE);
                    root.findViewById(R.id.instructView).setVisibility(View.GONE);

                    bluetoothController.StartDiscovery();
                }

                if(currentState == States.CONNECTING) {

                    timer.cancel();


                    bar.setProgress(0, false);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {

                        int x = 0;
                        int time = 30000;
                        @Override
                        public void run() {
                            root.post(new Runnable() {
                                @Override
                                public void run() {
                                    bar.setProgress(100 * x * period / time, true);
                                    x += 1;
                                }
                            });

                        }
                    }, 0, period);

                    root.findViewById(R.id.deviceSearchView).setVisibility(View.GONE);

                    root.findViewById(R.id.discoveringView).setVisibility(View.GONE);
                    root.findViewById(R.id.waitingView).setVisibility(View.VISIBLE);
                    launchOverlayController.setVisibility(View.GONE);
                    root.findViewById(R.id.errorView).setVisibility(View.GONE);
                    root.findViewById(R.id.instructView).setVisibility(View.GONE);

                    root.findViewById(R.id.wifiplay).setVisibility(View.GONE);
                }

                if(currentState == States.CONNECTED) {

                    UserData.timesConnectedBT += 1;
                    SaveClass.SaveFlags(requireContext());

                    root.findViewById(R.id.discoveringView).setVisibility(View.GONE);
                    root.findViewById(R.id.waitingView).setVisibility(View.GONE);

                    root.findViewById(R.id.wifiplay).setVisibility(View.GONE);

                    root.findViewById(R.id.deviceSearchView).setVisibility(View.GONE);
                    root.findViewById(R.id.waitingView).setVisibility(View.GONE);

                    launchOverlayController.setVisibility(View.VISIBLE);
                    root.findViewById(R.id.errorView).setVisibility(View.GONE);

                    root.findViewById(R.id.settings).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.immersiveModeIcon_1).setVisibility(View.GONE);
                    root.findViewById(R.id.instructView).setVisibility(View.GONE);

                    bluetoothController.endDescovery();


                    launchOverlayController.setupBluetooth(bluetoothController);
                    launchOverlayController.makeButtons();

                    launchOverlayController.start();

                    if(isWorkingQuestionFlag) {
                        isWorkingQuestionFlag = false;
                        root.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (currentState == States.CONNECTED) {
                                        root.findViewById(R.id.workingPopup).setVisibility(View.VISIBLE);

                                    root.findViewById(R.id.restartConnection).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            vibrator.vibrate(10);

                                            if (bluetoothController != null) {
                                                BluetoothDevice bluetoothDevice = bluetoothController.myComputer;

                                                if(bluetoothDevice == null) {
                                                    return;
                                                }

                                                bluetoothController.destroy();
                                                bluetoothController.ConnectToDevice(bluetoothDevice.getAddress(), requireContext());

                                                root.findViewById(R.id.workingPopup).setVisibility(View.GONE);
                                            } else {
                                                bluetoothController = new BluetoothController(getContext(), mp);
                                                bluetoothController.StartBluetooth(requireActivity(), bluetoothCallback);
                                            }
                                        }
                                    });

                                    root.findViewById(R.id.removePopup).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            root.findViewById(R.id.workingPopup).setVisibility(View.GONE);

                                            if (UserData.timesConnectedBT <= 2) {
                                                Toast.makeText(requireContext(), "Nice! Now try opening a game and start playing!", Toast.LENGTH_LONG).show();
                                            }

                                            vibrator.vibrate(10);

                                        }
                                    });
                                }
                            }
                        }, 1500);
                    }
                }

                if(currentState == States.SHOWERROR) {
                    root.findViewById(R.id.wifiplay).setVisibility(View.GONE);

                    root.findViewById(R.id.discoveringView).setVisibility(View.GONE);
                    root.findViewById(R.id.waitingView).setVisibility(View.GONE);

                    root.findViewById(R.id.deviceSearchView).setVisibility(View.GONE);
                    root.findViewById(R.id.errorView).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.instructView).setVisibility(View.GONE);

                    launchOverlayController.setVisibility(View.GONE);

                    isWorkingQuestionFlag = true;

                }
            }
        });

    }

    @Override
    public void onDestroy() {

        if(bluetoothController != null) {
            bluetoothController.destroy();
            bluetoothController.endDescovery();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if(bluetoothController != null) {
            bluetoothController.destroy();
            bluetoothController.endDescovery();
        }

        super.onDetach();
    }

    Timer timer2 = new Timer();
    void setDiscovering() {
        bluetoothController.StartDiscovery();

        root.findViewById(R.id.loadingProgressView).setVisibility(View.VISIBLE);
        root.findViewById(R.id.refresh).setVisibility(View.GONE);

        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {

                if(getActivity() == null) { return; }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        root.findViewById(R.id.loadingProgressView).setVisibility(View.GONE);
                        root.findViewById(R.id.refresh).setVisibility(View.VISIBLE);

                    }
                });


            }
        }, 12000);

    }
}
