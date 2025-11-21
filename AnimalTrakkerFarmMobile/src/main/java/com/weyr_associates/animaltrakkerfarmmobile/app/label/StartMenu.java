package com.weyr_associates.animaltrakkerfarmmobile.app.label;


import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.dantsu.thermalprinter.async.AsyncBluetoothEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncEscPosPrinter;
import com.google.zxing.client.android.encode.EncodeActivity;
import com.weyr_associates.animaltrakkerfarmmobile.R;
import com.weyr_associates.animaltrakkerfarmmobile.app.core.Sdk;
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.Permissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * make the main activity
 *
 * @author Brother Industries, Ltd.
 * @version 1.0
 * @author Weyr Associates, LLC.
 * @version 1.6
 */
public class StartMenu extends AppCompatActivity {

	/**
	 * Returns a File reference that leads to the temp
	 * bar code file for printing bar codes.
	 * @param context
	 * @return File for storage location of the temp
	 * bar code file.
	 */
	@Deprecated
	public static File tempFileForBarcodePrinting(Context context) {
		return EncodeActivity.tempFileForBarcodePrinting(context);
	}

	private static final int FILE_LIST = 1;
	//	private static final int SDK_EVENT = 50;
	private static final int UPDATE = 51;

	private BluetoothConnection selectedDevice;
	private BluetoothDevice printer;
	private String MACAddress = "00:00:00:00:00:00";
//	private String newMACAddress = "qwerty";
	private TextView selectedFile;
	private ImageView imageView01;
	private String fileName;
	private Bitmap mBitmap;
//	private ProgressDialog progressDialog;

	String path;
//	private boolean fromIntent = false;

//	private boolean isGetStatus = false;

//	private boolean isPrinting = false;
//	String memory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		Log.i("Print", "p1 ");
		Button button = (Button) this.findViewById(R.id.PrinterSelect);
		LoadPreferences(false);
		selectedDevice = new BluetoothConnection(printer);
		Log.i("Print", "Load Prefs OnCreate");
		button.setOnClickListener(view -> {
			tryBrowseBluetoothDevices();
		});

		Log.i("Print", "p1a ");
		fileName = getIntent().getExtras().get("android.intent.extra.STREAM").toString();

		Log.i("Print", "p1b " + fileName);
		selectedFile = (TextView) findViewById(R.id.selectedFile_id);
		Log.i("Print", "p1c " + selectedFile);
		imageView01 = (ImageView) this.findViewById(R.id.ImageView01);

		//Get file path from intent
		if (Intent.ACTION_SEND.equals(getIntent().getAction()) || Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			Log.i("Print", "p2");
			if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
				Log.i("Print", "p3");
				fileName = getIntent().getExtras().get("android.intent.extra.STREAM").toString();
				Log.i("Print", "p3a " + fileName);
				if (fileName.indexOf("content://") != -1) {

					final Uri imageUri = Uri.parse(getIntent().getExtras().get("android.intent.extra.STREAM").toString());
					Log.i("Print", "p4 " + imageUri);
					String[] projection = {MediaStore.Images.Media.DATA};
					Log.i("Print", "p4a " + projection);
					Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null);
					Log.i("Print", "p4b " + cursor);
					int columnindex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					fileName = cursor.getString(columnindex);
					cursor.close();
				} else if (fileName.indexOf("content://") != -1) {

					fileName = getIntent().getExtras().get("android.intent.extra.STREAM").toString();
					fileName = Uri.decode(fileName);
					fileName = fileName.substring(7);
				} else {

					fileName = getIntent().getExtras().get("android.intent.extra.STREAM").toString();
					Log.i("Print", "p8 " + fileName);
				}
			} else {
				String temp = getIntent().getType();
				String fileType = temp.substring(temp.indexOf("/") + 1);
				String folder = new File(getExternalFilesDir(null), "Printer").toString();
				Log.e("Print", "Storage");
				File newdir = new File(folder);
				if (!newdir.exists()) {
					newdir.mkdir();
				}
				fileName = folder + "tmp." + fileType;
				Log.i("Print", "p7 " + fileName);
				try {
					InputStream input = null;
					OutputStream output = null;
					input = getContentResolver().openInputStream(getIntent().getData());
					File dstFile = new File(fileName);
					output = new FileOutputStream(dstFile);
					int DEFAULT_BUFFER_SIZE = 1024 * 4;
					byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
					int n = 0;
					while (-1 != (n = input.read(buffer))) {
						output.write(buffer, 0, n);
					}
					input.close();
					output.close();
				} catch (FileNotFoundException e1) {
				} catch (IOException e) {
				}
			}
