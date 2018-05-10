package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gxwtech.roundtrip2.RT2Const;
import com.gxwtech.roundtrip2.RoundtripService.RileyLinkServiceMedtronic;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;
import com.gxwtech.roundtrip2.ServiceData.ServiceNotification;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/9/16.
 *
 * This class is intended to be run by the Service, for the Service.
 * Not intended for clients to run.
 */
public class InitializePumpManagerTask extends ServiceTask {
    private static final String TAG = "InitPumpManagerTask";
    public InitializePumpManagerTask() { super(); }
    public InitializePumpManagerTask(ServiceTransport transport) { super(transport); }

    @Override
    public void run() {
        SharedPreferences sharedPref = RileyLinkServiceMedtronic.getInstance().getApplicationContext().getSharedPreferences(RT2Const.serviceLocal.sharedPreferencesKey, Context.MODE_PRIVATE);
        double lastGoodFrequency = sharedPref.getFloat(RT2Const.serviceLocal.prefsLastGoodPumpFrequency,(float)0.0);
        if (lastGoodFrequency != 0) {
            Log.i(TAG,String.format("Setting radio frequency to %.2fMHz",lastGoodFrequency));
            RileyLinkServiceMedtronic.getInstance().pumpCommunicationManager.setRadioFrequencyForPump(lastGoodFrequency);
        }

        PumpModel reportedPumpModel = RileyLinkServiceMedtronic.getCommunicationManager().getPumpModel();
        if (!reportedPumpModel.equals(PumpModel.UNSET)) {
            RileyLinkServiceMedtronic.getInstance().sendNotification(new ServiceNotification(RT2Const.IPC.MSG_PUMP_pumpFound),null);
        } else {
            RileyLinkServiceMedtronic.getInstance().sendNotification(new ServiceNotification(RT2Const.IPC.MSG_PUMP_pumpLost),null);
        }
        RileyLinkServiceMedtronic.getInstance().sendNotification(new ServiceNotification(RT2Const.IPC.MSG_note_Idle),null);
    }
}
