package com.dantsu.thermalprinter.async;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.content.Context;

import androidx.annotation.RequiresPermission;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

public class AsyncBluetoothEscPosPrint extends AsyncEscPosPrint {

    @RequiresPermission(allOf = { BLUETOOTH_SCAN, BLUETOOTH_CONNECT })
    public AsyncBluetoothEscPosPrint(Context context) {
        super(context);
    }

    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return AsyncEscPosPrint.FINISH_NO_PRINTER;
        }

        AsyncEscPosPrinter printerData = printersData[0];
        DeviceConnection deviceConnection = printerData.getPrinterConnection();

        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        try {

            if (deviceConnection == null) {
                printersData[0] = new AsyncEscPosPrinter(
                        BluetoothPrintersConnections.selectFirstPaired(),
                        printerData.getPrinterDpi(),
                        printerData.getPrinterWidthMM(),
                        printerData.getPrinterNbrCharactersPerLine()
                );
                printersData[0].setTextToPrint(printerData.getTextToPrint());
            } else {
                try {
                    deviceConnection.connect();
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) {
            return FINISH_MISSING_PERMISSIONS;
        }

        return super.doInBackground(printersData);
    }
}
