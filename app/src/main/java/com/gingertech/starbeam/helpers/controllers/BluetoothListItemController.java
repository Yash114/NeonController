package com.gingertech.starbeam.helpers.controllers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gingertech.starbeam.R;

public class BluetoothListItemController extends ConstraintLayout {

    View root;

    public BluetoothDevice bluetoothDevice;

    public BluetoothListItemController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BluetoothListItemController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BluetoothListItemController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public BluetoothListItemController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate(R.layout.bluetooth_device_list_view, this, true);
    }

    @SuppressLint("MissingPermission")
    public void setup(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;

        ((TextView) root.findViewById(R.id.name)).setText(bluetoothDevice.getName());
    }
}
