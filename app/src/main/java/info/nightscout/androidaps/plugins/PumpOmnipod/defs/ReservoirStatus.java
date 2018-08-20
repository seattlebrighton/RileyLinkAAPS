package info.nightscout.androidaps.plugins.PumpOmnipod.defs;

public enum ReservoirStatus {
    PairingSuccess (3),
    Priming ( 4),
    ReadyForInjection ( 5),
    InjectionStarted ( 6),
    InjectionDone ( 7),
    AboveFiftyUnits ( 8),
    BelowFiftyUnits ( 9),
    DelayedPrime ( 14 ),
    Inactive ( 15);



    byte value;

    ReservoirStatus(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static ReservoirStatus fromByte(byte input) {
        for (ReservoirStatus type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
