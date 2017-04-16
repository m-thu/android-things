package com.github.mthu.hc05;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();

    // HC-05 Serial-to-Bluetooth adapter parameters
    private final static String HC05_NAME = "HC-05";
    private final static String HC05_MAC  = "XX:XX:XX:XX:XX:XX";
    private final static byte[] HC05_PIN  = {1, 2, 3, 4};

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            throw new RuntimeException("No default Bluetooth adapter!");

        // Register broadcast receiver for Bluetooth device discovery
        registerReceiver(mDeviceFoundBroadcastReceiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // Register broadcast receiver for Bluetooth adapter state changes
        registerReceiver(mStateChangeBroadcastReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        // Register broadcast receiver for start of discovery
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Bluetooth discovery has started!");
            }
        }, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        // Register broadcast receiver for bond state changes
        registerReceiver(mBondStateChangeBroadcastReceiver,
                new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startDiscovery();
        } else {
            mBluetoothAdapter.enable();
        }
    }

    private final BroadcastReceiver mDeviceFoundBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "New Bluetooth device found: " + device.getName() +
                        " (" + device.getAddress() + ")");

                if ((device.getName() != null && device.getName().equals(HC05_NAME))
                        || device.getAddress().equals(HC05_MAC)) {
                    mBluetoothAdapter.cancelDiscovery();
                    device.setPin(HC05_PIN);
                    device.createBond();
                }
            }
        }
    };

    private final BroadcastReceiver mStateChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                mBluetoothAdapter.startDiscovery();
        }
    };

    private final BroadcastReceiver mBondStateChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);

                switch (bondState) {
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG, "Bluetooth device not paired!");
                        break;

                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "Bluetooth device pairing!");
                        break;

                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG, "Bluetooth device paired!");
                        break;
                }
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mDeviceFoundBroadcastReceiver);
        unregisterReceiver(mStateChangeBroadcastReceiver);
    }
}
