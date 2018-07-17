package info.nightscout.androidaps.plugins.PumpOmnipod.comm.command;

import java.nio.ByteBuffer;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class AssignAddressCommand extends MessageBlock {

    int _address;
    public int getAddress() {
        return _address;
    };

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.AssignAddress;
    }

    public AssignAddressCommand(int address) {
        _address = 0x1f000000 | (address & 0x000fffff);
        rawData = ByteBuffer.allocate(4).putInt(_address).array();

    }


}
