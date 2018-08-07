package info.nightscout.androidaps.plugins.PumpOmnipod.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.RT2Const;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.FetchPumpHistoryTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadBolusWizardCarbProfileTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadISFProfileTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.ReadPumpClockTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.RetrieveHistoryPageTask;
import com.gxwtech.roundtrip2.RoundtripService.Tasks.UpdatePumpStatusTask;
import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.PumpHistoryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RileyLinkBLE;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.defs.RileyLinkTargetDevice;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.RileyLinkService;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.RileyLinkServiceData;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.data.ServiceResult;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.tasks.ServiceTask;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.tasks.ServiceTaskExecutor;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.tasks.WakeAndTuneTask;
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

    @Override
    public RileyLinkEncodingType getEncoding() {
        return RileyLinkEncodingType.FourByteSixByte;
    }

    public RileyLinkOmnipodService() {
        super(MainApp.instance().getApplicationContext());
        instance = this;
        LOG.debug("RileyLinkOmnipodService newly constructed");
        RileyLinkUtil.setRileyLinkService(this);
    }


    @Override
    protected void determineRileyLinkTargetFrequency() {
        this.rileyLinkTargetFrequency = RileyLinkTargetFrequency.Omnipod;

        // omnipod has only one frequency, and no tuneup required
        SP.putFloat(RileyLinkConst.Prefs.LastGoodDeviceFrequency, 433.91f);
    }


    @Override
    public void initRileyLinkServiceData() {

        rileyLinkServiceData = new RileyLinkServiceData(RileyLinkTargetDevice.Omnipod);

        RileyLinkUtil.setRileyLinkServiceData(rileyLinkServiceData);

        // get most recently used RileyLink address
        rileyLinkServiceData.rileylinkAddress = SP.getString(RileyLinkConst.Prefs.RileyLinkAddress, "");

        rileyLinkBLE = new RileyLinkBLE(this.context); // or this
        rfspy = new RFSpy(rileyLinkBLE);
        rfspy.startReader();

        RileyLinkUtil.setRileyLinkBLE(rileyLinkBLE);

        omnipodCommunicationManager = new OmnipodCommunicationManager(context, rfspy);
    }

    @Override
    public RileyLinkCommunicationManager getDeviceCommunicationManager() {
        return this.omnipodCommunicationManager;
    }


    @Override
    public void addPumpSpecificIntents(IntentFilter intentFilter) {
    }


    @Override
    public void handlePumpSpecificIntents(Intent intent) {
    }


    @Override
    public void handleIncomingServiceTransport(Intent intent) {
        Bundle bundle = intent.getBundleExtra(RT2Const.IPC.bundleKey);

        ServiceTransport serviceTransport = new ServiceTransport(bundle);

        if (serviceTransport.getServiceCommand().isPumpCommand()) {

            LOG.debug("IsPumpCommand not implemented.");
        } else {
            switch (serviceTransport.getOriginalCommandName()) {
                case "UseThisRileylink":
                    // If we are not connected, connect using the given address.
                    // If we are connected and the addresses differ, disconnect, connect to new.
                    // If we are connected and the addresses are the same, ignore.
                    String deviceAddress = serviceTransport.getServiceCommand().getMap().getString("rlAddress", "");
                    if ("".equals(deviceAddress)) {
                        LOG.error("handleIPCMessage: null RL address passed");
                    } else {
                        reconfigureRileylink(deviceAddress);
                    }
                    break;
                default:
                    LOG.error("handleIncomingServiceTransport: Failed to handle service command '" + serviceTransport.getOriginalCommandName() + "'");
                    break;
            }
        }

    }


    //@Nullable
    //@Override
    //public IBinder onBind(Intent intent) {
    //    return mBinder;
    //}


    public class LocalBinder extends Binder {
        public RileyLinkOmnipodService getServiceInstance() {
            return RileyLinkOmnipodService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return rileyLinkIPCConnection.doOnBind(intent);
    }


}
