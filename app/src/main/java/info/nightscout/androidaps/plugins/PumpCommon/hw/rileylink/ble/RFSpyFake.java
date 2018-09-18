package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble;

import android.support.annotation.Nullable;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.CC111XRegister;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;

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

    private int commsCount = 0;

    @Override
    public RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount) {
        return transmitThenReceive(pkt, sendChannel, repeatCount, delay_ms, listenChannel, timeout_ms, retryCount, 0);

    }

    @Override
    public RFSpyResponse transmitThenReceive(RadioPacket pkt, byte sendChannel, byte repeatCount, byte delay_ms, byte listenChannel, int timeout_ms, byte retryCount, int extendPreamble_ms) {
        byte[] bytesToSend = pkt.getRaw();
        if (testingFunction == "initializePod") {
            return testInitializePod(bytesToSend);
        }
        if (testingFunction == "finishPrime") {
            return testFinishPrime(bytesToSend);
        }
        return null;

    }

    @Nullable
    private RFSpyResponse testFinishPrime(byte[] bytesToSend) {
        String myString = ByteUtil.shortHexString(bytesToSend).replace(" ", "");
        byte[] setAlertConfig1Request = ByteUtil.fromHexString("1f05e70bbd1f05e70b200c190a1dad9494380010a203020045b1");
        byte[] setAlertConfig1Response = ByteUtil.fromHexString("0000001f05e70bfe1f05e70b240a1d050015c00000000bff82840b");
        switch(commsCount++) {
            case 0:
                if (ByteUtil.compare(bytesToSend, setAlertConfig1Request) == 0)
                    return new RFSpyResponse(setAlertConfig1Response);
                else
                    return null;
            case 1:
                return new RFSpyResponse(new byte[] {(byte) 0xAA});
            default:
                return null;
        }
    }

    @Nullable
    private RFSpyResponse testInitializePod(byte[] bytesToSend) {
        String myString = ByteUtil.shortHexString(bytesToSend).replace(" ", "");
        byte[] assignAddressRequest = ByteUtil.fromHexString("ffffffffaaffffffff000607041f05e70b834ce2");
        byte[] assignAddressResponse = ByteUtil.fromHexString("000000ffffffffebffffffff0417011502070002070002020000aaa700099d818e1f05e70b8321a6");
        byte[] confirmPairingRequest = ByteUtil.fromHexString("ffffffffadffffffff001503131f05e70b140408121214050000aaa700099d81016802");
        byte[] confirmPairingResponse1 = ByteUtil.fromHexString("000000ffffffffeeffffffff041d011b13881008340a5002070002070002030000aaa700099d8120");
        byte[] ackForConfirmPairingResponse1 = ByteUtil.fromHexString("FFFFFFFF4F1F05E70BAB");
        byte[] conForConfirmPairingResponse1 = ByteUtil.fromHexString("000000ffffffff901f05e70b035dc1");
        byte[] setAlertConfig1Request = ByteUtil.fromHexString("1f05e70bb21f05e70b080c190a9e5759aa4c0001f40102023c39");
        byte[] setAlertConfig1Response = ByteUtil.fromHexString("0000001f05e70bf31f05e70b0c0a1d0300001000000007ff026717");
        byte[] setAlertConfig2Request = ByteUtil.fromHexString("1f05e70bb51f05e70b100c190a395e56c17837000508028183d5");
        byte[] setAlertConfig2Response = ByteUtil.fromHexString("0000001f05e70bf61f05e70b140a1d0300002000000007ff03f665");

        byte[] primeRequest1 = ByteUtil.fromHexString("1f05e70bb81f05e70b18 1f1a0eacb8d63302010a0101a000340034170d000208000186a0da");
        byte[] primeAckForCon = ByteUtil.fromHexString("0000001f05e70b591f05e70bd7");
        byte[] primeConForConRequest = ByteUtil.fromHexString("1f05e70b9a000000000000813791");
        byte[] primeResponse = ByteUtil.fromHexString("0000001f05e70bfb1f05e70b1c0a1d4400003034000007ff80ff73");


        switch(commsCount++) {
            case 0:
                if (ByteUtil.compare(bytesToSend, assignAddressRequest) == 0 )
                    return new RFSpyResponse(assignAddressResponse);
                else
                    return null;
            case 2:
                if (ByteUtil.compare(bytesToSend, confirmPairingRequest) == 0 )
                    return new RFSpyResponse(confirmPairingResponse1);
                else
                    return null;
            case 3:
                if (ByteUtil.compare(bytesToSend, ackForConfirmPairingResponse1) == 0 )
                    return new RFSpyResponse(conForConfirmPairingResponse1);
                else
                    return null;

            case 5:
                if (ByteUtil.compare(bytesToSend, setAlertConfig1Request) == 0 )
                    return new RFSpyResponse(setAlertConfig1Response);
                else
                    return null;
            case 1:
            case 4:
            case 6:
            case 8:
            case 11:
                return new RFSpyResponse(new byte[] {(byte) 0xAA});
            case 7:
                if (ByteUtil.compare(bytesToSend, setAlertConfig2Request) == 0 )
                    return new RFSpyResponse(setAlertConfig2Response);
                else
                    return null;
            case 9:
                if (ByteUtil.compare(bytesToSend, primeRequest1) == 0 )
                    return new RFSpyResponse(primeAckForCon);
                else
                    return null;
            case 10:
                if (ByteUtil.compare(bytesToSend, primeConForConRequest) == 0 )
                    return new RFSpyResponse(primeResponse);
                else
                    return null;

            default:
                return null;

        }
    }

    @Override
    public RFSpyResponse updateRegister(CC111XRegister reg, int val) {
        return new RFSpyResponse(new byte[] {(byte) 0xDD});
    }

    @Override
    public void setBaseFrequency(double freqMHz) {
        RileyLinkUtil.setEncoding(RileyLinkEncodingType.Manchester);

    }

    String testingFunction = null;

    @Override
    public void setTestingFunction(String functionName) {
        commsCount = 0;
        this.testingFunction = functionName;

    }
}
