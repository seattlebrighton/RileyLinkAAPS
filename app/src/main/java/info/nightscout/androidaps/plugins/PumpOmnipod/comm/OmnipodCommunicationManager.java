package info.nightscout.androidaps.plugins.PumpOmnipod.comm;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.command.ConfigResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.command.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodPacket;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PodState;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationManager extends RileyLinkCommunicationManager {

    private static final int defaultAddress = 0xFFFFFFFF;
    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);
    static OmnipodCommunicationManager omnipodCommunicationManager;
    private int messageNumber = 0;
    private int packetNumber = 0;
    private PodState podState;
    private boolean showPumpMessages;

    public OmnipodCommunicationManager(Context context, RFSpy rfspy) {
        super(context, rfspy, RileyLinkTargetFrequency.Omnipod);
        omnipodCommunicationManager = this;
    }

    public static OmnipodCommunicationManager getInstance() {
        return omnipodCommunicationManager;
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


//    // All pump communications go through this function.
//    protected PodMessage sendAndListen(RLMessage msg, int timeout_ms) {
//
//        return sendAndListen(msg, timeout_ms, PodMessage.class);
//    }

    //FIXME: This one should be refactored as it sends/listens to raw packets and not messages
    @Override
    public <E extends RLMessage> E createResponseMessage(byte[] payload, Class<E> clazz) {
        OmnipodPacket pumpMessage = new OmnipodPacket();
        return (E) pumpMessage;
    }

    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        int packetAddress = defaultAddress;
        if (this.podState != null)
            packetAddress = this.podState.Address;
        if (addressOverride != null)
            packetAddress = addressOverride;

        Boolean firstPacket = true;
        byte[] encodedMessage = message.getEncoded();
        OmnipodPacket response = null;
        while (encodedMessage.length > 0) {
            PacketType packetType = firstPacket ? PacketType.Pdm : PacketType.Con;
            OmnipodPacket packet = new OmnipodPacket(packetAddress, packetType, packetNumber, encodedMessage);
            byte[] dataToSend = packet.getTxData();
            encodedMessage = ByteUtil.substring(encodedMessage, dataToSend.length - 1, encodedMessage.length - dataToSend.length);
            firstPacket = false;
            response = exchangePackets(packet);
            //We actually ignore (ack) responses if it is not last packet to send
        }
        if (response.getPacketType() == PacketType.Ack) {
            //we received ack instead of real response, something is wrong

        }
        OmnipodMessage receivedMessage = null;
        byte[] receivedData = response.getTxData();
        while (receivedMessage == null) {
            receivedMessage = OmnipodMessage.TryDecode(receivedData);
            if (receivedMessage == null) {
                OmnipodPacket ackForCon = makeAckPacket(packetAddress, ackAddressOverride);
                OmnipodPacket conPacket = exchangePackets(ackForCon, 3, 40);
                if (conPacket.getPacketType() != PacketType.Con) {
                    //We should throw an error as we expect only continuation packets
                }
                receivedData = ByteUtil.concat(receivedData, conPacket.getTxData());
            }
        }
        incrementMessageNumber(2);

        ackUntilQuiet(packetAddress, ackAddressOverride);


        return null;

    }

    private void ackUntilQuiet(int packetAddress, Integer messageAddress) {

    }

    private void incrementMessageNumber(int increment) {

    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet, int retries, int preambleExntension_ms) {
        return null;
    }

    private OmnipodPacket makeAckPacket(int packetAddress, Integer ackAddressOverride) {
        return null;
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet) {
        return null;
    }

    public Object initializePod() {
        Random rnd = new Random();
        int newAddress = rnd.nextInt();
        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(defaultAddress, new MessageBlock[]{assignAddress}, messageNumber);
        ConfigResponse config = exchangeMessages(assignAddressMessage, defaultAddress, newAddress);


        return null;
    }
}
