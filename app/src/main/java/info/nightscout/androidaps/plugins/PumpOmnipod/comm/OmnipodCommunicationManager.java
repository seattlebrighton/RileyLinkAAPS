package info.nightscout.androidaps.plugins.PumpOmnipod.comm;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.PodMessage;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationManager extends RileyLinkCommunicationManager {


    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);
    OmnipodCommunicationManager omnipodCommunicationManager;
    private boolean showPumpMessages;

    public OmnipodCommunicationManager(Context context, RFSpy rfspy) {
        super(context, rfspy, RileyLinkTargetFrequency.Omnipod);
        omnipodCommunicationManager = this;
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
        return new byte[0];
    }


    @Override
    public <E extends RLMessage> E createResponseMessage(byte[] payload, Class<E> clazz) {
        PodMessage pumpMessage = new PodMessage(payload);
        return (E) pumpMessage;
    }


    // All pump communications go through this function.
    protected PodMessage sendAndListen(RLMessage msg, int timeout_ms) {

        return sendAndListen(msg, timeout_ms, PodMessage.class);
    }


}
