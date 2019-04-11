package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.command;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class SetSoftwareEncoding extends RileyLinkCommand {
    private final RileyLinkEncodingType encoding;

    public SetSoftwareEncoding(RileyLinkFirmwareVersion version, RileyLinkEncodingType encoding) {
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
