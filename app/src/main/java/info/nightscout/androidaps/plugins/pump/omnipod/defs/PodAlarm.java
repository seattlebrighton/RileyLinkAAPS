package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import java.util.EnumSet;

public class PodAlarm {
    public enum PodAlarmType {

        PodExpired      ( 0b10000000),
        SuspendExpired  ( 0b01000000),
        Suspended       ( 0b00100000),
        BelowFiftyUnits ( 0b00010000),
        OneHourExpiry   ( 0b00001000),
        PodDeactivated  ( 0b00000100),
        UnknownBit2     ( 0b00000010),
        UnknownBit1     ( 0b00000001);

        public final byte value;

        PodAlarmType(int flag) {
            this.value = (byte) flag;
        }
    }

    private final byte value;
    private final EnumSet<PodAlarmType> flags = EnumSet.noneOf(PodAlarmType.class);

    public PodAlarm(byte value) {
        this.value = value;
        for(PodAlarmType a: PodAlarmType.values()) {
            if ((value & a.value) > 0) {
                flags.add(a);
            }
        }
    }

    public byte getAsByte() {
        return this.value;
    }

    public EnumSet<PodAlarmType> getFlags() {
        return flags;
    }
}
