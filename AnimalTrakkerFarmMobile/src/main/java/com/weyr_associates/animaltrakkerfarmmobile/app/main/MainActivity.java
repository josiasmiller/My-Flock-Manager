package com.weyr_associates.animaltrakkerfarmmobile.app.main;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.weyr_associates.animaltrakkerfarmmobile.R;
import com.weyr_associates.animaltrakkerfarmmobile.app.AboutActivity;
import com.weyr_associates.animaltrakkerfarmmobile.app.device.baacode.BaacodeReaderService;
import com.weyr_associates.animaltrakkerfarmmobile.app.device.eid.EIDReaderService;
import com.weyr_associates.animaltrakkerfarmmobile.app.device.scale.ScaleDeviceService;
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissions;
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsWatcher;
import com.weyr_associates.animaltrakkerfarmmobile.app.preferences.EditPreferencesActivity;

public class MainActivity extends AppCompatActivity {

    public static void returnFrom(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private String TAG = getClass().getSimpleName();

    private Boolean KeepScreenOn = false;
    private Boolean UseSerialEID = false;
    private Boolean ResetSettings = false;
    private Messenger mEIDService = null;
    private Messenger mScaleService = null;
    private Messenger mBaaCodeService = null;
    private boolean mIsBound;
    private boolean mIsScaleBound;
    private boolean mIsBAABound;

    private final Messenger mEIDMessenger = new Messenger(new EIDMessageHandler());
    private final Messenger mBaaCodeMessenger = new Messenger(new BaaCodeMessageHandler());
    private final Messenger mScaleServiceMessenger = new Messenger(new ScaleMessageHandler());

    private boolean hasExecutedForegroundChecks = false;

    private final RequiredPermissionsWatcher requiredPermissionsWatcher = new RequiredPermissionsWatcher(this);

    private final ServiceConnection mEIDConnection = new EIDServiceConnection();
    private final ServiceConnection mBaacodeConnection = new BaacodeServiceConnection();
    private final ServiceConnection mScaleConnection = new ScaleServiceConnection();

    private final OnBackPressedCallback backPressedHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            NavController navController = Navigation.findNavController(
                    MainActivity.this, R.id.container_content);
            navController.popBackStack();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTitle(R.string.app_name_long);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container_content);
        NavController navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
                backPressedHandler.setEnabled(navDestination.getId() != R.id.nav_dst_menu_root &&
                        navDestination.getId() != R.id.nav_dst_required_permissions);
        });
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dst_menu_root, R.id.nav_dst_required_permissions).build();
        NavigationUI.setupActionBarWithNavController(
                this, navController, appBarConfiguration);
        getOnBackPressedDispatcher().addCallback(this, backPressedHandler);
        requiredPermissionsWatcher.setOnRequiredPermissionsChecked(arePermissionsGranted -> {
            checkRequiredPermissions();
            return true;
        });
        getLifecycle().addObserver(requiredPermissionsWatcher);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        UseSerialEID = preferences.getBoolean("useserialeid", false);
        CheckIfServiceIsRunning();
    }

    @Override
    public void onResume() {
        super.onResume();
        executeForegroundChecks();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i("Activity", "Paused" );
        hasExecutedForegroundChecks = false;
        if (mEIDService != null) {
            try {
                //Stop tags eidService from sending tags
                Message msg = Message.obtain(null, EIDReaderService.MSG_NO_TAGS_PLEASE);
                msg.replyTo = mEIDMessenger;
                mEIDService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (KeepScreenOn) {
            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
        try {
//			Log.i("Activity", "onDestroy");
            doUnbindService();
            doUnbindScaleService();
            doUnbindBaaService();
            stopService(new Intent(MainActivity.this, EIDReaderService.class));
            stopService(new Intent(MainActivity.this, ScaleDeviceService.class));
            stopService(new Intent(MainActivity.this, BaacodeReaderService.class));
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service(s)", t);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.container_content);
        NavDestination currentDestination = navController.getCurrentDestination();
        boolean isShowingRequiredPermissions = currentDestination != null &&
                currentDestination.getId() == R.id.nav_dst_required_permissions;
        return (!isShowingRequiredPermissions && navController.navigateUp()) ||
                super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //startActivity(new Intent(this, EditPreferences.class));
        super.onCreateOptionsMenu(menu);
        menu.removeItem(1);
        return true;
    }

    // process three dot options menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_settings: //Settings
                startActivity(new Intent(this, EditPreferencesActivity.class));
                return true;

            case R.id.menu_about: //About
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        //       default:
        return super.onOptionsItemSelected(item);
    }

    private void useBluetoothReader () {
        // Display alerts here
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage( R.string.please_use_BT_reader )
                .setTitle( R.string.please_use_BT_reader1 );
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int idx) {
                dialog.cancel();
            }
        }).create() .show();
    }

    private void executeForegroundChecks() {

        if (hasExecutedForegroundChecks) return;

        hasExecutedForegroundChecks = true;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        KeepScreenOn = preferences.getBoolean("keepscreenon", false);
        ResetSettings = preferences.getBoolean("reset", false);
        UseSerialEID = preferences.getBoolean("useserialeid", false);

        if (UseSerialEID) {
            if (!"NAUTIZ_X6P".equals(Build.MODEL)) {
                useBluetoothReader ();
                Log.i("SerialEID", "Got pref2.");
            }
        }
        if (ResetSettings) {
//            Log.i("Main Activity", "ResetSettings");
            preferences.edit().putBoolean("reset",false).apply();
            preferences.edit().putString("bluetooth_mac","00:00:00:00:00:00").apply();
            preferences.edit().putString("bluetooth1_mac","00:00:00:00:00:00").apply();
            preferences.edit().putString("bluetooth2_mac","00:00:00:00:00:00").apply();
            preferences.edit().putString("bluetooth_printer","00:00:00:00:00:00").apply();
            preferences.edit().putString("filenamemod","0").apply();

        }
        if (mIsBound) { // Request a status update.
            if (mEIDService != null) {
                Log.i("Activity", "Resume Bound" );
                try {
                    //Request service reload preferences, in case those changed
                    Message msg = Message.obtain(null, EIDReaderService.MSG_RELOAD_PREFERENCES, 0, 0);
                    msg.replyTo = mEIDMessenger;
                    mEIDService.send(msg);
                } catch (RemoteException e) {}
            }
        }
        if (KeepScreenOn) {
            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        }
        if (mEIDService != null) {
            try {
                //Start eidService sending tags
                Message msg = Message.obtain(null, EIDReaderService.MSG_SEND_ME_TAGS);
                msg.replyTo = mEIDMessenger;
                mEIDService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
    }

    // runs once on app startup
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        // It usually isn't.
        if (EIDReaderService.isRunning()) {
            doBindService();
        }
        if (ScaleDeviceService.isScaleRunning()) {
            doBindScaleService();
        }
        if (BaacodeReaderService.isBaaRunning()) {
            doBindBaaService();
        }
    }

    // Service Binding and unbinding code below

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this, EIDReaderService.class), mEIDConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;
        if (mEIDService != null) {
            try {
                //Request status update
                Message msg = Message.obtain(null, EIDReaderService.MSG_UPDATE_STATUS, 0, 0);
                msg.replyTo = mEIDMessenger;
                mEIDService.send(msg);

                //Request full log from service.
                msg = Message.obtain(null, EIDReaderService.MSG_UPDATE_LOG_FULL, 0, 0);
                mEIDService.send(msg);
            } catch (RemoteException e) {}
        }
    }

    private void doBindScaleService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this, ScaleDeviceService.class), mScaleConnection, Context.BIND_AUTO_CREATE);

        mIsScaleBound = true;
        Log.i("Activity","Binding Scale Service");
        if (mScaleService != null) {
            try {
                //Request status update
                Message msg = Message.obtain(null, ScaleDeviceService.MSG_UPDATE_STATUS, 0, 0);
                msg.replyTo = mScaleServiceMessenger;
                mScaleService.send(msg);

                //Request full log from service.
                msg = Message.obtain(null, ScaleDeviceService.MSG_UPDATE_LOG_FULL, 0, 0);
                mScaleService.send(msg);
            } catch (RemoteException e) {}
        }
    }

    private void doBindBaaService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this, BaacodeReaderService.class), mBaacodeConnection, Context.BIND_AUTO_CREATE);

        mIsBAABound = true;
        if (mBaaCodeService != null) {
            try {
                //Request status update
                Message msg = Message.obtain(null, BaacodeReaderService.MSG_UPDATE_STATUS, 0, 0);
                msg.replyTo = mBaaCodeMessenger;
                mBaaCodeService.send(msg);

                //Request full log from service.
                msg = Message.obtain(null, BaacodeReaderService.MSG_UPDATE_LOG_FULL, 0, 0);
                mBaaCodeService.send(msg);
            } catch (RemoteException e) {}
        }
    }

    private void doUnbindService() {
//		Log.i("Activity", "At DoUnbindservice");
        if (mEIDService != null) {
            try {
                //Stop eidService from sending tags
                Message msg = Message.obtain(null, EIDReaderService.MSG_NO_TAGS_PLEASE);
                msg.replyTo = mEIDMessenger;
                mEIDService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mEIDService != null) {
                try {
                    Message msg = Message.obtain(null, EIDReaderService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mEIDMessenger;
                    mEIDService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mEIDConnection);
            mIsBound = false;
        }

    }

    private void doUnbindScaleService() {
//		Log.i("Activity", "At DoUnbindservice");
        if (mScaleService != null) {
            try {
                //Stop scale Service from sending tags
                Message msg = Message.obtain(null,  ScaleDeviceService.MSG_NO_TAGS_PLEASE);
                msg.replyTo = mScaleServiceMessenger;
                mScaleService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
        if (mIsScaleBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mEIDService != null) {
                try {
                    Message msg = Message.obtain(null, ScaleDeviceService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mScaleServiceMessenger;
                    mScaleService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mScaleConnection);
            mIsScaleBound = false;
        }
    }

    private void doUnbindBaaService() {
//		Log.i("Activity", "At DoUnbindservice");
        if (mBaaCodeService != null) {
            try {
                //Stop baa Service from sending tags
                Message msg = Message.obtain(null, BaacodeReaderService.MSG_NO_BAACODES_PLEASE);
                msg.replyTo = mBaaCodeMessenger;
                mBaaCodeService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }
        if (mIsBAABound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mEIDService != null) {
                try {
                    Message msg = Message.obtain(null, BaacodeReaderService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mBaaCodeMessenger;
                    mEIDService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mBaacodeConnection);
            mIsBAABound = false;
        }
    }

    private void checkRequiredPermissions() {
        boolean requiredPermissionsFulfilled = RequiredPermissions.areFulfilled(this);
        showRequiredPermissionsCurtain(!requiredPermissionsFulfilled);
    }

    private void showRequiredPermissionsCurtain(boolean show) {
        NavController navController = Navigation.findNavController(this, R.id.container_content);
        if (show) {
            navController.navigate(R.id.nav_dst_required_permissions, null,
                    new NavOptions.Builder().setLaunchSingleTop(true).build());
        } else {
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null && currentDestination.getId() == R.id.nav_dst_required_permissions) {
                navController.popBackStack(R.id.nav_dst_required_permissions, true);
            }
        }
    }

    private class EIDServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mEIDService = new Messenger(service);
            try {
                //Register client with service
                Message msg = Message.obtain(null, EIDReaderService.MSG_REGISTER_CLIENT);
                msg.replyTo = mEIDMessenger;
                mEIDService.send(msg);

                //Request a status update.
                msg = Message.obtain(null, EIDReaderService.MSG_UPDATE_STATUS, 0, 0);
                mEIDService.send(msg);

                //Request full log from service.
                msg = Message.obtain(null, EIDReaderService.MSG_UPDATE_LOG_FULL, 0, 0);
                mEIDService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        // this may never run
        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mEIDService = null;
        }
    }

    private class BaacodeServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBaaCodeService = new Messenger(service);
            try {
                //Register client with service
                Message msg = Message.obtain(null, BaacodeReaderService.MSG_REGISTER_CLIENT);
                msg.replyTo = mBaaCodeMessenger;
                mBaaCodeService.send(msg);

                //Request a status update.
                msg = Message.obtain(null, BaacodeReaderService.MSG_UPDATE_STATUS, 0, 0);
                mBaaCodeService.send(msg);

                //Request full log from service.
                msg = Message.obtain(null, BaacodeReaderService.MSG_UPDATE_LOG_FULL, 0, 0);
                mBaaCodeService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        // this may never run
        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mBaaCodeService = null;
        }
    }

    private class ScaleServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mScaleService = new Messenger(service);
            try {
                //Register client with service
                Message msg = Message.obtain(null, ScaleDeviceService.MSG_REGISTER_CLIENT);
                msg.replyTo = mScaleServiceMessenger;
                mScaleService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        // this may never run
        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mScaleService = null;
        }
    }

    private class EIDMessageHandler extends Handler {

        public EIDMessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EIDReaderService.MSG_THREAD_SUICIDE) {
                Log.i("Activity", "Service informed Activity of Suicide.");
                doUnbindService();
                stopService(new Intent(MainActivity.this, EIDReaderService.class));
            } else {
                super.handleMessage(msg);
            }
        }
    }

    private class BaaCodeMessageHandler extends Handler {

        public BaaCodeMessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == BaacodeReaderService.MSG_THREAD_SUICIDE) {
                Log.i("Activity", "BaaCode Service informed Activity of Suicide.");
                doUnbindBaaService();
                stopService(new Intent(MainActivity.this, BaacodeReaderService.class));
            } else {
                super.handleMessage(msg);
            }
        }
    }

    private class ScaleMessageHandler extends Handler {

        public ScaleMessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ScaleDeviceService.MSG_THREAD_SUICIDE) {
                Log.i("Activity", "Scale Service informed Activity of Suicide.");
                doUnbindScaleService();
                stopService(new Intent(MainActivity.this, ScaleDeviceService.class));
            } else {
                super.handleMessage(msg);
            }
        }
    }
}
