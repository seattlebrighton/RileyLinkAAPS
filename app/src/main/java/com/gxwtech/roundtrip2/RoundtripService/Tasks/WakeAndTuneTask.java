package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.RoundtripService.RileyLinkServiceMedtronic;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/16/16.
 */
public class WakeAndTuneTask extends PumpTask {
    private static final String TAG = "WakeAndTuneTask";
    public WakeAndTuneTask() { }
    public WakeAndTuneTask(ServiceTransport transport) {
        super(transport);
    }

    @Override
    public void run() {
        RileyLinkServiceMedtronic.getInstance().pumpCommunicationManager.wakeup(6);
        RileyLinkServiceMedtronic.getInstance().pumpCommunicationManager.tuneForPump();
    }

}
