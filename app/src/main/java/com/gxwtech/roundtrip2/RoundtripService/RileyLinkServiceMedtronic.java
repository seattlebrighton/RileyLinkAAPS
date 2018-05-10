package com.gxwtech.roundtrip2.RoundtripService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.gxwtech.roundtrip2.RT2Const;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.MedtronicCommManager;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RileyLinkBLE;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.DiscoverGattServicesTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.FetchPumpHistoryTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.InitializePumpManagerTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadBolusWizardCarbProfileTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadISFProfileTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadPumpClockTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.RetrieveHistoryPageTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ServiceTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ServiceTaskExecutor;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.UpdatePumpStatusTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.WakeAndTuneTask;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.Page;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.PumpHistoryManager;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.ServiceData.ServiceNotification;
import com.gxwtech.roundtrip2.ServiceData.ServiceResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;
import com.gxwtech.roundtrip2.util.ByteUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * RileyLinkServiceMedtronic is intended to stay running when the gui-app is closed.
 */
public class RileyLinkServiceMedtronic extends RileyLinkService {
    
    //private static final String TAG="RileyLinkSerMedtronic";
    private static final Logger LOG = LoggerFactory.getLogger(RileyLinkServiceMedtronic.class);
    
    private static RileyLinkServiceMedtronic instance;
    




    private BroadcastReceiver mBroadcastReceiver;




    // saved settings

    public SharedPreferences sharedPref;
    private String pumpIDString;
    private byte[] pumpIDBytes;


    // cache of most recently received set of pump history pages. Probably shouldn't be here.
    ArrayList<Page> mHistoryPages;
    PumpHistoryManager pumpHistoryManager;



    //public MedtronicCommManager pumpCommunicationManager; // interface to Minimed

    public MedtronicCommManager medtronicCommunicationManager;

    private static ServiceTask currentTask = null;

    public RileyLinkServiceMedtronic() {
        super();
        instance = this;
        LOG.debug("RileyLinkServiceMedtronic newly constructed");
    }

    public static RileyLinkServiceMedtronic getInstance() {
        return instance;
    }

    public static MedtronicCommManager getCommunicationManager()
    {
        return instance.medtronicCommunicationManager;
    }


