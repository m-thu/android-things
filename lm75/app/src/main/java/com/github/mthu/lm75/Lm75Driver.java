// Android Things user-space driver for LM75 temperature sensor

package com.github.mthu.lm75;

import android.hardware.Sensor;
import android.util.Log;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Lm75Driver extends UserSensorDriver implements AutoCloseable {
    private final static String TAG = Lm75Driver.class.getSimpleName();

    // Constants for sensor framework
    public  final static String DRIVER_NAME                = "LM75";
    private final static String DRIVER_VENDOR              = "";
    private final static int    DRIVER_VERSION             = 1;
    private final static int    DRIVER_MIN_DELAY           = 1_000_000; // us
    private final static int    DRIVER_MAX_DELAY           = 1_000_000; // us
    private final static String DRIVER_REQUIRED_PERMISSION = "";

    private Lm75 mLm75;
    private UserSensor mUserSensor;

    public Lm75Driver() throws IOException {
        mLm75 = new Lm75();
    }

    // Register driver in framework
    public void registerDriver() {
        if (mLm75 == null)
            throw new IllegalStateException(TAG + ": LM75 sensor isn't available!");

        if (mUserSensor == null) {
            mUserSensor = UserSensor.builder()
                    .setType(Sensor.TYPE_AMBIENT_TEMPERATURE)
                    .setName(DRIVER_NAME)
                    .setVendor(DRIVER_VENDOR)
                    .setVersion(DRIVER_VERSION)
                    //.setMaxRange(DRIVER_MAX_RANGE)
                    //.setResolution(DRIVER_RESOLUTION)
                    //.setPower(DRIVER_POWER)
                    .setMinDelay(DRIVER_MIN_DELAY)
                    .setMaxDelay(DRIVER_MAX_DELAY)
                    .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                    .setUuid(UUID.randomUUID())
                    .setDriver(this)
                    .build();
            UserDriverManager.getManager().registerSensor(mUserSensor);
        }
    }

    // Unregister driver from framework
    public void unregisterDriver() {
        if (mUserSensor != null) {
            UserDriverManager.getManager().unregisterSensor(mUserSensor);
            mUserSensor = null;
        }
    }

    // Free resources when we get closed
    @Override
    public void close() throws IOException {
        unregisterDriver();
        if (mLm75 != null) {
            try {
                mLm75.close();
            } finally {
                mLm75 = null;
            }
        }
    }

    // Called by framework to get sensor value
    @Override
    public UserSensorReading read() throws IOException {
        return new UserSensorReading(new float[] {mLm75.getTemp()});
    }

    // Called by framework to set low power mode
    @Override
    public void setEnabled(boolean enabled) throws IOException {
        if (enabled) {
            // Exit low power mode
            mLm75.powerUp();
            Log.i(TAG, "LM75 exited low power mode.");
        } else {
            // Enter low power mode
            mLm75.powerDown();
            Log.i(TAG, "LM75 entered low power mode.");
        }
    }
}
