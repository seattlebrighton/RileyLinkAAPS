package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class DeactivatePodCommand extends MessageBlock {
    private int nonce;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.DeactivatePod;
    }

    public DeactivatePodCommand( int nonce ) {
        super(null);
        this.nonce = nonce;
        encode();
    }
    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
    }

}
