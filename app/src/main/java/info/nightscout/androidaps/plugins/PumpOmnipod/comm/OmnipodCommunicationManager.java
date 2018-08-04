package info.nightscout.androidaps.plugins.PumpOmnipod.comm;

import android.content.Context;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Random;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;


import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ConfigResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodPacket;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ErrorResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ErrorResponseType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PodState;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationManager extends RileyLinkCommunicationManager {

    private static final int defaultAddress = 0xFFFFFFFF;
    private int messageNumber = 0;
    private int packetNumber = 0;
    private PodState podState;

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
        OmnipodPacket pumpMessage = new OmnipodPacket();
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


    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        int packetAddress = defaultAddress;
        if (this.podState != null)
            packetAddress = this.podState.Address;
        if (addressOverride != null)
            packetAddress = addressOverride;

        Boolean firstPacket = true;
        byte[] encodedMessage = message.getEncoded();
        OmnipodPacket response = null;
        while(encodedMessage.length > 0) {
            PacketType packetType = firstPacket? PacketType.Pdm : PacketType.Con;
            OmnipodPacket packet = new OmnipodPacket(packetAddress, packetType,packetNumber, encodedMessage);
            byte[] dataToSend = packet.getTxData();
            encodedMessage = ByteUtil.substring(encodedMessage, dataToSend.length - 1, encodedMessage.length - dataToSend.length);
            firstPacket = false;
            response = exchangePackets(packet);
            //We actually ignore (ack) responses if it is not last packet to send
        }
        if (response.getPacketType() == PacketType.Ack) {
            //FIXME: we received ack instead of real response, something is wrong
            return null;
        }
        OmnipodMessage receivedMessage = null;
        byte[] receivedData = response.getTxData();
        while(receivedMessage == null) {
            receivedMessage = OmnipodMessage.TryDecode(receivedData);
            if (receivedMessage == null) {
                OmnipodPacket ackForCon = makeAckPacket(packetAddress, ackAddressOverride);
                OmnipodPacket conPacket = exchangePackets(ackForCon, 3, 40);
                if (conPacket.getPacketType() != PacketType.Con) {
                    //FIXME: We should throw an error as we expect only continuation packets
                }
                receivedData = ByteUtil.concat(receivedData, conPacket.getTxData());
            }
        }
        incrementMessageNumber(2);

        ackUntilQuiet(packetAddress, ackAddressOverride);

        MessageBlock[] messageBlocks = receivedMessage.getMessageBlocks();
        if (messageBlocks.length == 0) {
            //FIXME: We should throw an error of not enough data
            return null;
        }

        MessageBlock block = messageBlocks[0];
        if (block.getType() == MessageBlockType.ErrorResponse) {
            ErrorResponse error = (ErrorResponse)block;
            if (error.getErrorResponseType() == ErrorResponseType.BadNonce) {
                //FIXME: Log that we have nonce out-of-sync
                if (podState != null) {
                    this.podState.ResyncNonce(error.getNonceSearchKey(), this.podState.CurrentNonce, message.getSequenceNumber());
                }
            }
            //FIXME: we should log and throw the error
            return null;

        }
        T responeBlock = (T) block;
        //FIXME: Log content here
        return responeBlock;

    }

    private void incrementMessageNumber(int increment) {
        messageNumber = (messageNumber + increment) & 0b1111;
    }
    private void incrementPacketNumber(int increment) {
        packetNumber = (packetNumber + increment) & 0b11111;
    }

    private OmnipodPacket makeAckPacket(Integer packetAddress, Integer messageAddress) {
        int addr1 = defaultAddress;
        int addr2 = defaultAddress;
        if (this.podState != null) {
            addr1 = addr2 = podState.Address;
        }
        if (packetAddress != null)
            addr1 = packetAddress;
        if (messageAddress != null)
            addr2 = messageAddress;
        return new OmnipodPacket(addr1, PacketType.Ack, packetNumber, ByteUtil.getUInt16BigEndian((short)addr2));

    }



    // FIXME: ===== Let's implement these!

    private void ackUntilQuiet(int packetAddress, Integer messageAddress) {
        throw new NotImplementedException("ackUntilQuiet");

    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet) {
        throw new NotImplementedException("exchangePackets");
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet, int retries, int preambleExntension_ms) {
        throw new NotImplementedException("exchangePackets");
    }


// FIXME: ===== Let's implement these! ==== END

    public Object initializePod() {
        Random rnd = new Random();
        int newAddress = rnd.nextInt();
        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(defaultAddress, new MessageBlock[] {assignAddress}, messageNumber);
        ConfigResponse config = exchangeMessages(assignAddressMessage, defaultAddress, newAddress);




        return null;
    }
}
