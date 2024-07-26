package com.gingertech.starbeam.helpers.controllers;

import static android.app.PendingIntent.getActivity;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.os.HandlerCompat.postDelayed;

import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.CONNECTED;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.CONNNECTING;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.DISCONNECTED;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.DISCOVERING;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.ERROR;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.NOCONNECTION;
import static com.gingertech.starbeam.helpers.controllers.BluetoothController.State.RESTART;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.gingertech.starbeam.helpers.ButtonHandler;
import com.gingertech.starbeam.helpers.SaveClass;
import com.gingertech.starbeam.helpers.UserData;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BluetoothController {

    View root;

    enum MouseButton {
        LEFT,
        RIGHT,
        MIDDLE
    }

    public enum State {
        NOCONNECTION,
        DISCOVERING,

        BONDED,
        CONNECTED,
        CONNNECTING,
        ERROR,
        RESTART,


        DISCONNECTED

    }

    public enum Permission {
        Accepted,
        Denied,
        NULL
    }

    public State connectionState = State.NOCONNECTION;

    public final static int BLUETOOTH_PERMISSION_REQUEST = 23372;
    public final static int requestCode = 3543;

    public BluetoothDevice myComputer;

    public static ArraySet<BluetoothDevice> foundBTDevice = new ArraySet<>();


    private boolean isDiscovering = false;


    byte[] Descriptor = {
            (byte) 0x05, (byte) 0x01,                    // USAGE_PAGE (Generic Desktop)
            (byte) 0x09, (byte) 0x02,                    // USAGE (Mouse)
            (byte) 0xa1, (byte) 0x01,                    // COLLECTION (Application)
            (byte) 0x09, (byte) 0x01,                    //   USAGE (Pointer)
            (byte) 0xa1, (byte) 0x00,                    //   COLLECTION (Physical)
            (byte) 0x85, (byte) 0x02,
            (byte) 0x05, (byte) 0x09,                    //     USAGE_PAGE (Button)
            (byte) 0x19, (byte) 0x01,                    //     USAGE_MINIMUM (Button 1)
            (byte) 0x29, (byte) 0x03,                    //     USAGE_MAXIMUM (Button 3)
            (byte) 0x15, (byte) 0x00,                    //     LOGICAL_MINIMUM (0)
            (byte) 0x25, (byte) 0x01,                    //     LOGICAL_MAXIMUM (1)
            (byte) 0x95, (byte) 0x03,                    //     REPORT_COUNT (3)
            (byte) 0x75, (byte) 0x01,                    //     REPORT_SIZE (1)
            (byte) 0x81, (byte) 0x02,                    //     INPUT (Data,Var,Abs)
            (byte) 0x95, (byte) 0x01,                    //     REPORT_COUNT (1)
            (byte) 0x75, (byte) 0x05,                    //     REPORT_SIZE (5)
            (byte) 0x81, (byte) 0x03,                    //     INPUT (Cnst,Var,Abs)
            (byte) 0x05, (byte) 0x01,                    //     USAGE_PAGE (Generic Desktop)
            (byte) 0x09, (byte) 0x30,                    //     USAGE (X)
            (byte) 0x09, (byte) 0x31,                    //     USAGE (Y)
            (byte) 0x15, (byte) 0x81,                    // LOGICAL_MINIMUM (-127)
            (byte) 0x25, (byte) 0x7F,                    // LOGICAL_MAXIMUM (127)
            (byte) 0x75, (byte) 0x08,                    //     REPORT_SIZE (16)
            (byte) 0x95, (byte) 0x02,                    //     REPORT_COUNT (2)
            (byte) 0x81, (byte) 0x06,                    //     INPUT (Data,Var,Rel)
            (byte) 0xc0,                          //   END_COLLECTION
            (byte) 0xc0,                           // END_COLLECTION

            (byte) 0x09, (byte) 0x06,                    // USAGE (Keyboard)
            (byte) 0xa1, (byte) 0x01,                    // COLLECTION (Application)
            (byte) 0x85, (byte) 0x01,
            (byte) 0x05, (byte) 0x07,                    //   USAGE_PAGE (Keyboard)
            (byte) 0x19, (byte) 0xe0,                    //   USAGE_MINIMUM (Keyboard LeftControl)
            (byte) 0x29, (byte) 0xe7,                    //   USAGE_MAXIMUM (Keyboard Right GUI)
            (byte) 0x15, (byte) 0x00,                    //   LOGICAL_MINIMUM (0)
            (byte) 0x25, (byte) 0x01,                    //   LOGICAL_MAXIMUM (1)
            (byte) 0x75, (byte) 0x01,                    //   REPORT_SIZE (1)
            (byte) 0x95, (byte) 0x08,                    //   REPORT_COUNT (8)
            (byte) 0x81, (byte) 0x02,                    //   INPUT (Data,Var,Abs)
            (byte) 0x95, (byte) 0x04,                    //   REPORT_COUNT (4)
            (byte) 0x75, (byte) 0x08,                    //   REPORT_SIZE (8)
            (byte) 0x15, (byte) 0x00,                    //   LOGICAL_MINIMUM (0)
            (byte) 0x25, (byte) 0x65,                    //   LOGICAL_MAXIMUM (101)
            (byte) 0x05, (byte) 0x07,                    //   USAGE_PAGE (Keyboard)
            (byte) 0x19, (byte) 0x00,                    //   USAGE_MINIMUM (Reserved (no event indicated))
            (byte) 0x29, (byte) 0x65,                    //   USAGE_MAXIMUM (Keyboard Application)
            (byte) 0x81, (byte) 0x00,                    //   INPUT (Data,Ary,Abs)
            (byte) 0xc0,                           // END_COLLECTION
    };


    BluetoothHidDevice bluetoothHidDevice;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    String deviceStickInLimbo;
    BluetoothDevice deviceStickInLimbod;

    public static Permission ConnectPermissionState = Permission.NULL;

    private GenericCallbackv2 updatesCallback;

    private MixpanelAPI mp;

    public BluetoothController(Context context, MixpanelAPI mixPanel) {

        this.mp = mixPanel;

        SaveClass.GetBluetooth(context);
    }

    public void StartBluetooth(Activity context, GenericCallbackv2 connectionCallback) {

        this.updatesCallback = connectionCallback;

        ConnectPermissionState = Permission.NULL;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE}, BLUETOOTH_PERMISSION_REQUEST);

            MixPanel.mpEventTracking(mp, "Bluetooth_Permission_Not_Granted", null);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (ConnectPermissionState == Permission.NULL) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }


                    if (updatesCallback != null) {

                        if (ConnectPermissionState == Permission.Accepted) {
                            MixPanel.mpEventTracking(mp, "Bluetooth_Permission_Accepted", null);

                            MixPanel.mpEventTracking(mp, "Finishing_Bluetooth_Setup", null);

                            bluetoothManager = context.getSystemService(BluetoothManager.class);
                            bluetoothAdapter = bluetoothManager.getAdapter();

                            if (!bluetoothAdapter.isEnabled()) {
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                            }

                            StartDiscovery();

                            if (!UserData.defaultBluetoothAddr.isEmpty()) {
                                bluetoothAdapter.getRemoteDevice(UserData.defaultBluetoothAddr);
                            }


                        } else {
                            MixPanel.mpEventTracking(mp, "Bluetooth_Permission_Denied", null);

                        }

                        connectionCallback.onChange(ConnectPermissionState);

                    }
                }
            }).start();

            return;
        }

        ConnectPermissionState = Permission.Accepted;

        bluetoothManager = context.getSystemService(BluetoothManager.class);

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }

        connectionCallback.onChange(Permission.Accepted);

        StartDiscovery();

        if (!UserData.defaultBluetoothAddr.isEmpty()) {
            bluetoothAdapter.getRemoteDevice(UserData.defaultBluetoothAddr);
        }

        MixPanel.mpEventTracking(mp, "Bluetooth_Permission_Accepted", null);

        MixPanel.mpEventTracking(mp, "Finishing_Bluetooth_Setup", null);

    }

    @SuppressLint("MissingPermission")
    public void StartDiscovery() {

        if (getPermission()) {
            if (getPermission() && bluetoothAdapter != null) {
                bluetoothAdapter.startDiscovery();
                isDiscovering = true;

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isDiscovering = false;

                    }
                }, 13000);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public Set<BluetoothDevice> GetDevices() {

        Set<BluetoothDevice> bluetoothDevices = new ArraySet<>();

        for(BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
            if(isValidDevice(bluetoothDevice)) {
                bluetoothDevices.add(bluetoothDevice);
            }
        }

        for(BluetoothDevice bluetoothDevice : foundBTDevice) {
            if(isValidDevice(bluetoothDevice)) {
                bluetoothDevices.add(bluetoothDevice);
            }
        }

        return bluetoothDevices;
    }

    Handler handler;

    @SuppressLint("MissingPermission")
    public boolean isValidDevice(BluetoothDevice bluetoothDevice) {

        if(!getPermission()) { return false; }

        if (bluetoothDevice.getName() == null) {
            return false;
        }
        if (bluetoothDevice.getName().isEmpty() || bluetoothDevice.getName().isBlank()) { return false; }

        return bluetoothDevice.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_DESKTOP ||
                bluetoothDevice.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_UNCATEGORIZED ||
                bluetoothDevice.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_LAPTOP;
    }

    public void ConnectToDevice(String bluetoothDevice, Context context) {
        myComputer = bluetoothAdapter.getRemoteDevice(bluetoothDevice);

        if(context != null) {
            createBluetooth(context);
        }
    }

    BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            super.onAppStatusChanged(pluggedDevice, registered);

            Log.e("BTprofiles", registered ? "registered" : "not registered");

            if(registered) {
                MixPanel.mpEventTracking(mp, "BT_HID_Registered", null);

                if(!bluetoothHidDevice.connect(myComputer)) {
                    updatesCallback.onChange(ERROR);
                }
            } else if(connectionState == CONNNECTING) {
                updatesCallback.onChange(RESTART);

                MixPanel.mpEventTracking(mp, "BT_HID_NOT_Registered", null);

            }

        }


        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {

            if (state == BluetoothHidDevice.STATE_CONNECTED) {
                Log.e("BTprofiles", "HID connected!");
                MixPanel.mpEventTracking(mp, "BT_HID_CONN", null);

                updatesCallback.onChange(CONNECTED);
                connectionState = CONNECTED;
            }

            if (state == BluetoothHidDevice.STATE_CONNECTING) {
                updatesCallback.onChange(CONNNECTING);
                connectionState = CONNNECTING;
                MixPanel.mpEventTracking(mp, "BT_HID_CONNecting", null);


                Log.e("BTprofiles", "HID connecting");

            }

            if (state == BluetoothHidDevice.STATE_DISCONNECTED) {
                MixPanel.mpEventTracking(mp, "BT_HID_Disconnected", null);

                updatesCallback.onChange(ERROR);


            }
        }

    };

    public void createBluetooth(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Set<BluetoothDevice> temp = GetDevices();

        new Thread(() -> {

            connectionState = CONNNECTING;
            updatesCallback.onChange(CONNNECTING);

            MixPanel.mpEventTracking(mp, "BT_Discovery_Start", null);

            Log.e("BTprofiles", "Getting Computer");

            while (myComputer == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if(connectionState == DISCONNECTED) {
                    return;
                }

                if (!temp.containsAll(GetDevices())) {
                    for (BluetoothDevice bluetoothDevice : GetDevices()) {

                        //if the bluetooth device is newly connected
                        if (!temp.contains(bluetoothDevice)) {
                            myComputer = bluetoothDevice;
                            break;
                        }
                    }
                }
            }

            MixPanel.mpEventTracking(mp, "BT_Device_found", null);


            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            myComputer.createBond();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            while(myComputer.getBondState() == BluetoothDevice.BOND_BONDING) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if(connectionState == DISCONNECTED) {
                    return;
                }
            }

            if(myComputer.getBondState() == BluetoothDevice.BOND_NONE) {
                updatesCallback.onChange(ERROR);
            }

            boolean success = bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {

                    if (i == BluetoothProfile.HID_DEVICE) {

                        bluetoothHidDevice = (BluetoothHidDevice) bluetoothProfile;

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat//requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat//requestPermissions for more details.
                            return;
                        }

                        BluetoothHidDeviceAppSdpSettings b = new BluetoothHidDeviceAppSdpSettings("Neon", "HID Profile", "Gingertech Inc.", BluetoothHidDevice.SUBCLASS1_COMBO, Descriptor);

                        BluetoothHidDeviceAppQosSettings settings = new BluetoothHidDeviceAppQosSettings(-1, -1, -1, -1, -1, -1);

                        bluetoothHidDevice.unregisterApp();
                        bluetoothHidDevice.registerApp(b, settings, settings, Executors.newCachedThreadPool(), callback);



                    }
                }

                @Override
                public void onServiceDisconnected(int i) {

                }

            }, BluetoothProfile.HID_DEVICE);

        }).start();
    }

    byte[] currentPresses = new byte[4];
    byte[] keyboardReport = new byte[5];


    @SuppressLint("MissingPermission")
    public void SendKeyboard(String s, boolean down) {

        if (bluetoothHidDevice == null || myComputer == null) {
            return;
        }

        if (connectionState == State.CONNECTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                int[] k = ButtonHandler.codePar(s);

                if (k[0] == ButtonHandler.SPECIAL) {

                    keyboardReport[0] = (byte) (down ? keyboardReport[0] | 0x01 << k[1] : keyboardReport[0] ^ 0x01 << k[1]);

                    bluetoothHidDevice.sendReport(myComputer, 1, keyboardReport);


                } else if (k[0] == ButtonHandler.NORMAL) {

                    byte a = (byte) k[1];

                    for (int i = 0; i < currentPresses.length; i++) {

                        //if a key is released, find the key and set it to zero
                        if (currentPresses[i] == a && !down) {
                            currentPresses[i] = 0;
                            break;
                        }

                        //if a key is pressed find the first zero and add it
                        if (currentPresses[i] == 0 && down) {
                            currentPresses[i] = a;
                            break;
                        }
                    }

                    int startIndex = keyboardReport.length - currentPresses.length;
                    System.arraycopy(currentPresses, 0, keyboardReport, startIndex, keyboardReport.length - startIndex);

                    bluetoothHidDevice.sendReport(myComputer, 1, keyboardReport);
                }
            }
        }
    }

    byte[] mouseReport = new byte[3];

    @SuppressLint("MissingPermission")
    public void SendMouse(ButtonID.Vector vector) {

        if (bluetoothHidDevice == null || myComputer == null) {
            return;
        }

        if (connectionState == State.CONNECTED) {

            mouseReport[1] = (byte) Math.round(vector.x);
            mouseReport[2] = (byte) Math.round(vector.y);

            if (mouseReport[1] == 0 && mouseReport[2] == 0) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bluetoothHidDevice.sendReport(myComputer, 2, mouseReport);
            }
        }

    }


    @SuppressLint("MissingPermission")
    public void SendMouseButton(MouseButton mouseButton, boolean down) {

        if (bluetoothHidDevice == null || myComputer == null) {
            return;
        }

        if (connectionState == State.CONNECTED) {


            if (mouseButton == MouseButton.LEFT) {
                mouseReport[0] = (byte) (down ? mouseReport[0] | 0x01 : mouseReport[0] ^ 0x01);
            }

            if (mouseButton == MouseButton.MIDDLE) {
                mouseReport[0] = (byte) (down ? mouseReport[0] | 0x04 : mouseReport[0] ^ 0x04);
            }

            if (mouseButton == MouseButton.RIGHT) {
                mouseReport[0] = (byte) (down ? mouseReport[0] | 0x02 : mouseReport[0] ^ 0x02);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bluetoothHidDevice.sendReport(myComputer, 2, mouseReport);
            }
        }
    }

    byte[] joystickReport = new byte[5];

    public void SendJoystick(ButtonID.Vector vector) {

        //TODO : Give error saying this is comming soon!

    }

    public void SendXbox(int x) {

    }

    @SuppressLint("MissingPermission")
    public void destroy() {
        if (bluetoothHidDevice != null && myComputer != null) {

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothHidDevice.disconnect(myComputer);
            connectionState = NOCONNECTION;

            bluetoothHidDevice.unregisterApp();

            callback.onVirtualCableUnplug(myComputer);

            myComputer = null;

            MixPanel.mpEventTracking(mp, "BT_RESET", null);

        }

    }

    public boolean getPermission() {
        return (ConnectPermissionState == Permission.Accepted);
    }

    @SuppressLint("MissingPermission")
    public void endDescovery() {

        if(getPermission() && bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

    }

    @SuppressLint("MissingPermission")
    public String getName(BluetoothDevice bluetoothDevice) {
        return bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress()).getName();
    }
}

