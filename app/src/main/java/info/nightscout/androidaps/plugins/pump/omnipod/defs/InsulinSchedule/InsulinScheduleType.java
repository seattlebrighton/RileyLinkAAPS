package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

public enum InsulinScheduleType {
    BasalSchedule(0),
    TempBasalSchedule(1),
    Bolus(2);

    byte value;

    InsulinScheduleType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static InsulinScheduleType fromByte(byte input) {
        for (InsulinScheduleType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