    public void onCreate() {
        super.onCreate();
        LOG.debug("onCreate");
        context = getApplicationContext();
        rileyLinkIPCConnection = new RileyLinkIPCConnection(context);

        //sharedPref = context.getSharedPreferences(RT2Const.serviceLocal.sharedPreferencesKey, Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // get most recently used pumpID
        pumpIDString = sharedPref.getString(RT2Const.serviceLocal.pumpIDKey,"000000");
        pumpIDBytes = ByteUtil.fromHexString(pumpIDString);
        if (pumpIDBytes == null) {
            LOG.error("Invalid pump ID? " + ByteUtil.shortHexString(pumpIDBytes));
            pumpIDBytes = new byte[] {0,0,0};
            pumpIDString = "000000";
        }
        if (pumpIDBytes.length != 3) {
            LOG.error("Invalid pump ID? " + ByteUtil.shortHexString(pumpIDBytes));
            pumpIDBytes = new byte[] {0,0,0};
            pumpIDString = "000000";
        }
        if (pumpIDString.equals("000000")) {
            LOG.error("Using pump ID "+pumpIDString);
        } else {
            LOG.info("Using pump ID "+pumpIDString);
        }

        // get most recently used RileyLink address
        rileylinkAddress = sharedPref.getString(RT2Const.serviceLocal.rileylinkAddressKey,"");

        pumpHistoryManager = new PumpHistoryManager(getApplicationContext());
        rileyLinkBLE = new RileyLinkBLE(this);
        rfspy = new RFSpy(context,rileyLinkBLE);
        rfspy.startReader();
        pumpCommunicationManager = new MedtronicCommManager(context,rfspy,pumpIDBytes);
        medtronicCommunicationManager = (MedtronicCommManager)pumpCommunicationManager;

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            /* here we can listen for local broadcasts, then send ourselves
             * a specific intent to deal with them, if we wish
              */
                if (intent == null) {
                    LOG.error("onReceive: received null intent");
                } else {
                    String action = intent.getAction();
                    if (action == null) {
                        LOG.error("onReceive: null action");
                    } else {
                        if (action.equals(RT2Const.serviceLocal.bluetooth_connected)) {
                            LOG.warn("serviceLocal.bluetooth_connected");
                            rileyLinkIPCConnection.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_note_FindingRileyLink),null);
                            ServiceTaskExecutor.startTask(new DiscoverGattServicesTask());
                            // If this is successful,
                            // We will get a broadcast of RT2Const.serviceLocal.BLE_services_discovered
                        } else if (action.equals(RT2Const.serviceLocal.BLE_services_discovered)) {
                            LOG.warn("serviceLocal.BLE_services_discovered");
                            rileyLinkIPCConnection.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_note_WakingPump),null);
                            rileyLinkBLE.enableNotifications();
                            rfspy.startReader(); // call startReader from outside?
                            ServiceTask task = new InitializePumpManagerTask();
                            ServiceTaskExecutor.startTask(task);
                            LOG.info( "Announcing RileyLink open For business");
                        } else if (action.equals(RT2Const.serviceLocal.ipcBound)) {
                            // If we still need permission for bluetooth, ask now.
                            if (needBluetoothPermission) {
                                sendBLERequestForAccess();
                            }

                        } else if (RT2Const.IPC.MSG_BLE_accessGranted.equals(action)) {
                            //initializeLeAdapter();
                            //bluetoothInit();
                        } else if (RT2Const.IPC.MSG_BLE_accessDenied.equals(action)) {
                            stopSelf(); // This will stop the service.
                        } else if (action.equals(RT2Const.IPC.MSG_PUMP_tunePump)) {
                            doTunePump();
                        } else if (action.equals(RT2Const.IPC.MSG_PUMP_quickTune)) {
                            doTunePump();
                        } else if (action.equals(RT2Const.IPC.MSG_PUMP_fetchHistory)) {
                            mHistoryPages = medtronicCommunicationManager.getAllHistoryPages();
                            final boolean savePages = true;
                            if (savePages) {
                                for (int i = 0; i < mHistoryPages.size(); i++) {
                                    String filename = "PumpHistoryPage-" + i;
                                    LOG.warn( "Saving history page to file " + filename);
                                    FileOutputStream outputStream;
                                    try {
                                        outputStream = openFileOutput(filename, 0);
                                        byte[] rawData= mHistoryPages.get(i).getRawData();
                                        if (rawData != null) {
                                            outputStream.write(rawData);
                                        }
                                        outputStream.close();
                                    } catch (FileNotFoundException fnf) {
                                        fnf.printStackTrace();
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }

                            Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC, 0, 0);
                            // Create a bundle with the data
                            Bundle bundle = new Bundle();
                            bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_PUMP_history);
                            ArrayList<Bundle> packedPages = new ArrayList<>();
                            for (Page page : mHistoryPages) {
                                packedPages.add(page.pack());
                            }
                            bundle.putParcelableArrayList(RT2Const.IPC.MSG_PUMP_history_key, packedPages);

                            // save it to SQL.
                            pumpHistoryManager.clearDatabase();
                            pumpHistoryManager.initFromPages(bundle);
                            // write html page to documents folder
                            pumpHistoryManager.writeHtmlPage();

                            // Set payload
                            msg.setData(bundle);
                            rileyLinkIPCConnection.sendMessage(msg,null/*broadcast*/);
                            LOG.debug( "sendMessage: sent Full history report");
                        } else if (RT2Const.IPC.MSG_PUMP_fetchSavedHistory.equals(action)) {
                            LOG.info("Fetching saved history");
                            FileInputStream inputStream;
                            ArrayList<Page> storedHistoryPages = new ArrayList<>();
                            for (int i = 0; i < 16; i++) {

                                String filename = "PumpHistoryPage-" + i;
                                try {
                                    inputStream = openFileInput(filename);
                                    byte[] buffer = new byte[1024];
                                    int numRead = inputStream.read(buffer, 0, 1024);
                                    if (numRead == 1024) {
                                        Page p = new Page();
                                        //p.parseFrom(buffer, PumpModel.MM522);
                                        // FIXME
                                        p.parseFrom(buffer, PumpModel.MM522);
                                        storedHistoryPages.add(p);
                                    } else {
                                        LOG.error( filename + " error: short file");
                                    }
                                } catch (FileNotFoundException fnf) {
                                    LOG.error( "Failed to open " + filename + " for reading.");
                                } catch (IOException e) {
                                    LOG.error( "Failed to read " + filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            mHistoryPages = storedHistoryPages;
                            if (storedHistoryPages.isEmpty()) {
                                LOG.error( "No stored history pages loaded");
                            } else {
                                Message msg = Message.obtain(null, RT2Const.IPC.MSG_IPC, 0, 0);
                                // Create a bundle with the data
                                Bundle bundle = new Bundle();
                                bundle.putString(RT2Const.IPC.messageKey, RT2Const.IPC.MSG_PUMP_history);
                                ArrayList<Bundle> packedPages = new ArrayList<>();
                                for (Page page : mHistoryPages) {
                                    packedPages.add(page.pack());
                                }
                                bundle.putParcelableArrayList(RT2Const.IPC.MSG_PUMP_history_key, packedPages);

                                // save it to SQL.
                                pumpHistoryManager.clearDatabase();
                                pumpHistoryManager.initFromPages(bundle);
                                // write html page to documents folder
                                pumpHistoryManager.writeHtmlPage();

                                // Set payload
                                msg.setData(bundle);
                                rileyLinkIPCConnection.sendMessage(msg,null/*broadcast*/);

                            }
                        } else if (RT2Const.IPC.MSG_ServiceCommand.equals(action)) {
                            Bundle bundle = intent.getBundleExtra(RT2Const.IPC.bundleKey);

                            handleIncomingServiceTransport(new ServiceTransport(bundle));
                        } else if (RT2Const.serviceLocal.INTENT_sessionCompleted.equals(action)) {
                            Bundle bundle = intent.getBundleExtra(RT2Const.IPC.bundleKey);
                            if (bundle != null) {
                                ServiceTransport transport = new ServiceTransport(bundle);
                                rileyLinkIPCConnection.sendTransport(transport, transport.getSenderHashcode());
                            } else {
                                LOG.error("sessionCompleted: no bundle!");
                            }
                        } else {
                            LOG.error( "Unhandled broadcast: action=" + action);
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RT2Const.serviceLocal.bluetooth_connected);
        intentFilter.addAction(RT2Const.serviceLocal.bluetooth_disconnected);
        intentFilter.addAction(RT2Const.serviceLocal.BLE_services_discovered);
        intentFilter.addAction(RT2Const.serviceLocal.ipcBound);
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_accessGranted);
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_accessDenied);
        intentFilter.addAction(RT2Const.IPC.MSG_BLE_useThisDevice);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_tunePump);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_fetchHistory);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_useThisAddress);
        intentFilter.addAction(RT2Const.IPC.MSG_PUMP_fetchSavedHistory);
        intentFilter.addAction(RT2Const.IPC.MSG_ServiceCommand);
        intentFilter.addAction(RT2Const.serviceLocal.INTENT_sessionCompleted);

        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, intentFilter);

        LOG.debug( "onCreate(): It's ALIVE!");
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LOG.warn("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return rileyLinkIPCConnection.doOnBind(intent);
    }








    /* private functions */


    private void setPumpIDString(String idString) {
        if (idString.length() != 6) {
            LOG.error("setPumpIDString: invalid pump id string: " + idString);
        }
        pumpIDString = idString;
        pumpIDBytes = ByteUtil.fromHexString(pumpIDString);
        SharedPreferences prefs = context.getSharedPreferences(RT2Const.serviceLocal.sharedPreferencesKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(RT2Const.serviceLocal.pumpIDKey,pumpIDString);
        editor.apply();
        LOG.info("setPumpIDString: saved pumpID "+pumpIDString);
    }



    private void reportPumpFound() {
        //rileyLinkIPCConnection.sendMessage(RT2Const.IPC.MSG_PUMP_pumpFound);
    }

    public void setCurrentTask(ServiceTask task) {
        if (currentTask == null) {
            currentTask = task;
        } else {
            LOG.error("setCurrentTask: Cannot replace current task");
        }
    }

    public void finishCurrentTask(ServiceTask task) {
        if (task != currentTask) {
            LOG.error("finishCurrentTask: task does not match");
        }
        // hack to force deep copy of transport contents
        ServiceTransport transport = task.getServiceTransport().clone();

        if (transport.hasServiceResult()) {
            sendServiceTransportResponse(transport,transport.getServiceResult());
        }
        currentTask = null;
    }

    private void handleIncomingServiceTransport(ServiceTransport serviceTransport) {
        if (serviceTransport.getServiceCommand().isPumpCommand()) {
            switch (serviceTransport.getOriginalCommandName()) {
                case "ReadPumpClock":
                    ServiceTaskExecutor.startTask(new ReadPumpClockTask(serviceTransport));
                    break;
                case "FetchPumpHistory":
                    ServiceTaskExecutor.startTask(new FetchPumpHistoryTask(serviceTransport));
                    break;
                case "RetrieveHistoryPage":
                    ServiceTask task = new RetrieveHistoryPageTask(serviceTransport);
                    ServiceTaskExecutor.startTask(task);
                    break;
                case "ReadISFProfile":
                    ServiceTaskExecutor.startTask(new ReadISFProfileTask(serviceTransport));
                /*
                ISFTable table = pumpCommunicationManager.getPumpISFProfile();
                ServiceResult result = new ServiceResult();
                if (table.isValid()) {
                    // convert from ISFTable to ISFProfile
                    Bundle map = result.getMap();
                    map.putIntArray("times", table.getTimes());
                    map.putFloatArray("rates", table.getRates());
                    map.putString("ValidDate", TimeFormat.standardFormatter().print(table.getValidDate()));
                    result.setMap(map);
                    result.setResultOK();
                }
                sendServiceTransportResponse(serviceTransport,result);
                */
                    break;
                case "ReadBolusWizardCarbProfile":
                    ServiceTaskExecutor.startTask(new ReadBolusWizardCarbProfileTask());
                    break;
                case "UpdatePumpStatus":
                    ServiceTaskExecutor.startTask(new UpdatePumpStatusTask());
                    break;
                case "WakeAndTune":
                    ServiceTaskExecutor.startTask(new WakeAndTuneTask());
                default:
                    LOG.error("Failed to handle pump command: " + serviceTransport.getOriginalCommandName());
                    break;
            }
        } else {
            switch (serviceTransport.getOriginalCommandName()) {
                case "SetPumpID":
                    // This one is a command to RileyLinkServiceMedtronic, not to the MedtronicCommManager
                    String pumpID = serviceTransport.getServiceCommand().getMap().getString("pumpID", "");
                    ServiceResult result = new ServiceResult();
                    if ((pumpID != null) && (pumpID.length() == 6)) {
                        setPumpIDString(pumpID);
                        result.setResultOK();
                    } else {
                        LOG.error( "handleIncomingServiceTransport: SetPumpID bundle missing 'pumpID' value");
                        result.setResultError(-1, "Invalid parameter (missing pumpID)");
                    }
                    sendServiceTransportResponse(serviceTransport, result);
                    break;
                case "UseThisRileylink":
                    // If we are not connected, connect using the given address.
                    // If we are connected and the addresses differ, disconnect, connect to new.
                    // If we are connected and the addresses are the same, ignore.
                    String deviceAddress = serviceTransport.getServiceCommand().getMap().getString("rlAddress", "");
                    if ("".equals(deviceAddress)) {
                        LOG.error( "handleIPCMessage: null RL address passed");
                    } else {
                        reconfigureRileylink(deviceAddress);
                    }
                    break;
                default:
                    LOG.error( "handleIncomingServiceTransport: Failed to handle service command '" + serviceTransport.getOriginalCommandName() + "'");
                    break;
            }
        }
    }



    public void announceProgress(int progressPercent) {
        if (currentTask != null) {
            ServiceNotification note = new ServiceNotification(RT2Const.IPC.MSG_note_TaskProgress);
            note.getMap().putInt("progress",progressPercent);
            note.getMap().putString("task",currentTask.getServiceTransport().getOriginalCommandName());
            Integer senderHashcode = currentTask.getServiceTransport().getSenderHashcode();
            rileyLinkIPCConnection.sendNotification(note, senderHashcode);
        } else {
            LOG.error("announceProgress: No current task");
        }
    }



    public void saveHistoryPage(int pagenumber, Page page) {
        if ((page == null) || (page.getRawData() == null)) {
            return;
        }
        String filename = "history-" + pagenumber;
        FileOutputStream os;
        try {
            os = openFileOutput(filename, Context.MODE_PRIVATE);
            os.write(page.getRawData());
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <E extends Object> void saveSetting(String key, Object value) {
        SharedPreferences.Editor ed = sharedPref.edit();

        if (value instanceof String)
            ed.putString(key, value.toString());
        ed.apply();
    }

    public <E extends Object>  Object getSetting(String key, Class<E> clazz)
    {
        if (clazz.isAssignableFrom(Float.class))
            return sharedPref.getFloat(key, (float)0.0);
        else if (clazz.isAssignableFrom(String.class))
            return sharedPref.getString(key, "");

        return null;
    }





}

