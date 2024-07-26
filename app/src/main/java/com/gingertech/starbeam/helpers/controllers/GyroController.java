package com.gingertech.starbeam.helpers.controllers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class GyroController implements SensorEventListener {

    GenericCallbackv2 gyroCallback;

    private final SensorManager sensorManager;
    private final Sensor gyroscopeSensor;

    public static boolean toggleEnabled = false;
    public static boolean gyroActivated = false;

    private boolean started = false;
    public GyroController(GenericCallbackv2 gyroCallback, Context context) {
        this.gyroCallback = gyroCallback;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void startSensor() {
        if(!started) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
            started = true;
        }
    }

    public void endSensor() {
        if(started) {
            sensorManager.unregisterListener(this);
            started = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && started) {
            float x = event.values[0];  // Gyroscope X-axis value
            float y = event.values[1];  // Gyroscope Y-axis value
            float z = event.values[2];  // Gyroscope Z-axis value

            // Do something with the gyroscope values
            // For example, update UI or perform calculations
            if(gyroCallback != null) {
                gyroCallback.onChange(x, y);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
