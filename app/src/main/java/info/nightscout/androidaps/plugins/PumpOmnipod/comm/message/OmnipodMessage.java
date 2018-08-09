package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.CRC;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.command.MessageBlock;

public class OmnipodMessage {

    private int address;
    private MessageBlock[] messageBlocks;
    private int sequenceNumber;


    public OmnipodMessage(int address, MessageBlock[] messageBlocks, int sequenceNumber) {

        this.address = address;
        this.messageBlocks = messageBlocks;
        this.sequenceNumber = sequenceNumber;
    }

    public static OmnipodMessage TryDecode(byte[] data) {
        return null;
    }

    public byte[] getEncoded() {
        byte[] rawData = new byte[0];
        for (int i = 0; i < messageBlocks.length; i++) {
            ByteUtil.concat(rawData, messageBlocks[i].getRawData());
        }

        //right before the message bloxks we have 6 bits of seqNum and 10 bits of length
        byte header = (byte) (((sequenceNumber & 0x1F) << 2) + ((rawData.length >> 8) & 0x03));
        rawData = ByteUtil.concat(header, rawData);
        int crc = CRC.crc16(rawData);
        rawData = ByteUtil.concat(rawData, ByteUtil.highByte((short) crc));
        rawData = ByteUtil.concat(rawData, ByteUtil.lowByte((short) crc));
        return rawData;
    }
}
