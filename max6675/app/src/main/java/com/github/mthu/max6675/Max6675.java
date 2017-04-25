package com.github.mthu.max6675;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Max6675 implements AutoCloseable {
    private final static String TAG = Max6675.class.getSimpleName();

    // Default SPI bus name
    private final static String DEFAULT_BUS_NAME = "SPI0.0";

    private SpiDevice mSpiDevice;

    public Max6675(String busName) throws IOException {
        try {
            mSpiDevice = new PeripheralManagerService().openSpiDevice(busName);

            // Clock idle low, read data on falling edge
            mSpiDevice.setMode(SpiDevice.MODE1);
            // Max. clock frequency: 4.3 MHz
            mSpiDevice.setFrequency(1_000_000);
            // Read 8 bit values
            mSpiDevice.setBitsPerWord(8);
            // MSB first
            mSpiDevice.setBitJustification(false);
            // Force /CS high after each transfer to initiate new conversion
            mSpiDevice.setCsChange(false);
        } catch (IOException e) {
            try {
                close();
            } catch (IOException ignored) {
                // ignore
            }

            throw e;
        }
    }

    public Max6675() throws IOException {
        this(DEFAULT_BUS_NAME);
    }

    // Get temperature (0.25 K accuracy, 0 °C ... 1023.75 °C)
    public float getTemp() throws IOException {
        byte[] buffer = new byte[2];
        mSpiDevice.read(buffer, 2);
        int tmp = (buffer[0] << 8) | (buffer[1] & 0xff);

        // Check if dummy sign bit is zero
        if ((tmp & 0x8000) == 0x8000)
            throw new IOException("Dummy sign bit isn't zero!");
        // Check device ID (has to be zero)
        if ((tmp & 0x0002) == 0x0002)
            throw new IOException("Incompatible device!");
        // Check if thermocouple is connected
        if ((tmp & 0x0004) == 0x0004)
            throw new IOException("No thermocouple connected!");

        // Get temperature (12 bit, 0.25 K accuracy, always >= 0°C)
        tmp = (tmp >>> 3) & 0xfff;
        return 0.25f * tmp;
    }

    // Free resources when we get destroyed
    @Override
    public void close() throws IOException {
        if (mSpiDevice != null) {
            try {
                mSpiDevice.close();
            } finally {
                mSpiDevice = null;
            }
        }
    }
}
