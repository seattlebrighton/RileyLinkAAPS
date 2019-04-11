package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.omnipod.defs.StatusType;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class GetStatusCommand extends MessageBlock {
    private final StatusType statusType;

    public GetStatusCommand(StatusType statusType) {
        super(null);
        this.statusType = statusType;
        encode();
    }

    private void encode() {
        encodedData = new byte[] {statusType.getValue()};
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.GetStatus;
    }
}
