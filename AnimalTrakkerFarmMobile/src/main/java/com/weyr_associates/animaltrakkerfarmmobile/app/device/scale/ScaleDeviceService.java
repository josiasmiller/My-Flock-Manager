package com.weyr_associates.animaltrakkerfarmmobile.app.device.scale;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.weyr_associates.animaltrakkerfarmmobile.BuildConfig;
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import kotlin.random.Random;

public class ScaleDeviceService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
	//Thread nThread;

	private BluetoothAdapter mBluetoothAdapter = null;
	private BTConnectThread mBTConnectThread;
    private BTConnectedThread mBTConnectedThread;
    private int mBTState;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	static boolean isScaleRunning = false;
	static boolean sendTagsOK = false;
	private Timer timer = new Timer();
	private String logmsgs = "";
	private int DisplayMsgType = 0;

	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	int mValue = 0; // Holds last value set by a client.
	public static final int MSG_THREAD_SUICIDE = DeviceService.MSG_THREAD_SUICIDE;
	public static final int MSG_REGISTER_CLIENT = DeviceService.MSG_REGISTER_CLIENT;
	public static final int MSG_UNREGISTER_CLIENT = DeviceService.MSG_UNREGISTER_CLIENT;
	public static final int MSG_UPDATE_STATUS = DeviceService.MSG_UPDATE_STATUS;
	public static final int MSG_NEW_SCALE_FOUND = 4;
	public static final int MSG_UPDATE_LOG_APPEND = 5;
	public static final int MSG_UPDATE_LOG_FULL = 6;
	public static final int MSG_TOGGLE_LOG_TYPE = 7;
	public static final int MSG_RELOAD_PREFERENCES = 8;
	public static final int MSG_SEND_ME_TAGS = 9;
	public static final int MSG_NO_TAGS_PLEASE = 10;
	public static final int MSG_TIMER_TICK = 1001;
	public static final int MSG_BT_LOG_MESSAGE = 1200;
	public static final int MSG_BT_GOT_DATA = 1201;
	public static final int MSG_BT_FINISHED = 1299;
	
	// to pass the last eid stuff for passing to do sheep task
	public final static String LASTEID = "com.weyr_associates.lambtracker.LASTEID";
	
	//Target we publish for clients to send messages to IncomingHandler.
	final Messenger mMessenger = new Messenger(new IncomingHandler()); 

	private String MACAddress = "00:00:00:00:00:00";
	private Boolean UseHTCConnectionWorkaround = false;
	private byte[] BinaryDataToSave = new byte[4096];
	private String EIDDataToSave = "";
	private int BinaryDataToSaveIndex = 0;
	private String EID = "";
	private String LastEID = "zzz";
	private boolean completeline;
	
	private int TicksSinceLastStatusSent = 0;

	private SharedPreferences preferences;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	class IncomingHandler extends Handler { // handler of incoming messages from clients
		@SuppressLint("DefaultLocale")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
//				Log.i("scaleService", "client register.");
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_UPDATE_STATUS:
//				Log.i("scaleService", "update status.");
				sendStatusMessageToUI(); // Client requested a status update
				break;
			case MSG_RELOAD_PREFERENCES:
				loadPreferences(); // Client requested that the service reload the shared preferences
				break;
			case MSG_UPDATE_LOG_FULL:
//				Log.i("scaleService", "update full log.");
				sendAllLogMessagesToUI(); // Client requested all of the log messages.
				if (!isScaleRunning) {
					InformActivityOfThreadSuicide();
				}
				break;
			case MSG_TOGGLE_LOG_TYPE:
				if (DisplayMsgType == 0) {
					SetDisplayMsgType(1);
				} else {
					SetDisplayMsgType(0);
				}
				break;
			case MSG_SEND_ME_TAGS:
				sendTagsOK = true; // Client requested Tags
				if (BuildConfig.SCALE_SERVICE_AUTO_POST_TEST_WEIGHT_ON_SCAN) {
					LastEID = String.format("%.2f", 75 * Random.Default.nextFloat());
					sendNewSCALEMessageToUI();
				}
//				Log.i("scaleService", "sendTagsOK.");
				break;
			case MSG_NO_TAGS_PLEASE:
				sendTagsOK = false; // Client requested No Tags
//				Log.i("scaleService", "No Tags.");
				break;
			default:
				super.handleMessage(msg);
			}
//			Log.i("scaleService", "after case statement re messages");
		}
	}
	private void InformActivityOfThreadSuicide() {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				mClients.get(i).send(Message.obtain(null, MSG_THREAD_SUICIDE, 0, 0));
//				Log.i("scaleService", "Service informed Activity of Suicide. +");
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
//				 Log.i("scaleService", "Service informed Activity of Suicide. -");
			}
		}
