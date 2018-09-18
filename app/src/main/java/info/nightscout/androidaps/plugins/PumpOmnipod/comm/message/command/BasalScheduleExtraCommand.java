package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class BasalScheduleExtraCommand extends MessageBlock {
    public BasalScheduleExtraCommand(byte[] encodedData) {
        super(encodedData);
    }

    @Override
    public MessageBlockType getType() {
        return null;
    }
}
