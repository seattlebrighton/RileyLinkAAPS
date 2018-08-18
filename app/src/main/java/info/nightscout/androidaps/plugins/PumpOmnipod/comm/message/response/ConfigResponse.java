package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class ConfigResponse extends MessageBlock {

    PairingState pairingState;
    FirmwareVersion pmVersion;
    FirmwareVersion piVersion;
    int lot;
    int tid;
    int address;




    public ConfigResponse(byte[] rawData) {
        super(rawData);

        int length = rawData[1] + 2;
        switch (length) {
            case 0x17:
                initializeMembers(2, rawData);
                break;
            case 0x1D:
                initializeMembers(9, rawData);
                break;
            default:
                return;
        }
        this.rawData = ByteUtil.substring(rawData, 1, length - 1);

    }

    private void initializeMembers(int startOffset, byte[] data) {
        this.pairingState = PairingState.fromByte(data[startOffset + 7]);
        this.pmVersion = new FirmwareVersion(data[startOffset + 0], data[startOffset + 1], data[startOffset + 2]);
        this.piVersion = new FirmwareVersion(data[startOffset + 3], data[startOffset + 4], data[startOffset + 5]);
        this.lot = ByteUtil.toInt(
                new Integer(data[startOffset + 8])
                , new Integer(data[startOffset + 9])
                , new Integer(data[startOffset + 10])
                , new Integer(data[startOffset + 11])
                , ByteUtil.BitConversion.BIG_ENDIAN);
        this.tid = ByteUtil.toInt(
                new Integer(data[startOffset + 12])
                , new Integer(data[startOffset + 13])
                , new Integer(data[startOffset + 14])
                , new Integer(data[startOffset + 15])
                , ByteUtil.BitConversion.BIG_ENDIAN);
        this.address = ByteUtil.toInt(
                new Integer(data[startOffset + 17])
                , new Integer(data[startOffset + 18])
                , new Integer(data[startOffset + 19])
                , new Integer(data[startOffset + 20])
                , ByteUtil.BitConversion.BIG_ENDIAN);

    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ConfigResponse;
    }

}
