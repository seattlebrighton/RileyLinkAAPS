package info.nightscout.androidaps.plugins.PumpOmnipod.defs;

import info.nightscout.androidaps.plugins.PumpMedtronic.comm.message.MessageBody;

/**
 * Created by andy on 6/1/18.
 */

public enum OmnipodCommandType {

    Pairing(0x07), //
    GetStatus(0x0E), //
    CancelBasal(0x1A), //
    InsulinSchedule(0x1A), //
    InvalidCommand(0x00), //
    ;


    private byte commandCode;

    OmnipodCommandType(int commandCode) {

        this.commandCode = (byte) commandCode;
    }

    public static OmnipodCommandType getByCode(byte rxDatum) {
        return null;
    }

    // TODO
    public static MessageBody constructMessageBody(OmnipodCommandType commandType, byte[] substring) {
        return null;
    }

    public byte getCommandCode() {
        return commandCode;
    }
}
