package info.nightscout.androidaps.plugins.pump.omnipod.defs;

public enum ReservoirStatus {
    Initialized (0),
    TankPowerActivated (1),
    TankFillCompleted (2),
    PairingSuccess (3),
    Priming ( 4),
    ReadyForInjection ( 5),
    InjectionStarted ( 6),
    InjectionDone ( 7),
    AboveFiftyUnits ( 8),
    BelowFiftyUnits ( 9),
    ErrorEventLoggedShuttingDown (13),
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
