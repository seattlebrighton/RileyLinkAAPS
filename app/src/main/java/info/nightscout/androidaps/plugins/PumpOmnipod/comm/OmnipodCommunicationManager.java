package info.nightscout.androidaps.plugins.PumpOmnipod.comm;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Random;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;


import info.nightscout.androidaps.plugins.PumpOmnipod.comm.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodMessage;
import info.nightscout.utils.SP;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationManager extends RileyLinkCommunicationManager {


    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);
    private boolean showPumpMessages;
    static OmnipodCommunicationManager omnipodCommunicationManager;

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


    //FIXME: This one should be refactored as it sends/listens to raw packets and not messages
    @Override
    public <E extends RLMessage> E createResponseMessage(byte[] payload, Class<E> clazz) {
        OmnipodMessage pumpMessage = new OmnipodMessage();
        return (E)pumpMessage;
    }


//    // All pump communications go through this function.
//    protected PodMessage sendAndListen(RLMessage msg, int timeout_ms) {
//
//        return sendAndListen(msg, timeout_ms, PodMessage.class);
//    }


    public static OmnipodCommunicationManager getInstance() {
        return omnipodCommunicationManager;
    }

    public Object initializePod() {
        Random rnd = new Random();
        int newAddress = rnd.nextInt();
        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);



        return null;
    }
}
