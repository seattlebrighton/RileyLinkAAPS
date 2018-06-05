package com.gxwtech.roundtrip2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.MedtronicCommunicationManager;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.BasalProfile;
import info.nightscout.androidaps.plugins.PumpMedtronic.defs.MedtronicDeviceType;

public class ShowAAPS2Activity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(ShowAAPS2Activity.class);

    Spinner spinner;

    Button btnStart;

    Map<String, CommandAction> allCommands = new HashMap<>();
    private BroadcastReceiver mBroadcastReceiver;
    private TextView tvDuration, tvAmount, tvCommandStatusText, textViewComm;
    private EditText tfDuration, tfAmount;
    CommandAction selectedCommandAction = null;


    public ShowAAPS2Activity() {
        // FIXME
        addCommandAction("Set TBR", ImplementationStatus.NotStarted, null);
        addCommandAction("Cancel TBR", ImplementationStatus.NotStarted, null);
        addCommandAction("Set Bolus", ImplementationStatus.NotStarted, null);
        addCommandAction("Set Basal Profile", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - Bolus", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - TBR", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - Ext. Bolus", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - Settings", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - Remaining Power", ImplementationStatus.WorkInProgress, "RefreshData.RemainingPower");

        // LOW PRIORITY
        addCommandAction("Read History", ImplementationStatus.NotStarted, null);
        addCommandAction("Set Ext Bolus", ImplementationStatus.NotStarted, null);
        addCommandAction("Load TDD", ImplementationStatus.NotStarted, null);


        // DONE
        addCommandAction("Get Model", ImplementationStatus.Done, "RefreshData.PumpModel");
        addCommandAction("Get Basal Profile", ImplementationStatus.Done, "RefreshData.BasalProfile");
        addCommandAction("Status - Remaining Insulin", ImplementationStatus.Done, "RefreshData.RemainingInsulin");
        addCommandAction("Status - Get Time", ImplementationStatus.Done, "RefreshData.GeTime");


        // NOT SUPPORTED
        addCommandAction("Cancel Ext Bolus", ImplementationStatus.NotSupportedByDevice, null);
        addCommandAction("Cancel Bolus", ImplementationStatus.NotSupportedByDevice, null);

    }


    private void addCommandAction(String action, ImplementationStatus implementationStatus, String intent) {
        allCommands.put(action, new CommandAction(action, implementationStatus, intent));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_aaps2);

        this.textViewComm = findViewById(R.id.textViewComm);

        this.tvDuration = findViewById(R.id.tvDuration);
        this.tvAmount = findViewById(R.id.tvAmount);

        this.tfAmount = findViewById(R.id.tfAmount);
        this.tfDuration = findViewById(R.id.tfDuration);

        this.btnStart = findViewById(R.id.btnStart);
        this.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bolus, duration
                startAction();
            }
        });


        tvCommandStatusText = (TextView) findViewById(R.id.tvCommandStatusText);
        spinner = (Spinner) findViewById(R.id.spinnerPumpCommands);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                commandSelected(itemAtPosition);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                commandSelected(null);
            }
        });


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

        for(CommandAction commandAction : allCommands.values()) {

            if (commandAction.implementationStatus == ImplementationStatus.Done || //
                    commandAction.implementationStatus == ImplementationStatus.WorkInProgress) {
                if (commandAction.intentString != null) {
                    intentFilter.addAction(commandAction.intentString);
                }
            }
        }

        intentFilter.addAction("RefreshData.ErrorCode");

        LocalBroadcastManager.getInstance(MainApp.instance().getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }


    public void commandSelected(Object id) {


        if (id == null) {
            tvCommandStatusText.setText("Nothing");
            enableFields(false, false);
            this.btnStart.setEnabled(false);
        } else {

            this.selectedCommandAction = allCommands.get((String) id);
            tvCommandStatusText.setText(selectedCommandAction.implementationStatus.text);
            enableFields(false, false);
            this.btnStart.setEnabled(selectedCommandAction.intentString != null);
        }

    }


    private void enableFields(boolean amount, boolean duration) {

        tfDuration.setEnabled(duration);
        tvDuration.setEnabled(duration);

        tvAmount.setEnabled(amount);
        tfAmount.setEnabled(amount);
    }


    public void putOnDisplay(String text) {
        this.textViewComm.append(text + "\n");
    }


    public enum ImplementationStatus {
        NotStarted("Not Started"), //
        WorkInProgress("Work In Progress"), //
        Done("Command Done"), //
        NotSupportedByDevice("Not supported by device"); //

        String text;


        ImplementationStatus(String text) {
            this.text = text;
        }
    }


    public class CommandAction {
        String action;
        ImplementationStatus implementationStatus;
        String intentString;


        public CommandAction(String action, //
                             ImplementationStatus implementationStatus, //
                             String intentString
        ) {
            this.action = action;
            this.implementationStatus = implementationStatus;
            this.intentString = intentString;
        }

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
            break;

            case "RefreshData.RemainingInsulin": {
                Float remainingInsulin = (Float) data;
                putOnDisplay("Remaining Insulin: " + remainingInsulin);
            }
            break;

            case "RefreshData.RemainingPower": {
                Integer power = (Integer) data;
                putOnDisplay("Remaining Battery: " + power);
            }
            break;

            case "RefreshData.GeTime": {
                LocalDateTime ldt = (LocalDateTime) data;
                putOnDisplay("Pump Time: " + ldt.toString("dd.MM.yyyy HH:mm:ss"));
            }

            case "RefreshData.ErrorCode": {
                putOnDisplay("Error: " + errorCode);
            }
            break;

            default:
                putOnDisplay("Unsupported action: " + action);
        }


    }


    private void startAction() {

        // FIXME
        new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("start Action: " + selectedCommandAction.action);

                Object returnData = null;

                switch (selectedCommandAction.intentString) {
                    case "RefreshData.PumpModel": {
                        returnData = MedtronicCommunicationManager.getInstance().getPumpModel();
                    }
                    break;

                    case "RefreshData.BasalProfile": {
                        returnData = MedtronicCommunicationManager.getInstance().getBasalProfile();
                    }
                    break;

                    case "RefreshData.RemainingInsulin": {
                        returnData = MedtronicCommunicationManager.getInstance().getRemainingInsulin();
                    }
                    break;

                    case "RefreshData.GeTime": {
                        returnData = MedtronicCommunicationManager.getInstance().getPumpTime();
                    }
                    break;

                    case "RefreshData.RemainingPower": {
                        returnData = MedtronicCommunicationManager.getInstance().getRemainingBattery();
                    }
                    break;

                    default:
                        LOG.warn("Action is not supported {}.", selectedCommandAction);

                }


                if (returnData == null) {
                    data = null;
                    errorCode = MedtronicCommunicationManager.getInstance().getErrorResponse();
                    RileyLinkUtil.sendBroadcastMessage("RefreshData.ErrorCode");
                } else {
                    data = returnData;
                    errorCode = null;
                    RileyLinkUtil.sendBroadcastMessage(selectedCommandAction.intentString);
                }


            }
        }).start();

    }


}
