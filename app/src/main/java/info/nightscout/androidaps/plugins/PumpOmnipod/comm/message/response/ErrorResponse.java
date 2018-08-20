package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class ErrorResponse extends MessageBlock {
    public ErrorResponse(byte[] encodedData) {
        super(encodedData);
        this.errorResponseType = ErrorResponseType.fromByte(encodedData[2]);
        this.nonceSearchKey = ByteUtil.toInt(
                new Integer(encodedData[3])
                , new Integer(encodedData[4])
                , new Integer(encodedData[5])
                , new Integer(encodedData[6])
                , ByteUtil.BitConversion.BIG_ENDIAN);
        int length = encodedData[1] + 2;
        this.encodedData = ByteUtil.substring(encodedData, 1, length - 1);

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
