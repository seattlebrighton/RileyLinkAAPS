package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class ErrorResponse extends MessageBlock {
    public ErrorResponse(byte[] rawData) {
        super(rawData);
        this.errorResponseType = ErrorResponseType.fromByte(rawData[2]);
        this.nonceSearchKey = ByteUtil.toInt(
                new Integer(rawData[3])
                , new Integer(rawData[4])
                , new Integer(rawData[5])
                , new Integer(rawData[6])
                , ByteUtil.BitConversion.BIG_ENDIAN);
        int length = rawData[1] + 2;
        this.rawData = ByteUtil.substring(rawData, 1, length - 1);

    }

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
