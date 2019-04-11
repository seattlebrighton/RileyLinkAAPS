package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble;

import android.support.annotation.Nullable;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.CC111XRegister;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkEncodingType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;

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

        byte[] setBasal1 = ByteUtil.fromHexString("1f05e70ba01f05e70ba8dc1a40b8cd2a250012ab282c30005600280064000a0073001400f2");
        byte[] setBasal1Ack = ByteUtil.fromHexString("0000001f05e70b411f05e70bfc");
        byte[] setBasal2 = ByteUtil.fromHexString("1f05e70b827d001e0087002800910032009b003c00a5004600af005000b9005a00c3006421");
        byte[] setBasal2Ack = ByteUtil.fromHexString("0000001f05e70b431f05e70b38");
        byte[] setBasal3 = ByteUtil.fromHexString("1f05e70b84000af06e506e000c280b1398401610450002c5a401900044aa2003e8001b779d");
        byte[] setBasal3Ack = ByteUtil.fromHexString("0000001f05e70b451f05e70b73");
        byte[] setBasal4 = ByteUtil.fromHexString("1f05e70b864000640112a880047e0017e22100c80089544004e20015f900012c005b8d807900");
        byte[] setBasal4Ack = ByteUtil.fromHexString("0000001f05e70b471f05e70bb7");
        byte[] setBasal5 = ByteUtil.fromHexString("1f05e70b8805460014585501900044aa2005aa0012f12301f40036ee80060e0011b84a02a5");
        byte[] setBasal5Ack = ByteUtil.fromHexString("0000001f05e70b491f05e70be5");
        byte[] setBasal6 = ByteUtil.fromHexString("");
        byte[] setBasal6Ack = ByteUtil.fromHexString("0000001f05e70b4b1f05e70b21");
        byte[] setBasal7 = ByteUtil.fromHexString("1f05e70b8c000ed8ac0384001e8480079e000e15c403e8001b77400069010594495e880031");
        byte[] setBasal7Ack = ByteUtil.fromHexString("0000001f05e70b4d1f05e70b6a");
        byte[] setBasal8 = ByteUtil.fromHexString("1f05e70b8e18f80b01cc00eed54d8091c5");
        byte[] setBasal8Response = ByteUtil.fromHexString("0000001f05e70bef1f05e70b2c0a1d160015d00000000fff83464f");


        switch(commsCount++) {
            case 0:
                if (ByteUtil.compare(bytesToSend, setAlertConfig1Request) == 0)
                    return new RFSpyResponse(setAlertConfig1Response);
                else
                    return null;
            case 2:
                if (ByteUtil.compare(bytesToSend, setBasal1) == 0)
                    return new RFSpyResponse(setBasal1Ack);
                else
                    return null;
            case 3:
                if (ByteUtil.compare(bytesToSend, setBasal2) == 0)
                    return new RFSpyResponse(setBasal2Ack);
                else
                    return null;
            case 4:
                if (ByteUtil.compare(bytesToSend, setBasal3) == 0)
                    return new RFSpyResponse(setBasal3Ack);
                else
                    return null;
            case 5:
                if (ByteUtil.compare(bytesToSend, setBasal4) == 0)
                    return new RFSpyResponse(setBasal4Ack);
                else
                    return null;
            case 6:
                if (ByteUtil.compare(bytesToSend, setBasal5) == 0)
                    return new RFSpyResponse(setBasal5Ack);
                else
                    return null;
            case 7:
                if (ByteUtil.compare(bytesToSend, setBasal6) == 0)
                    return new RFSpyResponse(setBasal6Ack);
                else
                    return null;
            case 8:
                if (ByteUtil.compare(bytesToSend, setBasal7) == 0)
                    return new RFSpyResponse(setBasal7Ack);
                else
                    return null;
            case 9:
                if (ByteUtil.compare(bytesToSend, setBasal8) == 0)
                    return new RFSpyResponse(setBasal8Response);
                else
                    return null;

            case 1:
            case 10:
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
