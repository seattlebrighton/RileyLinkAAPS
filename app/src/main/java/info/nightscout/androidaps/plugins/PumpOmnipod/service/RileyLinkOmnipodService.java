package info.nightscout.androidaps.plugins.PumpOmnipod.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.PumpHistoryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RileyLinkBLE;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.defs.RileyLinkTargetDevice;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.RileyLinkService;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.RileyLinkServiceData;
import info.nightscout.androidaps.plugins.PumpMedtronic.comm.MedtronicCommunicationManager;
import info.nightscout.androidaps.plugins.PumpMedtronic.service.RileyLinkMedtronicService;
import info.nightscout.androidaps.plugins.PumpMedtronic.util.MedtronicConst;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.OmnipodCommunicationManager;
import info.nightscout.utils.SP;

/**
 * Created by andy on 6/1/18.
 */

public class RileyLinkOmnipodService extends RileyLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(RileyLinkOmnipodService.class);

    private static RileyLinkOmnipodService instance;

    OmnipodCommunicationManager omnipodCommunicationManager;
    private IBinder mBinder = new LocalBinder();


    public RileyLinkOmnipodService() {
        super(MainApp.instance().getApplicationContext());
        instance = this;
        LOG.debug("RileyLinkOmnipodService newly constructed");
        RileyLinkUtil.setRileyLinkService(this);
    }


    @Override
    protected void determineRileyLinkTargetFrequency() {
        this.rileyLinkTargetFrequency = RileyLinkTargetFrequency.Omnipod;
    }


    @Override
    public void initRileyLinkServiceData() {

        rileyLinkServiceData = new RileyLinkServiceData(RileyLinkTargetDevice.Omnipod);

        RileyLinkUtil.setRileyLinkServiceData(rileyLinkServiceData);

        // get most recently used RileyLink address
        rileyLinkServiceData.rileylinkAddress = SP.getString(MedtronicConst.Prefs.RileyLinkAddress, "");

        rileyLinkBLE = new RileyLinkBLE(this.context); // or this
        rfspy = new RFSpy(rileyLinkBLE);
        rfspy.startReader();

        RileyLinkUtil.setRileyLinkBLE(rileyLinkBLE);


        // init rileyLinkCommunicationManager
        omnipodCommunicationManager = new OmnipodCommunicationManager(context, rfspy);
        pumpCommunicationManager = omnipodCommunicationManager;
    }


    @Override
    public void addPumpSpecificIntents(IntentFilter intentFilter) {
    }


    @Override
    public void handlePumpSpecificIntents(Intent intent) {
    }


    @Override
    public void handleIncomingServiceTransport(Intent intent) {

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        public RileyLinkOmnipodService getServiceInstance() {
            return RileyLinkOmnipodService.this;
        }
    }

}
