package com.gxwtech.roundtrip2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.gxwtech.roundtrip2.ServiceData.ServiceClientActions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceCommand;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.MedtronicCommunicationManager;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.BasalProfile;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.MedtronicDeviceType;

public class ShowAAPSActivity extends AppCompatActivity {

    TextView textViewComm;
    private static final Logger LOG = LoggerFactory.getLogger(ShowAAPSActivity.class);
    private BroadcastReceiver mBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_aaps);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        this.textViewComm = findViewById(R.id.textViewComm);


        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // FIXME
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
                    sendData(action);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RefreshData.PumpModel");
        intentFilter.addAction("RefreshData.ErrorCode");
        intentFilter.addAction("RefreshData.BasalProfile");

        LocalBroadcastManager.getInstance(MainApp.instance().getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);

    }


    public void onAAPSGetStatusButtonClick(View view) {
        putOnDisplay("onAAPSGetStatusButtonClick");
    }


    public void onAAPSSetTBRButtonClick(View view) {
        putOnDisplay("onAAPSSetTBRButtonClick");
    }


    public void onAAPSCancelTBRButtonClick(View view) {
        putOnDisplay("onAAPSCancelTBRButtonClick");
    }


    public void onAAPSSetBolusButtonClick(View view) {
        putOnDisplay("onAAPSSetBolusButtonClick");
    }


    public void onAAPSCancelBolusButtonClick(View view) {
        putOnDisplay("onAAPSCancelBolusButtonClick");
    }


    public void onAAPSGetBasalProfileButtonClick(View view) {
        putOnDisplay("onAAPSGetBasalProfileButtonClick");

        new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("onAAPSGetModelButtonClick - In Runnable");
                BasalProfile basalProfile = MedtronicCommunicationManager.getInstance().getBasalProfile();

                if (basalProfile == null) {
                    data = null;
                    errorCode = MedtronicCommunicationManager.getInstance().getErrorResponse();
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.ErrorCode");
                } else {
                    data = basalProfile;
                    errorCode = null;
                    LOG.info("onAAPSGetModelButtonClick - Data Read ");
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.BasalProfile");
                }


            }
        }).start();


        //MedtronicDeviceType pumpModel = MedtronicCommunicationManager.getMedtronicCommunicationManager().getPumpModel();
        //putOnDisplay("Model: " + pumpModel.name());
    }


    public void onAAPSSetBasalProfileButtonClick(View view) {
        putOnDisplay("onAAPSSetBasalProfileButtonClick");
    }


    public void onAAPSReadHistoryButtonClick(View view) {
        putOnDisplay("onAAPSReadHistoryButtonClick");
    }


    public void onAAPSSetExtBolusButtonClick(View view) {
        putOnDisplay("onAAPSSetExtBolusButtonClick");
    }


    public void onAAPSCancelExtBolusButtonClick(View view) {
        putOnDisplay("onAAPSCancelExtBolusButtonClick");
    }


    public void onAAPSLoadTDDButtonClick(View view) {
        putOnDisplay("onAAPSLoadTDDButtonClick");
    }


    public void putOnDisplay(String text) {
        this.textViewComm.append(text + "\n");
    }


    Object data;
    String errorCode;


    public void sendData(String action) {

        // FIXME
        switch (action) {
            case "RefreshData.PumpModel": {
                MedtronicDeviceType pumpModel = (MedtronicDeviceType) data;
                putOnDisplay("Model: " + pumpModel.name());
            }
            break;

            case "RefreshData.BasalProfile": {
                BasalProfile basalProfile = (BasalProfile) data;
                putOnDisplay("Basal Profile: " + basalProfile.getBasalProfileAsString());
            }

            case "RefreshData.ErrorCode": {
                putOnDisplay("Error: " + errorCode);
            }
            break;

            default:
                putOnDisplay("Unsupported action: " + action);
        }


    }


    public void onAAPSGetModelButtonClick(View view) {

        putOnDisplay("onAAPSGetModelButtonClick");
        LOG.info("onAAPSGetModelButtonClick");

        final ServiceCommand command = ServiceClientActions.getPumpModel();

//        ServiceTransport serviceTransport = sendPumpCommand(command);
//
//        String pumpModel = serviceTransport.getServiceResult().getMap().getString("PumpModel", "Unknown");
//
//        putOnDisplay("Mode2l: " + pumpModel);


        new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("onAAPSGetModelButtonClick - In Runnable");
                MedtronicDeviceType pumpModel = MedtronicCommunicationManager.getInstance().getPumpModel();

                if (pumpModel == null) {
                    data = null;
                    errorCode = MedtronicCommunicationManager.getInstance().getErrorResponse();
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.ErrorCode");
                } else {
                    data = pumpModel;
                    errorCode = null;
                    LOG.info("onAAPSGetModelButtonClick - Data Read ");
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.PumpModel");
                }

            }
        }).start();

        putOnDisplay("onAAPSGetModelButtonClick - Ended");
        LOG.info("onAAPSGetModelButtonClick - Ended");

    }


    Map<String, ServiceTransport> commandQueue = new HashMap<String, ServiceTransport>();


    public synchronized ServiceTransport sendPumpCommand(ServiceCommand serviceCommand) {

        //ServiceCommand command = ServiceClientActions.makeUseThisRileylinkCommand(address);

        // check connected

        String uuid = serviceCommand.getCommandID();

        RoundtripServiceClientConnection.getInstance().sendServiceCommand(serviceCommand);
        //Log.d(TAG, "sendIPCMessage: (use this address) " + address);


        // max allowed timeout 30s (20s for wakeup, 10s for command)
        for(int i = 0; i < 22; i++) {


            try {
                synchronized (serviceCommand) {
                    serviceCommand.wait(1000);
                }
            } catch (InterruptedException e) {
                LOG.error("sendMessage InterruptedException", e);
                e.printStackTrace();
            }

            if (commandQueue.containsKey(uuid)) {
                //ServiceTransport serviceTransport = commandQueue.get(uuid);
                //commandQueue.remove(uuid);

                return null;
            }

        }

        return null;


    }


}
