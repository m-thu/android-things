package com.github.mthu.lm75;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends Activity implements SensorEventListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private Lm75Driver mLm75Driver;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                super.onDynamicSensorConnected(sensor);

                // Register callback functions when sensor gets connected
                if (sensor.getName().equals(Lm75Driver.DRIVER_NAME)) {
                    mSensorManager.registerListener(MainActivity.this, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                    Log.i(TAG, "LM75 driver connected.");
                }
            }
        });

        try {
            mLm75Driver = new Lm75Driver();
            mLm75Driver.registerDriver();
            Log.i(TAG, "LM75 driver registered successfully!");
        } catch (IOException e) {
            Log.e(TAG, "Failed to register LM75 driver!", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mLm75Driver != null) {
            mSensorManager.unregisterListener(this);
            mLm75Driver.unregisterDriver();

            try {
                mLm75Driver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LM75 driver!", e);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, "Temperature: " + event.values[0] + " °C");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Accuracy changed to: " + accuracy);
    }
}
