package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import java.util.List;

import info.nightscout.androidaps.plugins.PumpOmnipod.comm.command.MessageBlock;

public class OmnipodMessage {

    private int address;
    private MessageBlock[] messageBlocks;
    private int sequenceNumber;



    public OmnipodMessage(int address, MessageBlock[] messageBlocks, int sequenceNumber) {

        this.address = address;
        this.messageBlocks = messageBlocks;
        this.sequenceNumber = sequenceNumber;
    }
}
