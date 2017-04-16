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

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get Bluetooth adapter
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
                Log.d(TAG, "New Bluetooth device found: "+device.getName()+
                        " ("+device.getAddress()+")");
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


    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mDeviceFoundBroadcastReceiver);
        unregisterReceiver(mStateChangeBroadcastReceiver);
    }
}
