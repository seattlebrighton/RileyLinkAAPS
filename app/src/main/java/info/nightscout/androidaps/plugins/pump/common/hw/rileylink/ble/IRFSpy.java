package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.CC111XRegister;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public interface IRFSpy {
    RileyLinkFirmwareVersion getRLVersionCached();

    String getBLEVersionCached();

    // Call this after the RL services are discovered.
    // Starts an async task to read when data is available
    void startReader();

    //Here should go generic RL initialisation + protocol adjustments depending on
    //firmware version
    void initializeRileyLink();

    // Call this from the "response count" notification handler.
    void newDataIsAvailable();

    // This gets the version from the BLE113, not from the CC1110.
    // I.e., this gets the version from the BLE interface, not from the radio.
    String getVersion();

    RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount);

    //FIXME: to be able to work with Omnipod we need to support preamble extensions so we should create a class for the SnedAndListen RL command
    //To avoid snedAndListen command assembly magic
    RFSpyResponse transmitThenReceive(
            RadioPacket pkt
            , byte sendChannel
            , byte repeatCount
            , byte delay_ms
            , byte listenChannel
            , int timeout_ms
            , byte retryCount
            , int extendPreamble_ms);

    RFSpyResponse updateRegister(CC111XRegister reg, int val);

    void setBaseFrequency(double freqMHz);

    void setTestingFunction(String functionName);
}
