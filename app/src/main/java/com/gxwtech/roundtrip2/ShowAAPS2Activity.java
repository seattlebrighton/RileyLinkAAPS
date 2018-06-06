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
import java.util.List;
import java.util.Map;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.HexDump;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.MedtronicCommunicationManager;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.BasalProfile;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.TempBasalPair;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.PumpMessage;
import info.nightscout.androidaps.plugins.PumpMedtronic.data.dto.PumpSettingDTO;
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

        // SET MODEL WHEN FIRST READ

        // FIXME
        addCommandAction("Set TBR", ImplementationStatus.WorkInProgress, "RefreshData.SetTBR"); // not working
        addCommandAction("Cancel TBR", ImplementationStatus.NotStarted, null);
        addCommandAction("Set Bolus", ImplementationStatus.NotStarted, null);
        addCommandAction("Set Basal Profile", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - Bolus", ImplementationStatus.WorkInProgress, "RefreshData.GetStatus"); // weird on 512?
        addCommandAction("Status - Settings", ImplementationStatus.WorkInProgress, "RefreshData.GetSettings");

        // WORK IN PROGRESS - waiting for something
        addCommandAction("Status - Remaining Power", ImplementationStatus.WorkInProgress, "RefreshData.RemainingPower");

        // LOW PRIORITY
        addCommandAction("Read History", ImplementationStatus.NotStarted, null);
        addCommandAction("Set Ext Bolus", ImplementationStatus.NotStarted, null);
        addCommandAction("Status - Ext. Bolus", ImplementationStatus.WorkInProgress, "RefreshData.GetBolus");
        addCommandAction("Load TDD", ImplementationStatus.NotStarted, null);


        // DONE
        addCommandAction("Get Model", ImplementationStatus.Done, "RefreshData.PumpModel");
        addCommandAction("Get Basal Profile", ImplementationStatus.Done, "RefreshData.BasalProfile");
        addCommandAction("Status - Remaining Insulin", ImplementationStatus.Done, "RefreshData.RemainingInsulin");
        addCommandAction("Status - Get Time", ImplementationStatus.Done, "RefreshData.GetTime");
        addCommandAction("Status - TBR", ImplementationStatus.Done, "RefreshData.GetTBR");

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
            enableFields(isAmountEnabled(), isDurationEnabled());
            this.btnStart.setEnabled(selectedCommandAction.intentString != null);
        }

    }


    private boolean isAmountEnabled() {
        String action = this.selectedCommandAction.action;

        return (action.equals("Set TBR") || //
                action.equals("Set Bolus") || //
                action.equals("Set Basal Profile"));
    }


    private boolean isDurationEnabled() {
        String action = this.selectedCommandAction.action;

        return (action.equals("Set TBR"));
    }


    private void enableFields(boolean amount, boolean duration) {

        tfDuration.setEnabled(duration);
        tvDuration.setEnabled(duration);
        if (!duration)
            tfDuration.setText("");

        tvAmount.setEnabled(amount);
        tfAmount.setEnabled(amount);

        if (!amount)
            tfAmount.setText("");

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

    MedtronicCommunicationManager mcmInstance = null;


    private MedtronicCommunicationManager getCommunicationManager() {
        if (mcmInstance == null) {
            mcmInstance = MedtronicCommunicationManager.getInstance();
        }

        return mcmInstance;
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

            case "RefreshData.GetTime": {
                LocalDateTime ldt = (LocalDateTime) data;
                putOnDisplay("Pump Time: " + ldt.toString("dd.MM.yyyy HH:mm:ss"));
            }
            break;

            case "RefreshData.ErrorCode": {
                putOnDisplay("Error: " + errorCode);
            }
            break;

            case "RefreshData.SetTBR": {
                PumpMessage result = (PumpMessage) data;
                // FIXME
                putOnDisplay("Response after action: " + HexDump.toHexStringDisplayable(result.getRawContent()));
            }
            break;

            case "RefreshData.GetTBR": {
                TempBasalPair tbr = (TempBasalPair) data;

                putOnDisplay(String.format("TBR: Amount: %s, Duration: %s", "" + tbr.getInsulinRate(), "" + tbr.getDurationMinutes()));
            }
            break;

            case "RefreshData.GetStatus": {
                // FIXME
                putOnDisplay("Status undefined ?");
            }
            break;


            case "RefreshData.GetSettings": {
                List<PumpSettingDTO> settings = (List<PumpSettingDTO>) data;

                putOnDisplay("Settings on pump:");
                LOG.debug("Settings on front: " + settings);
                for(PumpSettingDTO entry : settings) {
                    putOnDisplay(entry.key + " = " + entry.value);
                }
            }
            break;

            default:
                putOnDisplay("Unsupported action: " + action);
        }


    }


    private void startAction() {

        putOnDisplay("Start Action: " + selectedCommandAction.action);

        // FIXME
        new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("start Action: " + selectedCommandAction.action);

                Object returnData = null;

                switch (selectedCommandAction.intentString) {
                    case "RefreshData.PumpModel": {
                        returnData = getCommunicationManager().getPumpModel();
                    }
                    break;

                    case "RefreshData.BasalProfile": {
                        returnData = getCommunicationManager().getBasalProfile();
                    }
                    break;

                    case "RefreshData.RemainingInsulin": {
                        returnData = getCommunicationManager().getRemainingInsulin();
                    }
                    break;

                    case "RefreshData.GetTime": {
                        returnData = getCommunicationManager().getPumpTime();
                    }
                    break;

                    case "RefreshData.RemainingPower": {
                        returnData = getCommunicationManager().getRemainingBattery();
                    }
                    break;

                    case "RefreshData.SetTBR": {
                        TempBasalPair tbr = getTBRSettings();
                        if (tbr != null) {
                            returnData = getCommunicationManager().setTBR(tbr);
                        }
                    }
                    break;

                    case "RefreshData.GetTBR": {
                        returnData = getCommunicationManager().getTemporaryBasal();
                    }
                    break;

                    case "RefreshData.GetStatus": {
                        returnData = getCommunicationManager().getPumpState();
                    }
                    break;

                    case "RefreshData.GetBolus": {
                        returnData = getCommunicationManager().getBolusStatus();
                    }
                    break;

                    case "RefreshData.GetSettings": {
                        returnData = getCommunicationManager().getPumpSettings();
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


    private TempBasalPair getTBRSettings() {

        Float valAmount = getAmount();

        TempBasalPair tbp = new TempBasalPair();

        if (valAmount != null) {
            tbp.setInsulinRate(valAmount);
        } else
            return null;

        Integer dur = getDuration();

        if (dur != null) {
            tbp.setDurationMinutes(dur);
            return tbp;
        }

        return null;
    }


    private Float getAmount() {
        CharSequence am = tfAmount.getText();
        String amount = am.toString().replaceAll(",", ".");

        try {
            return Float.parseFloat(amount);
        } catch (Exception ex) {
            putOnDisplay("Error parsing amount: " + ex.getMessage());
            return null;
        }

    }


    private Integer getDuration() {
        CharSequence am = tfDuration.getText();
        String duration = am.toString();

        int timeMin = 0;

        if (duration.contains(".") || duration.contains(",")) {
            putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
            return null;
        }

        if (duration.contains(":")) {
            String[] time = duration.split(":");

            if ((!time[1].equals("00")) && (!time[1].equals("30"))) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }

            try {
                timeMin += Integer.parseInt(time[0]);
            } catch (Exception ex) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }

            if (time[1].equals("30")) {
                timeMin += 30;
            }


        }

        return timeMin;
    }

}
