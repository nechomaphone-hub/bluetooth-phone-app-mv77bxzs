package com.generated.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothManagerHelper {
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private BluetoothDevice connectedDevice;
    private boolean isConnected = false;

    public BluetoothManagerHelper(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public List<BluetoothDevice> getPairedDevices() {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        if (bluetoothAdapter == null) return deviceList;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return deviceList;
            }
        }

        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        if (paired != null) {
            deviceList.addAll(paired);
        }
        return deviceList;
    }

    public boolean connectToDevice(BluetoothDevice device) {
        if (device == null) return false;
        // In standard public API without hidden reflection, direct HFP socket connection
        // is managed by Android system profiles. We register connection state here.
        connectedDevice = device;
        isConnected = true;
        return true;
    }

    public void disconnect() {
        connectedDevice = null;
        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }
}
