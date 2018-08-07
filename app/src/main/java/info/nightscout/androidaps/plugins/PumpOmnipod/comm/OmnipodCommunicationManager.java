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
        OmnipodPacket pumpMessage = new OmnipodPacket(payload);
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
        byte[] receivedMessageData = response.getEncodedMessage();
        while(receivedMessage == null) {
            receivedMessage = OmnipodMessage.TryDecode(receivedMessageData);
            if (receivedMessage == null) {
                OmnipodPacket ackForCon = makeAckPacket(packetAddress, ackAddressOverride);
                OmnipodPacket conPacket = exchangePackets(ackForCon, 3, 40);
                if (conPacket.getPacketType() != PacketType.Con) {
                    //FIXME: We should throw an error as we expect only continuation packets
                }
                receivedMessageData = ByteUtil.concat(receivedMessageData, conPacket.getEncodedMessage());
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

    private void ackUntilQuiet(Integer packetAddress, Integer messageAddress) {
        OmnipodPacket ack = makeAckPacket(packetAddress, messageAddress);
        Boolean quiet = false;
        while(!quiet) {
            OmnipodPacket response = sendAndListen(ack, 600, 5, 40, OmnipodPacket.class);
            if (response == null)
                quiet = true;
        }
        incrementPacketNumber(1);
    }



    private OmnipodPacket exchangePackets(OmnipodPacket packet) {
        return exchangePackets(
                packet
                , 0
                , 165
                ,20000
                , 127
        );
    }

    private OmnipodPacket exchangePackets(
            OmnipodPacket packet
            , int repeatCount
            , int preambleExtension_ms) {
        return exchangePackets(
                packet
                , repeatCount
                , 165
                , 20000
                , preambleExtension_ms);
    }
    private OmnipodPacket exchangePackets(
            OmnipodPacket packet
            , int repeatCount
            , int responseTimeout_ms
            , int exchangeTimeout_ms
            , int preambleExtension_ms) {
        int radioRetriesCount = 20;
        long timeoutTime = System.currentTimeMillis() + exchangeTimeout_ms;
        while(System.currentTimeMillis() < timeoutTime) {
            OmnipodPacket response = sendAndListen(packet, responseTimeout_ms, repeatCount, preambleExtension_ms, OmnipodPacket.class);
            if (response == null || response.isValid() == false)
                continue;
            if (response.getAddress() != packet.getAddress()) {
                continue;
            }
            if (response.getSequenceNumber() != ((packetNumber + 1) & 0b11111))
                continue;

            incrementPacketNumber(2);
            return response;

        }
        //FIXME: throw timeout (no response) exception
        return null;
    }


    public Object initializePod() {
        Random rnd = new Random();
        int newAddress = rnd.nextInt();
        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(defaultAddress, new MessageBlock[] {assignAddress}, messageNumber);
        ConfigResponse config = exchangeMessages(assignAddressMessage, defaultAddress, newAddress);




        return null;
    }
}
