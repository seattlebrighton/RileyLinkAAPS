package info.nightscout.androidaps.plugins.PumpOmnipod.comm.command;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class ConfigResponse extends MessageBlock {
    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ConfigResponse;
    }

}
