package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.command;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLSoftwareEncodingType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class SetSoftwareEncoding extends RileyLinkCommand {
    private final RLSoftwareEncodingType encoding;

    public SetSoftwareEncoding(RileyLinkFirmwareVersion version, RLSoftwareEncodingType encoding) {
        super(version);
        this.type = RileyLinkCommandType.SetSWEncoding;
        this.encoding = encoding;
    }

    @Override
    public byte[] getRaw() {
        return getByteArray(type.code, encoding.value);
    }
}
