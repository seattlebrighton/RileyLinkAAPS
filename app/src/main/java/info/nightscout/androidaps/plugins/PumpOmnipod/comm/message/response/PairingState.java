package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message.response;

public enum PairingState  {
    Sleeping(0),
    ReadyToPair(1),
    AddressAssigned(2),
    Paired(3),
    PairingExpired(14)
    ;



    byte value;

    PairingState(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static PairingState fromByte(byte input) {
        for (PairingState type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }

}
