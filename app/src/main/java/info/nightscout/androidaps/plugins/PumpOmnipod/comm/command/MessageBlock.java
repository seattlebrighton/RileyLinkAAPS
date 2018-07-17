package info.nightscout.androidaps.plugins.PumpOmnipod.comm.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public abstract class MessageBlock {
    abstract public  MessageBlockType getType();
    protected byte[] rawData;

    //This method returns raw message representation
    //It should be rewritten in a derived class if raw representation of a concrete message
    //is something else than just message type concatenated with message data
    public byte[] getRawMessage() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(this.getType().getValue());
        stream.write(rawData);
        return stream.toByteArray();
    }
}
