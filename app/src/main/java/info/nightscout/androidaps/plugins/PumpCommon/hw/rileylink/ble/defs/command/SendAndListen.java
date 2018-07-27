package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.command;

import org.apache.commons.lang3.NotImplementedException;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class SendAndListen extends RileyLinkCommand {

    private byte sendChannge;
    private byte repeatCount;
    private int delayBetweenPackets_ms;
    private byte listenChannel;
    private int timeout_ms;
    private byte retryCount;
    private int preambleExtension_ms;
    private RadioPacket packetToSend;


    public SendAndListen(
            RileyLinkFirmwareVersion version
            , byte sendChannge
            , byte repeatCount
            , byte delayBetweenPackets_ms
            , byte listenChannel
            , int timeout_ms
            , byte retryCount
            , RadioPacket packetToSend

    ) {
        this(
                version
                , sendChannge
                , repeatCount
                , delayBetweenPackets_ms
                , listenChannel
                , timeout_ms
                , retryCount
                , 0
                , packetToSend
        );
    }

    public SendAndListen(
            RileyLinkFirmwareVersion version
            , byte sendChannge
            , byte repeatCount
            , int delayBetweenPackets_ms
            , byte listenChannel
            , int timeout_ms
            , byte retryCount
            , int preambleExtension_ms
            , RadioPacket packetToSend

    ) {
        super(version);
        this.type = RileyLinkCommandType.SendAndListen;
        this.sendChannge = sendChannge;
        this.repeatCount = repeatCount;
        this.delayBetweenPackets_ms = delayBetweenPackets_ms;
        this.listenChannel = listenChannel;
        this.timeout_ms = timeout_ms;
        this.retryCount = retryCount;
        this.preambleExtension_ms = preambleExtension_ms;
        this.packetToSend = packetToSend;
    }

    @Override
    public byte[] getRaw() {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        bytes.add(this.type.code);
        bytes.add(this.sendChannge);
        bytes.add(this.retryCount);
        if (this.version.getMajor() >= 2) { //delay is unsigned 16-bit integer
            byte[] delayBuff = ByteBuffer.allocate(4).putInt(delayBetweenPackets_ms).array();
            bytes.add(delayBuff[2]);
            bytes.add(delayBuff[3]);
        } else {
            bytes.add((byte)delayBetweenPackets_ms);
        }
        bytes.add(this.listenChannel);
        byte[] timeoutBuff = ByteBuffer.allocate(4).putInt(timeout_ms).array();
        bytes.add(timeoutBuff[2]);
        bytes.add(timeoutBuff[3]);
        bytes.add(retryCount);
        if (this.version.getMajor() >= 2) { //2.x (and probably higher versions) support preamble extension
            byte[] preambleBuf = ByteBuffer.allocate(4).putInt(preambleExtension_ms).array();
            bytes.add(preambleBuf[2], preambleBuf[3]);
        }

        byte[] rawBytesToSend = packetToSend.getEncoded();
        for(int i = 0; i < rawBytesToSend.length; i++) {
            bytes.add(rawBytesToSend[i]);
        }

        byte[] output = new byte[bytes.size()];
        for (int i = 1; i < bytes.size(); i++) {
            output[i] = bytes.get(i);
        }

        return output;

    }
}