//			fromIntent = true;
			if (isImageFile(fileName)) {
				//load picture
				if (mBitmap != null && !mBitmap.isRecycled()) {
					mBitmap.recycle();
					mBitmap = null;
				}
				mBitmap = fileToBitmap(fileName);
				imageView01.setImageBitmap(mBitmap);
			}

			//enable print button
			Button printButton = (Button) findViewById(R.id.printButton);
			printButton.setEnabled(true);
		}
// +++++++++++++++++
		if (fileName != null && fileName != "") {
			Log.i("Print", "p20 " + fileName);
			selectedFile.setText(fileName);

			if (isImageFile(fileName)) {
				//load picture
				Log.i("Print", "p21 ");
				if (mBitmap != null && !mBitmap.isRecycled()) {
					Log.i("Print", "p22 ");
					mBitmap.recycle();
					mBitmap = null;
				}
				mBitmap = fileToBitmap(fileName);
				imageView01.setImageBitmap(mBitmap);
				Log.i("Print", "p23 ");
				//enable print button
				Button printButton = (Button) findViewById(R.id.printButton);
				printButton.setEnabled(true);
			}
// +++++++++++++++
		}
		Log.i("Print", "p24 ");
		if (getIntent() != null &&
				(getIntent().getFlags() & Intent.FLAG_ACTIVITY_NO_USER_ACTION) != 0) {
			Log.i("Print", "p25 ");
			tryPrintToBluetooth();
//			 finish();
		}

	}

    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

	private static final int REQUEST_PERMISSION_BROWSE_BT_DEVICES = 1;

	private static final int REQUEST_PERMISSION_PRINT_TO_BT_DEVICE = 2;

	private static final int REQUEST_ENABLE_BLUETOOTH_BROWSE_BT_DEVICES = 3;

	private static final int REQUEST_ENABLE_BLUETOOTH_PRINT_TO_BT_DEVICE = 4;

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_PERMISSION_BROWSE_BT_DEVICES:
				if (checkBrowseBluetoothPermission()) {
					tryBrowseBluetoothDevices();
				}
				break;
			case REQUEST_PERMISSION_PRINT_TO_BT_DEVICE:
				if (checkPrintBluetoothPermission()) {
					tryPrintToBluetooth();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private boolean checkBrowseBluetoothPermission() {
		return !Sdk.requiresRuntimeBluetoothPermissions() ||
				Permissions.checkAllSelfPermission(this, BLUETOOTH_CONNECT);
	}

	private void requestBrowseBluetoothDevicePermission() {
		ActivityCompat.requestPermissions(this,
				new String[] { BLUETOOTH_CONNECT },
				REQUEST_PERMISSION_BROWSE_BT_DEVICES);
	}

	private void tryBrowseBluetoothDevices() {
		//Check enable bluetooth after permissions
		//to ensure proper permissions exist to
		//enable bluetooth.
		if (!checkBrowseBluetoothPermission()) {
			requestBrowseBluetoothDevicePermission();
		} else if (!isBluetoothEnabled()) {
			requestEnableBlueTooth(REQUEST_ENABLE_BLUETOOTH_BROWSE_BT_DEVICES);
		}
		browseBluetoothDevices();
	}

	@RequiresPermission(BLUETOOTH_CONNECT)
	private void browseBluetoothDevices() {
		try {
			final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();
			if (bluetoothDevicesList != null) {
				final String[] items = new String[bluetoothDevicesList.length + 1];
				items[0] = "Default printer";
				int i = 0;
				for (BluetoothConnection device : bluetoothDevicesList) {
					items[++i] = device.getDevice().getName();
				}

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(StartMenu.this);
				alertDialog.setTitle("Bluetooth printer selection");
				alertDialog.setItems(items, (dialogInterface, i1) -> {
                    int index = i1 - 1;
                    if (index == -1) {
                        selectedDevice = null;
                    } else {
                        selectedDevice = bluetoothDevicesList[index];
                    }

                    Button button = (Button) findViewById(R.id.PrinterSelect);
                    button.setText(items[i1]);
                }).create().show();
			}
        } catch (SecurityException e) {
			Toast.makeText(this, R.string.start_menu_browse_missing_bt_permissions, Toast.LENGTH_LONG).show();
		}
    }

	private boolean checkPrintBluetoothPermission() {
		return !Sdk.requiresRuntimeBluetoothPermissions() ||
				Permissions.checkAllSelfPermission(this,
						BLUETOOTH_CONNECT,
						BLUETOOTH_SCAN);
	}

	private void requestPrintToBluetoothPermission() {
		ActivityCompat.requestPermissions(this,
				new String[] { BLUETOOTH_CONNECT, BLUETOOTH_SCAN },
				REQUEST_PERMISSION_PRINT_TO_BT_DEVICE);
	}

	private void tryPrintToBluetooth() {
		//Check enable bluetooth after permissions
		//to ensure proper permissions exist to
		//enable bluetooth.
		if (!checkPrintBluetoothPermission()) {
			requestPrintToBluetoothPermission();
		} else if (!isBluetoothEnabled()) {
			requestEnableBlueTooth(REQUEST_ENABLE_BLUETOOTH_PRINT_TO_BT_DEVICE);
		} else {
			printToBluetooth();
		}
	}

	@RequiresPermission(allOf = { BLUETOOTH_CONNECT, BLUETOOTH_SCAN})
    private void printToBluetooth() {
		Log.i("Print", "PBT 2 " + selectedDevice);
		new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(selectedDevice));
 	}

    /**
     * Asynchronous printing
     */
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {

        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        return printer.setTextToPrint(

				"[R]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, mBitmap) + "</img>\n"

        );
    }

