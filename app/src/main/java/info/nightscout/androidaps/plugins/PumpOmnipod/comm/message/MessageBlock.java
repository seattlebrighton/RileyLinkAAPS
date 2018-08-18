package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.MessageBlockType;

public abstract class MessageBlock {
    protected byte[] rawData;

    public MessageBlock(byte[] rawData) {

    }
    public abstract MessageBlockType getType();

    //This method returns raw message representation
    //It should be rewritten in a derived class if raw representation of a concrete message
    //is something else than just message type concatenated with message data
    public byte[] getRawData() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            stream.write(this.getType().getValue());
            stream.write((byte)rawData.length);
            stream.write(rawData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return stream.toByteArray();
    }
}
