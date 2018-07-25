package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.command;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public abstract class RileyLinkCommand {

    protected RileyLinkFirmwareVersion version;

    public RileyLinkCommand(RileyLinkFirmwareVersion version) {
        this.version = version;
    }

    protected RileyLinkCommandType type;

    public abstract byte[] getRaw();

    protected byte[] getRawSimple() {
        return getByteArray(type.code);

    }
    public RileyLinkCommandType getType() {
        return type;
    }

    protected byte[] getByteArray(byte... input) {
        return input;
    }


}
