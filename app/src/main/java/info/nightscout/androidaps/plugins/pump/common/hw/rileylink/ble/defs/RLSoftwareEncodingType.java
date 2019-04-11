package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs;

public enum RLSoftwareEncodingType {
    None(0x00),
    Manchester(0x01),
    FourBSixB(0x02),;
    public byte value;

    RLSoftwareEncodingType(int value) {
        this.value = (byte) value;
    }
}
