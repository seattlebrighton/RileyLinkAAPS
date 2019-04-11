package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFTools;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.common.utils.CRC;

/**
 * Created by geoff on 5/22/16.
 */

public class RadioPacket {
    private final RileyLinkFirmwareVersion version;
    protected byte[] pkt;

    public RadioPacket(byte[] pkt, RileyLinkFirmwareVersion version) {
        this.pkt = pkt;
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
        byte[] encoded;
        switch (RileyLinkUtil.getEncoding()) {
            case Manchester://We have this encoding in RL firmware
                encoded = pkt;
                break;
            case FourByteSixByte:
                byte[] withCRC = ByteUtil.concat(pkt, CRC.crc8(pkt));
                encoded = RFTools.encode4b6b(withCRC);
                break;
            default:
                throw new NotImplementedException(("Encoding not supported: " + RileyLinkUtil.getEncoding().toString()));
        }
        // Starting with 2.0 we don't put ending 0
        if (version.isSameVersion(RileyLinkFirmwareVersion.Version2AndHigher))
            return encoded;

        byte[] withNullTerm = ByteUtil.concat(encoded, (byte) 0);
        return withNullTerm;
    }

}
