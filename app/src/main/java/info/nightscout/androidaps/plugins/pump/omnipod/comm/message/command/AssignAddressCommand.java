package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import java.nio.ByteBuffer;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class AssignAddressCommand extends MessageBlock {

    //Documentation is here: https://github.com/openaps/openomni/wiki/Command-07-Assign-ID

    int address;

    public int getAddress() {
        return address;
    };

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.AssignAddress;
    }

    public AssignAddressCommand(int address) {
        super(null);
        this.address = address;
        encodedData = ByteBuffer.allocate(4).putInt(this.address).array();

    }


}