//		Log.i("scaleService", "Service informed Activity of Suicide.");
	}
	private void LogMessage(String m) {
		// Check if log is too long, shorten if necessary.
		if (logmsgs.length() > 1000) {
			int tempi = logmsgs.length();
			tempi = logmsgs.indexOf("\n", tempi - 500);
			logmsgs = logmsgs.substring(tempi + 1);
		}

		// Append new message to the log.
		logmsgs += "\n" + TheDateTimeIs() + m;

		if (DisplayMsgType == 0) {
			// Build bundle
			Bundle b = new Bundle();
			b.putString("logappend", TheDateTimeIs() + m);
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {
					Message msg = Message.obtain(null, MSG_UPDATE_LOG_APPEND);
					msg.setData(b);
					mClients.get(i).send(msg);
				} catch (RemoteException e) {
					// The client is dead. Remove it from the list; we are going
					// through the list from back to front so this is safe to do
					// inside the loop.
					mClients.remove(i);
				}
			}
		}
	}

	private void sendAllLogMessagesToUI() {
		Bundle b = new Bundle();
			b.putString("logfull1", logmsgs);
		
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, MSG_UPDATE_LOG_FULL);
				msg.setData(b);
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}
	private void SetDisplayMsgType(int MsgType) {
			MsgType = 0;

		if (DisplayMsgType != MsgType) { //Type changed. Need to re-send everything
			DisplayMsgType = MsgType;
//			sendAllLogMessagesToUI();
		}
	}
	private void sendStatusMessageToUI() {
		// Build bundle
		String Stat;
			Stat = "Scanning";

		Bundle b = new Bundle();
		b.putString("stat", Stat);
		b.putString("info1", LastEID);
		b.putString("info2", TheTimeIs());
		
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, MSG_UPDATE_STATUS);
				msg.setData(b);
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
		TicksSinceLastStatusSent = 0; // Reset to zero
	}
	private void sendNewSCALEMessageToUI() {
		// Build bundle
		String Stat;
			Stat = "Listening";

		Bundle b = new Bundle();
		b.putString("stat", Stat);
		b.putString("info1", LastEID);
		b.putString("info2", TheTimeIs());
		
	if ( sendTagsOK == true) {	
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, MSG_NEW_SCALE_FOUND);
				msg.setData(b);
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
		sendTagsOK = false;
//		Log.i("scaleService", "No Tags1.");
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		//Log.i("scaleService", "Service Started.");
//		logmsgs = TheDateTimeIs() + "Service Started";

		isScaleRunning = true;
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		loadPreferences();
		registerOnPreferencesChanged();
///		Notification jNotification = new Notification(R.drawable.scale,getText(R.string.scale_service_started), System.currentTimeMillis());
///		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
///		jNotification.setLatestEventInfo(this, getText(R.string.scale_service_label), getText(R.string.scale_service_started), contentIntent);
///		startForeground(5213583, jNotification);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		timer.scheduleAtFixedRate(new TimerTask(){ public void run()
		{onTimerTick_TimerThread();}}, 0, 500L);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i("scaleService", "Received start id " + startId + ": " + intent);
		return START_STICKY; // run until explicitly stopped.
	}

	private void registerOnPreferencesChanged() {
		preferences.registerOnSharedPreferenceChangeListener(this);
	}

	private void unregisterOnPreferencesChanged() {
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
		if ("htcconnectworkaround".equals(key) || "bluetooth1_mac".equals(key)) {
			loadPreferences();
		}
	}

	private void loadPreferences() {
		try {
			String newMACAddress = preferences.getString("bluetooth1_mac", "00:00:00:00:00:00");
			boolean newUseHTCConnectionWorkaround = preferences.getBoolean("htcconnectworkaround", false);
			if (!newMACAddress.equals(MACAddress) || newUseHTCConnectionWorkaround != UseHTCConnectionWorkaround) {
				if (!MACAddress.equals("00:00:00:00:00:00")) {
					LogMessage("Scale: Target Device Changed. You will need to Disconnect/Reconnect.");
				}
				MACAddress = newMACAddress;
				UseHTCConnectionWorkaround = newUseHTCConnectionWorkaround;
				BTstop();
			}
			
		} catch (NumberFormatException nfe) {}
		
	}
	public boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isScaleRunning(){
		return isScaleRunning;
	}
	
	private String TheDateTimeIs() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		return Make2Digits(month + 1) + "/" +  Make2Digits(day) + "/" + year + ":" + Make2Digits(hours) + ":" + Make2Digits(minutes) + ":"
				+ Make2Digits(seconds) + " ";
	}
	private String TheTimeIs() {
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		return Make2Digits(hours) + ":" + Make2Digits(minutes) + ":"
				+ Make2Digits(seconds) + " ";
	}
	private String Make2Digits(int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return Integer.toString(i);
		}
	}
	private void onTimerTick_TimerThread() {
		// This is running on a separate thread. Cannot do UI stuff from here.
		// Send a message to the handler to do that stuff on the main thread.
		handler.sendMessage(handler.obtainMessage(MSG_TIMER_TICK));
	}
	private void onTimerTick() { // Back on the main thread.
		TicksSinceLastStatusSent++;
		if (TicksSinceLastStatusSent > 0) {
			sendStatusMessageToUI();
		}

		if (getBTState() == STATE_NONE) { //We're not connected, try to start.
			if (mBluetoothAdapter == null) { //No adapter. Fail
				//Log.e("Bluetooth", "getDefaultAdapter returned null");
				Toast.makeText(this, "This device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
//				LogMessage("Bluetooth is NOT supported.");
				haltService();
			} else {
				if (mBluetoothAdapter.isEnabled()) {
					BTstart();
				} else {
					Toast.makeText(this, "Bluetooth is Disabled", Toast.LENGTH_SHORT).show();
					haltService();
				}
			}
		}
	}

	private void haltService() {
		isScaleRunning = false;
		InformActivityOfThreadSuicide();
		if (timer != null) {timer.cancel();}
		this.stopSelf();
	}

	// Bluetooth Reader Stuff
	
	private synchronized void setBTState(int state) {
//        Log.i("scaleService", "setBTState() " + mBTState + " -> " + state);
        mBTState = state;
    }
	public synchronized int getBTState() {
	        return mBTState;
	    }
	public synchronized void BTstart() {
		SetDisplayMsgType(0);
//		Log.i("scaleService", "BTstart");
        // Cancel any thread attempting to make a connection
        if (mBTConnectThread != null) {mBTConnectThread.cancel(); mBTConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {mBTConnectedThread.cancel(); mBTConnectedThread = null;}
//		Log.i("scaleService", "Bluetooth MAC Address: \"" + MACAddress + "\"");
        if (!BluetoothAdapter.checkBluetoothAddress(MACAddress)) {
//        	LogMessage("Invalid Bluetooth MAC Address: \"" + MACAddress + "\"");
//			Log.i("scaleService", "Bluetooth MAC Address: \"" + MACAddress + "\"");
        	InformActivityOfThreadSuicide();
        } else if (MACAddress.equals("00:00:00:00:00:00")) {
//        	LogMessage("Error: No Bluetooth device has been selected.");
        	isScaleRunning = false;
			InformActivityOfThreadSuicide();
			if (timer != null) {timer.cancel();}
			this.stopSelf();
        } else {
        	setBTState(STATE_LISTEN);
            BluetoothDevice btdevice = mBluetoothAdapter.getRemoteDevice(MACAddress);
            BTconnect(btdevice);	
        }
    }

	public synchronized void BTconnect(BluetoothDevice device) {
//        LogMessage("Device: " + device.getName());
//		Log.i("scaleService", "BTconnect");
		// Cancel any thread attempting to make a connection
        if (mBTState == STATE_CONNECTING) {
            if (mBTConnectThread != null) {mBTConnectThread.cancel(); mBTConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {mBTConnectedThread.cancel(); mBTConnectedThread = null;}

        // Start the thread to connect with the given device
        mBTConnectThread = new BTConnectThread(device, UseHTCConnectionWorkaround);
        mBTConnectThread.start();
        setBTState(STATE_CONNECTING);
    }
	private class BTConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        
        public BTConnectThread(BluetoothDevice device, boolean IsAnHTCDevice) {
        	mmDevice = device;
            BluetoothSocket tmp = null;
        
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            if (IsAnHTCDevice) {
				try {
					Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
					tmp = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
				} catch (Exception e) {
					handleExceptionForSocketCreation(e);
				}
			} else {
				try {
					UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
					tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				} catch (SecurityException e) {
					handleExceptionForSocketCreation(e);
					//Prevent lint from flagging duplicate catch block for Exception.
					//noinspection
				}
				catch (Exception e) {
					handleExceptionForSocketCreation(e);
				}
			}
            
            mmSocket = tmp;
        }
        public void run() {
//            Log.i("ScaleService", "BEGIN BTConnectThread");
            handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Trying to Connect..."));
           
            // Always try to cancel discovery because it will slow down a connection
			try {
				mBluetoothAdapter.cancelDiscovery();
			} catch (SecurityException e) {
				// No BLUETOOTH_SCAN permission. Shouldn't happen, but it could.
				// NO-OP since there is nothing we can really do here except abort
				// and connections can still be made with discovery in progress...
				// just slowly...
				e.printStackTrace();
			}

            // Make a connection to the BluetoothSocket
            try {
				// This is a blocking call and will only return on a successful connection or an exception
				mmSocket.connect();
            } catch (SecurityException|IOException e) {
				handleExceptionOnConnectionFailure(e);
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (ScaleDeviceService.this) {
            	mBTConnectThread = null;
            }

            // Start the connected thread
            BTconnected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e("ScaleService", "close() of connect socket failed", e);
            }
        }

		private void handleExceptionForSocketCreation(Exception e) {
			//Log.e("ScaleService", "Error at createRfcommSocketToServiceRecord: " + e);
			e.printStackTrace();
			handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Exception creating socket: " + e));
		}

		private void handleExceptionOnConnectionFailure(Exception e) {
			// Close the socket
			try {
				mmSocket.close();
			} catch (IOException e2) {
				//Log.e("ScaleService", "unable to close() socket during connection failure", e2);
			}
			//Log.e("ScaleService", "unable to connect() socket. Error: ", e);
			handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Failed to Connect: " + e));
			handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
		}
	}
	public synchronized void BTconnected(BluetoothSocket socket, BluetoothDevice device) {
//        Log.i("ScaleService", "Connected");
        // Cancel the thread that completed the connection
        if (mBTConnectThread != null) {mBTConnectThread.cancel(); mBTConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {mBTConnectedThread.cancel(); mBTConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mBTConnectedThread = new BTConnectedThread(socket);
        mBTConnectedThread.start();

        setBTState(STATE_CONNECTED);
        sendTagsOK = false; // Lets get some tags
    }
	private class BTConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        
        public BTConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //Log.e("ScaleService", "temp sockets not created", e);
                handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Could not create Streams"));
                handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
 //           Log.i("scaleService", "BEGIN BTConnectedThread");
            handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Bluetooth Device Connected"));

            
            byte[] buffer = new byte[1024];
            int bytesread;
				try {
					bytesread = mmInStream.read(buffer);
	//				Log.i("ScaleService", "bytesread1: " + bytesread);
					buffer[bytesread] = '\0';

				}  catch (IOException e) {
					handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
				}

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                	bytesread = mmInStream.read(buffer); //This is a blocking call
//					Log.i("ScaleService", "bytesread2: " + bytesread);
                    byte[] tempdata = new byte[bytesread];
                    System.arraycopy(buffer, 0, tempdata, 0, bytesread);
      //              Log.i("ScaleService", "Got Data: " + new String(tempdata));
                    handler.sendMessage(handler.obtainMessage(MSG_BT_GOT_DATA, tempdata));
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    //Log.e("ScaleService", "ConnectionLost. Error: " + e);
                    handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                //Log.e("EidService", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e("EidService", "close() of connected socket failed", e);
            }
        }
    }
	public synchronized void BTstop() {
//        Log.i("eidService", "BTstop");
        if (mBTConnectThread != null) {mBTConnectThread.cancel(); mBTConnectThread = null;}
        if (mBTConnectedThread != null) {mBTConnectedThread.cancel(); mBTConnectedThread = null;}
        setBTState(STATE_NONE);
    }
	public void SendDataToBluetooth(byte[] buffer) { // You run this from the main thread.
		// Create temporary object
		BTConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mBTState != STATE_CONNECTED) return;
            r = mBTConnectedThread;
            //Log.d("EidService", "Sent Data");
        }
        // Perform the write unsynchronized
        r.write(buffer);
	}
	private void ParseBTDataStream(byte[] buffer) {
//		LogMessage("bytes from bt:" + buffer.length + ", " + new String(buffer));
		ParseEIDStream(new String(buffer));
// Save all data to file
//			SaveRawDataToFile(buffer); // helpful for debugging 
	}


	private void ParseEIDxxStream(String newdata) {
		
		EID += newdata;
//		String[] lines = EID.split("\\r?\\n"); //works for Priority1
//		String[] lines = EID.split("\r"); //works for y-tex
		String[] lines = EID.split("\n"); // works for both


//		LogMessage("In Parse1" + " " + lines.length + ", " + new String(newdata) + ", " + EID);
		if (lines.length > 0) {
			for (int i = 0; i < lines.length; i++) {
				completeline = false; // Reset this
				if (lines.length > 0) { // There is some data here
//					LogMessage("In Parse2" + " " + i + " " + lines[i].length() + " " + lines[i].lastIndexOf("\r"));
					if (lines[i].lastIndexOf("\r") + 1 == lines[i].length()) { // Line ends with a \r 
						completeline = true;
							if (lines[i].substring(3, 4).equals("_")) {
//								LogMessage("eidService0, Priority1");
							} else 
							if (lines[i].substring(3, 4).equals(" ")) {
//									LogMessage("eidService0, Y-Tex Panel");
									EID = EID.substring(0, 3) + "_" + EID.substring(4,EID.length()-1);	
							} else {		
								if (lines[i].substring(0, 1).equals("0")) {
//								LogMessage("eidService0, ShearWell Wand");
								EID = EID.substring(1, 4) + "_" + EID.substring(4,EID.length());
							} else {
//								LogMessage("eidService0, Y-Tex Wand");
								EID = EID.substring(0, 3) + "_" + EID.substring(3,EID.length()-1);								
							}
							}																				
//						LogMessage("eidService1, completeline true");				
						LastEID = EID.substring(0, EID.length() - 1);  //prune off end of line for the database
 						sendNewSCALEMessageToUI();
						EID = ""; // Clear out EID for next one
						if (lines[i].length() > 10) { // There are still at least 6 characters
							if (lines[i].substring(0, 3).equals("840")) {
										LogMessage("USA");
								} else if (lines[i].substring(0, 3).equals("940")) {
										LogMessage("ShearWell");
								} else 
										LogMessage("Other");						
//							SaveEIDLineToFile(lines[i]);
//							SaveEIDLineToFile(LastEID + "\r");
						}
					}
				}
			}

			if (!completeline) { // Last line wasn't complete, put last incomplete line back
//				LogMessage("eidService2, completeline false but more data to come");
				if (lines[lines.length - 1].length() < 1000) { // Only if less than 1000 characters long.
					EID = lines[lines.length - 1];
				}
			}
		}
	}



	private void ParseEIDStream(String newdata) {

		EID += newdata;

		String[] lines = EID.split("\n"); // works for both


//		LogMessage("In Parse1" + " " + lines.length + ", " + new String(newdata) + ", " + EID);
		if (lines.length > 0) {
			for (int i = 0; i < lines.length; i++) {
				completeline = false; // Reset this
				if (lines.length > 0) { // There is some data here
//					LogMessage("In Parse2" + " " + i + " " + lines[i].length() + " " + lines[i].lastIndexOf("\r"));
					if (lines[i].lastIndexOf("\r") + 1 == lines[i].length()) { // Line ends with a \r
						completeline = true;

//						LogMessage("scaleService1, completeline true");
						LastEID = EID.substring(0, EID.length() - 1);  //prune off end of line for the database
						LogMessage(EID);
						sendNewSCALEMessageToUI();
						EID = ""; // Clear out EID for next one

					}
				}
			}

			if (!completeline) { // Last line wasn't complete, put last incomplete line back
//				LogMessage("scaleService2, completeline false but more data to come");
				if (lines[lines.length - 1].length() < 1000) { // Only if less than 1000 characters long.
					EID = lines[lines.length - 1];
				}
			}
		}
	}


	private void ParseEIDxStream(String newdata) {
		EID += newdata;


		String[] lines = EID.split("\n"); // split off the newline

//		LogMessage("Got Weight" + " " + lines.length + ", " + new String(newdata) + ", " + EID);
//		LogMessage(EID);
        LastEID = EID.substring(0, EID.length() - 1);  //prune off end of line for the database
        sendNewSCALEMessageToUI();
        EID = ""; // Clear out EID for next one

	}

	public Handler handler = new Handler() { // Handler for data coming from bluetooth sockets
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TIMER_TICK:
				onTimerTick();
				break;
			case MSG_BT_GOT_DATA:
//				Log.i("handleMessageS", "MSG_BT_GOT_DATA");
				byte[] buffer2 = (byte[]) msg.obj;
				ParseBTDataStream(buffer2);
				break;
			case MSG_BT_LOG_MESSAGE:
//				Log.i("handleMessageS", "MSG_BT_LOG_MESSAGE");
//				LogMessage((String) msg.obj);
				break;
			case MSG_BT_FINISHED:
//				Log.i("handleMessageS", "MSG_BT_FINISHED");
				BTstop();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterOnPreferencesChanged();
		// Kill threads
		if (timer != null) {timer.cancel();}

		BTstop();

		stopForeground(true);
		isScaleRunning = false;
	}
}
