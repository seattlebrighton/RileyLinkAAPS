package com.gxwtech.roundtrip2.RoundtripService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.widget.Toast;

import com.gxwtech.roundtrip2.RT2Const;
import com.gxwtech.roundtrip2.ServiceData.ServiceNotification;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RileyLinkBLE;

/**
 * Created by andy on 5/6/18.
 */

public abstract class RileyLinkService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(RileyLinkService.class);

    protected BluetoothManager bluetoothManager;
    protected BluetoothAdapter bluetoothAdapter;

    // Our hardware/software connection
    public RileyLinkBLE rileyLinkBLE; // android-bluetooth management
    protected RFSpy rfspy; // interface for xxx Mhz (916MHz) radio.
    protected String rileylinkAddress;
    protected boolean needBluetoothPermission = true;
    protected RileyLinkIPCConnection rileyLinkIPCConnection;
    protected Context context;
    public RileyLinkCommManager pumpCommunicationManager;

    protected static final String WAKELOCKNAME = "com.gxwtech.roundtrip2.RoundtripServiceWakeLock";
    protected static volatile PowerManager.WakeLock lockStatic = null;

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.warn("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        LOG.warn("onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.error( "I die! I die!");
    }


    // Here is where the wake-lock begins:
    // We've received a service startCommand, we grab the lock.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.debug( "onStartCommand");
        if (intent != null) {
            PowerManager.WakeLock lock = getLock(this.getApplicationContext());

            if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
                lock.acquire();
            }

            // This will end up running onHandleIntent
            super.onStartCommand(intent, flags, startId);
        } else {
            LOG.error( "Received null intent?");
        }
        bluetoothInit(); // this kicks off our process of device discovery.
        return (START_REDELIVER_INTENT | START_STICKY);
    }

    void bluetoothInit() {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                LOG.error( "Unable to initialize BluetoothManager.");
            }
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if ((bluetoothAdapter ==null) || (!bluetoothAdapter.isEnabled())) {
            sendBLERequestForAccess();
        } else {
            needBluetoothPermission = false;
            initializeLeAdapter();
        }
    }

    public boolean initializeLeAdapter() {
        LOG.debug("initializeLeAdapter: attempting to get an adapter");
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            LOG.error( "Unable to obtain a BluetoothAdapter.");
            return false;
        } else if (!bluetoothAdapter.isEnabled()) {
            // NOTE: This does not work!
            LOG.error( "Bluetooth is not enabled.");
        }
        return true;
    }


    // returns true if our Rileylink configuration changed
    public boolean reconfigureRileylink(String deviceAddress) {
        if (rileyLinkBLE.isConnected()) {
            if (deviceAddress.equals(rileylinkAddress)) {
                LOG.info( "No change to RL address.  Not reconnecting.");
                return false;
            } else {
                LOG.warn( "Disconnecting from old RL (" + rileylinkAddress + "), reconnecting to new: " + deviceAddress);
                rileyLinkBLE.disconnect();
                // prolly need to shut down listening thread too?
                saveSetting("rlAddress", deviceAddress);

                rileylinkAddress = deviceAddress;
                rileyLinkBLE.findRileyLink(rileylinkAddress);
                return true;
            }
        } else {
            Toast.makeText(context, "Using RL " + deviceAddress, Toast.LENGTH_SHORT).show();
            LOG.debug( "handleIPCMessage: Using RL " + deviceAddress);
            if (bluetoothAdapter == null) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    // FIXME: this may be a long running function:
                    rileyLinkBLE.findRileyLink(deviceAddress);
                    // If successful, we will get a broadcast from RileyLinkBLE: RT2Const.serviceLocal.bluetooth_connected
                    return true;
                } else {
                    LOG.error( "Bluetooth is not enabled.");
                    return false;
                }
            } else {
                LOG.error( "Failed to get adapter");
                return false;
            }
        }
    }


    public synchronized static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr =
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCKNAME);
            lockStatic.setReferenceCounted(true);
        }

        return lockStatic;
    }

    public void sendServiceTransportResponse(ServiceTransport transport, ServiceResult serviceResult) {
        // get the key (hashcode) of the client who requested this
        Integer clientHashcode = transport.getSenderHashcode();
        // make a new bundle to send as the message data
        transport.setServiceResult(serviceResult);
        // FIXME
        transport.setTransportType(RT2Const.IPC.MSG_ServiceResult);
        rileyLinkIPCConnection.sendTransport(transport,clientHashcode);
    }

    public boolean sendNotification(ServiceNotification notification, Integer clientHashcode) {
        return rileyLinkIPCConnection.sendNotification(notification, clientHashcode);
    }

    protected void sendBLERequestForAccess() {
        //serviceConnection.sendMessage(RT2Const.IPC.MSG_BLE_requestAccess);
    }


    // FIXME: This needs to be run in a session so that is interruptable, has a separate thread, etc.
    protected void doTunePump() {
        double lastGoodFrequency = (float)getSetting(RT2Const.serviceLocal.prefsLastGoodPumpFrequency, Float.class); //sharedPref.getFloat(RT2Const.serviceLocal.prefsLastGoodPumpFrequency,(float)0.0);
        double newFrequency;
        if (lastGoodFrequency != 0.0) {
            LOG.info("Checking for pump near last saved frequency of %.2fMHz",lastGoodFrequency);
            // we have an old frequency, so let's start there.
            newFrequency = pumpCommunicationManager.quickTuneForPump(lastGoodFrequency);
            if (newFrequency == 0.0) {
                // quick scan failed to find pump.  Try full scan
                LOG.warn(String.format("Failed to find pump near last saved frequency, doing full scan"));
                newFrequency = pumpCommunicationManager.tuneForPump();
            }
        } else {
            LOG.warn("No saved frequency for pump, doing full scan.");
            // we don't have a saved frequency, so do the full scan.
            newFrequency = pumpCommunicationManager.tuneForPump();

        }
        if ((newFrequency!=0.0) && (newFrequency != lastGoodFrequency)) {
            LOG.info("Saving new pump frequency of %.2fMHz",newFrequency);
            saveSetting(RT2Const.serviceLocal.prefsLastGoodPumpFrequency, Float.valueOf((float)newFrequency));
//            SharedPreferences.Editor ed = sharedPref.edit();
//            ed.putFloat(RT2Const.serviceLocal.prefsLastGoodPumpFrequency, (float)newFrequency);
//            ed.apply();
        }
    }


    public abstract <E extends Object> void saveSetting(String key, Object value);

    public abstract <E extends Object>  Object getSetting(String key, Class<E> clazz);

}
