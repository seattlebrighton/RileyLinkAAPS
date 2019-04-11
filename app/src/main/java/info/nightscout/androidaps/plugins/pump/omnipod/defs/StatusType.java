package info.nightscout.androidaps.plugins.pump.omnipod.defs;

public enum StatusType {
    Normal ((byte)0x00),
    ConfiguredAlerts ((byte)0x01),
    FaultEvents ((byte)0x02),
    DataLog ((byte)0x03),
    FaultDataInitializationTime ((byte)0x04),
    HardcodedValues ((byte)0x06),
    ResetStatus ((byte)0x46), // including state, initialization time, any faults
    DumpRecentFlashLog ((byte)0x50),
    DumpOlderFlashlog  ((byte)0x51), // but dumps entries before the last 50
    ;
    // https://github.com/openaps/openomni/wiki/Command-0E-Status-Request

    private final byte value;
    StatusType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}