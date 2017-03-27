// I2C driver for HMC5883L 3-axis digital compass
// Datasheet: https://cdn-shop.adafruit.com/datasheets/HMC5883L_3-Axis_Digital_Compass_IC.pdf

package com.github.mthu.hmc5883l;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Hmc5883l implements AutoCloseable {
    private final static String TAG = Hmc5883l.class.getSimpleName();

    // Default bus name and slave address
    private final static String DEFAULT_BUS_NAME      = "I2C1";
    private final static int    DEFAULT_SLAVE_ADDRESS = 0x1e;

    // Registers //

    private final static int REG_CONFIG_A =  0; // Configuration Register A   (R/W)
    private final static int REG_CONFIG_B =  1; // Configuration Register B   (R/W)
    private final static int REG_MODE     =  2; // Mode Register              (R/W)
    private final static int REG_X_MSB    =  3; // Data Output X MSB Register (R  )
    private final static int REG_X_LSB    =  4; // Data Output X LSB Register (R  )
    private final static int REG_Z_MSB    =  5; // Data Output Z MSB Register (R  )
    private final static int REG_Z_LSB    =  6; // Data Output Z LSB Register (R  )
    private final static int REG_Y_MSB    =  7; // Data Output Y MSB Register (R  )
    private final static int REG_Y_LSB    =  8; // Data Output Y LSB Register (R  )
    private final static int REG_STATUS   =  9; // Status Register            (R  )
    private final static int REG_ID_A     = 10; // Identification Register A  (R  )
    private final static int REG_ID_B     = 11; // Identification Register B  (R  )
    private final static int REG_ID_C     = 12; // Identification Register C  (R  )

    // Configuration Register A (R/W) //

    // Bit 7   : Reserved
    // Bit 6..5: Number of sampled averaged per measurement
    @Retention(SOURCE)
    @IntDef({AVERAGE_1, AVERAGE_2, AVERAGE_4, AVERAGE_8})
    public @interface Average {}
    public  final static int  AVERAGE_1    = 0x0 << 5; // 1
    public  final static int  AVERAGE_2    = 0x1 << 5; // 2
    public  final static int  AVERAGE_4    = 0x2 << 5; // 4
    public  final static int  AVERAGE_8    = 0x3 << 5; // 8
    private final static byte CRA_AVG_MASK = 0x3 << 5;
    // Bit 4..2: Data output rate (continuous measurement mode)
    @Retention(SOURCE)
    @IntDef({RATE_0_75, RATE_1_5, RATE_3, RATE_7_5, RATE_15, RATE_30, RATE_75})
    public @interface Rate {}
    public  final static int  RATE_0_75     = 0x0 << 2; // 0.75 Hz
    public  final static int  RATE_1_5      = 0x1 << 2; // 1.5  Hz
    public  final static int  RATE_3        = 0x2 << 2; // 3    Hz
    public  final static int  RATE_7_5      = 0x3 << 2; // 7.5  Hz
    public  final static int  RATE_15       = 0x4 << 2; // 15   Hz (default)
    public  final static int  RATE_30       = 0x5 << 2; // 30   Hz
    public  final static int  RATE_75       = 0x6 << 2; // 75   Hz
    private final static byte CRA_RATE_MASK = 0x7 << 2;
    // Bit 1..0: Measurement mode
    @Retention(SOURCE)
    @IntDef({MODE_NORMAL, MODE_POS, MODE_NEG})
    public @interface Mode {}
    public  final static int  MODE_NORMAL     = 0x0; // Normal measurement mode (default)
    public  final static int  MODE_POS        = 0x1; // Positive bias for X, Y, Z
    public  final static int  MODE_NEG        = 0x2; // Negative bias for X, Y, Z
    private final static byte CRA_MODE_MASK   = 0x3;

    // Configuration Register B (R/W) //

    // Bit 7..5: Gain configuration (LSb/Gauss)
    @Retention(SOURCE)
    @IntDef({GAIN_1370, GAIN_1090, GAIN_820, GAIN_660, GAIN_440, GAIN_390, GAIN_330, GAIN_230})
    public @interface Gain {}
    public  final static int  GAIN_1370     = 0x0 << 5; // 1370
    public  final static int  GAIN_1090     = 0x1 << 5; // 1090 (default)
    public  final static int  GAIN_820      = 0x2 << 5; // 820
    public  final static int  GAIN_660      = 0x3 << 5; // 660
    public  final static int  GAIN_440      = 0x4 << 5; // 440
    public  final static int  GAIN_390      = 0x5 << 5; // 390
    public  final static int  GAIN_330      = 0x6 << 5; // 330
    public  final static int  GAIN_230      = 0x7 << 5; // 230
    private final static byte CRB_GAIN_MASK = (byte)(0x7 << 5);
    // Bit 4..0: Always 0

    // Mode Register (R/W) //

    // Bit 7   : Enable high-speed I2C (3400 kHz)
    // Bit 6..2: Always 0
    // Bit 1..0: Operating Mode
    @Retention(SOURCE)
    @IntDef({OP_CONTINUOUS, OP_SINGLE, OP_IDLE_1, OP_IDLE_2})
    public @interface Op {}
    public  final static int  OP_CONTINUOUS = 0x0; // Continuous-measurement mode
    public  final static int  OP_SINGLE     = 0x1; // Single-measurement mode (default)
    public  final static int  OP_IDLE_1     = 0x2; // Idle mode
    public  final static int  OP_IDLE_2     = 0x3; // Idle mode
    private final static byte MODE_OP_MASK  = 0x3;

    // Status Register (R) //

    // Bit 7..2: Reserved
    // Bit 1   : Data output register lock
    private final static byte STATUS_LOCK = 1 << 1;
    // Bit 0   : Ready bit
    private final static byte STATUS_RDY  = 1 << 0;

    // Identification Registers //

    // ID A: ASCII value 'H'
    // ID B: ASCII value '4'
    // ID C: ASCII value '3'
    public final static byte ID_A = 'H';
    public final static byte ID_B = '4';
    public final static byte ID_C = '3';

    // Digital resolution (mGauss / LSb), default: 0.92
    private double mResolution = 0.92;
    private I2cDevice mI2cDevice;

    public class RangeOverflowException extends Exception {}

    public Hmc5883l(String busName, int slaveAddress) throws IOException {
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

        byte[] id = getId();
        if (id[0] == ID_A && id[1] == ID_B && id[2] == ID_C) {
            Log.i(TAG, "HMC5883L detected!");
        } else {
            throw new IOException("ID registers don't match expected values!");
        }

    }

    public Hmc5883l() throws IOException {
        this(DEFAULT_BUS_NAME, DEFAULT_SLAVE_ADDRESS);
    }

    // Set number of samples averaged per measurement
    public void setAverage(@Average int average) throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte cra = (byte)(mI2cDevice.readRegByte(REG_CONFIG_A) & ~CRA_AVG_MASK);
        cra |= average;
        mI2cDevice.writeRegByte(REG_CONFIG_A, cra);
    }

    // Set measurement rate in continuous mode (Hz)
    public void setRate(@Rate int rate) throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte cra = (byte)(mI2cDevice.readRegByte(REG_CONFIG_A) & ~CRA_RATE_MASK);
        cra |= rate;
        mI2cDevice.writeRegByte(REG_CONFIG_A, cra);
    }

    // Set measurement mode
    public void setMode(@Mode int mode) throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte cra = (byte)(mI2cDevice.readRegByte(REG_CONFIG_A) & ~CRA_MODE_MASK);
        cra |= mode;
        mI2cDevice.writeRegByte(REG_CONFIG_A, cra);
    }

    // Set gain (LSb/Gauss)
    public void setGain(@Gain int gain) throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte crb = (byte)(mI2cDevice.readRegByte(REG_CONFIG_B) & ~CRB_GAIN_MASK);
        crb |= gain;
        mI2cDevice.writeRegByte(REG_CONFIG_B, crb);

        switch (gain) {
            case GAIN_1370:
                mResolution = 0.73;
                break;
            case GAIN_1090:
                mResolution = 0.92; // default
                break;
            case GAIN_820:
                mResolution = 1.22;
                break;
            case GAIN_660:
                mResolution = 1.52;
                break;
            case GAIN_440:
                mResolution = 2.27;
                break;
            case GAIN_390:
                mResolution = 2.56;
                break;
            case GAIN_330:
                mResolution = 3.03;
                break;
            case GAIN_230:
                mResolution = 4.35;
                break;
        }
    }

    // Set operating mode
    public void setOperatingMode(@Op int op) throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte mode = (byte)(mI2cDevice.readRegByte(REG_MODE) & ~MODE_OP_MASK);
        mode |= op;
        mI2cDevice.writeRegByte(REG_MODE, mode);
    }

    // Get X value (mGauss)
    public double getX() throws IOException, RangeOverflowException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        int x = Short.reverseBytes(mI2cDevice.readRegWord(REG_X_MSB));
        if (x == -4096)
            throw new RangeOverflowException();

        return x * mResolution;
    }

    // Get Y value (mGauss)
    public double getY() throws IOException, RangeOverflowException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        int y = Short.reverseBytes(mI2cDevice.readRegWord(REG_X_MSB));
        if (y == -4096)
            throw new RangeOverflowException();

        return y * mResolution;
    }

    // Get Z value (mGauss)
    public double getZ() throws IOException, RangeOverflowException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        int z = Short.reverseBytes(mI2cDevice.readRegWord(REG_X_MSB));
        if (z == -4096)
            throw new RangeOverflowException();

        return z * mResolution;
    }

    // Get X, Y, Z values (mGauss)
    public double[] getXYZ() throws IOException, RangeOverflowException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte[] buffer = new byte[3*2];
        mI2cDevice.readRegBuffer(REG_X_MSB, buffer, buffer.length);

        int x = (buffer[0] << 8) | buffer[1];
        int z = (buffer[2] << 8) | buffer[3];
        int y = (buffer[4] << 8) | buffer[5];

        if (x == -4096 || y == -4096 || z == -4096)
            throw new RangeOverflowException();

        return new double[] {x * mResolution, y * mResolution, z * mResolution};
    }

    // Get LOCK bit from status register
    public boolean getLock() throws IOException {
        return (mI2cDevice.readRegByte(REG_STATUS) & STATUS_LOCK) == STATUS_LOCK;
    }

    // Get RDY bit from status registers
    public boolean getRdy() throws IOException {
        return (mI2cDevice.readRegByte(REG_STATUS) & STATUS_RDY) == STATUS_RDY;
    }

    // Return ID registers A, B, C
    public byte[] getId() throws IOException {
        if (mI2cDevice == null)
            throw new IllegalStateException("I2C device is not connected!");

        byte[] buffer = new byte[3];
        mI2cDevice.readRegBuffer(REG_ID_A, buffer, buffer.length);

        return buffer;
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