/*	public void printIt(DeviceConnection printerConnection) {
		try {

			EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 48f, 32);

			printer.printFormattedTextAndCut(

							"[R]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, mBitmap) + "</img>\n"

					);
		} catch (EscPosConnectionException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this)
					.setTitle("Broken connection")
					.setMessage(e.getMessage())
					.show();
		} catch (EscPosParserException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this)
					.setTitle("Invalid formatted text")
					.setMessage(e.getMessage())
					.show();
		} catch (EscPosEncodingException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this)
					.setTitle("Bad selected encoding")
					.setMessage(e.getMessage())
					.show();
		}
	}*/

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu_option, menu);
		LoadPreferences(false);
		Log.i("StartMenu","Load prefs options menu");
		return super.onCreateOptionsMenu(menu);
	}*/

/*	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){

		case R.id.option_menu_about:
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.about_title);
			dialog.setMessage(R.string.about_text);
			dialog.setPositiveButton(R.string.ok_label, null);
			dialog.show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}*/

	private boolean isBluetoothEnabled() {
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		return bta != null && bta.isEnabled();
	}

	@IntDef({
		REQUEST_ENABLE_BLUETOOTH_BROWSE_BT_DEVICES,
		REQUEST_ENABLE_BLUETOOTH_PRINT_TO_BT_DEVICE
	})
	@interface EnableBluetoothReason {}

	@RequiresPermission(BLUETOOTH_CONNECT)
	private void requestEnableBlueTooth(@EnableBluetoothReason int requestCode) {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) ||
				BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(this, R.string.toast_bluetooth_is_supported_on_device, Toast.LENGTH_LONG).show();
			return;
        }
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		Log.i("Print", "p6 ");
		if(requestCode == FILE_LIST) {
			if(resultCode == RESULT_OK){
				setContentView(R.layout.mainmenu);
				Button printButton = (Button)findViewById(R.id.printButton);
				selectedFile = (TextView)findViewById(R.id.selectedFile_id);
				selectedFile.setText(data.getStringExtra("fileName"));
				CharSequence fileList_label = selectedFile.getText();
				String path = fileList_label.toString();
				if(isImageFile(path)){
					if(mBitmap != null && !mBitmap.isRecycled()){
						mBitmap.recycle();
						mBitmap = null;
					}
					mBitmap = fileToBitmap(path);
					imageView01 = (ImageView)this.findViewById(R.id.ImageView01);
					imageView01.setImageBitmap(mBitmap);
				}

				printButton.setEnabled(true);
			}
		}
		else if (requestCode == REQUEST_ENABLE_BLUETOOTH_BROWSE_BT_DEVICES && resultCode == RESULT_OK) {
			tryBrowseBluetoothDevices();
		}
		else if (requestCode == REQUEST_ENABLE_BLUETOOTH_PRINT_TO_BT_DEVICE && resultCode == RESULT_OK) {
			tryPrintToBluetooth();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	//file select
//	public void fileSelectButton_click(View v) {
//		Intent fileList = new Intent(this, FileList.class);
//		selectedFile = (TextView)findViewById(R.id.selectedFile_id);
//		CharSequence fileList_label = selectedFile.getText();
//		String path = fileList_label.toString();
//		Log.i("Print", "p9 " + path );
//		if(path.equals(getString(R.string.selectedFile_label))){
//			fileList.putExtra("fileName", "");
//		}else{
//			fileList.putExtra("fileName", path);
//		}
//		startActivityForResult(fileList, FILE_LIST);
//		Log.i("Print", "p9a " + path );
//	}


	//print
	public void printButton_click(View v) {
		tryPrintToBluetooth();
		Log.i("Print", "p11a ");
	}

	//file limitation
	private boolean isImageFile(String path){
		String extention = path.substring(path.lastIndexOf(".", path.length())+1,path.length());
		if((extention.equalsIgnoreCase("jpg")) || (extention.equalsIgnoreCase("jpeg")) || (extention.equalsIgnoreCase("bmp"))
			|| (extention.equalsIgnoreCase("png")) || (extention.equalsIgnoreCase("gif"))){
			return true;
		}
		return false;
	}

	private boolean isBmpFile(String path){
		String extention = path.substring(path.lastIndexOf(".", path.length())+1,path.length());
		if(extention.equalsIgnoreCase("bmp")){
			return true;
		}
		return false;
	}
	
	private boolean isPrnFile(String path){
		return path.substring(path.length() - 3).equalsIgnoreCase("prn");
	}

	private boolean fileExist(String path){
    	File file = new File(path);
    	if(file.exists()){
    		return true;
    	}
    	return false;
	}	

	// show message from SDK
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){

			case UPDATE:
				finish();
				break;
			default:
				break;
			}
		}
	};

	//make bitmap from file path
	private Bitmap fileToBitmap(String path){
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();
		TextView mainMenuTitle = (TextView)findViewById(R.id.mainMenuTitle_label);
		int mainMenuTitleHeight = mainMenuTitle.getHeight();

		TableLayout tableLayout01 = (TableLayout)findViewById(R.id.TableLayout01);
		int tableLayout01Height = tableLayout01.getHeight();
		Button printButton = (Button)findViewById(R.id.printButton);
		int printButtonHeight = printButton.getHeight();

		int imageView01Height = displayHeight - mainMenuTitleHeight - tableLayout01Height - printButtonHeight;

		int imageView01Width = displayWidth;

		final long imageView01Resolution = imageView01Height * imageView01Width;

		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(path, options);

		int imgSize = options.outWidth * options.outHeight;

		if(imgSize < imageView01Resolution){
			options.inSampleSize = 1;
		}else if(imgSize < imageView01Resolution * 2 * 2){
			options.inSampleSize = 2;
		}else{
			options.inSampleSize = 4;
		}

		float resizeScaleWidth;
		float resizeScaleHeight;
		Matrix matrix = new Matrix();
		resizeScaleWidth = (float)imageView01Width / options.outWidth;
		resizeScaleHeight = (float)imageView01Height / options.outHeight;
		float scale = Math.min(resizeScaleWidth, resizeScaleHeight);

		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		if(scale < 1.0){
			matrix.postScale(scale * options.inSampleSize, scale * options.inSampleSize);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		}
		return bitmap;
	}

	private void LoadPreferences(Boolean NotifyOfChanges) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		try {

			String newMACAddress = preferences.getString("bluetooth_printer", "NULL");
			if (newMACAddress != MACAddress) {
				if (!MACAddress.equals("00:00:00:00:00:00")) {
//					LogMessage("BT: Target Device Changed. You will need to Disconnet/Reconnect.");
				}
				if (!newMACAddress.equals("NULL")) {
					MACAddress = newMACAddress;
				}else{
					MACAddress = "00:00:00:00:00:00";
				}
				BluetoothDevice btdevice = bta.getRemoteDevice(MACAddress);
				printer = btdevice;
			}

		} catch (NumberFormatException nfe) {}
		Log.i("Print","Loaded Prefs");
	}

	//show tips when close application
	private void showTips(){
//		return;
		finish();
/*		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("End")
		.setMessage("close application")
		.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				finish();
			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				return;
			}
		}).create();
		alertDialog.show();*/
	}

	//close application from return button
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
			showTips();
//			Log.i("Print", "p11 " + path );
			return false;
		}
		return false;
	}
		  
}