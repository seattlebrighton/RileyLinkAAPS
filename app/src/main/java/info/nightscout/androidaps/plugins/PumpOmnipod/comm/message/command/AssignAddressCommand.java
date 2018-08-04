package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import java.nio.ByteBuffer;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class AssignAddressCommand extends MessageBlock {

    int address;
    public int getAddress() {
        return address;
    };

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.AssignAddress;
    }

    public AssignAddressCommand(int address) {
        this.address = 0x1f000000 | (address & 0x000fffff);
        rawData = ByteBuffer.allocate(4).putInt(this.address).array();

    }


}
