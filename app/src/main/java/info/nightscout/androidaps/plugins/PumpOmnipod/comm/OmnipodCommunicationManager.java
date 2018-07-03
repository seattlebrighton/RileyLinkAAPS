package info.nightscout.androidaps.plugins.PumpOmnipod.comm;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;


import info.nightscout.androidaps.plugins.PumpMedtronic.util.MedtronicConst;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.PumpMessage;
import info.nightscout.utils.SP;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationManager extends RileyLinkCommunicationManager {


    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);
    private boolean showPumpMessages;
    OmnipodCommunicationManager omnipodCommunicationManager;

    public OmnipodCommunicationManager(Context context, RFSpy rfspy) {
        super(context, rfspy, RileyLinkTargetFrequency.Omnipod);
        omnipodCommunicationManager = this;
        //this.medtronicConverter = new MedtronicConverter();
    }


    @Override
    protected void configurePumpSpecificSettings() {

    }

    @Override
    public boolean tryToConnectToDevice() {
        // TODO
        return false;
    }

    @Override
    public byte[] createPumpMessageContent(RLMessageType type) {
        // TODO
        return new byte[0];
    }


    // All pump communications go through this function.
    protected PumpMessage sendAndListen(RLMessage msg, int timeout_ms) {

        if (showPumpMessages) {
            LOG.info("Sent:" + ByteUtil.shortHexString(msg.getTxData()));
        }

        RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(msg.getTxData()), timeout_ms);
        PumpMessage rval = new PumpMessage(resp.getRadioResponse().getPayload());
        if (rval.isValid()) {
            // Mark this as the last time we heard from the pump.
            rememberLastGoodPumpCommunicationTime();
        } else {
            LOG.warn("Response is invalid. !!!");
        }

        if (showPumpMessages) {
            LOG.info("Received:" + ByteUtil.shortHexString(resp.getRadioResponse().getPayload()));
        }
        return rval;
    }


    protected void rememberLastGoodPumpCommunicationTime() {
        lastGoodReceiverCommunicationTime = System.currentTimeMillis();

        SP.putLong(MedtronicConst.Prefs.LastGoodPumpCommunicationTime, lastGoodReceiverCommunicationTime);
        pumpStatus.setLastDataTimeToNow();
    }


}
