package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.command;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLSoftwareEncodingType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class SetSoftwareEncoding extends RileyLinkCommand {
    private final RLSoftwareEncodingType encoding;

    public SetSoftwareEncoding(RileyLinkFirmwareVersion version, RLSoftwareEncodingType encoding) {
        super(version);
        this.encoding = encoding;
    }

    @Override
    public RileyLinkCommandType getCommandType() {
        return RileyLinkCommandType.SetSWEncoding;
    }

    @Override
    public byte[] getRaw() {
        return getByteArray(getCommandType().code, encoding.value);
    }
}
