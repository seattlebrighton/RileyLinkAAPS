package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.os.Bundle;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.ISFTable;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.data.ServiceResult;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.tasks.PumpTask;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.data.history_old.TimeFormat;
import info.nightscout.androidaps.plugins.PumpMedtronic.service.RileyLinkMedtronicService;

/**
 * Created by geoff on 7/10/16.
 */
public class ReadISFProfileTask extends PumpTask {
    public ReadISFProfileTask() {
    }


    public ReadISFProfileTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void preOp() {
    }


    @Override
    public void run() {
        ISFTable table = RileyLinkMedtronicService.getCommunicationManager().getPumpISFProfile();
        ServiceResult result = getServiceTransport().getServiceResult();
        if (table.isValid()) {
            // convert from ISFTable to ISFProfile
            Bundle map = result.getMap();
            map.putIntArray("times", table.getTimes());
            map.putFloatArray("rates", table.getRates());
            map.putString("ValidDate", TimeFormat.standardFormatter().print(table.getValidDate()));
            result.setMap(map);
            result.setResultOK();
            getServiceTransport().setServiceResult(result);
        }
    }

}
