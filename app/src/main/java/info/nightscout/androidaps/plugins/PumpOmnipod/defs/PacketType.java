package info.nightscout.androidaps.plugins.PumpOmnipod.defs;

public enum PacketType {
    Invalid(0),
    Pod(0b111),
    Pdm(0b101),
    Con(0b100),
    Ack(0b010);

    byte value;

    PacketType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static PacketType fromByte(byte input) {
        for (PacketType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }

}
