package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.command;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public abstract class RileyLinkCommand {

    protected RileyLinkFirmwareVersion _version;

    public RileyLinkCommand(RileyLinkFirmwareVersion version) {
        this._version = version;

    }
    public abstract byte[] getRaw();
}
