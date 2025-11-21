package com.dantsu.escposprinter.connection.bluetooth;

import static android.Manifest.permission.BLUETOOTH_CONNECT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import java.util.Set;

public class BluetoothConnections {
    protected BluetoothAdapter bluetoothAdapter;
    
    /**
     * Create a new instance of BluetoothConnections
     */
    public BluetoothConnections() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    /**
     * Get a list of bluetooth devices available.
     * @return Return an array of BluetoothConnection instances,
     * or null if proper permissions have not been acquired or
     * Bluetooth has not been enabled.
     */
    @Nullable
    @RequiresPermission(BLUETOOTH_CONNECT)
    public BluetoothConnection[] getList() {
        if (this.bluetoothAdapter == null) {
            return null;
        }
    
        if(!this.bluetoothAdapter.isEnabled()) {
            return null;
        }

        Set<BluetoothDevice> bluetoothDevicesList = getBondedDevices();

        if (bluetoothDevicesList == null) {
            return null;
        }

        BluetoothConnection[] bluetoothDevices = new BluetoothConnection[bluetoothDevicesList.size()];
    
        if (!bluetoothDevicesList.isEmpty()) {
            int i = 0;
            for (BluetoothDevice device : bluetoothDevicesList) {
                bluetoothDevices[i++] = new BluetoothConnection(device);
            }
        }
        
        return bluetoothDevices;
    }

    /**
     * Attempts to get the bonded bluetooth devices from
     * the bluetooth adapter.
     *
     * @return null if the proper permissions are not granted,
     * empty set if there are no bonded devices, and a non-empty
     * set if there are bonded devices.
     */
    @Nullable
    @RequiresPermission(BLUETOOTH_CONNECT)
    private Set<BluetoothDevice> getBondedDevices() {
        try {
            return bluetoothAdapter.getBondedDevices();
        } catch (SecurityException ex) {
            return null;
        }
    }
}
