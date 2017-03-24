// I2C driver for LM75 temperature sensor
// LM75 datasheet (Maxim): https://datasheets.maximintegrated.com/en/ds/LM75.pdf
// LM75 datasheet (TI): http://www.ti.com/lit/ds/symlink/lm75b.pdf

package com.github.mthu.lm75;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Lm75 implements AutoCloseable {
    private final static String TAG = Lm75.class.getSimpleName();

    // Default bus name and slave address
    private final static String DEFAULT_BUS_NAME      = "I2C1";
    private final static int    DEFAULT_SLAVE_ADDRESS = 0x48;

    // LM75 registers //

    // Temperature (read only)
    private final static int LM75_REG_TEMP   = 0x0;
    // Configuration (read/write)
    private final static int LM75_REG_CONFIG = 0x1;
    // T_HYST (r/w)
    private final static int LM75_REG_THYST  = 0x2;
    // T_OS (r/w)
    private final static int LM75_REG_TOS    = 0x3;

    // Command register

    // bit 7-5: always zero
    // bit 4-3: fault queue
    private final static int LM75_FAULTS_1    = (0x0 << 3);
    private final static int LM75_FAULTS_2    = (0x1 << 3);
    private final static int LM75_FAULTS_4    = (0x2 << 3);
    private final static int LM75_FAULTS_6    = (0x3 << 3);
    // bit   2: o.s. polarity (0: active low, 1: active high)
    private final static int LM75_OS_POLARITY = (1   << 2);
    // bit   1: comparator/interrupt mode (0: comparator, 1: interrupt)
    private final static int LM75_CMP_INT     = (1   << 1);
    // bit   0: shutdown (1: low power shutdown mode)
    private final static int LM75_SHUTDOWN    = (1   << 0);

    private I2cDevice mI2cDevice;

    public Lm75(String busName, int slaveAddress) throws IOException {
        try {
            mI2cDevice = new PeripheralManagerService().openI2cDevice(busName, slaveAddress);
        } catch (IOException e) {
            try {
                close();
            } catch (IOException ignored) {
                // ignored
            }

            throw e;
        }
    }

    public Lm75() throws IOException {
        this(DEFAULT_BUS_NAME, DEFAULT_SLAVE_ADDRESS);
    }

    // Power up sensor
    public void powerUp() throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException(TAG + ": I2C device is not connected!");

        mI2cDevice.writeRegByte(LM75_REG_CONFIG, (byte)0x00);
    }

    // Power down sensor
    public void powerDown() throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException(TAG + ": I2C device is not connected!");

        mI2cDevice.writeRegByte(LM75_REG_CONFIG, (byte)LM75_SHUTDOWN);
    }

    // Read temperature
    public float getTemp() throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException(TAG + ": I2C device is not connected!");

        return (float)(Short.reverseBytes(mI2cDevice.readRegWord(LM75_REG_TEMP)) >> 7) / 2;
    }

    // Free resources when we get closed
    @Override
    public void close() throws IOException {
        if (mI2cDevice != null) {
            try {
                mI2cDevice.close();
            } finally {
                mI2cDevice = null;
            }
        }
    }
}
