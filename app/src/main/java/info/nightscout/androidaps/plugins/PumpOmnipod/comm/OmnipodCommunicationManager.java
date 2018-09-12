package info.nightscout.androidaps.plugins.PumpOmnipod.comm;

import android.content.Context;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Random;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.IRFSpy;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkTargetFrequency;


import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.AlertConfiguration;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command.ConfigureAlertsCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command.ConfirmPairingCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ConfigResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.OmnipodPacket;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ErrorResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.ErrorResponseType;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.PodLifeStage;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.ExpirationAdvisory;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.InsulinSchedule.Bolus;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command.BolusExtraCommand;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PodState;
import info.nightscout.androidaps.plugins.PumpOmnipod.util.OmniPodConst;
import info.nightscout.utils.SP;

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

    public OmnipodCommunicationManager(Context context, IRFSpy rfspy) {
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

    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message) {
        return exchangeMessages(message, null, null);
    }


    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        int packetAddress = defaultAddress;
        if (this.podState != null)
            packetAddress = this.podState.Address;
        if (addressOverride != null)
            packetAddress = addressOverride;

        Boolean firstPacket = true;
        byte[] encodedMessage = message.getEncoded();
        String myString =  ByteUtil.shortHexString(encodedMessage);

        OmnipodPacket response = null;
        while(encodedMessage.length > 0) {
            PacketType packetType = firstPacket? PacketType.Pdm : PacketType.Con;
            OmnipodPacket packet = new OmnipodPacket(packetAddress, packetType,packetNumber, encodedMessage);
            byte[] encodedMessageInPacket = packet.getEncodedMessage();
            //getting the data remaining to be sent
            encodedMessage = ByteUtil.substring(encodedMessage, encodedMessageInPacket.length, encodedMessage.length - encodedMessageInPacket.length);
            firstPacket = false;
            response = exchangePackets(packet);
            //We actually ignore (ack) responses if it is not last packet to send
        }
        if (response == null) {
            LOG.debug("Timeout on receive");
            //FIXME: Log timeout
            return null;
        }


        if (response.getPacketType() == PacketType.Ack) {
            LOG.warn("Received ack instead of real response");
            //FIXME: we received ack instead of real response, something is wrong
            incrementPacketNumber(1);
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
                    LOG.debug("Received a non-con packet type:", conPacket.getPacketType());
                    //FIXME: We should throw an error as we expect only continuation packets
                    return null;
                }
                receivedMessageData = ByteUtil.concat(receivedMessageData, conPacket.getEncodedMessage());
            }
        }
        incrementMessageNumber(2);

        ackUntilQuiet(packetAddress, ackAddressOverride);

        MessageBlock[] messageBlocks = receivedMessage.getMessageBlocks();
        if (messageBlocks.length == 0) {
            LOG.debug("Not enough data");
            //FIXME: We should throw an error of not enough data
            return null;
        }

        MessageBlock block = messageBlocks[0];
        if (block.getType() == MessageBlockType.ErrorResponse) {
            ErrorResponse error = (ErrorResponse)block;
            if (error.getErrorResponseType() == ErrorResponseType.BadNonce) {
                LOG.debug("Nonce out-of-sync");
                //FIXME: Log that we have nonce out-of-sync
                if (podState != null) {
                    this.podState.ResyncNonce(error.getNonceSearchKey(), this.podState.getCurrentNonce(), message.getSequenceNumber());
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
        int pktAddress = defaultAddress;
        int msgAddress = defaultAddress;
        if (this.podState != null) {
            pktAddress = msgAddress = podState.Address;
        }
        if (packetAddress != null)
            pktAddress = packetAddress;
        if (messageAddress != null)
            msgAddress = messageAddress;
        return new OmnipodPacket(pktAddress, PacketType.Ack, packetNumber, ByteUtil.getBytesFromInt(msgAddress));

    }

    private void ackUntilQuiet(Integer packetAddress, Integer messageAddress) {
        OmnipodPacket ack = makeAckPacket(packetAddress, messageAddress);
        Boolean quiet = false;
        while(!quiet) {
            OmnipodPacket response = sendAndListen(ack, 600, 5, 40, OmnipodPacket.class);
            //FIXME: instead of this crappy core we should make a proper timeout handling (exception-based?)
            if (response == null || (!response.isValid() && response.getPacketType() == PacketType.Invalid))
                quiet = true;
        }
        incrementPacketNumber(1);
    }



    private OmnipodPacket exchangePackets(OmnipodPacket packet) {
        return exchangePackets(
                packet
                , 0
                , 250
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
                , 250
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

    public <T extends MessageBlock> T sendCommand(MessageBlock command) {
        int msgAddress = defaultAddress;
        if (this.podState != null) {
            msgAddress = podState.Address;
        }
        OmnipodMessage message = new OmnipodMessage(msgAddress, new MessageBlock[]{command}, messageNumber);
        return exchangeMessages(message);

    }

    private int nonceValue() {
        if (this.podState == null)
            //FIXME: we should have a set of application-level meaningfull exceptions
            throw new IllegalArgumentException("Getting nonce without active pod");
        return podState.getCurrentNonce();
    }
    private void advanceToNextNonce() {
        if (this.podState == null)
            //FIXME: we should have a set of application-level meaningfull exceptions
            throw new IllegalArgumentException("Getting nonce without active pod");
        podState.AdvanceToNextNonce();

    }


    public Object initializePod() {

        if (SP.contains(OmniPodConst.Prefs.PodState)) {
            //FIXME: We should ask "are you sure?"
            SP.remove(OmniPodConst.Prefs.PodState);
        }

        rfspy.setTestingFunction("initializePod");

        Random rnd = new Random();
        int newAddress = rnd.nextInt();
        this.packetNumber = 0x0A;
        this.messageNumber = 0;
        newAddress = 0x1f05e70b;
        //LOG.debug("New address: " + ByteUtil.shortHexString());
        newAddress = (newAddress & 0x001fffff) | 0x1f000000;
        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(
                defaultAddress
                , new MessageBlock[] {assignAddress}
                , messageNumber);
        ConfigResponse config = exchangeMessages(assignAddressMessage, defaultAddress, newAddress);
        if (config == null) {
            LOG.debug("config is null (timeout or wrong answer)");
            //FIXME: show communication timeout
            return null;
        }


        //DateTime activationDate = DateTime.now();
        DateTime activationDate = new DateTime(
                2018,
                8,
                18,
                20,
                5);

        //at this point for an unknown reason PDM starts counting messages from 0 again
        messageNumber = 0;
        ConfirmPairingCommand confirmPairing = new ConfirmPairingCommand(
                newAddress
                , activationDate
                , config.lot
                , config.tid);
        OmnipodMessage confirmPairingMessage = new OmnipodMessage(
                defaultAddress
                , new MessageBlock[]{confirmPairing}
                , messageNumber);
        ConfigResponse config2 = exchangeMessages(confirmPairingMessage, defaultAddress, newAddress);
        if (config == null) {
            LOG.debug("Second command timeout, that's bad");
            return null;
        }

        if (config2.podLifeStage != PodLifeStage.Paired) {
            //FIXME: Log invalid data (we should have received a paired-state response
            LOG.error("Invalid pairing state");
            return null;
        }

        this.podState = new PodState(
                newAddress
                , activationDate
                , config2.piVersion
                , config2.pmVersion
                , config2.lot
                , config2.tid);

        AlertConfiguration lweReservoir = new AlertConfiguration(
                AlertType.LowReservoir,
                true,
                false,
                0,
                new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.Reservoir, 50), //50 to match the capture
                0x0102
                );
        int nonce = nonceValue();
        ConfigureAlertsCommand lowReservoirCommand = new ConfigureAlertsCommand(
                nonce,
                new AlertConfiguration[]{lweReservoir});
        StatusResponse status = sendCommand(lowReservoirCommand);
        advanceToNextNonce();

        AlertConfiguration insertionTimer = new AlertConfiguration(
                AlertType.TimerLimit,
                true,
                false,
                55,
                new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.Timer, new Duration(5 * 60 * 1000)), //1 hour // 5 minutes to match the capture
                0x0802
        );
        ConfigureAlertsCommand insertionTimerCommand = new ConfigureAlertsCommand(nonceValue(), new AlertConfiguration[]{insertionTimer});
        status = sendCommand(insertionTimerCommand);
        advanceToNextNonce();

        double primeUnits = 2.6;
        Bolus primeBolus = new Bolus(primeUnits, 8);
        SetInsulinScheduleCommand primeCommand = new SetInsulinScheduleCommand(nonceValue(), primeBolus);
        BolusExtraCommand extraBolusCommand = new BolusExtraCommand(primeUnits, (byte) 0, ByteUtil.fromHexString("000186a0"));
        OmnipodMessage prime = new OmnipodMessage(newAddress, new MessageBlock[]{primeCommand, extraBolusCommand}, messageNumber);
        status = exchangeMessages(prime);


        Gson gson = new Gson();
        String s = gson.toJson(podState);
        SP.putString(OmniPodConst.Prefs.PodState, s);

        //FIXME: should we return something like "OK"?
        return "OK";
    }

    public Object finishPrime() {
        rfspy.setTestingFunction("finishPrime");

        if (this.podState == null) {
            String serialized = "{\"ActivatedAt\":{\"iChronology\":{\"iBase\":{\"iMinDaysInFirstWeek\":4}},\"iMillis\":1534622700000},\"Address\":520480523,\"Lot\":43687,\"PiVersion\":{\"major\":2,\"minor\":7,\"patch\":0},\"PmVersion\":{\"major\":2,\"minor\":7,\"patch\":0},\"Tid\":630145,\"nonceState\":{\"index\":1,\"table\":[1159728387,1369320680,1799221675,-1397172685,1859143840,497915028,-1194513883,1557972065,1353375686,-736718945,541733452,-833859995,1587621096,-543494224,426786215,-1655021558,-1174888418,-1772703871,0,0,0]}}";
            Gson gson = new Gson();
            this.podState = gson.fromJson(serialized, PodState.class);
        }

        //Fere goes new alarm settings and basal schedule set




        return "OK";
    }


}
