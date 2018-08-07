package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class StatusResponse extends MessageBlock {
    public StatusResponse(byte[] rawData) {
        super(rawData);
        throw new NotImplementedException("StatusResponse");
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.StatusResponse;
    }
}
