package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

public enum PodLifeStage {
    Sleeping(0),
    ReadyToPair(1),
    AddressAssigned(2),
    Paired(3),
    Purging(4),
    ReadyForInjection(5),
    InjectionDone(6),
    PrimingCanula(7),
    Running(8),
    RunningLeffThan50u(9),
    ErrorLoggedShuttingDown(0x0D),
    PairingExpired(0x0E),
    PodInactivated(0x0F)
    ;



    byte value;

    PodLifeStage(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static PodLifeStage fromByte(byte input) {
        for (PodLifeStage type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }

}
