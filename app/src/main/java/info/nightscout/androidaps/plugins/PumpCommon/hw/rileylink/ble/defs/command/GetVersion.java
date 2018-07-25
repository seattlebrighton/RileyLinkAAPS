package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.command;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class GetVersion extends RileyLinkCommand {
    public GetVersion(RileyLinkFirmwareVersion version) {
        super(version);
        this.type = RileyLinkCommandType.GetVersion;
    }

    @Override
    public byte[] getRaw() {
        return super.getRawSimple();
    }
}
