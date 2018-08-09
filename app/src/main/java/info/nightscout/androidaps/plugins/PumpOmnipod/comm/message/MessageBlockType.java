package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

public enum MessageBlockType {
    ConfigResponse(0x01),
    StatusError(0x02),
    ConfirmPairing(0x03),
    ErrorResponse(0x06),
    AssignAddress(0x07),
    GetStatus(0x0e),
    BasalScheduleExtra(0x13),
    TempBasalExtra(0x16),
    BolusExtra(0x17),
    ConfigureAlerts(0x19),
    SetInsulinSchedule(0x1a),
    DeactivatePod(0x1c),
    StatusResponse(0x1d),
    CancelDelivery(0x1f);

    byte value;

    MessageBlockType(int value) {
        this.value = (byte) value;
    }

    public static MessageBlockType fromByte(byte input) {
        for (MessageBlockType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }

    public byte getValue() {
        return value;
    }
}
