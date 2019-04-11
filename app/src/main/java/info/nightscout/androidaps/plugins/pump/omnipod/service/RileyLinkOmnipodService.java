package info.nightscout.androidaps.plugins.pump.omnipod.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.RT2Const;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RileyLinkBLEFake;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpyFake;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.defs.RileyLinkTargetDevice;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.RileyLinkService;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.RileyLinkServiceData;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationManager;
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

        //rileyLinkBLE = new RileyLinkBLE(this.context); // or this
        //rfspy = new RFSpy(rileyLinkBLE);


    }

    @Override
    public RileyLinkCommunicationManager getDeviceCommunicationManager() {
        return this.omnipodCommunicationManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //delete when capture-debug is finished
        RileyLinkUtil.setRileyLinkService(this);
        rfspy = new RFSpyFake();
        //this.onCreate();
        rileyLinkBLE = new RileyLinkBLEFake();
        rfspy.startReader();

        RileyLinkUtil.setRileyLinkBLE(rileyLinkBLE);

        omnipodCommunicationManager = new OmnipodCommunicationManager(context, rfspy);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return rileyLinkIPCConnection.doOnBind(intent);
    }

    public class LocalBinder extends Binder {
        public RileyLinkOmnipodService getServiceInstance() {
            return RileyLinkOmnipodService.this;
        }
    }


}
