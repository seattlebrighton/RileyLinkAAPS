package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.RFTools;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLSoftwareEncodingType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.CRC;

/**
 * Created by geoff on 5/22/16.
 */

public class RadioPacket {
    private final RileyLinkFirmwareVersion version;
    protected byte[] pkt;
    protected RLSoftwareEncodingType encoding;


    public RadioPacket(byte[] pkt, RLSoftwareEncodingType encoding, RileyLinkFirmwareVersion version) {
        this.pkt = pkt;
        this.encoding = encoding;
        this.version = version;
    }


    public byte[] getRaw() {
        return pkt;
    }

    public byte[] getWithCRC() {
        byte[] withCRC = ByteUtil.concat(pkt, CRC.crc8(pkt));
        return withCRC;
    }

    public byte[] getEncoded() {
        byte[] withCRC = ByteUtil.concat(pkt, CRC.crc8(pkt));
        byte[] encoded;
        switch (encoding) {
            case Manchester://We have this encoding in RL firmware
                encoded = withCRC;
                break;
            case FourBSixB:
                encoded = RFTools.encode4b6b(withCRC);
                break;
            default:
                throw new NotImplementedException(("Encoding not supported: " + encoding.toString() ));
        }
        // Starting with 2.0 we don't put ending 0
        if (version.getMajor() >= 2)
            return encoded;

        byte[] withNullTerm = ByteUtil.concat(encoded, (byte) 0);
        return withNullTerm;
    }

}
