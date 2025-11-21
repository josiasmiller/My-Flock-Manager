package com.weyr_associates.animaltrakkerfarmmobile.app.device.baacode;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceService;
import com.weyr_associates.animaltrakkerfarmmobile.app.storage.files.AppDirectories;
import com.weyr_associates.animaltrakkerfarmmobile.BuildConfig;
import com.weyr_associates.animaltrakkerfarmmobile.app.device.DeviceConnectionState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BaacodeReaderService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
	//Thread nThread;
	private static final String TEST_BAA = "<Test BAA Code>";

	private BluetoothAdapter mBluetoothAdapter = null;
	private BTConnectThread mBTConnectThread;
    private BTConnectedThread mBTConnectedThread;
    private int mBTState;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	static boolean isBaaRunning = false;
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
	public static final int MSG_UPDATE_LOG_APPEND = 25;
	public static final int MSG_UPDATE_LOG_FULL = 26;
	public static final int MSG_TOGGLE_LOG_TYPE = 27;
	public static final int MSG_RELOAD_PREFERENCES = 28;
	public static final int MSG_SEND_ME_BAACODES = 29;
	public static final int MSG_NO_BAACODES_PLEASE = 30;
	public static final int MSG_NEW_BAACODE_FOUND = 31;
	public static final int MSG_TIMER_TICK = 102;
	public static final int MSG_BT_LOG_MESSAGE = 1300;
	public static final int MSG_BT_GOT_DATA = 1301;
	public static final int MSG_BT_FINISHED = 1399;
	
	// to pass the last baa stuff for passing to do sheep task
	public final static String LASTBAA = "com.weyr_associates.lambtracker.LASTBAA";
	
	//Target we publish for clients to send messages to IncomingHandler.
	final Messenger mMessenger = new Messenger(new IncomingHandler()); 

	private String MACAddress = "00:00:00:00:00:00";
	private Boolean UseHTCConnectionWorkaround = false;
	private byte[] BinaryDataToSave = new byte[4096];
	private String BAADataToSave = "";
	private int BinaryDataToSaveIndex = 0;
	private String BAA = "";
	private String LastBAA = "yyy";
	private boolean completeline;
	
	private int TicksSinceLastStatusSent = 0;

	private SharedPreferences preferences;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	class IncomingHandler extends Handler { // handler of incoming messages from clients
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_UPDATE_STATUS:
				sendStatusMessageToUI(); // Client requested a status update
				break;
			case MSG_RELOAD_PREFERENCES:
				loadPreferences(); // Client requested that the service reload the shared preferences
				break;
			case MSG_UPDATE_LOG_FULL:
				sendAllLogMessagesToUI(); // Client requested all of the log messages.
				if (!isBaaRunning) {
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
			case MSG_SEND_ME_BAACODES:
				sendTagsOK = true; // Client requested Tags
				if (BuildConfig.BAACODE_SERVICE_AUTO_POST_TEST_BAA_ON_SCAN) {
					LastBAA = TEST_BAA;
					sendNewBaacodeMessageToUI();
				}
				break;
			case MSG_NO_BAACODES_PLEASE:
				sendTagsOK = false; // Client requested No Tags
//				Log.i("baacodeService", "No Tags.");
				break;
			default:
				super.handleMessage(msg);
			}
//			Log.i("baaService", "after case statement re messages");
		}
	}
	private void InformActivityOfThreadSuicide() {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				mClients.get(i).send(Message.obtain(null, MSG_THREAD_SUICIDE, 0, 0));
				Log.i("baaService", "Service informed Activity of Suicide. +");
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
				 Log.i("baaService", "Service informed Activity of Suicide. -");
			}
		}
		Log.i("baaService", "Service informed Activity of Suicide.");
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
			b.putString("logfull2", logmsgs);
		
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
			sendAllLogMessagesToUI();
		}
	}
	private void sendStatusMessageToUI() {
		// Build bundle
		String Stat;

		switch (mBTState) {
			case STATE_NONE:
				Stat = DeviceConnectionState.NONE.getStateString();
				break;
			case STATE_LISTEN:
				Stat = DeviceConnectionState.LISTENING.getStateString();
				break;
			case STATE_CONNECTED:
				Stat = DeviceConnectionState.CONNECTED.getStateString();
				break;
			case STATE_CONNECTING:
				Stat = DeviceConnectionState.CONNECTING.getStateString();
				break;
			default:
				Stat = DeviceConnectionState.SCANNING.getStateString();
		}

		Bundle b = new Bundle();
		b.putString("stat", Stat);
		b.putString("info1", LastBAA);
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
	private void sendNewBaacodeMessageToUI() {
		// Build bundle
		String Stat;
			Stat = "Scanning";

		Bundle b = new Bundle();
		b.putString("stat", Stat);
		b.putString("info1", LastBAA);
		b.putString("info2", TheTimeIs());
		
	if ( sendTagsOK == true) {	
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				Message msg = Message.obtain(null, MSG_NEW_BAACODE_FOUND);
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
//		Log.i("baaService", "No Tags1.");
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		//Log.i("baaService", "Service Started.");
//		logmsgs = TheDateTimeIs() + "Service Started";

		isBaaRunning = true;
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		loadPreferences();
		registerOnPreferencesChanged();
///		Notification lNotification = new Notification(R.drawable.reader,getText(R.string.service_started), System.currentTimeMillis());
///		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
///		lNotification.setLatestEventInfo(this, getText(R.string.service_label), getText(R.string.service_started), contentIntent);
///		startForeground(5213585, lNotification);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		timer.scheduleAtFixedRate(new TimerTask(){ public void run()
		{onTimerTick_TimerThread();}}, 0, 500L);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i("baaService", "Received start id " + startId + ": " + intent);
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
		if ("htcconnectworkaround".equals(key) || "bluetooth2_mac".equals(key)) {
			loadPreferences();
		}
	}

	private void loadPreferences() {
		try {

			String newMACAddress = preferences.getString("bluetooth2_mac", "00:00:00:00:00:00");
			boolean newUseHTCConnectionWorkaround = preferences.getBoolean("htcconnectworkaround", false);
			if (!newMACAddress.equals(MACAddress) || newUseHTCConnectionWorkaround != UseHTCConnectionWorkaround) {
				if (!MACAddress.equals("00:00:00:00:00:00")) {
					LogMessage("BT: Target Device Changed. You will need to Disconnect/Reconnect.");
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
	
	public static boolean isBaaRunning(){
		return isBaaRunning;
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
				LogMessage("Bluetooth is NOT supported.");
				haltService();
			} else {
				if (mBluetoothAdapter.isEnabled()) {
					BTstart();
				} else {
					//Log.e("Bluetooth", "Bluetooth is Disabled and we can't autoswitch it on");
					Toast.makeText(this, "Bluetooth is Disabled", Toast.LENGTH_SHORT).show();
					LogMessage("Bluetooth is Disabled");
					haltService();
				}
			}
		}
	}

	private void haltService() {
		isBaaRunning = false;
		InformActivityOfThreadSuicide();
		if (timer != null) {timer.cancel();}
		this.stopSelf();
	}

	// Bluetooth Reader Stuff
	
	private synchronized void setBTState(int state) {
//        Log.i("baaService", "setBTState() " + mBTState + " -> " + state);
        mBTState = state;
    }
	public synchronized int getBTState() {
	        return mBTState;
	    }
	public synchronized void BTstart() {
		SetDisplayMsgType(0);
//		Log.i("baaService", "BTstart");
        // Cancel any thread attempting to make a connection
        if (mBTConnectThread != null) {mBTConnectThread.cancel(); mBTConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {mBTConnectedThread.cancel(); mBTConnectedThread = null;}
        
        if (!BluetoothAdapter.checkBluetoothAddress(MACAddress)) {
        	LogMessage("Invalid Bluetooth MAC Address: \"" + MACAddress + "\"");
        	InformActivityOfThreadSuicide();
        } else if (MACAddress.equals("00:00:00:00:00:00")) {
        	LogMessage("Error: No Bluetooth device has been selected.");
        	isBaaRunning = false;
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
        LogMessage("Device: " + device.getAddress());
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
				} catch (Exception e) {
					handleExceptionForSocketCreation(e);
				}
			}
            
            mmSocket = tmp;
        }
        public void run() {
            Log.i("baaService", "BAA Trying to Connect");
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
            synchronized (BaacodeReaderService.this) {
            	mBTConnectThread = null;
            }

            // Start the connected thread
            BTconnected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("baaService", "close() of connect socket failed", e);
            }
        }

		private void handleExceptionForSocketCreation(Exception e) {
			//Log.e("baaService", "Error at createRfcommSocketToServiceRecord: " + e);
			e.printStackTrace();
			handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Exception creating socket: " + e));
		}

		private void handleExceptionOnConnectionFailure(Exception e) {
			// Close the socket
			Log.i("baaService", "BAA Trying to Connect exception");
			try {
				mmSocket.close();
			} catch (IOException e2) {
				Log.e("baaService", "unable to close() socket during connection failure", e2);
			}
			Log.e("baaService", "unable to connect() socket. Error: ", e);
			handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Failed to Connect: " + e));
			handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
		}
    }
	public synchronized void BTconnected(BluetoothSocket socket, BluetoothDevice device) {
        //Log.i("baaService", "Connected");
        // Cancel the thread that completed the connection
        if (mBTConnectThread != null) {mBTConnectThread.cancel(); mBTConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mBTConnectedThread != null) {mBTConnectedThread.cancel(); mBTConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mBTConnectedThread = new BTConnectedThread(socket);
        mBTConnectedThread.start();

        setBTState(STATE_CONNECTED);
        sendTagsOK = true; // Lets get some tags
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
                //Log.e("baaService", "temp sockets not created", e);
                handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Could not create Streams"));
                handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i("baaService", "BAA Connected");
            handler.sendMessage(handler.obtainMessage(MSG_BT_LOG_MESSAGE, "Bluetooth Device Connected"));

            
            byte[] buffer = new byte[1024];
            int bytesread;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                	bytesread = mmInStream.read(buffer); //This is a blocking call
                    byte[] tempdata = new byte[bytesread];
                    System.arraycopy(buffer, 0, tempdata, 0, bytesread);
//                    Log.d("baaService", "Got Data: " + new String(tempdata));
                    handler.sendMessage(handler.obtainMessage(MSG_BT_GOT_DATA, tempdata));
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    //Log.e("baaService", "ConnectionLost. Error: " + e);
                    handler.sendMessage(handler.obtainMessage(MSG_BT_FINISHED));
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                //Log.e("baaService", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e("baaService", "close() of connected socket failed", e);
            }
        }
    }
	public synchronized void BTstop() {
//        Log.i("baaService", "BTstop");
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
            //Log.d("baaService", "Sent Data");
        }
        // Perform the write unsynchronized
        r.write(buffer);
	}
	private void ParseBTDataStream(byte[] buffer) {
//		LogMessage("bytes from bt:" + buffer.length + ", " + new String(buffer));
		ParseBaacodeStream(new String(buffer));
// Save all data to file
//			SaveRawDataToFile(buffer); // helpful for debugging 
	}
	
	private void ParseBaacodeStream(String newdata) {
		
		BAA += newdata;
//		String[] lines = BAA.split("\\r?\\n"); //works for Priority1
//		String[] lines = BAA.split("\r"); //works for y-tex
		String[] lines = BAA.split("\n"); // works for both


		LogMessage("In Parse1" + " " + lines.length + ", " + new String(newdata) + ", " + BAA);
		if (lines.length > 0) {
			for (int i = 0; i < lines.length; i++) {
				completeline = false; // Reset this
				if (lines.length > 0) { // There is some data here
//					LogMessage("In Parse2" + " " + i + " " + lines[i].length() + " " + lines[i].lastIndexOf("\r"));
					if (lines[i].lastIndexOf("\r") + 1 == lines[i].length()) { // Line ends with a \r 
						completeline = true;
							if (lines[i].substring(3, 4).equals("_")) {
								//LogMessage("baaService0, Priority1");
							} else 
							if (lines[i].substring(3, 4).equals(" ")) {
									//LogMessage("baaService0, Y-Tex Panel");
									BAA = BAA.substring(0, 3) + "_" + BAA.substring(4,BAA.length()-1);
							} else {
								if (lines[i].substring(3, 4).equals("0")) {
								LogMessage("baaService0, Barcode Reader EID");
								BAA = BAA.substring(0, 3) + "_" + BAA.substring(3,BAA.length());
							} else {
								LogMessage("baaService0, Barcode Reader Data Matrix");
								BAA = BAA.substring(0, 3) + "" + BAA.substring(3,BAA.length()-1);
							}
							}																				
//						LogMessage("baaService1, completeline true");
						LastBAA = BAA.substring(0, BAA.length() - 1);  //prune off end of line for the database
 						sendNewBaacodeMessageToUI();
						BAA = ""; // Clear out BAA for next one
						if (lines[i].length() > 10) { // There are still at least 6 characters
							if (lines[i].substring(0, 3).equals("840")) {
										LogMessage("USA");
								} else
									if (lines[i].substring(0, 3).equals("940")) {
										LogMessage("ShearWell");
								} else
									if (lines[i].substring(0, 2).equals("NE")) {
										LogMessage("AllFlex TSU");
									}
//							SaveBAALineToFile(lines[i]);
										SaveBAALineToFile(LastBAA + "\r");
						}
					}
				}
			}

			if (!completeline) { // Last line wasn't complete, put last incomplete line back
//				LogMessage("baaService2, completeline false but more data to come");
				if (lines[lines.length - 1].length() < 1000) { // Only if less than 1000 characters long.
					BAA = lines[lines.length - 1];
				}
			}
		}
	}
	
	
	private void SaveBAALineToFile(String line) {
		BAADataToSave += TheDateTimeIs() + line;
		if (BAADataToSave.length() > 4096) { // There is at least 4KB to save.
			SaveBAAChunk();
		}
	
	}
	private void SaveBAAChunk() {
		if (BAADataToSave.length() > 0) {
				try {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) { // We can read and write the media
					File dir = AppDirectories.baaTagsDirectory(this);
					dir.mkdirs();

					Calendar calendar = Calendar.getInstance();
					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH) + 1;
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					String filename = year + "-" + Make2Digits(month) + "-" + Make2Digits(day) + ".txt";

					File file = new File(dir, filename);
					FileWriter writer = new FileWriter(file, true);
					writer.append(BAADataToSave);
					writer.flush();
					writer.close();
					BAADataToSave = "";
				}
			} catch (Exception e) {
				//Log.d("SaveBAAChunk", e.getMessage());
			}
		}
	}
	
	private void SaveRawDataToFile(byte[] buffer) {
		int copystart = 0;
		int inlen = buffer.length;
		//Log.d("RAWData", "Received " + inlen + " bytes. BinaryDataToSaveIndex is currently " + BinaryDataToSaveIndex);
		while (true) {
			if (BinaryDataToSaveIndex + (inlen - copystart) < 4096) { //Easy, won't fill buffer
//				LogMessage("Appending " + (inlen - copystart) + " bytes. BinaryDataToSaveIndex=" + BinaryDataToSaveIndex);
				System.arraycopy(buffer, copystart, BinaryDataToSave, BinaryDataToSaveIndex, inlen - copystart);
				BinaryDataToSaveIndex += inlen - copystart;
				//Log.d("RAWData", "Append complete");
				break;

			} else { //Buffer will get full, need to write data to file
				int copylength = 4096 - BinaryDataToSaveIndex;
//				LogMessage("Writing out " + BinaryDataToSaveIndex + "+" + copylength + " bytes.");
				System.arraycopy(buffer, copystart, BinaryDataToSave, BinaryDataToSaveIndex, copylength);
				BinaryDataToSaveIndex += copylength;
				copystart += copylength;
				SaveRawDataChunk();
				//Log.d("RAWData", "Write complete");
			}
		}
	}
	private void SaveRawDataChunk() {
		if (BinaryDataToSaveIndex > 0) {
			try {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) { // We can read and write the media
					File dir = AppDirectories.rawBaaTagsDirectory(this);
					dir.mkdirs();

					Calendar calendar = Calendar.getInstance();
					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH) + 1;
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					String filename = year + "-" + Make2Digits(month) + "-" + Make2Digits(day) + ".txt";
					File file = new File(dir, filename);

					FileOutputStream f = new FileOutputStream(file, true);
					f.write(BinaryDataToSave, 0, BinaryDataToSaveIndex);
					f.flush();
					f.close();

					//Log.d("SaveRawDataChunk", "Saved data to file: " + filename);
				}
			} catch (Exception e) {
				//Log.d("SaveRawDataChunk", e.getMessage());
			}
			BinaryDataToSaveIndex = 0;
		}	
	}
	

	public Handler handler = new Handler() { // Handler for data coming from bluetooth sockets
	@Override
	public void handleMessage(Message msg) {
		//todo flagged as a potential leak here, try to fix
			switch (msg.what) {
			case MSG_TIMER_TICK:
				onTimerTick();
				break;
			case MSG_BT_GOT_DATA:
				// Log.i("handleMessage", "MSG_BT_GOT_DATA");
				byte[] buffer2 = (byte[]) msg.obj;
				ParseBTDataStream(buffer2);
				break;
			case MSG_BT_LOG_MESSAGE:
				LogMessage((String) msg.obj);
				break;
			case MSG_BT_FINISHED:
//				Log.i("handleMessage", "MSG_BT_FINISHED");
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
		SaveRawDataChunk(); //Write data to file
		SaveBAAChunk();
		
		// Kill threads
		if (timer != null) {timer.cancel();}

//		Log.i("baaService", "onDestroy.");
		BTstop();

		stopForeground(true);
//		Log.i("baaService", "Service Stopped.");
		isBaaRunning = false;
	}
	
}