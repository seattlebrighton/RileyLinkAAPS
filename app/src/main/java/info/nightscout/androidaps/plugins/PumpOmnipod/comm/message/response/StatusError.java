package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class StatusError extends MessageBlock {
    public StatusError(byte[] encodedData) {
        super(encodedData);
        throw new NotImplementedException("StatusError");
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.StatusError;
    }
}
