package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.command;

import java.nio.ByteBuffer;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public class AssignAddressCommand extends MessageBlock {

    int address;

    public AssignAddressCommand(byte[] encodedData) {
        super(encodedData);
    }

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
