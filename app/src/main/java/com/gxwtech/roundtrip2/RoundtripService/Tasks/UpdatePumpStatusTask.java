package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.PumpStatus;

import com.gxwtech.roundtrip2.RoundtripService.RileyLinkServiceMedtronic;
import com.gxwtech.roundtrip2.ServiceData.PumpStatusResult;
import com.gxwtech.roundtrip2.ServiceData.ServiceTransport;

/**
 * Created by geoff on 7/16/16.
 */
public class UpdatePumpStatusTask extends PumpTask {
    private static final String TAG = "UpdatePumpStatusTask";
    public UpdatePumpStatusTask() { }
    public UpdatePumpStatusTask(ServiceTransport transport) {
        super(transport);
    }

    @Override
    public void run() {
        // force pump to update everything it can
        RileyLinkServiceMedtronic.getCommunicationManager().updatePumpManagerStatus();
        // get the newly cached status
        PumpStatus status = RileyLinkServiceMedtronic.getCommunicationManager().getPumpStatus();
        // fill a PumpStatusResult message with the goods
        PumpStatusResult result = new PumpStatusResult();
  /*
    double remainBattery;
    double remainUnits;
    double currentBasal;
    double lastBolusAmount;
    String lastBolusTime;
    int tempBasalInProgress;
    double tempBasalRatio;
    double tempBasalRemainMin;
    String tempBasalStart;
    String time;
    String timeLastSync;
    */

        result.setRemainBattery(status.remainBattery);
        result.setRemainUnits(status.remainUnits);
        result.setCurrentBasal(status.currentBasal);
        result.setLastBolusAmount(status.last_bolus_amount);
        result.setLastBolusTime(status.last_bolus_time.toString());
        result.setTempBasalInProgress(status.tempBasalInProgress);
        result.setTempBasalRatio(status.tempBasalRatio);
        result.setTempBasalRemainMin(status.tempBasalRemainMin);
        result.setTempBasalStart(status.tempBasalStart.toString());
        result.setTime(status.time.toString());
        result.setTimeLastSync("");
        getServiceTransport().setServiceResult(result);

    }

}
