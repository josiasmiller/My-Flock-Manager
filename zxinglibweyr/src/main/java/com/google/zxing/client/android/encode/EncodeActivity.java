/*
 * Copyright (C) 2008 ZXing authors
 * Modifications Copyright (C) 2013-2014 Weyr Associates LLC authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.encode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.FinishListener;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class encodes data from an Intent into a QR code, and then displays it full screen so that
 * another person can scan it with their device.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EncodeActivity extends AppCompatActivity {

    /**
     * Returns a File reference that leads to the temp
     * bar code file for printing bar codes.
     * @param context The associated Context
     * @return File for storage location of the temp
     * bar code file.
     */
    @Deprecated
    public static File tempFileForBarcodePrinting(Context context) {
        //TODO: Get rid of this once we can eliminate the need for this activity.
        return new File(barcodeScannerDirectory(context), "temp_barcode.png");
    }

    private static File barcodeScannerDirectory(Context context) {
        File directory = new File(context.getExternalFilesDir(null), "BarcodeScanner");
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        return directory;
    }

    private static final String TAG = EncodeActivity.class.getSimpleName();

//  private static final int MAX_BARCODE_FILENAME_LENGTH = 24;
//  private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");
//  private static final String USE_VCARD_KEY = "USE_VCARD";

  private QRCodeEncoder qrCodeEncoder;
  
  private boolean autoPrint = false;
  
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    Intent intent = getIntent();
      Log.d(TAG, "Encoding create");
    if (intent == null) {
      finish();
    } else {
      String action = intent.getAction();

      
      if (Intents.Encode.ACTION.equals(action) || Intent.ACTION_SEND.equals(action)) {
          String data = intent.getStringExtra(Intents.Encode.AUTOPRINT);
		   
    	  if (data.equals("true")) {
    	  autoPrint = true;
    	  };
          Log.d(TAG, "Encoding before share");
    	  share_test();   // Try to send pix directly to print utility
    	  finish();  // needed if above commented out
      } else {
        finish();
      }
    }
  }


    private void share_test() {

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        Log.i("Encoding", "width= "+ width + " height= "+ height);
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;
        Bitmap bitmap = null;
        Log.d(TAG, "Encoding at share1");
        try {
            qrCodeEncoder = new QRCodeEncoder(this, getIntent(), smallerDimension);
            bitmap = qrCodeEncoder.encodeAsBitmap();
            if (bitmap == null) {
                Log.w(TAG, "Could not encode barcode");
                showErrorMessage(R.string.msg_encode_contents_failed);
                qrCodeEncoder = null;
                return;
            }
        } catch (WriterException e) {
            Log.w(TAG, "Could not encode barcode", e);
            showErrorMessage(R.string.msg_encode_contents_failed);
            qrCodeEncoder = null;
        }
        Log.d(TAG, "Encoding at share2");
        String contents = qrCodeEncoder.getContents();

        if (contents == null) {
            Log.w(TAG, "No existing barcode to send?2");
            return;
        }
        Log.d(TAG, "Encoding at share3");
        //TODO: This bsRoot directory setup can't use AppDirectories because it is a separate module. Discuss solutions later.
        File barcodesRoot = barcodeScannerDirectory(this);
        Log.d(TAG, "Encoding at share3a");
        if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
            Log.w(TAG, "Couldn't make dir " + barcodesRoot);
            showErrorMessage(R.string.msg_unmount_usb);
            return;
        }
        Log.d(TAG, "Encoding at share4");

        File barcodeFile = tempFileForBarcodePrinting(this);
        Log.d(TAG, "Encoding at share4a");
        barcodeFile.delete();
        Log.d(TAG, "Encoding at share4b");
        FileOutputStream fos = null;
        Log.d(TAG, "Encoding at share4c");
        try {
            fos = new FileOutputStream(barcodeFile);
            Log.d(TAG, "Encoding at share4d");
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
            Log.d(TAG, "Encoding at share4e");

        } catch (FileNotFoundException fnfe) {
            Log.w(TAG, "Couldn't access file " + barcodeFile + " due to " + fnfe);

            return;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "Encoding at share4f");
                    // do nothing
                }
            }
        }
            Log.d(TAG, "Encoding at share5");

        Intent encodex = new Intent(this, EncodeActivity.class);
        Bundle data1 = new Bundle();
        encodex.putExtra(encodex.EXTRA_STREAM, Uri.parse("content://" + barcodeFile.getAbsolutePath()));
        data1.putString("NAME",  barcodeFile.getAbsolutePath());
        encodex.setType("image/png");
        Log.i("EncodeActivity ", " File = " + barcodeFile.getAbsolutePath());
        if (autoPrint) {
            encodex.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        }
        encodex.putExtras(data1);
        setResult(444, encodex);
        finish();
    }


  @Override
  protected void onResume() {
    super.onResume();
    // This assumes the view is full screen, which is a good assumption
    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    int width = display.getWidth();
    int height = display.getHeight();
    int smallerDimension = width < height ? width : height;
    smallerDimension = smallerDimension * 7 / 8;
      Log.w(TAG, "At onresume");
    Intent intent = getIntent();

    if (intent == null) {
      return;
    }
  }


  private void showErrorMessage(int message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(message);
    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }
}
