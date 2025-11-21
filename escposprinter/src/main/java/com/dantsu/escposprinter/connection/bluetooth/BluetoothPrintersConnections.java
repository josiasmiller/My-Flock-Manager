package com.dantsu.escposprinter.connection.bluetooth;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

public class BluetoothPrintersConnections extends BluetoothConnections {

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @return a EscPosPrinterCommands instance
     */
    @Nullable
    @RequiresPermission(allOf = { BLUETOOTH_SCAN, BLUETOOTH_CONNECT })
    public static BluetoothConnection selectFirstPaired() {
        BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
        BluetoothConnection[] bluetoothPrinters = printers.getList();

        if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
            for (BluetoothConnection printer : bluetoothPrinters) {
                try {
                    return printer.connect();
                } catch (SecurityException|EscPosConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of EscPosPrinterCommands
     */
    @Nullable
    @RequiresPermission(BLUETOOTH_CONNECT)
    public BluetoothConnection[] getList() {
        try {
            BluetoothConnection[] bluetoothDevicesList = super.getList();

            if (bluetoothDevicesList == null) {
                return null;
            }

            int i = 0;
            BluetoothConnection[] printersTmp = new BluetoothConnection[bluetoothDevicesList.length];
            for (BluetoothConnection bluetoothConnection : bluetoothDevicesList) {
                BluetoothDevice device = bluetoothConnection.getDevice();

                int majDeviceCl = device.getBluetoothClass().getMajorDeviceClass(), //TODO: BLUETOOTH_CONNECT
                        deviceCl = device.getBluetoothClass().getDeviceClass(); //TODO: BLUETOOTH_CONNECT

                if (majDeviceCl == BluetoothClass.Device.Major.IMAGING && (deviceCl == 1664 || deviceCl == BluetoothClass.Device.Major.IMAGING)) {
                    printersTmp[i++] = new BluetoothConnection(device);
                }
            }
            BluetoothConnection[] bluetoothPrinters = new BluetoothConnection[i];
            System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i);

            return bluetoothPrinters;

        } catch (SecurityException e) {
            return null;
        }
    }
}
