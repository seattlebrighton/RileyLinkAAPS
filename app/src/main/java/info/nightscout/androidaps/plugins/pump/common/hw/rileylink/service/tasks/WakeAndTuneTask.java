package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks;

import android.util.Log;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;

/**
 * Created by geoff on 7/16/16.
 */
public class WakeAndTuneTask extends PumpTask {
    private static final String TAG = "WakeAndTuneTask";


    public WakeAndTuneTask() {
    }


    public WakeAndTuneTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {
        Log.w(TAG, "Not supported for Omnipod");
        //RileyLinkMedtronicService.getInstance().getDeviceCommunicationManager().tuneForDevice();
    }

}
