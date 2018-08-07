package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import org.apache.commons.lang3.NotImplementedException;

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
                this.pairingState = PairingState.fromByte(rawData[9]);
                this.pmVersion = new FirmwareVersion(rawData[2], rawData[3], rawData[4]);
                this.piVersion = new FirmwareVersion(rawData[5], rawData[6], rawData[7]);
                this.lot = ByteUtil.toInt(
                        new Integer(rawData[10])
                        , new Integer(rawData[11])
                        , new Integer(rawData[12])
                        , new Integer(rawData[13])
                        , ByteUtil.BitConversion.BIG_ENDIAN);
                this.tid = ByteUtil.toInt(
                        new Integer(rawData[14])
                        , new Integer(rawData[15])
                        , new Integer(rawData[16])
                        , new Integer(rawData[17])
                        , ByteUtil.BitConversion.BIG_ENDIAN);
                this.address = ByteUtil.toInt(
                        new Integer(rawData[19])
                        , new Integer(rawData[20])
                        , new Integer(rawData[21])
                        , new Integer(rawData[22])
                        , ByteUtil.BitConversion.BIG_ENDIAN);

                break;
            case 0x1D:
                break;
        }
        this.rawData = ByteUtil.substring(rawData, 1, length - 1);
    }
    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ConfigResponse;
    }

}
