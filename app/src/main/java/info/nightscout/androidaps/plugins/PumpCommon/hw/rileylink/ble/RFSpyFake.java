package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.CC111XRegister;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class RFSpyFake implements IRFSpy {



    @Override
    public RileyLinkFirmwareVersion getRLVersionCached() {
        return RileyLinkFirmwareVersion.Version_2_2;
    }

    @Override
    public String getBLEVersionCached() {
        return "1.3";
    }

    @Override
    public void startReader() {

    }

    @Override
    public void initializeRileyLink() {

    }

    @Override
    public void newDataIsAvailable() {

    }

    @Override
    public String getVersion() {
        return "subg_rfspy 2.2";
    }

    @Override
    public RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount) {
        return null;
    }

    @Override
    public RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount, int extendPreamble_ms) {
        return null;
    }

    @Override
    public RFSpyResponse updateRegister(CC111XRegister reg, int val) {
        return new RFSpyResponse(new byte[] {(byte) 0xDD});
    }

    @Override
    public void setBaseFrequency(double freqMHz) {

    }
}
