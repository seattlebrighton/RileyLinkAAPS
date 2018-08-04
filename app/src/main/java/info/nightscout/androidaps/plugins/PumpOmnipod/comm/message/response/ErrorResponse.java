package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class ErrorResponse extends MessageBlock {
    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ErrorResponse;
    }
    private ErrorResponseType errorResponseType;
    private int nonceSearchKey;

    public ErrorResponseType getErrorResponseType() {
        return errorResponseType;
    }
    public int getNonceSearchKey() {
        return nonceSearchKey;
    }
}
